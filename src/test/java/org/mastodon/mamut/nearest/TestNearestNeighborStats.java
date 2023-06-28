package org.mastodon.mamut.nearest;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class TestNearestNeighborStats
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final WindowManager wm = new WindowManager( new Context() );

		final MamutProject project = new MamutProjectIO().load( "samples/drosophila_crop.mastodon" );
		wm.getProjectManager().open( project );
		new MainWindow( wm ).setVisible( true );

		wm.getAppModel().getPlugins().getPluginActions().getActionMap().get( NearestObjectStatPlugin.SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION ).actionPerformed( null );
	}
}
