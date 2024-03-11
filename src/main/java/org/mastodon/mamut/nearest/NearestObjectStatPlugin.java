/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.nearest;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.feature.MamutFeatureProjectionsManager;
import org.mastodon.mamut.nearest.ui.NearestObjectStatController;
import org.mastodon.mamut.nearest.ui.NearestObjectStatModelManager;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;
import bdv.ui.settings.style.StyleProfileManager;

@Plugin( type = NearestObjectStatPlugin.class )
public class NearestObjectStatPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins", "Compute Feature" };

	public static final String SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION = "generate statistics on nearest neighbors";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, "Statistics on nearest neighbors" );
	}

	private final ShowDialogAction toggleDialog = new ShowDialogAction();

	private ProjectModel appModel;

	private SelectAndEditProfileSettingsPage< StyleProfile< NearestObjectStatModel > > page;

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Collections.singletonList( MamutMenuBuilder.makeFullMenuItem(
				SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, MENU_PATH ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( toggleDialog, ACTION_1_KEYS );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appModel )
	{
		this.appModel = appModel;
		final NearestObjectStatModelManager manager = new NearestObjectStatModelManager();
		final MamutFeatureProjectionsManager featureProjectionsManager = appModel.getWindowManager().getManager( MamutFeatureProjectionsManager.class );
		final NearestObjectStatController controller = new NearestObjectStatController(
				manager.getSelectedStyle(),
				appModel.getModel(),
				appModel.getMinTimepoint(),
				appModel.getMaxTimepoint(),
				featureProjectionsManager );

		this.page = new SelectAndEditProfileSettingsPage<>(
				"Plugins > Stats on nearest-neighbors",
				new StyleProfileManager<>( manager, new NearestObjectStatModelManager( false ) ),
				controller.getProfileEditor() );
		appModel.getWindowManager().getPreferencesDialog().addPage( page );
	}

	/**
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, ACTION_1_KEYS,
					"Cpmpute statistics on nearest neighbors and store them as feature values." );
		}
	}

	private class ShowDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		public ShowDialogAction()
		{
			super( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( appModel == null )
				return;

			final PreferencesDialog dialog = appModel.getWindowManager().getPreferencesDialog();
			if ( null == dialog )
				return;

			dialog.showPage( page.getTreePath() );
			dialog.setVisible( true );
		}
	}
}
