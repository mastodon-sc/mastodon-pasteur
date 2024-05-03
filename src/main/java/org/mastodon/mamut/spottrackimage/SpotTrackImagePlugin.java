/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.spottrackimage;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mastodon.app.MastodonIcons;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

@Plugin( type = SpotTrackImagePlugin.class )
public class SpotTrackImagePlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	public static final String SHOW_TRACK_IMAGE_DIALOG_ACTION = "show spot track image dialog";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_TRACK_IMAGE_DIALOG_ACTION, "Track image" );
	}

	private final ToggleTrackImageDialogAction toggleDialog = new ToggleTrackImageDialogAction();

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Collections.singletonList( MamutMenuBuilder.makeFullMenuItem(
				SHOW_TRACK_IMAGE_DIALOG_ACTION, MENU_PATH ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( toggleDialog, ACTION_1_KEYS );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appModel )
	{
		toggleDialog.setAppModel( appModel );
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
			descriptions.add( SHOW_TRACK_IMAGE_DIALOG_ACTION, ACTION_1_KEYS,
					"Spot Track image: Extract an image centered on a spot over time." );
		}
	}

	public static class ToggleTrackImageDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private JDialog trackImageDialog;

		private SpotTrackImageUIController controller;

		public ToggleTrackImageDialogAction()
		{
			super( SHOW_TRACK_IMAGE_DIALOG_ACTION );
		}

		public void setAppModel( final ProjectModel appModel )
		{
			final Context context = appModel.getContext();

			final SharedBigDataViewerData bdvData = appModel.getSharedBdvData();
			final Model model = appModel.getModel();
			final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();

			controller = new SpotTrackImageUIController(
					bdvData,
					model,
					selectionModel,
					context );
			trackImageDialog = new JDialog( ( Frame ) null, "Track Image" );
			trackImageDialog.setIconImages( Arrays.asList( new Image[] {
					MastodonIcons.BVV_ICON_LARGE.getImage(),
					MastodonIcons.BVV_ICON_MEDIUM.getImage(),
					MastodonIcons.BVV_ICON_SMALL.getImage() } ) );
			trackImageDialog.getContentPane().add( controller.getView(), BorderLayout.CENTER );
			trackImageDialog.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
			trackImageDialog.setLocationRelativeTo( null );
			trackImageDialog.setSize( 352, 320 );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( null == trackImageDialog )
				return;
			trackImageDialog.setVisible( !trackImageDialog.isVisible() );
		}
	}
}
