package org.mastodon.mamut.nearest.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.feature.ui.FeatureSelectionPanel;
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

	private final JRadioButton rdbtnDistance;

	private final FeatureSelectionPanel featureSelectionPanel;

	public NearestObjectStatPanel( final AvailableFeatureProjections afp )
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 106, 120, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblN = new JLabel( "n" );
		final GridBagConstraints gbcLblN = new GridBagConstraints();
		gbcLblN.anchor = GridBagConstraints.EAST;
		gbcLblN.insets = new Insets( 0, 0, 5, 5 );
		gbcLblN.gridx = 0;
		gbcLblN.gridy = 0;
		add( lblN, gbcLblN );

		this.spinnerModel = new SpinnerNumberModel( 6, 1, 200, 1 );
		final JSpinner spinner = new JSpinner( spinnerModel );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinner.insets = new Insets( 0, 0, 5, 5 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 0;
		add( spinner, gbcSpinner );

		final JLabel lblMetrics = new JLabel( "value" );
		final GridBagConstraints gbcLblMetrics = new GridBagConstraints();
		gbcLblMetrics.anchor = GridBagConstraints.EAST;
		gbcLblMetrics.insets = new Insets( 0, 0, 5, 5 );
		gbcLblMetrics.gridx = 0;
		gbcLblMetrics.gridy = 1;
		add( lblMetrics, gbcLblMetrics );

		this.rdbtnDistance = new JRadioButton( "distance" );
		final GridBagConstraints gbcRdbtnDistance = new GridBagConstraints();
		gbcRdbtnDistance.anchor = GridBagConstraints.WEST;
		gbcRdbtnDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnDistance.gridx = 1;
		gbcRdbtnDistance.gridy = 1;
		add( rdbtnDistance, gbcRdbtnDistance );

		this.featureSelectionPanel = new FeatureSelectionPanel();
		featureSelectionPanel.setAvailableFeatureProjections( afp, TargetType.VERTEX );
		final GridBagConstraints gbcFeatureSelectionPanel = new GridBagConstraints();
		gbcFeatureSelectionPanel.anchor = GridBagConstraints.EAST;
		gbcFeatureSelectionPanel.insets = new Insets( 0, 0, 5, 5 );
		gbcFeatureSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcFeatureSelectionPanel.gridwidth = 3;
		gbcFeatureSelectionPanel.gridx = 0;
		gbcFeatureSelectionPanel.gridy = 3;
		add( featureSelectionPanel.getPanel(), gbcFeatureSelectionPanel );

		final JRadioButton rdbtnFeature = new JRadioButton( "feature" );
		final GridBagConstraints gbcRdbtnFeature = new GridBagConstraints();
		gbcRdbtnFeature.anchor = GridBagConstraints.WEST;
		gbcRdbtnFeature.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnFeature.gridx = 2;
		gbcRdbtnFeature.gridy = 1;
		add( rdbtnFeature, gbcRdbtnFeature );

		final JLabel lblStat = new JLabel( "statistics" );
		final GridBagConstraints gbcLblStat = new GridBagConstraints();
		gbcLblStat.anchor = GridBagConstraints.EAST;
		gbcLblStat.insets = new Insets( 0, 0, 5, 5 );
		gbcLblStat.gridx = 0;
		gbcLblStat.gridy = 3;
		add( lblStat, gbcLblStat );

		this.cmbboxStat = new JComboBox<>( new Vector<>( Arrays.asList( Stat.values() ) ) );
		final GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.insets = new Insets( 0, 0, 5, 5 );
		gbcComboBox.gridx = 1;
		gbcComboBox.gridy = 3;
		add( cmbboxStat, gbcComboBox );

		final JLabel lblInclude = new JLabel( "include obj" );
		final GridBagConstraints gbcLblInclude = new GridBagConstraints();
		gbcLblInclude.insets = new Insets( 0, 0, 0, 5 );
		gbcLblInclude.anchor = GridBagConstraints.EAST;
		gbcLblInclude.gridx = 0;
		gbcLblInclude.gridy = 4;
		add( lblInclude, gbcLblInclude );

		this.chckbxInclude = new JCheckBox( "" );
		final GridBagConstraints gbcChckbxInclude = new GridBagConstraints();
		gbcChckbxInclude.insets = new Insets( 0, 0, 0, 5 );
		gbcChckbxInclude.anchor = GridBagConstraints.WEST;
		gbcChckbxInclude.gridx = 1;
		gbcChckbxInclude.gridy = 4;
		add( chckbxInclude, gbcChckbxInclude );

		// Listeners.
		rdbtnDistance.addItemListener( e -> toggleFeatureEnabled( !rdbtnDistance.isSelected() ) );

		// Massage.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnDistance );
		buttonGroup.add( rdbtnFeature );

		// Defaults
		final NearestObjectStatItem item = NearestObjectStatItem.defaultValue();
		spinnerModel.setValue( Integer.valueOf( item.n ) );
		rdbtnDistance.setSelected( item.value == Value.DISTANCE );
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
				spinnerModel.getNumber().intValue(),
				rdbtnDistance.isSelected() ? Value.DISTANCE : Value.FEATURE,
				featureID,
				( Stat ) cmbboxStat.getSelectedItem(),
				chckbxInclude.isSelected() );
	}

	public void setAvailableFeatureProjections( final AvailableFeatureProjections afp )
	{
		featureSelectionPanel.setAvailableFeatureProjections( afp, TargetType.VERTEX );
	}
}
