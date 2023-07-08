package org.mastodon.mamut.nearest.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.feature.ui.FeatureSelectionPanel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.CollectBy;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Stat;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Value;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.TargetType;

public class NearestObjectStatPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final SpinnerNumberModel spinnerModel;

	private final JComboBox< Stat > cmbboxStat;

	private final JCheckBox chckbxInclude;

	private final JRadioButton rdbtnDistanceOrN;

	private final FeatureSelectionPanel featureSelectionPanel;

	private final JRadioButton rdbtnNNearest;

	private final JFormattedTextField ftfMaxDistance;

	public NearestObjectStatPanel( final AvailableFeatureProjections afp, final String units )
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 106, 120, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		setLayout( gridBagLayout );

		final JLabel lblCollectBy = new JLabel( "collect neighbors by:" );
		final GridBagConstraints gbcLblCollectBy = new GridBagConstraints();
		gbcLblCollectBy.anchor = GridBagConstraints.EAST;
		gbcLblCollectBy.insets = new Insets( 0, 0, 5, 5 );
		gbcLblCollectBy.gridx = 0;
		gbcLblCollectBy.gridy = 0;
		add( lblCollectBy, gbcLblCollectBy );

		rdbtnNNearest = new JRadioButton( "specifying N" );
		final GridBagConstraints gbcRdbtnNNearest = new GridBagConstraints();
		gbcRdbtnNNearest.anchor = GridBagConstraints.WEST;
		gbcRdbtnNNearest.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnNNearest.gridwidth = 2;
		gbcRdbtnNNearest.gridx = 1;
		gbcRdbtnNNearest.gridy = 0;
		add( rdbtnNNearest, gbcRdbtnNNearest );

		final JRadioButton rdbtnMaxdistance = new JRadioButton( "max distance" );
		final GridBagConstraints gbcRdbtnMaxdistance = new GridBagConstraints();
		gbcRdbtnMaxdistance.anchor = GridBagConstraints.WEST;
		gbcRdbtnMaxdistance.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnMaxdistance.gridwidth = 2;
		gbcRdbtnMaxdistance.gridx = 1;
		gbcRdbtnMaxdistance.gridy = 1;
		add( rdbtnMaxdistance, gbcRdbtnMaxdistance );

		final JLabel lblN = new JLabel( "N:" );
		final GridBagConstraints gbcLblN = new GridBagConstraints();
		gbcLblN.anchor = GridBagConstraints.EAST;
		gbcLblN.insets = new Insets( 0, 0, 5, 5 );
		gbcLblN.gridx = 0;
		gbcLblN.gridy = 2;
		add( lblN, gbcLblN );

		this.spinnerModel = new SpinnerNumberModel( 6, 1, 200, 1 );
		final JSpinner spinner = new JSpinner( spinnerModel );
		spinner.setMaximumSize( new Dimension( 120, 120 ) );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.gridwidth = 1;
		gbcSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinner.insets = new Insets( 0, 0, 5, 5 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 2;
		add( spinner, gbcSpinner );

		final JLabel lblMaxDistance = new JLabel( "max distance:" );
		final GridBagConstraints gbcLblMaxDistance = new GridBagConstraints();
		gbcLblMaxDistance.anchor = GridBagConstraints.EAST;
		gbcLblMaxDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcLblMaxDistance.gridx = 0;
		gbcLblMaxDistance.gridy = 3;
		add( lblMaxDistance, gbcLblMaxDistance );

		this.ftfMaxDistance = new JFormattedTextField( "0.00" );
		ftfMaxDistance.setHorizontalAlignment( JFormattedTextField.RIGHT );
		final GridBagConstraints gbcFtfMaxDistance = new GridBagConstraints();
		gbcFtfMaxDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcFtfMaxDistance.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfMaxDistance.gridx = 1;
		gbcFtfMaxDistance.gridy = 3;
		add( ftfMaxDistance, gbcFtfMaxDistance );

		final JLabel lblUnits = new JLabel( units );
		final GridBagConstraints gbcLblUnits = new GridBagConstraints();
		gbcLblUnits.anchor = GridBagConstraints.WEST;
		gbcLblUnits.insets = new Insets( 0, 0, 5, 0 );
		gbcLblUnits.gridx = 2;
		gbcLblUnits.gridy = 3;
		add( lblUnits, gbcLblUnits );

		final JLabel lblMetrics = new JLabel( "value:" );
		final GridBagConstraints gbcLblMetrics = new GridBagConstraints();
		gbcLblMetrics.anchor = GridBagConstraints.EAST;
		gbcLblMetrics.insets = new Insets( 0, 0, 5, 5 );
		gbcLblMetrics.gridx = 0;
		gbcLblMetrics.gridy = 4;
		add( lblMetrics, gbcLblMetrics );

		this.rdbtnDistanceOrN = new JRadioButton( "distance" );
		final GridBagConstraints gbcRdbtnDistance = new GridBagConstraints();
		gbcRdbtnDistance.anchor = GridBagConstraints.WEST;
		gbcRdbtnDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnDistance.gridx = 1;
		gbcRdbtnDistance.gridy = 4;
		add( rdbtnDistanceOrN, gbcRdbtnDistance );


		final JRadioButton rdbtnFeature = new JRadioButton( "feature" );
		final GridBagConstraints gbcRdbtnFeature = new GridBagConstraints();
		gbcRdbtnFeature.anchor = GridBagConstraints.WEST;
		gbcRdbtnFeature.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnFeature.gridx = 2;
		gbcRdbtnFeature.gridy = 4;
		add( rdbtnFeature, gbcRdbtnFeature );

		this.featureSelectionPanel = new FeatureSelectionPanel();
		featureSelectionPanel.setAvailableFeatureProjections( afp, TargetType.VERTEX );
		final GridBagConstraints gbcFeatureSelectionPanel = new GridBagConstraints();
		gbcFeatureSelectionPanel.anchor = GridBagConstraints.EAST;
		gbcFeatureSelectionPanel.insets = new Insets( 0, 0, 5, 5 );
		gbcFeatureSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcFeatureSelectionPanel.gridwidth = 3;
		gbcFeatureSelectionPanel.gridx = 0;
		gbcFeatureSelectionPanel.gridy = 5;
		add( featureSelectionPanel.getPanel(), gbcFeatureSelectionPanel );

		final JLabel lblStat = new JLabel( "statistics:" );
		final GridBagConstraints gbcLblStat = new GridBagConstraints();
		gbcLblStat.anchor = GridBagConstraints.EAST;
		gbcLblStat.insets = new Insets( 0, 0, 5, 5 );
		gbcLblStat.gridx = 0;
		gbcLblStat.gridy = 7;
		add( lblStat, gbcLblStat );

		this.cmbboxStat = new JComboBox<>( new Vector<>( Arrays.asList( Stat.values() ) ) );
		final GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.insets = new Insets( 0, 0, 5, 5 );
		gbcComboBox.gridx = 1;
		gbcComboBox.gridy = 7;
		add( cmbboxStat, gbcComboBox );

		final JLabel lblInclude = new JLabel( "include center:" );
		final GridBagConstraints gbcLblInclude = new GridBagConstraints();
		gbcLblInclude.insets = new Insets( 0, 0, 5, 5 );
		gbcLblInclude.anchor = GridBagConstraints.EAST;
		gbcLblInclude.gridx = 0;
		gbcLblInclude.gridy = 8;
		add( lblInclude, gbcLblInclude );

		this.chckbxInclude = new JCheckBox( "" );
		final GridBagConstraints gbcChckbxInclude = new GridBagConstraints();
		gbcChckbxInclude.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxInclude.anchor = GridBagConstraints.WEST;
		gbcChckbxInclude.gridx = 1;
		gbcChckbxInclude.gridy = 8;
		add( chckbxInclude, gbcChckbxInclude );

		// Listeners.
		rdbtnDistanceOrN.addItemListener( e -> {
			toggleFeatureEnabled( !rdbtnDistanceOrN.isSelected() );
			cmbboxStat.setEnabled( rdbtnNNearest.isSelected() || !rdbtnDistanceOrN.isSelected() );
		} );

		rdbtnNNearest.addItemListener( e -> {
			final boolean isN = rdbtnNNearest.isSelected();
			ftfMaxDistance.setEnabled( !isN );
			spinner.setEnabled( isN );
			rdbtnDistanceOrN.setText( isN ? "distance" : "N neighbors" );
			cmbboxStat.setEnabled( isN || !rdbtnDistanceOrN.isSelected() );
		} );

		// Massage.
		final ButtonGroup buttonGroupCollect = new ButtonGroup();
		buttonGroupCollect.add( rdbtnNNearest );
		buttonGroupCollect.add( rdbtnMaxdistance );

		final ButtonGroup buttonGroupValue = new ButtonGroup();
		buttonGroupValue.add( rdbtnDistanceOrN );
		buttonGroupValue.add( rdbtnFeature );

		// Defaults
		final NearestObjectStatItem item = NearestObjectStatItem.defaultValue();
		rdbtnNNearest.setSelected( item.collectBy == CollectBy.SPECIFY_N );
		spinnerModel.setValue( Integer.valueOf( item.n ) );
		ftfMaxDistance.setValue( Double.valueOf( item.maxDistance ) );
		rdbtnDistanceOrN.setSelected( item.value == Value.DISTANCE_OR_N );
		featureSelectionPanel.setSelection( item.featureID );
		cmbboxStat.setSelectedItem( item.stat );
		chckbxInclude.setSelected( item.include );
	}

	private void toggleFeatureEnabled( final boolean enabled )
	{
		for ( final Component c : featureSelectionPanel.getPanel().getComponents() )
			c.setEnabled( enabled );
	}

	public NearestObjectStatItem get()
	{
		final FeatureProjectionId featureID = featureSelectionPanel.getSelection();
		return new NearestObjectStatItem(
				rdbtnNNearest.isSelected() ? CollectBy.SPECIFY_N : CollectBy.MAX_DISTANCE,
				spinnerModel.getNumber().intValue(),
				( ( Number ) ftfMaxDistance.getValue() ).doubleValue(),
				rdbtnDistanceOrN.isSelected() ? Value.DISTANCE_OR_N : Value.FEATURE,
				featureID,
				( Stat ) cmbboxStat.getSelectedItem(),
				chckbxInclude.isSelected() );
	}

	public void setAvailableFeatureProjections( final AvailableFeatureProjections afp )
	{
		featureSelectionPanel.setAvailableFeatureProjections( afp, TargetType.VERTEX );
	}

	public static void main( final String[] args )
	{
		final NearestObjectStatPanel panel = new NearestObjectStatPanel( null, "µm" );
		final JFrame frame = new JFrame("stat panel");
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}
}
