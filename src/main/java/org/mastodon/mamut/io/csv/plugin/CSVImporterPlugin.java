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
package org.mastodon.mamut.io.csv.plugin;

import static org.mastodon.app.ui.ViewMenuBuilder.item;
import static org.mastodon.app.ui.ViewMenuBuilder.menu;

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
import org.mastodon.mamut.io.csv.plugin.ui.CSVImporterUIController;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

@Plugin( type = CSVImporterPlugin.class )
public class CSVImporterPlugin implements MamutPlugin
{

	public static final String SHOW_CSV_IMPORTER_DIALOG_ACTION = "show csv importer dialog";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_CSV_IMPORTER_DIALOG_ACTION, "CSV Importer" );
	}

	private final ToggleCSVImporterDialogAction toggleImporterDialog = new ToggleCSVImporterDialogAction();

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Collections.singletonList(
				MamutMenuBuilder.fileMenu( menu( "Import", item( SHOW_CSV_IMPORTER_DIALOG_ACTION ) ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( toggleImporterDialog, ACTION_1_KEYS );
	}

	@Override
	public void setAppPluginModel( final ProjectModel appModel )
	{
		toggleImporterDialog.setModel( appModel.getModel() );
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
			descriptions.add( SHOW_CSV_IMPORTER_DIALOG_ACTION, ACTION_1_KEYS, "Show the CSV importer dialog." );
		}
	}

	public static class ToggleCSVImporterDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private JDialog csvImporterDialog;

		private CSVImporterUIController controller;

		public ToggleCSVImporterDialogAction()
		{
			super( SHOW_CSV_IMPORTER_DIALOG_ACTION );
		}

		public void setModel( final Model model )
		{
			controller = new CSVImporterUIController( model );
			csvImporterDialog = new JDialog( ( Frame ) null, "CSV Importer" );
			csvImporterDialog.setIconImages( Arrays.asList( new Image[] {
					MastodonIcons.LOAD_ICON_LARGE.getImage(),
					MastodonIcons.LOAD_ICON_MEDIUM.getImage(),
					MastodonIcons.LOAD_ICON_SMALL.getImage() } ) );
			csvImporterDialog.getContentPane().add( controller.getView(), BorderLayout.CENTER );
			csvImporterDialog.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
			csvImporterDialog.setLocationRelativeTo( null );
			csvImporterDialog.pack();
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( null == csvImporterDialog )
				return;
			csvImporterDialog.setVisible( !csvImporterDialog.isVisible() );
		}

		public CSVImporterUIController getController()
		{
			return controller;
		}
	}
}
