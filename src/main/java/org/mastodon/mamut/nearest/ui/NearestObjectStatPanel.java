package org.mastodon.mamut.nearest.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;

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
		gridBagLayout.columnWidths = new int[] { 0, 0, 106, 120, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		setLayout( gridBagLayout );

		final JLabel lblInputValue = new JLabel( "Input values:" );
		final GridBagConstraints gbc_lblInputValue = new GridBagConstraints();
		gbc_lblInputValue.gridwidth = 5;
		gbc_lblInputValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblInputValue.anchor = GridBagConstraints.WEST;
		gbc_lblInputValue.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblInputValue.gridx = 0;
		gbc_lblInputValue.gridy = 0;
		add( lblInputValue, gbc_lblInputValue );

		final JLabel lblCollectBy = new JLabel( "collect neighbors by:" );
		final GridBagConstraints gbcLblCollectBy = new GridBagConstraints();
		gbcLblCollectBy.anchor = GridBagConstraints.EAST;
		gbcLblCollectBy.insets = new Insets( 0, 0, 5, 5 );
		gbcLblCollectBy.gridx = 2;
		gbcLblCollectBy.gridy = 1;
		add( lblCollectBy, gbcLblCollectBy );

		rdbtnNNearest = new JRadioButton( "specifying N" );
		rdbtnNNearest.setToolTipText( TOOLTIP_COLLECT_N );
		final GridBagConstraints gbcRdbtnNNearest = new GridBagConstraints();
		gbcRdbtnNNearest.anchor = GridBagConstraints.WEST;
		gbcRdbtnNNearest.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnNNearest.gridwidth = 2;
		gbcRdbtnNNearest.gridx = 3;
		gbcRdbtnNNearest.gridy = 1;
		add( rdbtnNNearest, gbcRdbtnNNearest );

		final JRadioButton rdbtnMaxdistance = new JRadioButton( "max distance" );
		rdbtnMaxdistance.setToolTipText( TOOLTIP_COLLECT_DISTANCE );
		final GridBagConstraints gbcRdbtnMaxdistance = new GridBagConstraints();
		gbcRdbtnMaxdistance.anchor = GridBagConstraints.WEST;
		gbcRdbtnMaxdistance.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnMaxdistance.gridwidth = 2;
		gbcRdbtnMaxdistance.gridx = 3;
		gbcRdbtnMaxdistance.gridy = 2;
		add( rdbtnMaxdistance, gbcRdbtnMaxdistance );

		final JLabel lblN = new JLabel( "N:" );
		lblN.setToolTipText( TOOLTIP_N );
		final GridBagConstraints gbcLblN = new GridBagConstraints();
		gbcLblN.anchor = GridBagConstraints.EAST;
		gbcLblN.insets = new Insets( 0, 0, 5, 5 );
		gbcLblN.gridx = 2;
		gbcLblN.gridy = 3;
		add( lblN, gbcLblN );

		this.spinnerModel = new SpinnerNumberModel( 6, 1, 200, 1 );
		final JSpinner spinner = new JSpinner( spinnerModel );
		spinner.setToolTipText( TOOLTIP_N );
		spinner.setMaximumSize( new Dimension( 120, 120 ) );
		final GridBagConstraints gbcSpinner = new GridBagConstraints();
		gbcSpinner.gridwidth = 1;
		gbcSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbcSpinner.insets = new Insets( 0, 0, 5, 5 );
		gbcSpinner.gridx = 3;
		gbcSpinner.gridy = 3;
		add( spinner, gbcSpinner );

		final JLabel lblMaxDistance = new JLabel( "max distance:" );
		lblMaxDistance.setToolTipText( TOOLTIP_DISTANCE );
		final GridBagConstraints gbcLblMaxDistance = new GridBagConstraints();
		gbcLblMaxDistance.anchor = GridBagConstraints.EAST;
		gbcLblMaxDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcLblMaxDistance.gridx = 2;
		gbcLblMaxDistance.gridy = 4;
		add( lblMaxDistance, gbcLblMaxDistance );

		this.ftfMaxDistance = new JFormattedTextField( new DecimalFormat( "0.0" ) )
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void commitEdit() throws ParseException
			{
				if ( !isEditValid() )
					return;
				super.commitEdit();
			}

			@Override
			public boolean isEditValid()
			{
				if ( !super.isEditValid() )
					return false;

				try
				{
					final double val = Double.parseDouble( getText() );
					return val >= 0;
				}
				catch ( final NumberFormatException nfe )
				{
					return false;
				}
			}
		};
		( ( DefaultFormatter ) ftfMaxDistance.getFormatter() ).setOverwriteMode( false );
		( ( DefaultFormatter ) ftfMaxDistance.getFormatter() ).setCommitsOnValidEdit( true );
		ftfMaxDistance.setToolTipText( TOOLTIP_DISTANCE );
		ftfMaxDistance.setHorizontalAlignment( JFormattedTextField.RIGHT );
		final GridBagConstraints gbcFtfMaxDistance = new GridBagConstraints();
		gbcFtfMaxDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcFtfMaxDistance.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfMaxDistance.gridx = 3;
		gbcFtfMaxDistance.gridy = 4;
		add( ftfMaxDistance, gbcFtfMaxDistance );

		final JLabel lblUnits = new JLabel( units );
		final GridBagConstraints gbcLblUnits = new GridBagConstraints();
		gbcLblUnits.anchor = GridBagConstraints.WEST;
		gbcLblUnits.insets = new Insets( 0, 0, 5, 0 );
		gbcLblUnits.gridx = 4;
		gbcLblUnits.gridy = 4;
		add( lblUnits, gbcLblUnits );

		final JLabel lblOutputValues = new JLabel( "Output values:" );
		final GridBagConstraints gbc_lblOutputValues = new GridBagConstraints();
		gbc_lblOutputValues.anchor = GridBagConstraints.WEST;
		gbc_lblOutputValues.gridwidth = 4;
		gbc_lblOutputValues.insets = new Insets( 0, 0, 5, 5 );
		gbc_lblOutputValues.gridx = 1;
		gbc_lblOutputValues.gridy = 5;
		add( lblOutputValues, gbc_lblOutputValues );

		final JLabel lblMetrics = new JLabel( "value:" );
		final GridBagConstraints gbcLblMetrics = new GridBagConstraints();
		gbcLblMetrics.anchor = GridBagConstraints.EAST;
		gbcLblMetrics.insets = new Insets( 0, 0, 5, 5 );
		gbcLblMetrics.gridx = 2;
		gbcLblMetrics.gridy = 6;
		add( lblMetrics, gbcLblMetrics );

		this.rdbtnDistanceOrN = new JRadioButton( "distance" );
		rdbtnDistanceOrN.setToolTipText( rdbtnNNearest.isSelected() ? TOOLTIP_VALUE_DISTANCE : TOOLTIP_VALUE_N );
		final GridBagConstraints gbcRdbtnDistance = new GridBagConstraints();
		gbcRdbtnDistance.anchor = GridBagConstraints.WEST;
		gbcRdbtnDistance.insets = new Insets( 0, 0, 5, 5 );
		gbcRdbtnDistance.gridx = 3;
		gbcRdbtnDistance.gridy = 6;
		add( rdbtnDistanceOrN, gbcRdbtnDistance );

		final JRadioButton rdbtnFeature = new JRadioButton( "feature" );
		rdbtnFeature.setToolTipText( TOOLTIP_VALUE_FEATURE );
		final GridBagConstraints gbcRdbtnFeature = new GridBagConstraints();
		gbcRdbtnFeature.anchor = GridBagConstraints.WEST;
		gbcRdbtnFeature.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnFeature.gridx = 4;
		gbcRdbtnFeature.gridy = 6;
		add( rdbtnFeature, gbcRdbtnFeature );

		this.featureSelectionPanel = new FeatureSelectionPanel();
		featureSelectionPanel.getPanel().setToolTipText( TOOLTIP_FEATURE );
		for ( final Component c : featureSelectionPanel.getPanel().getComponents() )
			( ( JComponent ) c ).setToolTipText( TOOLTIP_FEATURE );
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
		lblStat.setToolTipText( TOOLTIP_STAT );
		final GridBagConstraints gbcLblStat = new GridBagConstraints();
		gbcLblStat.anchor = GridBagConstraints.EAST;
		gbcLblStat.insets = new Insets( 0, 0, 5, 5 );
		gbcLblStat.gridx = 2;
		gbcLblStat.gridy = 9;
		add( lblStat, gbcLblStat );

		this.cmbboxStat = new JComboBox<>( new Vector<>( Arrays.asList( Stat.values() ) ) );
		cmbboxStat.setToolTipText( TOOLTIP_STAT );
		final GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.insets = new Insets( 0, 0, 5, 5 );
		gbcComboBox.gridx = 3;
		gbcComboBox.gridy = 9;
		add( cmbboxStat, gbcComboBox );

		final JLabel lblInclude = new JLabel( "include center:" );
		lblInclude.setToolTipText( TOOLTIP_INCLUDE );
		final GridBagConstraints gbcLblInclude = new GridBagConstraints();
		gbcLblInclude.insets = new Insets( 0, 0, 5, 5 );
		gbcLblInclude.anchor = GridBagConstraints.EAST;
		gbcLblInclude.gridx = 2;
		gbcLblInclude.gridy = 10;
		add( lblInclude, gbcLblInclude );

		this.chckbxInclude = new JCheckBox( "" );
		chckbxInclude.setToolTipText( TOOLTIP_INCLUDE );
		final GridBagConstraints gbcChckbxInclude = new GridBagConstraints();
		gbcChckbxInclude.insets = new Insets( 0, 0, 5, 5 );
		gbcChckbxInclude.anchor = GridBagConstraints.WEST;
		gbcChckbxInclude.gridx = 3;
		gbcChckbxInclude.gridy = 10;
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
			rdbtnDistanceOrN.setToolTipText( isN ? TOOLTIP_VALUE_DISTANCE : TOOLTIP_VALUE_N );
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
		final JFrame frame = new JFrame( "stat panel" );
		frame.getContentPane().add( panel );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	private static final String TOOLTIP_COLLECT_N = "<html>"
			+ "If selected, the statistics will be built using a fixed <br>"
			+ "number of neighbors. This number is specified with the <br>"
			+ "spinner below."
			+ "</html>";

	private static final String TOOLTIP_COLLECT_DISTANCE = "<html>"
			+ "If selected, the statistics will be built from all the <br>"
			+ "neighbors that are within a given distance from the spot. <br>"
			+ "The distance is specified in the numeric field below."
			+ "</html>";

	private static final String TOOLTIP_N = "<html>"
			+ "Specifies the number of nearest neighbors to include in <br>"
			+ "the statistics when collecting them by number."
			+ "</html>";

	private static final String TOOLTIP_DISTANCE = "<html>"
			+ "Specifies the maximum distance when collecting nearest <br>"
			+ "neighbors based on their distance to the spot."
			+ "</html>";

	private static final String TOOLTIP_VALUE_DISTANCE = "<html>"
			+ "If selected, the statistics will be computed on the <br>"
			+ "distances from the spot to the nearest-neighbors <br>"
			+ "collected."
			+ "</html>";

	private static final String TOOLTIP_VALUE_N = "<html>"
			+ "If selected, the statistics will report the number of <br>"
			+ "neighbors that are found withing the maximal distance <br>"
			+ "specified above. The statistics function below has no <br>"
			+ "impact."
			+ "</html>";

	private static final String TOOLTIP_VALUE_FEATURE = "<html>"
			+ "If selected, the statistics will be computed on the feature <br>"
			+ "values collected from the nearest-neighbors. The feature to <br>"
			+ "read values from is specified below."
			+ "</html>";

	private static final String TOOLTIP_FEATURE = "<html>"
			+ "Specifies what feature to read values from when the value <br>"
			+ "setting above is 'feature'. The feature values must be <br>"
			+ "computed <b>before</b> running the statistics computation <br>"
			+ "as they are not automatically recomputed. If a neighbor has <br>"
			+ " missing value for the requested feature, then the NN <br>"
			+ "statistics will report 'NaN' for this spot."
			+ "</html>";

	private static final String TOOLTIP_STAT = "<html>"
			+ "Specifies how to generate a summarized value from the <br>"
			+ "collection of values extracted from neighbors."
			+ "</html>";

	private static final String TOOLTIP_INCLUDE = "<html>"
			+ "If selected, the statistics will be computed on values <br>"
			+ "that include the spot. When collecting the number of <br>"
			+ "neighbors within a max distance, the spot will be included <br>"
			+ "in the count."
			+ "</html>";
}
