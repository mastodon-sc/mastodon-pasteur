/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import org.mastodon.kdtree.ClipConvexPolytope;
import org.mastodon.mamut.crown.CrownIntensityPlugin;
import org.mastodon.mamut.crown.ScreenScaledVertexMath;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.views.bdv.overlay.ScreenVertexMath.Ellipse;
import org.mastodon.views.bdv.overlay.Visibilities.Visibility;
import org.mastodon.views.bdv.overlay.Visibilities.VisibilityMode;

import net.imglib2.algorithm.kdtree.ConvexPolytope;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Renderer for a time-resliced graph overlay on a BDV, adding a rendering that
 * displays the crown which intensity we are measuring.
 * <p>
 * Copied from {@link OverlayGraphRenderer}.
 *
 * @param <V>
 *            the type of model vertex.
 * @param <E>
 *            the type of model edge.
 *
 * @author Tobias Pietzsch
 */
public class OverlayGraphCrownRenderer< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends OverlayGraphRenderer< V, E >
{

	public OverlayGraphCrownRenderer(
			final OverlayGraph< V, E > graph,
			final HighlightModel< V, E > highlight,
			final FocusModel< V, E > focus,
			final SelectionModel< V, E > selection,
			final GraphColorGenerator< V, E > coloring )
	{
		super( graph, highlight, focus, selection, coloring );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		if ( visibilities.getMode() == VisibilityMode.NONE )
			return;

		final Graphics2D graphics = ( Graphics2D ) g;
		final BasicStroke defaultVertexStroke = new BasicStroke();
		final BasicStroke crownVertexStroke = new BasicStroke( 1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 3f, 3f }, 0 );
		final BasicStroke highlightedVertexStroke = new BasicStroke( 4f );
		final BasicStroke focusedVertexStroke = new BasicStroke( 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 8f, 3f }, 0 );
		final BasicStroke defaultEdgeStroke = new BasicStroke();
		final BasicStroke highlightedEdgeStroke = new BasicStroke( 3f );

		final AffineTransform3D transform = getRenderTransformCopy();
		final int currentTimepoint = renderTimepoint;

		final Object antialiasing = settings.getUseAntialiasing()
				? RenderingHints.VALUE_ANTIALIAS_ON
				: RenderingHints.VALUE_ANTIALIAS_OFF;
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialiasing );

		final V ref1 = graph.vertexRef();
		final V ref2 = graph.vertexRef();
		final E ref3 = graph.edgeRef();
		final V source = graph.vertexRef();
		final V target = graph.vertexRef();

		final double sliceDistanceFade = settings.getEllipsoidFadeDepth();
		final double timepointDistanceFade = 0.5;

		final ScreenVertexMath screenVertexMath = new ScreenVertexMath();
		final double scale = 1. + CrownIntensityPlugin.getScale(); // Warning: static access.
		final ScreenScaledVertexMath screenCrownVertexMath = new ScreenScaledVertexMath( scale  );
		final boolean useGradient = settings.getUseGradient();
		final boolean drawArrowHeads = settings.getDrawArrowHeads();
		final int colorSpot = settings.getColorSpot();
		final int colorPast = settings.getColorPast();
		final int colorFuture = settings.getColorFuture();

		graph.getLock().readLock().lock();
		index.readLock().lock();
		try
		{
			if ( settings.getDrawLinks() )
			{
				final E highlighted = highlight.getHighlightedEdge( ref3 );
				graphics.setStroke( defaultEdgeStroke );
				forEachVisibleEdge( transform, currentTimepoint, ( edge, td0, td1, sd0, sd1, x0, y0, x1, y1 ) -> {
					final boolean isHighlighted = edge.equals( highlighted );

					edge.getSource( source );
					edge.getTarget( target );
					final int edgeColor = coloring.color( edge, source, target );
					final Color c1 = getColor(
							sd1,
							td1,
							sliceDistanceFade,
							timepointDistanceFade,
							selection.isSelected( edge ),
							isHighlighted,
							colorSpot,
							colorPast,
							colorFuture,
							edgeColor );
					if ( useGradient )
					{
						final Color c0 = getColor(
								sd0,
								td0,
								sliceDistanceFade,
								timepointDistanceFade,
								selection.isSelected( edge ),
								isHighlighted,
								colorSpot,
								colorPast,
								colorFuture,
								edgeColor );
						graphics.setPaint( new GradientPaint( x0, y0, c0, x1, y1, c1 ) );
					}
					else
					{
						graphics.setPaint( c1 );
					}
					if ( isHighlighted )
						graphics.setStroke( highlightedEdgeStroke );
					graphics.drawLine( x0, y0, x1, y1 );

					// Draw arrows for edge direction.
					if ( drawArrowHeads )
					{
						final double dx = x1 - x0;
						final double dy = y1 - y0;
						final double alpha = Math.atan2( dy, dx );
						final double l = 5;
						final double theta = Math.PI / 6.;
						final int x1a = ( int ) Math.round( x1 - l * Math.cos( alpha - theta ) );
						final int x1b = ( int ) Math.round( x1 - l * Math.cos( alpha + theta ) );
						final int y1a = ( int ) Math.round( y1 - l * Math.sin( alpha - theta ) );
						final int y1b = ( int ) Math.round( y1 - l * Math.sin( alpha + theta ) );
						graphics.drawLine( x1, y1, x1a, y1a );
						graphics.drawLine( x1, y1, x1b, y1b );
					}

					if ( isHighlighted )
						graphics.setStroke( defaultEdgeStroke );
				} );
			}

			if ( settings.getDrawSpots() )
			{
				final double ellipsoidFadeDepth = settings.getEllipsoidFadeDepth();
				final Visibility< V, E > visibility = visibilities.getVisibility();

				final V highlighted = highlight.getHighlightedVertex( ref1 );
				final V focused = focus.getFocusedVertex( ref2 );

				graphics.setStroke( defaultVertexStroke );
				final AffineTransform torig = graphics.getTransform();

				final ConvexPolytope cropPolytopeGlobal = getVisiblePolytopeGlobal( transform, currentTimepoint );
				final ClipConvexPolytope< V > ccp = index.getSpatialIndex( currentTimepoint ).getClipConvexPolytope();
				ccp.clip( cropPolytopeGlobal );
				for ( final V vertex : ccp.getInsideValues() )
				{
					if ( !visibility.isVisible( vertex ) )
						continue;

					final int color = coloring.color( vertex );
					final boolean isHighlighted = vertex.equals( highlighted );
					final boolean isFocused = vertex.equals( focused );

					screenVertexMath.init( vertex, transform );
					screenCrownVertexMath.init( vertex, transform );

					if ( screenVertexMath.intersectsViewPlane() )
					{
						final Ellipse ellipse = screenVertexMath.getIntersectEllipse();
						final Ellipse crown = screenCrownVertexMath.getIntersectEllipse();

						graphics.setColor( getColor(
								0,
								0,
								ellipsoidFadeDepth,
								timepointDistanceFade,
								selection.isSelected( vertex ),
								isHighlighted,
								colorSpot,
								colorPast,
								colorFuture,
								color ) );
						if ( isHighlighted )
							graphics.setStroke( highlightedVertexStroke );
						else if ( isFocused )
							graphics.setStroke( focusedVertexStroke );
						drawEllipse( graphics, ellipse, torig );
						graphics.setStroke( crownVertexStroke );
						drawEllipse( graphics, crown, torig );
						graphics.setStroke( defaultVertexStroke );
					}
				}
			}
		}
		finally
		{
			graph.getLock().readLock().unlock();
			index.readLock().unlock();
		}
		graph.releaseRef( ref1 );
		graph.releaseRef( ref2 );
		graph.releaseRef( ref3 );
		graph.releaseRef( source );
		graph.releaseRef( target );
	}
}
