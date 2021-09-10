package org.mastodon.mamut.crown;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class CrownScaleDialog extends JDialog
{

	private static final long serialVersionUID = 1L;

	public CrownScaleDialog( final DoubleConsumer scaleSetter, final DoubleSupplier scaleGetter )
	{
		setBounds( 100, 100, 300, 300 );
		getContentPane().setLayout( new BorderLayout() );
		final JPanel contentPanel = new JPanel();
		contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		getContentPane().add( contentPanel, BorderLayout.CENTER );
		final GridBagLayout gblContentPanel = new GridBagLayout();
		gblContentPanel.columnWidths = new int[] { 251, 71, 0 };
		gblContentPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gblContentPanel.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gblContentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout( gblContentPanel );

		final JLabel lblTitle = new JLabel( "Crown intensity" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getSize() + 2f ) );
		final GridBagConstraints gbcLblTitle = new GridBagConstraints();
		gbcLblTitle.gridwidth = 2;
		gbcLblTitle.insets = new Insets( 5, 5, 5, 5 );
		gbcLblTitle.gridx = 0;
		gbcLblTitle.gridy = 0;
		contentPanel.add( lblTitle, gbcLblTitle );

		final JLabel lblText = new JLabel( "<html>Set the scale for crown intensity measurements.</html>" );
		lblText.setFont( lblText.getFont().deriveFont( lblText.getFont().getStyle() | Font.ITALIC ) );
		final GridBagConstraints gbcLblText = new GridBagConstraints();
		gbcLblText.fill = GridBagConstraints.HORIZONTAL;
		gbcLblText.insets = new Insets( 10, 5, 10, 5 );
		gbcLblText.gridwidth = 2;
		gbcLblText.gridx = 0;
		gbcLblText.gridy = 1;
		contentPanel.add( lblText, gbcLblText );

		final JLabel lblScale = new JLabel( "scale:" );
		final GridBagConstraints gbcLblScale = new GridBagConstraints();
		gbcLblScale.anchor = GridBagConstraints.EAST;
		gbcLblScale.insets = new Insets( 5, 5, 5, 5 );
		gbcLblScale.gridx = 0;
		gbcLblScale.gridy = 2;
		contentPanel.add( lblScale, gbcLblScale );

		final JFormattedTextField ftfScale = new JFormattedTextField( Double.valueOf( scaleGetter.getAsDouble() ) );
		selectAllOnFocus( ftfScale );

		ftfScale.setHorizontalAlignment( SwingConstants.CENTER );
		ftfScale.setColumns( 10 );
		final GridBagConstraints gbcFtfScale = new GridBagConstraints();
		gbcFtfScale.insets = new Insets( 5, 5, 5, 5 );
		gbcFtfScale.fill = GridBagConstraints.HORIZONTAL;
		gbcFtfScale.gridx = 1;
		gbcFtfScale.gridy = 2;
		contentPanel.add( ftfScale, gbcFtfScale );

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
		getContentPane().add( buttonPane, BorderLayout.SOUTH );

		final JButton okButton = new JButton( "Set" );
		okButton.setActionCommand( "Set" );
		buttonPane.add( okButton );
		getRootPane().setDefaultButton( okButton );
		okButton.addActionListener( e -> {
			final double scale = ( ( Number ) ftfScale.getValue() ).doubleValue();
			scaleSetter.accept( Math.abs( scale ) );
		} );
	}

	// TODO add to a util class.
	private static final FocusListener selectAllFocusListener = new FocusListener()
	{

		@Override
		public void focusLost( final FocusEvent e )
		{}

		@Override
		public void focusGained( final FocusEvent fe )
		{
			if ( !( fe.getSource() instanceof JTextField ) )
				return;
			final JTextField txt = ( JTextField ) fe.getSource();
			SwingUtilities.invokeLater( () -> txt.selectAll() );
		}
	};

	public static final void selectAllOnFocus( final JTextField tf )
	{
		tf.addFocusListener( selectAllFocusListener );
	}
}
