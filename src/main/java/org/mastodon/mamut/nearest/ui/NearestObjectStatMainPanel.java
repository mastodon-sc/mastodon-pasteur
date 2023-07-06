package org.mastodon.mamut.nearest.ui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import org.mastodon.app.MastodonIcons;
import org.mastodon.feature.ui.AvailableFeatureProjections;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.ui.ProgressListener;

public class NearestObjectStatMainPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	final JButton btnAdd;

	final JButton btnCompute;

	final JButton btnCancel;

	final JLabel lblLog;

	private final NearestObjectStatPanel nearestObjectStatPanel;

	private final JProgressBar pbar;

	public NearestObjectStatMainPanel( final NearestObjectStatModel model, final AvailableFeatureProjections afp, final String units )
	{
		setLayout( new BorderLayout( 5, 5 ) );
		setBorder( new EmptyBorder( 5, 5, 5, 5 ) );

		final JPanel panelRun = new JPanel();
		add( panelRun, BorderLayout.SOUTH );
		panelRun.setLayout( new BoxLayout( panelRun, BoxLayout.X_AXIS ) );

		pbar = new JProgressBar();
		pbar.setStringPainted( true );
		panelRun.add( pbar );

		btnCompute = new JButton( "Compute" );
		panelRun.add( btnCompute );

		btnCancel = new JButton( "Cancel" );
		btnCancel.setVisible( false );
		panelRun.add( btnCancel );

		final JSplitPane splitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
		splitPane.setBorder( null );
		splitPane.setResizeWeight( 0.2 );
		add( splitPane, BorderLayout.CENTER );

		/*
		 * TOP
		 */

		final JPanel panelTop = new JPanel();
		splitPane.setLeftComponent( panelTop );
		panelTop.setLayout( new BoxLayout( panelTop, BoxLayout.Y_AXIS ) );

		this.nearestObjectStatPanel = new NearestObjectStatPanel( afp, units );
		panelTop.add( nearestObjectStatPanel );
		panelTop.add( Box.createVerticalGlue() );
		this.btnAdd = new JButton( MastodonIcons.ADD_ICON );
		final JPanel pnlButton = new JPanel();
		pnlButton.setLayout( new BoxLayout( pnlButton, BoxLayout.LINE_AXIS ) );
		this.lblLog = new JLabel();
		pnlButton.add( lblLog );
		pnlButton.add( Box.createHorizontalGlue() );
		pnlButton.add( new JLabel( "Add to list" ) );
		pnlButton.add( Box.createHorizontalStrut( 5 ) );
		pnlButton.add( btnAdd );
		panelTop.add( pnlButton );

		/*
		 * RIGHT
		 */

		final JPanel panelBottom = new JPanel();
		splitPane.setRightComponent( panelBottom );
		panelBottom.setLayout( new BorderLayout() );
		panelBottom.add( new JLabel( "Statistics list in current configuration" ), BorderLayout.NORTH );
		final NearestObjectStatListPanel listPanel = new NearestObjectStatListPanel( model, units );
		panelBottom.add( listPanel, BorderLayout.CENTER );
	}

	public NearestObjectStatItem getCurrentItem()
	{
		return nearestObjectStatPanel.get();
	}

	public void setAvailableFeatureProjections( final AvailableFeatureProjections afp )
	{
		nearestObjectStatPanel.setAvailableFeatureProjections( afp );
	}

	public ProgressListener getProgressListener()
	{
		return new ProgressListener()
		{

			@Override
			public void showStatus( final String string )
			{
				pbar.setString( string );
			}

			@Override
			public void showProgress( final int current, final int total )
			{
				pbar.setMaximum( total );
				pbar.setValue( current );
			}

			@Override
			public void clearStatus()
			{
				pbar.setString( null );
			}
		};
	}
}
