package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.update.GraphUpdate;
import org.mastodon.feature.update.GraphUpdate.UpdateLocality;
import org.mastodon.feature.update.GraphUpdateStack;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import bdv.util.Affine3DHelpers;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin( type = SpotMedianIntensityFeatureComputer.class, name = "Spot median intensity" )
public class SpotMedianIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
{

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private GraphUpdateStack< Spot, Link > update;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotMedianIntensityFeature output;

	private boolean[] processSource;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotMedianIntensityFeature(
					bdvData.getSources().size(),
					model.getGraph().vertices().getRefPool() );
	}

	@Override
	public void run()
	{
		cancelReason = null;

		// TODO Take into account that some sources might not be computed.
		this.processSource = new boolean[ bdvData.getSources().size() ];
		Arrays.fill( processSource, true );

		// Spots to process, per time-point.
		final IntFunction< Iterable< Spot > > index;
		final GraphUpdate< Spot, Link > changes = update.changesFor( SpotMedianIntensityFeature.SPEC );

		if (null == changes)
		{
			// Redo all.
			index = ( timepoint ) -> model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.medians )
				map.beforeClearPool();
		}
		else
		{
			// Only process modified spots.
			index = new MyIndex( changes, model.getGraph().vertices().getRefPool() );
		}

		// Calculation are made on resolution level 0.
		final int level = 0;
		// Covariance holder.
		final double[][] cov = new double[ 3 ][ 3 ];
		// Affine transform holder.
		final AffineTransform3D transform = new AffineTransform3D();
		// Physical calibration holder.
		final double[] calibration = new double[ 3 ];
		// Spot center position holder in image coords.
		final double[] pos = new double[ 3 ];
		// Spot center holder in image coords.
		final RealPoint center = RealPoint.wrap( pos );
		// Spot center position holder in integer image coords.
		final long[] p = new long[ 3 ];
		// ROI min & max holders;
		final long[] roiMin = new long[3];
		final long[] roiMax = new long[3];
		// Storage for intensity values.
		final ResizableDoubleArray array = new ResizableDoubleArray();
		// Median computer.
		final Median median = new Median();

		final int numTimepoints = bdvData.getNumTimepoints();
		int nSourcesToCompute = 0;
		for ( final boolean process : processSource )
			if (process)
				nSourcesToCompute++;
		final int todo = numTimepoints * nSourcesToCompute;


		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();
		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			if ( !processSource[ iSource ] )
				continue;

			final Source< ? > source = sources.get( iSource ).getSpimSource();
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{

				status.notifyProgress( ( double ) done++ / todo );

				source.getSourceTransform( timepoint, level, transform );
				for ( int d = 0; d < calibration.length; d++ )
					calibration[ d ] = Affine3DHelpers.extractScale( transform, d );

				@SuppressWarnings( "unchecked" )
				final RandomAccessibleInterval< RealType< ? > > rai = ( RandomAccessibleInterval< RealType< ? > > ) source.getSource( timepoint, level );

				for ( final Spot spot : index.apply( timepoint ) )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					// Spot location in pixel units.
					transform.applyInverse( center, spot );
					for ( int d = 0; d < pos.length; d++ )
						p[ d ] = Math.round( pos[ d ] );

					// Compute ROI.
					final double minRadius = minRadius( spot, cov );
					for ( int d = 0; d < 3; d++ )
					{
						roiMin[d] = ( long ) Math.max( rai.min( d ), p[ d ] - minRadius / calibration[ d ] + 1 );
						roiMax[d] = ( long ) Math.min( rai.max( d ), p[ d ] + minRadius / calibration[ d ] - 1 );
					}

					// Iterate over pixels.
					array.clear();
					for ( final RealType< ? > pixel : Views.interval( rai, roiMin, roiMax ) )
						array.addElement( pixel.getRealDouble() );

					// Store median.
					output.medians.get( iSource ).set( spot, median.evaluate( array.getElements() ) );
				}
			}
		}
	}

	private static final JamaEigenvalueDecomposition eig = new JamaEigenvalueDecomposition( 3 );

	private static final double minRadius( final Spot spot, final double[][] cov )
	{
		// Best radius is smallest radius of ellipse.
		spot.getCovariance( cov );
		eig.decomposeSymmetric( cov );
		final double[] eigVals = eig.getRealEigenvalues();
		double minEig = Double.POSITIVE_INFINITY;
		for ( int k = 0; k < eigVals.length; k++ )
			minEig = Math.min( minEig, eigVals[ k ] );
		final double radius = Math.sqrt( minEig );
		return radius;
	}


	private static final class MyIndex implements IntFunction< Iterable< Spot > >
	{

		private final Map< Integer, Collection< Spot > > index;

		public MyIndex( final GraphUpdate< Spot, Link > update, final RefPool< Spot > pool )
		{
			this.index = new HashMap<>();
			for ( final Spot spot : update.vertices( UpdateLocality.SELF ) )
			{
				final int timepoint = spot.getTimepoint();
				index
						.computeIfAbsent( Integer.valueOf( timepoint ), t -> new RefArrayList<>( pool ) )
						.add( spot );
			}
		}

		@Override
		public Iterable< Spot > apply( final int timepoint )
		{
			final Collection< Spot > collection = index.get( Integer.valueOf( timepoint ) );
			if ( null == collection )
				return Collections.emptyList();
			return collection;
		}
	}

	@Override
	public boolean isCanceled()
	{
		return null != cancelReason;
	}

	@Override
	public void cancel( final String reason )
	{
		cancelReason = reason;
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}
}
