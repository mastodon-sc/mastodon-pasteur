/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.csv.plugin.ui;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.mastodon.mamut.io.csv.CSVImporter;
import org.mastodon.mamut.model.Model;
import org.scijava.log.Logger;
import org.scijava.log.StderrLogService;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CSVImporterUIController
{
	private static final String NONE_COLUMN = "Don't use";

	private static final FileDialog dialog = new FileDialog( new JFrame(), "Open a CSV file", FileDialog.LOAD );

	private final CSVImporterPanel view;

	private File file;

	private Map< String, Integer > headerMap;

	private final Logger logger;

	private final Model model;

	public CSVImporterUIController( final Model model )
	{
		this.model = model;
		this.view = new CSVImporterPanel();
		view.btnBrowse.addActionListener( ( e ) -> browse() );
		view.textFieldFile.addActionListener( ( e ) -> setCSVFile( new File( view.textFieldFile.getText() ) ) );
		view.btnImport.addActionListener( ( e ) -> export() );
		logger = new StderrLogService();

		/*
		 * Tentative automatic radius.
		 */
		view.labelRadiusUnit.setText( model.getSpaceUnits() );
		final double r = 2.5;
		view.ftfRadius.setValue( Double.valueOf( r ) );
	}

	public CSVImporterPanel getView()
	{
		return view;
	}

	private void export()
	{
		view.btnImport.setEnabled( false );
		new Thread( "Mastodon CSV importer thread" )
		{
			@Override
			public void run()
			{
				try
				{
					final String filePath = view.textFieldFile.getText();
					final double radius = ( ( Number ) view.ftfRadius.getValue() ).doubleValue();

					final CSVImporter importer = CSVImporter
							.create()
							.model( model )
							.csvFilePath( filePath )
							.radius( radius )
							.xColumnName( ( String ) view.comboBoxXCol.getSelectedItem() )
							.yColumnName( ( String ) view.comboBoxYCol.getSelectedItem() )
							.zColumnName( ( String ) view.comboBoxZCol.getSelectedItem() )
							.labelColumnName( ( String ) view.comboBoxNameCol.getSelectedItem() )
							.frameColumnName( ( String ) view.comboBoxFrameCol.getSelectedItem() )
							.qualityColumnName( ( String ) view.comboBoxQualityCol.getSelectedItem() )
							.idColumnName( ( String ) view.comboBoxIDCol.getSelectedItem() )
							.get();

					if ( !importer.checkInput() || !importer.process() )
					{
						error( "Error importing CSV file:\n" + importer.getErrorMessage() );
						return;
					}
					log( "CSV import successful.\n" );
				}
				finally
				{
					view.btnImport.setEnabled( true );
				}
			}
		}.start();
	}

	private void browse()
	{
		final File file = askForCSVfile();
		if ( null == file )
			return;

		setCSVFile( file );
	}

	private File askForCSVfile()
	{
		if ( null == file )
			this.file = new File( System.getProperty( "user.home" ) );

		dialog.setDirectory( file.getAbsolutePath() );
		dialog.setFile( file.getName() );
		dialog.setVisible( true );
		String selectedFile = dialog.getFile();
		if ( null == selectedFile )
		{ return null; }
		if ( !selectedFile.endsWith( ".csv" ) )
			selectedFile += ".csv";
		file = new File( dialog.getDirectory(), selectedFile );
		return file;
	}

	public void setCSVFile( final File file )
	{
		this.file = file;
		view.textFieldFile.setText( file.getAbsolutePath() );
		log( "Inspecting CSV file: " + file + '\n' );

		view.btnImport.setEnabled( false );
		if ( readHeaders() )
			view.btnImport.setEnabled( true );
	}

	private boolean readHeaders()
	{
		final String filePath = view.textFieldFile.getText();

		/*
		 * Open and parse file.
		 */

		final CSVParser parser =
				new CSVParserBuilder()
						.withIgnoreQuotations( true )
						.build();
		try
		{
			final CSVReader reader = new CSVReaderBuilder( new FileReader( filePath ) )
					.withCSVParser( parser )
					.build();
			final Iterator< String[] > it = reader.iterator();

			/*
			 * Parse first line and reads it as the header of the file.
			 */

			if ( !it.hasNext() )
			{
				error( "CSV file is empty." );
				clearComboBoxes();
				return false;
			}

			final String[] firstLine = it.next();
			this.headerMap = new HashMap<>( firstLine.length );
			for ( int i = 0; i < firstLine.length; i++ )
			{
				final String cleanKey = firstLine[ i ].trim().replaceAll( "\\p{C}", "" );
				headerMap.put( cleanKey, Integer.valueOf( i ) );
			}

			// Iterate in column orders.
			final ArrayList< String > headers = new ArrayList<>( headerMap.keySet() );
			headers.removeIf( ( e ) -> e.trim().isEmpty() );

			if ( headers.isEmpty() )
			{
				error( "Could not read the header of the CSV file.\nIt does not seem present.\n" );
				clearComboBoxes();
				return false;
			}

			final String[] mandatory = headers.toArray( new String[] {} );
			view.comboBoxXCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
			view.comboBoxYCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
			view.comboBoxZCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
			view.comboBoxFrameCol.setModel( new DefaultComboBoxModel<>( mandatory ) );
			view.comboBoxTrackCol.setModel( new DefaultComboBoxModel<>( mandatory ) );

			// Try to be clever and guess from header names.
			int tcol = -1;
			int xcol = -1;
			int ycol = -1;
			int zcol = -1;
			int trackcol = -1;
			for ( int i = 0; i < mandatory.length; i++ )
			{
				final String current = mandatory[ i ];

				if ( current.toLowerCase().startsWith( "x" ) || current.toLowerCase().endsWith( "x" ) )
				{
					if ( xcol < 0 || ( current.length() < mandatory[ xcol ].length() ) )
						xcol = i;
				}

				if ( current.toLowerCase().startsWith( "y" ) || current.toLowerCase().endsWith( "y" ) )
				{
					if ( ycol < 0 || ( current.length() < mandatory[ ycol ].length() ) )
						ycol = i;
				}

				if ( current.toLowerCase().startsWith( "z" ) || current.toLowerCase().endsWith( "z" ) )
				{
					if ( zcol < 0 || ( current.length() < mandatory[ zcol ].length() ) )
						zcol = i;
				}

				if ( current.toLowerCase().startsWith( "frame" )
						|| current.toLowerCase().startsWith( "time" )
						|| current.toLowerCase().startsWith( "t" ) )
				{
					if ( tcol < 0 || current.equals( "frame" ) )
						tcol = i;
				}

				if ( current.toLowerCase().startsWith( "track" ) || current.toLowerCase().startsWith( "traj" ) )
				{
					if ( trackcol < 0 || current.equals( "track" ) )
						trackcol = i;
				}
			}
			if ( tcol < 0 )
				tcol = 0 % ( mandatory.length - 1 );
			if ( xcol < 0 )
				xcol = 1 % ( mandatory.length - 1 );
			if ( ycol < 0 )
				ycol = 2 % ( mandatory.length - 1 );
			if ( zcol < 0 )
				zcol = 3 % ( mandatory.length - 1 );
			if ( trackcol < 0 )
				trackcol = 4 % ( mandatory.length - 1 );

			view.comboBoxXCol.setSelectedIndex( xcol );
			view.comboBoxYCol.setSelectedIndex( ycol );
			view.comboBoxZCol.setSelectedIndex( zcol );
			view.comboBoxFrameCol.setSelectedIndex( tcol );
			view.comboBoxTrackCol.setSelectedIndex( trackcol );

			// Add a NONE for non mandatory columns
			headers.add( NONE_COLUMN );
			final String[] nonMandatory = headers.toArray( new String[] {} );
			view.comboBoxQualityCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
			view.comboBoxNameCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );
			view.comboBoxIDCol.setModel( new DefaultComboBoxModel<>( nonMandatory ) );

			int idcol = headers.indexOf( NONE_COLUMN );
			int qualitycol = headers.indexOf( NONE_COLUMN );
			int namecol = headers.indexOf( NONE_COLUMN );
			for ( int i = 0; i < nonMandatory.length; i++ )
			{
				final String current = nonMandatory[ i ];

				if ( current.toLowerCase().startsWith( "id" )
						|| current.toLowerCase().startsWith( "index" ) )
					idcol = i;

				if ( current.toLowerCase().startsWith( "name" ) )
					namecol = i;

				if ( current.toLowerCase().startsWith( "q" ) )
					qualitycol = i;
			}

			view.comboBoxIDCol.setSelectedIndex( idcol );
			view.comboBoxQualityCol.setSelectedIndex( qualitycol );
			view.comboBoxNameCol.setSelectedIndex( namecol );
		}
		catch ( final FileNotFoundException e )
		{
			error( "Cannot find file " + filePath );
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void clearComboBoxes()
	{
		final ArrayList< JComboBox< String > > comboBoxes = new ArrayList<>();
		comboBoxes.add( view.comboBoxXCol );
		comboBoxes.add( view.comboBoxYCol );
		comboBoxes.add( view.comboBoxZCol );
		comboBoxes.add( view.comboBoxFrameCol );
		comboBoxes.add( view.comboBoxQualityCol );
		comboBoxes.add( view.comboBoxNameCol );
		comboBoxes.add( view.comboBoxIDCol );
		comboBoxes.add( view.comboBoxTrackCol );
		for ( final JComboBox< String > cb : comboBoxes )
			cb.setModel( new DefaultComboBoxModel<>() );
	}

	private void error( final String string )
	{
		logger.error( string );
	}

	private void log( final String string )
	{
		logger.info( string );
	}
}
