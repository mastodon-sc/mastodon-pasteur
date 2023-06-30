package org.mastodon.mamut.nearest.ui;

import javax.swing.JLabel;

import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.nearest.NearestObjectStatComputation;
import org.mastodon.mamut.nearest.NearestObjectStatFeature;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.scijava.listeners.Listeners;

import bdv.ui.settings.ModificationListener;
import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;

public class NearestObjectStatController
{

	private final Model model;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final NearestObjectStatModelProfileEditPanel profileEditor;

	private final NearestObjectStatModel selectedStyle;

	private final NearestObjectStatMainPanel view;

	public NearestObjectStatController(
			final NearestObjectStatModel initialStyle,
			final Model model,
			final int minTimepoint,
			final int maxTimepoint,
			final MamutFeatureProjectionsManager featureProjectionsManager )
	{
		this.model = model;
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;

		this.profileEditor = new NearestObjectStatModelProfileEditPanel( initialStyle, featureProjectionsManager.getAvailableFeatureProjections() );
		this.selectedStyle = profileEditor.editedStyle;
		this.view = profileEditor.getJPanel();
		view.btnAdd.addActionListener( e -> add( view.getCurrentItem() ) );
		view.btnCompute.addActionListener( e -> compute() );
		featureProjectionsManager.listeners().add( () -> profileEditor.getJPanel()
				.setAvailableFeatureProjections( featureProjectionsManager.getAvailableFeatureProjections() ) );

	}

	private void add( final NearestObjectStatItem item )
	{
		if ( selectedStyle.size() >= NearestObjectStatModel.MAX_N_ITEMS)
		{
			view.lblLog.setText( "Cannot have more than " + NearestObjectStatModel.MAX_N_ITEMS + " stat items." );
			return;
		}
		if ( selectedStyle.contains( item ) )
		{
			view.lblLog.setText( "Current configuration already has this stat item." );
			return;
		}
		selectedStyle.add( item );
		view.lblLog.setText( "Stat item added." );
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
				try
				{
					final NearestObjectStatFeature feature = NearestObjectStatComputation.compute(
							model,
							selectedStyle,
							minTimepoint,
							maxTimepoint,
							view.getProgressListener() );
					model.getFeatureModel().declareFeature( feature );
				}
				finally
				{
					view.btnCompute.setVisible( true );
					view.btnCancel.setVisible( false );
					enabler.reenable();
				}
			};
		}.start();
	}

	public NearestObjectStatModelProfileEditPanel getProfileEditor()
	{
		return profileEditor;
	}

	private static class NearestObjectStatModelProfileEditPanel implements NearestObjectStatModel.StatModelListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< NearestObjectStatModel > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final NearestObjectStatModel editedStyle;

		private final NearestObjectStatMainPanel styleEditorPanel;

		private boolean trackModifications = true;

		public NearestObjectStatModelProfileEditPanel( final NearestObjectStatModel initialStyle, final AvailableFeatureProjections afp )
		{
			this.editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new NearestObjectStatMainPanel( editedStyle, afp );
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
