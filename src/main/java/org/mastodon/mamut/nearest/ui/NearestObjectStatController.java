/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.nearest.ui;

import java.util.Timer;
import java.util.TimerTask;

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

	private Timer resetLogTimer;

	private int previousNItems;

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
		this.resetLogTimer = new Timer();
		this.profileEditor = new NearestObjectStatModelProfileEditPanel( initialStyle, featureProjectionsManager.getAvailableFeatureProjections(), model.getSpaceUnits() );
		this.selectedStyle = profileEditor.editedStyle;
		this.view = profileEditor.getJPanel();
		this.previousNItems = selectedStyle.size();
		selectedStyle.statModelListeners().add( () -> logModelChange() );
		view.btnAdd.addActionListener( e -> add( view.getCurrentItem() ) );
		view.btnCompute.addActionListener( e -> compute() );
		featureProjectionsManager.listeners().add( () -> profileEditor.getJPanel()
				.setAvailableFeatureProjections( featureProjectionsManager.getAvailableFeatureProjections() ) );

	}

	private void add( final NearestObjectStatItem item )
	{
		if ( selectedStyle.size() >= NearestObjectStatModel.MAX_N_ITEMS )
		{
			log( "Cannot have more than " + NearestObjectStatModel.MAX_N_ITEMS + " statistic items." );
			return;
		}
		if ( selectedStyle.contains( item ) )
		{
			log( "Current configuration already has this statistic item." );
			return;
		}
		selectedStyle.add( item );
	}

	private void logModelChange()
	{
		final int currentSize = selectedStyle.size();
		if ( currentSize > previousNItems )
			log( "Statistic item added." );
		else if ( currentSize < previousNItems )
			log( "Statistic item removed." );
		previousNItems = currentSize;
		view.btnCompute.setEnabled( currentSize > 0 );
	}

	private void log( final String msg )
	{
		view.lblLog.setText( msg );
		resetLogTimer.cancel();
		resetLogTimer = new Timer();
		final TimerTask resetLogTask = new TimerTask()
		{
			@Override
			public void run()
			{
				view.lblLog.setText( "" );
			}
		};
		resetLogTimer.schedule( resetLogTask, 2000 );
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

		public NearestObjectStatModelProfileEditPanel( final NearestObjectStatModel initialStyle, final AvailableFeatureProjections afp, final String units )
		{
			this.editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new NearestObjectStatMainPanel( editedStyle, afp, units );
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
