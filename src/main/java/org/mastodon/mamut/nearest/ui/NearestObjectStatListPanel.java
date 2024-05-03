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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;

import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.ui.util.WrapLayout;

public class NearestObjectStatListPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JPanel mainPanel;

	private final String units;

	public NearestObjectStatListPanel( final NearestObjectStatModel model, final String units )
	{
		super( new BorderLayout() );
		this.units = units;
		model.statModelListeners().add( () -> refresh( model ) );
		this.mainPanel = new JPanel();
		mainPanel.setLayout( new WrapLayout( FlowLayout.LEFT, 10, 10 ) );

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder( new LineBorder( getBackground().darker() ) );
		scrollPane.setOpaque( false );
		scrollPane.getViewport().setOpaque( false );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setViewportView( mainPanel );

		add( scrollPane, BorderLayout.CENTER );
		refresh( model );
	}

	private void refresh( final NearestObjectStatModel model )
	{
		final Color bg = getBackground();
		final double FACTOR = 0.9;
		final Color bg2 = new Color( Math.max( ( int ) ( bg.getRed() * FACTOR ), 0 ),
				Math.max( ( int ) ( bg.getGreen() * FACTOR ), 0 ),
				Math.max( ( int ) ( bg.getBlue() * FACTOR ), 0 ),
				bg.getAlpha() );

		mainPanel.removeAll();
		for ( final NearestObjectStatItem item : model )
		{
			final JPanel panel = new JPanel();
			panel.setLayout( new BoxLayout( panel, BoxLayout.LINE_AXIS ) );
			panel.add( new JLabel( item.echo( units ) ) );
			panel.add( Box.createHorizontalStrut( 5 ) );
			final JButton btn = new JButton( MastodonIcons.REMOVE_ICON );
			btn.setOpaque( false );
			btn.addActionListener( e -> model.remove( item ) );
			panel.add( btn );

			panel.setBorder( new RoundedBorder( Color.BLACK.brighter(), bg2, 30 ) );
			mainPanel.add( panel );
		}
		mainPanel.revalidate();
		repaint();
	}

	private static class RoundedBorder extends AbstractBorder
	{

		private static final long serialVersionUID = 1L;

		private final Color color;

		private final Color colorBg;

		private final int gap;

		public RoundedBorder( final Color c, final Color bg, final int g )
		{
			color = c;
			colorBg = bg;
			gap = g;
		}

		@Override
		public void paintBorder( final Component c, final Graphics g, final int x, final int y, final int width, final int height )
		{
			final Graphics2D g2d = ( Graphics2D ) g.create();
			g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
			g2d.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
			g2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
			g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
			g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
			g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
			final Double border = new RoundRectangle2D.Double( x + 1, y + 1, width - 2, height - 2, gap, gap );
			g2d.setColor( colorBg );
			g2d.fill( border );
			g2d.setColor( color );
			g2d.draw( border );
			g2d.dispose();
		}

		@Override
		public Insets getBorderInsets( final Component c )
		{
			return ( getBorderInsets( c, new Insets( gap / 2, gap / 2, gap / 2, gap / 2 ) ) );
		}

		@Override
		public Insets getBorderInsets( final Component c, final Insets insets )
		{
			insets.left = insets.top = insets.right = insets.bottom = gap / 4;
			return insets;
		}

		@Override
		public boolean isBorderOpaque()
		{
			return false;
		}
	}
}
