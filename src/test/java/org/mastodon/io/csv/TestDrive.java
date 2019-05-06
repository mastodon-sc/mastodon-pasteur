package org.mastodon.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.WindowManager;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class TestDrive
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final WindowManager wm = new WindowManager( new Context() );
		final File targetFile = new File("samples/featureserialized.mastodon");
		wm.getProjectManager().open( new MamutProjectIO().load( targetFile.getAbsolutePath() ) );
		new MainWindow( wm ).setVisible( true );
	}
}
