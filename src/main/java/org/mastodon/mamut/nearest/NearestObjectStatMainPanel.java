package org.mastodon.mamut.nearest;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;

public class NearestObjectStatMainPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JButton btnAdd;

	final JButton btnCompute;

	final JProgressBar pbar;

	final JButton btnCancel;

	public NearestObjectStatMainPanel( final NearestObjectStatModel model, final NearestObjectStatItem currentItem )
	{
		setLayout( new BorderLayout( 5, 5 ) );
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );

		final JLabel lblTitle = new JLabel( "Generate statistics on N nearest neighbors" );
		lblTitle.setFont( lblTitle.getFont().deriveFont( lblTitle.getFont().getSize() + 2f ) );
		lblTitle.setHorizontalAlignment( SwingConstants.LEFT );
		lblTitle.setVerticalAlignment( SwingConstants.TOP );
		add( lblTitle, BorderLayout.NORTH );

		final JPanel panelRun = new JPanel();
		add( panelRun, BorderLayout.SOUTH );
		panelRun.setLayout( new BoxLayout( panelRun, BoxLayout.X_AXIS ) );

		pbar = new JProgressBar();
		panelRun.add( pbar );

		btnCompute = new JButton( "Compute" );
		panelRun.add( btnCompute );

		btnCancel = new JButton( "Cancel" );
		btnCancel.setVisible( false );
		panelRun.add( btnCancel );

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder( null );
		splitPane.setResizeWeight( 0.2 );
		add( splitPane, BorderLayout.CENTER );

		/*
		 * LEFT
		 */

		final JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent( panelLeft );
		panelLeft.setLayout( new BoxLayout( panelLeft, BoxLayout.Y_AXIS ) );

		final NearestObjectStatPanel nearestObjectStatPanel = new NearestObjectStatPanel( currentItem );
		panelLeft.add( nearestObjectStatPanel );
		panelLeft.add( Box.createVerticalGlue() );
		this.btnAdd = new JButton( MastodonIcons.ADD_ICON );
		final JPanel pnlButton = new JPanel();
		pnlButton.setLayout( new BoxLayout( pnlButton, BoxLayout.LINE_AXIS ) );
		pnlButton.add( Box.createHorizontalGlue() );
		pnlButton.add( btnAdd );
		panelLeft.add( pnlButton );

		/*
		 * RIGHT
		 */

		final JPanel panelRight = new JPanel();
		splitPane.setRightComponent( panelRight );
		panelRight.setLayout( new BoxLayout( panelRight, BoxLayout.Y_AXIS ) );
		panelRight.add( new JLabel( "To compute:" ) );
		panelRight.add( Box.createVerticalStrut( 5 ) );
		final NearestObjectStatListPanel listPanel = new NearestObjectStatListPanel( model );
		panelRight.add( listPanel );
		panelRight.setMinimumSize( new Dimension( 200, 400 ) );
	}
}
