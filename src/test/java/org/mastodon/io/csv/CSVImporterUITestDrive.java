package org.mastodon.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.io.csv.plugin.CSVImporterPlugin;
import org.mastodon.io.csv.plugin.CSVImporterPlugin.ToggleCSVImporterDialogAction;
import org.mastodon.io.csv.ui.CSVImporterUIController;
import org.mastodon.project.MamutProject;
import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.WindowManager;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class CSVImporterUITestDrive
{

	/*
	 * MAIN METHOD.
	 */

	public static void main( final String[] args ) throws IOException, SpimDataException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final WindowManager wm = new WindowManager( new Context() );

		final String bdvFile = "samples/200212__pos1.xml";
		final MamutProject project = new MamutProject( null, new File( bdvFile ) );
		wm.getProjectManager().open( project );
		new MainWindow( wm ).setVisible( true );

		final String csvFilePath = "samples/200212__pos1.csv";

		final ToggleCSVImporterDialogAction action = ( ToggleCSVImporterDialogAction ) wm.getAppModel()
				.getPlugins()
				.getPluginActions()
				.getActionMap()
				.get( CSVImporterPlugin.SHOW_CSV_IMPORTER_DIALOG_ACTION );

		final CSVImporterUIController controller = action.getController();
		controller.setCSVFile( new File( csvFilePath ) );

		action.actionPerformed( null );
	}
}
