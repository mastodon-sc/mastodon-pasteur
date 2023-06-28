package org.mastodon.mamut.nearest;

import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import javax.swing.JLabel;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;

import gnu.trove.list.array.TDoubleArrayList;

public class NearestObjectStatController
{

	private final NearestObjectStatModel statModel;

	private final NearestObjectStatMainPanel view;

	private final NearestObjectStatItem currentItem;

	private final Model model;

	private final int minTimepoint;

	private final int maxTimepoint;

	public NearestObjectStatController( final Model model, final int minTimepoint, final int maxTimepoint )
	{
		this.model = model;
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
		this.statModel = new NearestObjectStatModel();
		this.currentItem = NearestObjectStatItem.defaultValue();
		add();
		this.view = new NearestObjectStatMainPanel( statModel, currentItem );

		view.btnAdd.addActionListener( e -> add() );
		view.btnCompute.addActionListener( e -> compute() );
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
				final NearestObjectStatFeature feature = NearestObjectStatFeature.createFeature( statModel, model );

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
				for ( final NearestObjectStatItem item : statModel )
				{
					final int n = item.n();
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

					for ( final NearestObjectStatItem item : statModel )
					{
						// The first one is always the spot itself.
						final int start = item.includeItem() ? 0 : 1;

						arr.resetQuick();
						for ( int i = start; i <= item.n(); i++ )
							arr.add( item.eval( list.get( i ), spot, model.getFeatureModel() ) );

						final double val = item.summarize( arr );
						feature.set( spot, item, val );
					}
				}
			}
		};
	}

	private void add()
	{
		statModel.add( currentItem.copy() );
	}

	public NearestObjectStatMainPanel getView()
	{
		return view;
	}
}
