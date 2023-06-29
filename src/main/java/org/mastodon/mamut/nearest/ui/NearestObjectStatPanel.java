package org.mastodon.mamut.nearest.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStat;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;

public class NearestObjectStatPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final SpinnerNumberModel spinnerModel;

	private final JComboBox< NearestObjectStat > cmbboxStat;

	private final JCheckBox chckbxInclude;

	public NearestObjectStatPanel(  )
	{
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 106, 120, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblN = new JLabel( "n:" );
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
		gbcSpinner.insets = new Insets( 0, 0, 5, 0 );
		gbcSpinner.gridx = 1;
		gbcSpinner.gridy = 0;
		add( spinner, gbcSpinner );

		final JLabel lblStat = new JLabel( "Statistics:" );
		final GridBagConstraints gbcLblStat = new GridBagConstraints();
		gbcLblStat.anchor = GridBagConstraints.EAST;
		gbcLblStat.insets = new Insets( 0, 0, 5, 5 );
		gbcLblStat.gridx = 0;
		gbcLblStat.gridy = 1;
		add( lblStat, gbcLblStat );

		this.cmbboxStat = new JComboBox<>( new Vector<>( Arrays.asList( NearestObjectStat.values() ) ) );
		final GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.insets = new Insets( 0, 0, 5, 0 );
		gbcComboBox.gridx = 1;
		gbcComboBox.gridy = 1;
		add( cmbboxStat, gbcComboBox );

		final JLabel lblInclude = new JLabel( "Include obj:" );
		final GridBagConstraints gbcLblInclude = new GridBagConstraints();
		gbcLblInclude.insets = new Insets( 0, 0, 0, 5 );
		gbcLblInclude.anchor = GridBagConstraints.EAST;
		gbcLblInclude.gridx = 0;
		gbcLblInclude.gridy = 2;
		add( lblInclude, gbcLblInclude );

		this.chckbxInclude = new JCheckBox( "" );
		final GridBagConstraints gbcChckbxInclude = new GridBagConstraints();
		gbcChckbxInclude.anchor = GridBagConstraints.WEST;
		gbcChckbxInclude.gridx = 1;
		gbcChckbxInclude.gridy = 2;
		add( chckbxInclude, gbcChckbxInclude );

		// Defaults
		final NearestObjectStatItem item = new NearestObjectStatItem( 6, NearestObjectStat.MEAN, false );
		spinnerModel.setValue( Integer.valueOf( item.n ) );
		cmbboxStat.setSelectedItem( item.statStat );
		chckbxInclude.setSelected( item.include );
	}

	public NearestObjectStatItem get()
	{
		return new NearestObjectStatItem(
				spinnerModel.getNumber().intValue(),
				( NearestObjectStat ) cmbboxStat.getSelectedItem(),
				chckbxInclude.isSelected() );
	}
}
