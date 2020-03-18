package org.mastodon.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.project.MamutProject;
import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class CSVImporterTestDrive
{
	/*
	 * MAIN METHOD
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

		final Model model = wm.getAppModel().getModel();
		final String csvFilePath = "samples/200212__pos1.csv";
		final CSVImporter importer = CSVImporter.create()
				.model( model )
				.csvFilePath( csvFilePath )
				.radius( 2. )
				.xColumnName( "x" )
				.yColumnName( "y" )
				.zColumnName( "z" )
				.frameColumnName( "time" )
				.idColumnName( "index" )
				.get();

		System.out.println( "Starting import" );
		final long start = System.currentTimeMillis();
		if ( !importer.checkInput() || !importer.process() )
		{
			System.out.println( importer.getErrorMessage() );
			return;
		}
		System.out.println( String.format( "Finished import of %d spots in %.1f s.",
				model.getGraph().vertices().size(),
				( System.currentTimeMillis() - start ) / 1000. ) );
	}
}
