/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.spottrackimage;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JLabel;

import org.mastodon.collection.RefList;
import org.mastodon.graph.algorithm.ShortestPath;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.SelectionModel;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.display.DisplayService;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Affine3DHelpers;
import bdv.viewer.ConverterSetups;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageDisplay;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ColorTable8;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class SpotTrackImageUIController
{

	private static final double EXTRA_BORDER = 0.5;

	private final SpotTrackImagePanel view;

	private final List< SourceAndConverter< ? > > sources;

	private final Model model;

	private final SelectionModel< Spot, Link > selectionModel;

	private final Context context;

	private final ConverterSetups converterSetups;

	public SpotTrackImageUIController( final SharedBigDataViewerData bdvData, final Model model, final SelectionModel< Spot, Link > selectionModel, final Context context )
	{
		this.sources = bdvData.getSources();
		this.model = model;
		this.selectionModel = selectionModel;
		this.converterSetups = bdvData.getConverterSetups();
		this.context = context;
		this.view = new SpotTrackImagePanel( sources );
		view.btnRun.addActionListener( ( e ) -> run(
				view.getSelectedSetupID(),
				view.getSizeFactor(),
				view.is3D(),
				view.getLogger() ) );
	}

	public SpotTrackImagePanel getView()
	{
		return view;
	}

	private void run( final int setupID, final double sizeFactor, final boolean is3D, final Consumer< String > logger )
	{
		final EverythingDisablerAndReenabler enabler = new EverythingDisablerAndReenabler( view,
				new Class[] { JLabel.class } );
		enabler.disable();

		new Thread( "Mastodon track image thread" )
		{
			@SuppressWarnings( "unchecked" )
			@Override
			public void run()
			{
				try
				{
					@SuppressWarnings( "rawtypes" )
					final ImgPlus output = extract( sources, model, selectionModel, setupID, sizeFactor, is3D, logger );
					if ( output != null )
					{
						final DefaultDataset dataset = new DefaultDataset( context, output );

						final DisplayService displayService = context.getService( DisplayService.class );
						final ImageDisplay display = ( ImageDisplay ) displayService.createDisplay( dataset );

						final DatasetView view = ( DatasetView ) display.get( 0 );
						for ( int c = 0; c < dataset.dimension( Axes.CHANNEL ); c++ )
						{
							final SourceAndConverter< ? > source = sources.get( c );
							final ConverterSetup setup = converterSetups.getConverterSetup( source );
							
							// Set min & max display range.
							final double min = setup.getDisplayRangeMin();
							final double max = setup.getDisplayRangeMax();
							output.setChannelMinimum( c, min );
							output.setChannelMaximum( c, max );
							view.setChannelRange( c, min, max );
							
							// Set LUT
							final ARGBType color = setup.getColor();
							final ColorTable8 colorTable = fromColor( new Color( color.get() ) );
							view.setColorTable( colorTable, c );
						}
						display.update();
					}
				}
				finally
				{
					enabler.reenable();
				}
			}
		}.start();
	}

	public static ImgPlus< ? > extract(
			final List< SourceAndConverter< ? > > sources,
			final Model model,
			final SelectionModel< Spot, Link > selectionModel,
			final int setupID,
			final double sizeFactor,
			final boolean is3d,
			final Consumer< String > logger )
	{
		logger.accept( "Capturing track image." );

		final Spot[] spots = getRootAndLeaf( selectionModel, model.getGraph() );
		if ( spots == null )
		{
			logger.accept( "<html>Expected 1 or 2 spots in the selection, got "
					+ selectionModel.getSelectedVertices().size() + ". Aborting.</html>" );
			return null;
		}

		final Spot root = spots[ 0 ];
		final Spot leaf = spots[ 1 ];

		// Build path.
		final RefList< Spot > path = getPath( root, leaf, model.getGraph() );
		if ( path == null )
		{
			logger.accept( "The two spots are not connected. Aborting." );
			return null;
		}

		return extract( sources, model, path, setupID, sizeFactor, is3d, logger );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public static ImgPlus< ? > extract(
			final List< SourceAndConverter< ? > > sources,
			final Model model,
			final RefList< Spot > path,
			final int setupID,
			final double sizeFactor,
			final boolean is3d,
			final Consumer< String > logger )
	{
		final boolean isMultiC = sources.size() > 1;

		final String rootLbl = path.get( 0 ).getLabel();
		final String leafLbl = path.get( path.size() - 1 ).getLabel();
		logger.accept( "<html>Extracting track image from spot " + rootLbl
				+ " to " + leafLbl + "</html>" );

		// Build spot list & Get largest diameter
		double maxRadiusSquared = Double.NEGATIVE_INFINITY;
		int frameLargest = -1;
		for ( final Spot spot : path )
		{
			final double radiusSquared = spot.getBoundingSphereRadiusSquared();
			if ( radiusSquared > maxRadiusSquared )
			{
				maxRadiusSquared = radiusSquared;
				frameLargest = spot.getTimepoint();
			}
		}
		final double radius = Math.sqrt( maxRadiusSquared );

		// Main source. Patch axes will correspond to main source axes.
		final Source< ? > mainSource = sources.get( setupID ).getSpimSource();

		// Get scale
		final AffineTransform3D sourceToGlobal = new AffineTransform3D();
		mainSource.getSourceTransform( frameLargest, 0, sourceToGlobal );
		final double dx = Affine3DHelpers.extractScale( sourceToGlobal, 0 );
		final double dy = Affine3DHelpers.extractScale( sourceToGlobal, 1 );
		final double dz = Affine3DHelpers.extractScale( sourceToGlobal, 2 );

		final int width = ( int ) Math.ceil( 2. * radius * ( sizeFactor + EXTRA_BORDER ) / dx );
		final int height = ( int ) Math.ceil( 2 * radius * ( sizeFactor + EXTRA_BORDER ) / dy );
		final int depth = is3d
				? ( int ) Math.ceil( 2 * radius * ( sizeFactor + EXTRA_BORDER ) / dz )
				: 1;

		// Iterate over set to grab imglib image
		final List< RandomAccessibleInterval< RealType > > timepoints = new ArrayList<>( path.size() );
		for ( final Spot spot : path )
		{
			final List< Img< RealType > > channels = new ArrayList<>( sources.size() );

			// Main transform and source.
			final int frame = spot.getTimepoint();
			final AffineTransform3D mainTransform = new AffineTransform3D();
			mainSource.getSourceTransform( frame, 0, mainTransform );

			// Collect patch for each source.
			for ( final SourceAndConverter< ? > sac : sources )
			{
				final Source< ? > source = sac.getSpimSource();

				if ( is3d )
				{
					final Interval patch = SpotTrackImageUtils.getIntervalAround( spot, width, height, depth, mainSource );
					final Img< ? > collect = SpotTrackImageUtils.collectInterval(
							patch,
							mainTransform,
							source,
							frame );
					channels.add( ( Img< RealType > ) collect );
				}
				else
				{
					final Interval patch = SpotTrackImageUtils.getIntervalAround( spot, width, height, 0, mainSource );
					final Img< ? > collect = SpotTrackImageUtils.collectInterval(
							patch,
							mainTransform,
							source,
							frame );
					channels.add( ( Img< RealType > ) collect );
				}
			}

			if ( isMultiC )
			{
				final RandomAccessibleInterval< RealType > multiChannel = Views.stack( channels );
				timepoints.add( multiChannel );
			}
			else
			{
				timepoints.add( channels.get( 0 ) );
			}
		}
		final RandomAccessibleInterval< RealType > stack = Views.stack( timepoints );

		// Add calibration and dimensionality.
		int nDims = 3;
		if ( isMultiC )
			nDims++;
		if ( is3d )
			nDims++;

		final AxisType[] axesType = new AxisType[ nDims ];
		int id = 0;
		axesType[ id++ ] = Axes.X;
		axesType[ id++ ] = Axes.Y;
		if ( is3d )
			axesType[ id++ ] = Axes.Z;
		if ( isMultiC )
			axesType[ id++ ] = Axes.CHANNEL;
		axesType[ id++ ] = Axes.TIME;

		final double[] cal = new double[ nDims ];
		id = 0;
		cal[ id++ ] = dx;
		cal[ id++ ] = dy;
		if ( is3d )
			cal[ id++ ] = dz;
		if ( isMultiC )
			cal[ id++ ] = 1.;
		cal[ id++ ] = 1.;

		final String name = rootLbl + "→" + leafLbl;
		final String spaceUnits = model.getSpaceUnits();
		final String timeUnits = "link"; // not a true time.
		final String[] units = new String[ nDims ];
		id = 0;
		units[ id++ ] = spaceUnits;
		units[ id++ ] = spaceUnits;
		if ( is3d )
			units[ id++ ] = spaceUnits;
		if ( isMultiC )
			units[ id++ ] = "source";
		units[ id++ ] = timeUnits;

		final ImgPlus< ? > imgplus = new ImgPlus( ImgView.wrap( stack ), name, axesType, cal, units );
		return imgplus;
	}

	public static final Spot[] getRootAndLeaf( final SelectionModel< Spot, Link > selectionModel, final ModelGraph graph )
	{

		final Set< Spot > selection = selectionModel.getSelectedVertices();
		final int nspots = selection.size();
		if ( nspots < 1 || nspots > 2 )
			return null;

		final Spot root;
		final Spot leaf;
		if ( nspots == 1 )
		{

			final Spot spot = selection.iterator().next();
			root = getRootOf( spot, graph );
			leaf = getLeafOf( spot, graph );
		}
		else
		{
			final Spot spot1 = graph.vertexRef();
			final Spot spot2 = graph.vertexRef();
			final Iterator< Spot > it = selection.iterator();
			spot1.refTo( it.next() );
			spot2.refTo( it.next() );
			if ( spot1.getTimepoint() < spot2.getTimepoint() )
			{
				root = spot1;
				leaf = spot2;
			}
			else
			{
				root = spot2;
				leaf = spot1;
			}
		}
		return new Spot[] { root, leaf };
	}

	public static final RefList< Spot > getPath( final Spot root, final Spot leaf, final ModelGraph graph )
	{
		// Build path.
		final ShortestPath< Spot, Link > pathFinder = new ShortestPath<>( graph, SearchDirection.DIRECTED );
		final RefList< Spot > path = pathFinder.findPath( root, leaf );
		if ( path == null )
			return null;

		path.sort( Comparator.comparing( s -> s.getTimepoint() ) );
		return path;
	}

	private static Spot getRootOf( final Spot spot, final ModelGraph graph )
	{
		final Predicate< Spot > test = s -> s.incomingEdges().isEmpty();
		return getOf( spot, graph, SearchDirection.REVERSED, test );
	}

	private static Spot getLeafOf( final Spot spot, final ModelGraph graph )
	{
		final Predicate< Spot > test = s -> s.outgoingEdges().isEmpty();
		return getOf( spot, graph, SearchDirection.DIRECTED, test );
	}

	private static Spot getOf( final Spot spot, final ModelGraph graph, final SearchDirection direction, final Predicate< Spot > test )
	{
		final DepthFirstSearch< Spot, Link > search = new DepthFirstSearch<>( graph, direction );
		final Spot target = graph.vertexRef();
		search.setTraversalListener( new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
		{

			@Override
			public void processVertexLate( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
			{}

			@Override
			public void processVertexEarly( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
			{
				if ( test.test( vertex ) )
				{
					target.refTo( vertex );
					search.abort();
				}
			}

			@Override
			public void processEdge( final Link edge, final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
			{}

			@Override
			public void crossComponent( final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
			{}
		} );
		search.start( spot );
		return target;
	}

	private static ColorTable8 fromColor( final Color color )
	{
		final byte[][] lut = new byte[ 3 ][ 256 ];
		for ( int i = 0; i < 256; i++ )
		{
			lut[ 0 ][ i ] = ( byte ) ( ( double ) i * color.getRed() / 256. );
			lut[ 1 ][ i ] = ( byte ) ( ( double ) i * color.getGreen() / 256. );
			lut[ 2 ][ i ] = ( byte ) ( ( double ) i * color.getBlue() / 256. );
		}
		return new ColorTable8( lut );
	}
}
