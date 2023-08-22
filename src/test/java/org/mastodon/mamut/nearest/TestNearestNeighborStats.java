package org.mastodon.mamut.nearest;

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

		final ProjectModel projectModel = ProjectLoader.open( "samples/drosophila_crop.mastodon", new Context() );
		new MainWindow( projectModel ).setVisible( true );

		projectModel.getPlugins().getPluginActions().getActionMap().get( NearestObjectStatPlugin.SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION ).actionPerformed( null );
	}
}
