package org.mastodon.mamut.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class TestDrivePasteurPlugins
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final WindowManager wm = new WindowManager( new Context() );
//		final File targetFile = new File("samples/mamutproject-singlespot.mastodon");
		final File targetFile = new File("samples/mamutproject.mastodon");
		wm.getProjectManager().open( new MamutProjectIO().load( targetFile.getAbsolutePath() ) );
		new MainWindow( wm ).setVisible( true );
	}
}
