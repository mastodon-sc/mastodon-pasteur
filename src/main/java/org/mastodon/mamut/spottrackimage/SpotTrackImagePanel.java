/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.spottrackimage;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mastodon.tracking.mamut.trackmate.wizard.util.SetupIDComboBox;

import bdv.viewer.SourceAndConverter;

public class SpotTrackImagePanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final double STEP = 0.1;

	private static final double MIN_SIZE = STEP;

	private static final double MAX_SIZE = 10.;

	private static final double DEFAULT_SIZE = 1.;

	final JButton btnRun;

	private SpinnerNumberModel spinnerModel;

	private SetupIDComboBox cmbboxSource;

	private JRadioButton rdbtn3D;

	private JLabel lblLog;

	public SpotTrackImagePanel( final List< SourceAndConverter< ? > > sources )
	{
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		final Font bigFont = getFont().deriveFont( getFont().getSize2D() + 2f );

		final BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap( 10 );
		setLayout( borderLayout );

		final JPanel panelControl = new JPanel();
		add( panelControl, BorderLayout.SOUTH );
		panelControl.setBorder( null );
		final GridBagLayout layout = new GridBagLayout();
		layout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0 };
		layout.rowHeights = new int[] { 24, 5, 5, 5, 0, 0 };
		layout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		layout.columnWidths = new int[] { 79, 50, 30 };
		panelControl.setLayout( layout );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.fill = GridBagConstraints.BOTH;
		gbcSeparator.gridwidth = 3;
		gbcSeparator.insets = new Insets( 5, 5, 5, 5 );
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 0;
		panelControl.add( new JSeparator(), gbcSeparator );

		final JLabel lblSource = new JLabel( "With respect to source" );
		lblSource.setFont( lblSource.getFont().deriveFont( lblSource.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblSource = new GridBagConstraints();
		gbcLblSource.anchor = GridBagConstraints.EAST;
		gbcLblSource.insets = new Insets( 5, 5, 5, 5 );
		gbcLblSource.gridx = 0;
		gbcLblSource.gridy = 1;
		panelControl.add( lblSource, gbcLblSource );

		cmbboxSource = new SetupIDComboBox( sources );
		cmbboxSource.setFont( cmbboxSource.getFont().deriveFont( cmbboxSource.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcCmbboxSource = new GridBagConstraints();
		gbcCmbboxSource.gridwidth = 2;
		gbcCmbboxSource.insets = new Insets( 5, 5, 5, 5 );
		gbcCmbboxSource.fill = GridBagConstraints.HORIZONTAL;
		gbcCmbboxSource.gridx = 1;
		gbcCmbboxSource.gridy = 1;
		panelControl.add( cmbboxSource, gbcCmbboxSource );

		final JLabel lblSize = new JLabel( "Image Size (spot radius units)" );
		lblSize.setFont( lblSize.getFont().deriveFont( lblSize.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblSize = new GridBagConstraints();
		gbcLblSize.anchor = GridBagConstraints.EAST;
		gbcLblSize.insets = new Insets( 5, 5, 5, 5 );
		gbcLblSize.gridx = 0;
		gbcLblSize.gridy = 2;
		panelControl.add( lblSize, gbcLblSize );

		final int minSlider = 1;
		final int maxSlider = 1 + ( int ) ( MAX_SIZE / STEP );
		final int valueSlider = 1 + ( int ) ( DEFAULT_SIZE / STEP );
		final JSlider sliderSize = new JSlider( minSlider, maxSlider, valueSlider );
		final GridBagConstraints gbcSliderSize = new GridBagConstraints();
		gbcSliderSize.fill = GridBagConstraints.HORIZONTAL;
		gbcSliderSize.insets = new Insets( 5, 5, 5, 5 );
		gbcSliderSize.gridx = 1;
		gbcSliderSize.gridy = 2;
		panelControl.add( sliderSize, gbcSliderSize );

		spinnerModel = new SpinnerNumberModel( DEFAULT_SIZE, MIN_SIZE, MAX_SIZE, STEP );
		final JSpinner spinnerSize = new JSpinner( spinnerModel );
		spinnerSize.setFont( spinnerSize.getFont().deriveFont( spinnerSize.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcSpinnerSize = new GridBagConstraints();
		gbcSpinnerSize.fill = GridBagConstraints.BOTH;
		gbcSpinnerSize.insets = new Insets( 5, 5, 5, 5 );
		gbcSpinnerSize.gridx = 2;
		gbcSpinnerSize.gridy = 2;
		panelControl.add( spinnerSize, gbcSpinnerSize );

		final JPanel panel3D = new JPanel();
		final GridBagConstraints gbcPanel3D = new GridBagConstraints();
		gbcPanel3D.anchor = GridBagConstraints.EAST;
		gbcPanel3D.gridwidth = 3;
		gbcPanel3D.insets = new Insets( 5, 5, 5, 5 );
		gbcPanel3D.fill = GridBagConstraints.VERTICAL;
		gbcPanel3D.gridx = 0;
		gbcPanel3D.gridy = 3;
		panelControl.add( panel3D, gbcPanel3D );

		final JRadioButton rdbtnCentralSlice = new JRadioButton( "Central slice", true );
		rdbtnCentralSlice.setFont( rdbtnCentralSlice.getFont().deriveFont( rdbtnCentralSlice.getFont().getSize() - 2f ) );
		rdbtn3D = new JRadioButton( "3D" );
		rdbtn3D.setFont( rdbtn3D.getFont().deriveFont( rdbtn3D.getFont().getSize() - 2f ) );

		panel3D.add( rdbtnCentralSlice );
		panel3D.add( rdbtn3D );
		final ButtonGroup btngrp = new ButtonGroup();
		btngrp.add( rdbtn3D );
		btngrp.add( rdbtnCentralSlice );

		lblLog = new JLabel( " " );
		lblLog.setFont( lblLog.getFont().deriveFont( lblLog.getFont().getStyle() | Font.ITALIC, lblLog.getFont().getSize() - 2f ) );
		final GridBagConstraints gbcLblLog = new GridBagConstraints();
		gbcLblLog.fill = GridBagConstraints.BOTH;
		gbcLblLog.gridwidth = 3;
		gbcLblLog.insets = new Insets( 5, 5, 5, 5 );
		gbcLblLog.gridx = 0;
		gbcLblLog.gridy = 4;
		panelControl.add( lblLog, gbcLblLog );

		final JPanel panelButtonExport = new JPanel();
		final GridBagConstraints gbcPanelButtonExport = new GridBagConstraints();
		gbcPanelButtonExport.anchor = GridBagConstraints.EAST;
		gbcPanelButtonExport.gridwidth = 3;
		gbcPanelButtonExport.gridx = 0;
		gbcPanelButtonExport.gridy = 5;
		panelControl.add( panelButtonExport, gbcPanelButtonExport );
		final FlowLayout flowLayout = ( FlowLayout ) panelButtonExport.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );

		btnRun = new JButton( "Run" );
		panelButtonExport.add( btnRun );

		final JPanel panelTitle = new JPanel();
		add( panelTitle, BorderLayout.NORTH );
		panelTitle.setLayout( new BorderLayout( 0, 5 ) );

		final JLabel lblCsvImporter = new JLabel( "Spot-track Image", JLabel.CENTER );
		lblCsvImporter.setFont( bigFont );
		lblCsvImporter.setHorizontalAlignment( SwingConstants.CENTER );
		panelTitle.add( lblCsvImporter, BorderLayout.NORTH );

		final JLabel lblVersion = new JLabel( "<html>Create a time-lapse image centered on a spot along time in a track. Select one spot (all the track will be captured) or two connected spots (the path from the first to the last will be captured).</html>", JLabel.CENTER );
		lblVersion.setFont( lblVersion.getFont().deriveFont( lblVersion.getFont().getStyle() | Font.ITALIC, lblVersion.getFont().getSize() - 2f ) );
		panelTitle.add( lblVersion, BorderLayout.SOUTH );

		// Listener stuff.
		sliderSize.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double val = MIN_SIZE + ( sliderSize.getValue() - 1 ) * STEP;
				final double spinVal = ( ( Number ) spinnerModel.getValue() ).doubleValue();
				if ( val == spinVal )
					return;
				spinnerModel.setValue( Double.valueOf( val ) );
			}
		} );
		spinnerModel.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final double spinVal = ( ( Number ) spinnerModel.getValue() ).doubleValue();
				final int intVal = ( int ) ( spinVal / STEP );
				final int sliderVal = sliderSize.getValue();
				if ( sliderVal == spinVal )
					return;
				
				sliderSize.setValue( intVal );
			}
		} );
	}

	int getSelectedSetupID()
	{
		return cmbboxSource.getSelectedSetupID();
	}

	double getSizeFactor()
	{
		return ( ( Number ) spinnerModel.getValue() ).doubleValue();
	}

	boolean is3D()
	{
		return rdbtn3D.isSelected();
	}

	public Consumer< String > getLogger()
	{
		return msg -> lblLog.setText( msg );
	}
}
