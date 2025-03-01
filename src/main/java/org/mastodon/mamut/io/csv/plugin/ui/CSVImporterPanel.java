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
package org.mastodon.mamut.io.csv.plugin.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.mamut.io.csv.CSVImporter;

public class CSVImporterPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	final JTextField textFieldFile;

	final JButton btnBrowse;

	final JButton btnImport;

	final JComboBox< String > comboBoxXCol;

	final JComboBox< String > comboBoxYCol;

	final JComboBox< String > comboBoxZCol;

	final JComboBox< String > comboBoxFrameCol;

	final JComboBox< String > comboBoxQualityCol;

	final JComboBox< String > comboBoxRadiusCol;

	final JComboBox< String > comboBoxNameCol;

	final JComboBox< String > comboBoxIDCol;

	final JComboBox< String > comboBoxParentIdCol;

	final JComboBox< String > comboBoxTagCol;

	final JComboBox< String > comboBoxTrackCol;

	final JCheckBox chckbxImportTracks;

	final JLabel labelRadiusUnit;

	final JFormattedTextField ftfRadius;

	public CSVImporterPanel()
	{
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final Font bigFont = getFont().deriveFont( getFont().getSize2D() + 2f );

		final BorderLayout borderLayout = new BorderLayout();
		setLayout( borderLayout );

		final JPanel panelControl = new JPanel();
		add( panelControl, BorderLayout.SOUTH );
		panelControl.setBorder( null );
		final GridBagLayout layout = new GridBagLayout();
		layout.rowHeights = new int[] { 5, 5, 5, 5, 5, 0, 5, 5, 5, 5, 5, 5, 5, 5, 0 };
		layout.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 1.0 };
		layout.columnWidths = new int[] { 79, 50, 30, 50, 30 };
		panelControl.setLayout( layout );

		final JLabel lblCsvFile = new JLabel( "CSV file:" );
		final GridBagConstraints gbc_lblCsvFile = new GridBagConstraints();
		gbc_lblCsvFile.anchor = GridBagConstraints.WEST;
		gbc_lblCsvFile.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblCsvFile.gridx = 0;
		gbc_lblCsvFile.gridy = 0;
		panelControl.add( lblCsvFile, gbc_lblCsvFile );

		textFieldFile = new JTextField();
		final GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets( 5, 5, 5, 0 );
		gbc_textField.gridwidth = 5;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panelControl.add( textFieldFile, gbc_textField );
		textFieldFile.setColumns( 5 );

		btnBrowse = new JButton( "Browse" );
		final GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.anchor = GridBagConstraints.EAST;
		gbc_btnBrowse.gridwidth = 2;
		gbc_btnBrowse.insets = new Insets( 5, 5, 5, 0 );
		gbc_btnBrowse.gridx = 3;
		gbc_btnBrowse.gridy = 2;
		panelControl.add( btnBrowse, gbc_btnBrowse );

		chckbxImportTracks = new JCheckBox( "Import tracks?" );
		final GridBagConstraints gbc_chckbxImportTracks = new GridBagConstraints();
		gbc_chckbxImportTracks.anchor = GridBagConstraints.EAST;
		gbc_chckbxImportTracks.gridwidth = 5;
		gbc_chckbxImportTracks.insets = new Insets( 5, 5, 5, 0 );
		gbc_chckbxImportTracks.gridx = 0;
		gbc_chckbxImportTracks.gridy = 3;
		panelControl.add( chckbxImportTracks, gbc_chckbxImportTracks );
		chckbxImportTracks.setVisible( false ); // TODO

		final JSeparator separator = new JSeparator();
		final GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.BOTH;
		gbc_separator.gridwidth = 5;
		gbc_separator.insets = new Insets( 5, 5, 5, 5 );
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 4;
		panelControl.add( separator, gbc_separator );

		final JLabel lblMandatory = new JLabel( "Mandatory" );
		lblMandatory.setFont( new Font( Font.SANS_SERIF, Font.ITALIC, 12 ) );
		final GridBagConstraints gbc_lblRadiusMandatory = new GridBagConstraints();
		gbc_lblRadiusMandatory.anchor = GridBagConstraints.EAST;
		gbc_lblRadiusMandatory.insets = new Insets( 0, 0, 5, 50 );
		gbc_lblRadiusMandatory.gridx = 0;
		gbc_lblRadiusMandatory.gridy = 5;
		panelControl.add( lblMandatory, gbc_lblRadiusMandatory );

		final JLabel lblRadius = new JLabel( "Radius:" );
		final GridBagConstraints gbc_lblRadius = new GridBagConstraints();
		gbc_lblRadius.anchor = GridBagConstraints.EAST;
		gbc_lblRadius.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblRadius.gridx = 0;
		gbc_lblRadius.gridy = 6;
		panelControl.add( lblRadius, gbc_lblRadius );

		ftfRadius = new JFormattedTextField( NumberFormat.getNumberInstance() );
		ftfRadius.setHorizontalAlignment( SwingConstants.TRAILING );
		final GridBagConstraints gbc_ftfRadius = new GridBagConstraints();
		gbc_ftfRadius.gridwidth = 2;
		gbc_ftfRadius.insets = new Insets( 5, 5, 5, 5 );
		gbc_ftfRadius.fill = GridBagConstraints.HORIZONTAL;
		gbc_ftfRadius.gridx = 1;
		gbc_ftfRadius.gridy = 6;
		panelControl.add( ftfRadius, gbc_ftfRadius );

		labelRadiusUnit = new JLabel();
		final GridBagConstraints gbc_labelRadiusUnitl = new GridBagConstraints();
		gbc_labelRadiusUnitl.anchor = GridBagConstraints.WEST;
		gbc_labelRadiusUnitl.insets = new Insets( 0, 0, 5, 5 );
		gbc_labelRadiusUnitl.gridx = 3;
		gbc_labelRadiusUnitl.gridy = 6;
		panelControl.add( labelRadiusUnit, gbc_labelRadiusUnitl );

		final JLabel lblXColumn = new JLabel( "X column:" );
		final GridBagConstraints gbc_lblXColumn = new GridBagConstraints();
		gbc_lblXColumn.anchor = GridBagConstraints.EAST;
		gbc_lblXColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblXColumn.gridx = 0;
		gbc_lblXColumn.gridy = 7;
		panelControl.add( lblXColumn, gbc_lblXColumn );

		comboBoxXCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxX = new GridBagConstraints();
		gbc_comboBoxX.gridwidth = 4;
		gbc_comboBoxX.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxX.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxX.gridx = 1;
		gbc_comboBoxX.gridy = 7;
		panelControl.add( comboBoxXCol, gbc_comboBoxX );

		final JLabel lblYColumn = new JLabel( "Y column:" );
		final GridBagConstraints gbc_lblYColumn = new GridBagConstraints();
		gbc_lblYColumn.anchor = GridBagConstraints.EAST;
		gbc_lblYColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblYColumn.gridx = 0;
		gbc_lblYColumn.gridy = 8;
		panelControl.add( lblYColumn, gbc_lblYColumn );

		comboBoxYCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxY = new GridBagConstraints();
		gbc_comboBoxY.gridwidth = 4;
		gbc_comboBoxY.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxY.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxY.gridx = 1;
		gbc_comboBoxY.gridy = 8;
		panelControl.add( comboBoxYCol, gbc_comboBoxY );

		final JLabel lblZColumn = new JLabel( "Z column:" );
		final GridBagConstraints gbc_lblZColumn = new GridBagConstraints();
		gbc_lblZColumn.anchor = GridBagConstraints.EAST;
		gbc_lblZColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblZColumn.gridx = 0;
		gbc_lblZColumn.gridy = 9;
		panelControl.add( lblZColumn, gbc_lblZColumn );

		comboBoxZCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxZ = new GridBagConstraints();
		gbc_comboBoxZ.gridwidth = 4;
		gbc_comboBoxZ.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxZ.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxZ.gridx = 1;
		gbc_comboBoxZ.gridy = 9;
		panelControl.add( comboBoxZCol, gbc_comboBoxZ );

		final JLabel lblFrameColumn = new JLabel( "Frame column:" );
		final GridBagConstraints gbc_lblFrameColumn = new GridBagConstraints();
		gbc_lblFrameColumn.anchor = GridBagConstraints.EAST;
		gbc_lblFrameColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblFrameColumn.gridx = 0;
		gbc_lblFrameColumn.gridy = 10;
		panelControl.add( lblFrameColumn, gbc_lblFrameColumn );

		comboBoxFrameCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxFrame = new GridBagConstraints();
		gbc_comboBoxFrame.gridwidth = 4;
		gbc_comboBoxFrame.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxFrame.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxFrame.gridx = 1;
		gbc_comboBoxFrame.gridy = 10;
		panelControl.add( comboBoxFrameCol, gbc_comboBoxFrame );

		final JSeparator separatorOptional = new JSeparator();
		final GridBagConstraints gbc_separatorOptional = new GridBagConstraints();
		gbc_separatorOptional.fill = GridBagConstraints.BOTH;
		gbc_separatorOptional.gridwidth = 5;
		gbc_separatorOptional.insets = new Insets( 5, 5, 5, 5 );
		gbc_separatorOptional.gridx = 0;
		gbc_separatorOptional.gridy = 11;
		panelControl.add( separatorOptional, gbc_separatorOptional );

		final JLabel lblOptional = new JLabel( "Optional" );
		lblOptional.setFont( new Font( Font.SANS_SERIF, Font.ITALIC, 12 ) );
		final GridBagConstraints gbc_lblRadiusOptional = new GridBagConstraints();
		gbc_lblRadiusOptional.anchor = GridBagConstraints.EAST;
		gbc_lblRadiusOptional.insets = new Insets( 0, 0, 5, 50 );
		gbc_lblRadiusOptional.gridx = 0;
		gbc_lblRadiusOptional.gridy = 12;
		panelControl.add( lblOptional, gbc_lblRadiusOptional );

		final JLabel lblTrackColumn = new JLabel( "Track column:" );
		final GridBagConstraints gbc_lblTrackColumn = new GridBagConstraints();
		gbc_lblTrackColumn.anchor = GridBagConstraints.EAST;
		gbc_lblTrackColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblTrackColumn.gridx = 0;
		gbc_lblTrackColumn.gridy = 13;
		panelControl.add( lblTrackColumn, gbc_lblTrackColumn );
		lblTrackColumn.setVisible( false ); // TODO

		comboBoxTrackCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxTrackCol = new GridBagConstraints();
		gbc_comboBoxTrackCol.gridwidth = 4;
		gbc_comboBoxTrackCol.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxTrackCol.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxTrackCol.gridx = 1;
		gbc_comboBoxTrackCol.gridy = 13;
		panelControl.add( comboBoxTrackCol, gbc_comboBoxTrackCol );
		chckbxImportTracks.addActionListener( ( e ) -> comboBoxTrackCol.setEnabled( chckbxImportTracks.isSelected() ) );
		comboBoxTrackCol.setEnabled( chckbxImportTracks.isSelected() );
		comboBoxTrackCol.setVisible( false ); // TODO

		final JLabel lblQualityColumn = new JLabel( "Quality column:" );
		final GridBagConstraints gbc_lblQualityColumn = new GridBagConstraints();
		gbc_lblQualityColumn.anchor = GridBagConstraints.EAST;
		gbc_lblQualityColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblQualityColumn.gridx = 0;
		gbc_lblQualityColumn.gridy = 14;
		panelControl.add( lblQualityColumn, gbc_lblQualityColumn );

		comboBoxQualityCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxQuality = new GridBagConstraints();
		gbc_comboBoxQuality.gridwidth = 4;
		gbc_comboBoxQuality.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxQuality.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxQuality.gridx = 1;
		gbc_comboBoxQuality.gridy = 14;
		panelControl.add( comboBoxQualityCol, gbc_comboBoxQuality );

		final JLabel lblRadiusColumn = new JLabel( "Radius column:" );
		final GridBagConstraints gbc_lblRadiusColumn = new GridBagConstraints();
		gbc_lblRadiusColumn.anchor = GridBagConstraints.EAST;
		gbc_lblRadiusColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblRadiusColumn.gridx = 0;
		gbc_lblRadiusColumn.gridy = 15;
		panelControl.add( lblRadiusColumn, gbc_lblRadiusColumn );

		comboBoxRadiusCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxRadius = new GridBagConstraints();
		gbc_comboBoxRadius.gridwidth = 4;
		gbc_comboBoxRadius.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxRadius.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxRadius.gridx = 1;
		gbc_comboBoxRadius.gridy = 15;
		panelControl.add( comboBoxRadiusCol, gbc_comboBoxRadius );

		final JLabel lblNameColumn = new JLabel( "Label column:" );
		final GridBagConstraints gbc_lblNameColumn = new GridBagConstraints();
		gbc_lblNameColumn.anchor = GridBagConstraints.EAST;
		gbc_lblNameColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblNameColumn.gridx = 0;
		gbc_lblNameColumn.gridy = 16;
		panelControl.add( lblNameColumn, gbc_lblNameColumn );

		comboBoxNameCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxName = new GridBagConstraints();
		gbc_comboBoxName.gridwidth = 4;
		gbc_comboBoxName.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxName.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxName.gridx = 1;
		gbc_comboBoxName.gridy = 16;
		panelControl.add( comboBoxNameCol, gbc_comboBoxName );

		final JLabel lblIdColumn = new JLabel( "ID column:" );
		final GridBagConstraints gbc_lblIdColumn = new GridBagConstraints();
		gbc_lblIdColumn.anchor = GridBagConstraints.EAST;
		gbc_lblIdColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblIdColumn.gridx = 0;
		gbc_lblIdColumn.gridy = 17;
		panelControl.add( lblIdColumn, gbc_lblIdColumn );

		comboBoxIDCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxID = new GridBagConstraints();
		gbc_comboBoxID.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxID.gridwidth = 4;
		gbc_comboBoxID.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxID.gridx = 1;
		gbc_comboBoxID.gridy = 17;
		panelControl.add( comboBoxIDCol, gbc_comboBoxID );

		final JLabel lblParentIdColumn = new JLabel( "Parent ID column:" );
		final GridBagConstraints gbc_lblParentIdColumn = new GridBagConstraints();
		gbc_lblParentIdColumn.anchor = GridBagConstraints.EAST;
		gbc_lblParentIdColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblParentIdColumn.gridx = 0;
		gbc_lblParentIdColumn.gridy = 18;
		panelControl.add( lblParentIdColumn, gbc_lblParentIdColumn );

		comboBoxParentIdCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxParentID = new GridBagConstraints();
		gbc_comboBoxParentID.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxParentID.gridwidth = 4;
		gbc_comboBoxParentID.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxParentID.gridx = 1;
		gbc_comboBoxParentID.gridy = 18;
		panelControl.add( comboBoxParentIdCol, gbc_comboBoxParentID );

		final JLabel lblTagColumn = new JLabel( "Tag column:" );
		final GridBagConstraints gbc_lblTagColumn = new GridBagConstraints();
		gbc_lblTagColumn.anchor = GridBagConstraints.EAST;
		gbc_lblTagColumn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblTagColumn.gridx = 0;
		gbc_lblTagColumn.gridy = 19;
		panelControl.add( lblTagColumn, gbc_lblTagColumn );

		comboBoxTagCol = new JComboBox<>();
		final GridBagConstraints gbc_comboBoxTag = new GridBagConstraints();
		gbc_comboBoxTag.insets = new Insets( 5, 5, 5, 0 );
		gbc_comboBoxTag.gridwidth = 4;
		gbc_comboBoxTag.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxTag.gridx = 1;
		gbc_comboBoxTag.gridy = 19;
		panelControl.add( comboBoxTagCol, gbc_comboBoxTag );

		final JSeparator separatorButton = new JSeparator();
		final GridBagConstraints gbc_separatorButton = new GridBagConstraints();
		gbc_separatorButton.fill = GridBagConstraints.BOTH;
		gbc_separatorButton.gridwidth = 5;
		gbc_separatorButton.insets = new Insets( 5, 5, 5, 5 );
		gbc_separatorButton.gridx = 0;
		gbc_separatorButton.gridy = 20;
		panelControl.add( separatorButton, gbc_separatorButton );

		final JPanel panelButtonExport = new JPanel();
		final GridBagConstraints gbc_panelButtonExport = new GridBagConstraints();
		gbc_panelButtonExport.anchor = GridBagConstraints.EAST;
		gbc_panelButtonExport.gridwidth = 5;
		gbc_panelButtonExport.gridx = 0;
		gbc_panelButtonExport.gridy = 21;
		panelControl.add( panelButtonExport, gbc_panelButtonExport );
		final FlowLayout flowLayout = ( FlowLayout ) panelButtonExport.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );

		btnImport = new JButton( "Import" );
		panelButtonExport.add( btnImport );

		final JPanel panelTitle = new JPanel();
		add( panelTitle, BorderLayout.NORTH );
		panelTitle.setLayout( new BorderLayout( 0, 0 ) );

		final JLabel lblCsvImporter = new JLabel( "Mastodon CSV importer", JLabel.CENTER );
		lblCsvImporter.setFont( bigFont );
		lblCsvImporter.setHorizontalAlignment( SwingConstants.CENTER );
		panelTitle.add( lblCsvImporter, BorderLayout.NORTH );

		final JLabel lblVersion = new JLabel( "v" + CSVImporter.PLUGIN_VERSION, JLabel.CENTER  );
		panelTitle.add( lblVersion, BorderLayout.SOUTH );
	}
}
