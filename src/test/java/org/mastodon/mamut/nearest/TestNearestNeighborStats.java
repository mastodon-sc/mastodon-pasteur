package org.mastodon.mamut.nearest;

import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class TestNearestNeighborStats
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

//		try 
		final Context context = new Context();
//		{
//			final ProjectModel projectModel = ProjectLoader.open( "samples/drosophila_crop.mastodon", context );
		final ProjectModel projectModel = ProjectLoader.open( "/Users/tinevez/Google Drive/Mastodon/Datasets/Local/Uzuki2D/Choana.mastodon", context );
		final MainWindow mw = new MainWindow( projectModel );
//		mw.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		mw.setVisible( true );
		mw.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final java.awt.event.WindowEvent e )
			{
				context.close();
				System.exit( 0 );
			};
		} );

		projectModel.getPlugins().getPluginActions().getActionMap().get( NearestObjectStatPlugin.SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION ).actionPerformed( null );
//		}
	}
}
