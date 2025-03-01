/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.csv;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectCreator;
import org.mastodon.mamut.io.csv.plugin.CSVImporterPlugin;
import org.mastodon.mamut.io.csv.plugin.CSVImporterPlugin.ToggleCSVImporterDialogAction;
import org.mastodon.mamut.io.csv.plugin.ui.CSVImporterUIController;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class CSVImporterUITestDrive
{

	public static void main( final String[] args ) throws IOException, SpimDataException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.ROOT );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

//		final String bdvFile = "samples/200212__pos1.xml";
		final String bdvFile = "../mastodon/samples/datasethdf5.xml";
		final ProjectModel projectModel = ProjectCreator.createProjectFromBdvFile( new File( bdvFile ), new Context() );
		new MainWindow( projectModel ).setVisible( true );

		final String csvFilePath = "samples/MastodonTable-Spot-1lineheader.csv";
		final ToggleCSVImporterDialogAction action = ( ToggleCSVImporterDialogAction ) projectModel
				.getPlugins()
				.getPluginActions()
				.getActionMap()
				.get( CSVImporterPlugin.SHOW_CSV_IMPORTER_DIALOG_ACTION );

		final CSVImporterUIController controller = action.getController();
		controller.setCSVFile( new File( csvFilePath ) );

		action.actionPerformed( null );
	}
}
