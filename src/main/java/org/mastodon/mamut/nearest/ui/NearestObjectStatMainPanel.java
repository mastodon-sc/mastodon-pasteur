/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
				pbar.setValue( 0 );
				pbar.setString( null );
			}
		};
	}
}
