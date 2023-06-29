package org.mastodon.mamut.nearest.ui;

import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.swing.JLabel;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatFeature;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.scijava.listeners.Listeners;

import bdv.ui.settings.ModificationListener;
import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;
import gnu.trove.list.array.TDoubleArrayList;

public class NearestObjectStatController
{

	private final Model model;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final NearestObjectStatModelProfileEditPanel profileEditor;

	private final NearestObjectStatModel selectedStyle;

	private final NearestObjectStatMainPanel view;

	public NearestObjectStatController( final NearestObjectStatModel initialStyle, final Model model, final int minTimepoint, final int maxTimepoint )
	{
		this.model = model;
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;

		this.profileEditor = new NearestObjectStatModelProfileEditPanel( initialStyle );
		this.selectedStyle = profileEditor.editedStyle;
		this.view = profileEditor.getJPanel();
		view.btnAdd.addActionListener( e -> add( view.getCurrentItem() ) );
		view.btnCompute.addActionListener( e -> compute() );
	}

	private void add( final NearestObjectStatItem item )
	{
		selectedStyle.add( item );
	}

	private void compute()
	{
		new Thread( "Mastodon stats on N nearest neighbor thread" )
		{
			@Override
			public void run()
			{
				final EverythingDisablerAndReenabler enabler = new EverythingDisablerAndReenabler( view, new Class[] { JLabel.class } );
				enabler.disable();
				view.btnCompute.setVisible( false );
				view.btnCancel.setVisible( true );
				view.btnCancel.setEnabled( true );

				// Prepare feature to hold results.
				final NearestObjectStatFeature feature = NearestObjectStatFeature.createFeature( selectedStyle, model );

				final ReadLock lock = model.getGraph().getLock().readLock();
				lock.lock();
				try
				{
					for ( int t = minTimepoint; t <= maxTimepoint; t++ )
						process( t, feature ).run();

					model.getFeatureModel().declareFeature( feature );
				}
				finally
				{
					lock.unlock();
					view.btnCompute.setVisible( true );
					view.btnCancel.setVisible( false );
					enabler.reenable();
				}
			};
		}.start();
	}

	protected Runnable process( final int t, final NearestObjectStatFeature feature )
	{
		return new Runnable()
		{

			@Override
			public void run()
			{
				// Max number of neighbors we need to collect.
				int maxN = -1;
				for ( final NearestObjectStatItem item : selectedStyle )
				{
					final int n = item.n;
					if ( n > maxN )
						maxN = n;
				}
				final RefList< Spot > list = RefCollections.createRefList( model.getGraph().vertices(), maxN );

				// Spatial search.
				final SpatialIndex< Spot > si = model.getSpatioTemporalIndex().getSpatialIndex( t );
				final IncrementalNearestNeighborSearch< Spot > search = si.getIncrementalNearestNeighborSearch();

				// Storage for numerical values fetched from neighbor.
				final TDoubleArrayList arr = new TDoubleArrayList( maxN );
				for ( final Spot spot : si )
				{
					// Build list of neighbors.
					search.search( spot );
					for ( int i = 0; i <= maxN; i++ )
						list.add( search.next() );

					for ( final NearestObjectStatItem item : selectedStyle )
					{
						// The first one is always the spot itself.
						final int start = item.include ? 0 : 1;

						arr.resetQuick();
						for ( int i = start; i <= item.n; i++ )
							arr.add( item.eval( list.get( i ), spot, model.getFeatureModel() ) );

						final double val = item.summarize( arr );
						feature.set( spot, item, val );
					}
				}
			}
		};
	}

	public SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< NearestObjectStatModel > > getProfileEditor()
	{
		return profileEditor;
	}

	private static class NearestObjectStatModelProfileEditPanel implements NearestObjectStatModel.StatModelListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< NearestObjectStatModel > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final NearestObjectStatModel editedStyle;

		private final NearestObjectStatMainPanel styleEditorPanel;

		private boolean trackModifications = true;

		public NearestObjectStatModelProfileEditPanel( final NearestObjectStatModel initialStyle )
		{
			this.editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new NearestObjectStatMainPanel( editedStyle );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.statModelListeners().add( this );
		}

		@Override
		public void loadProfile( final StyleProfile< NearestObjectStatModel > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< NearestObjectStatModel > profile )
		{
			trackModifications = false;
			editedStyle.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public NearestObjectStatMainPanel getJPanel()
		{
			return styleEditorPanel;
		}

		@Override
		public void statModelChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::setModified );
		}
	}
}
