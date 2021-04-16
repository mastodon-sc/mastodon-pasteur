package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.DoubleArray;

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
	private AtomicBoolean forceComputeAll;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotMedianIntensityFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotMedianIntensityFeature.SPEC );
			if ( null != feature )
			{
				output = ( SpotMedianIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List< DoublePropertyMap< Spot > > medians = new ArrayList<>( nSources );
			for ( int i = 0; i < nSources; i++ )
				medians.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );

			output = new SpotMedianIntensityFeature( medians );
		}
	}

	@Override
	public void run()
	{
		cancelReason = null;

		final boolean recomputeAll = forceComputeAll.get();

		if ( recomputeAll )
		{
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.medians )
				map.beforeClearPool();
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
		final long[] roiMin = new long[ 3 ];
		final long[] roiMax = new long[ 3 ];
		// Storage for intensity values.
		final DoubleArray array = new DoubleArray();

		final int numTimepoints = bdvData.getNumTimepoints();
		final int nSourcesToCompute = bdvData.getSources().size();
		final int todo = numTimepoints * nSourcesToCompute;

		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();
		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{

			final Source< ? > source = sources.get( iSource ).getSpimSource();
			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{

				status.notifyProgress( ( double ) done++ / todo );

				source.getSourceTransform( timepoint, level, transform );
				for ( int d = 0; d < calibration.length; d++ )
					calibration[ d ] = Affine3DHelpers.extractScale( transform, d );

				@SuppressWarnings( "unchecked" )
				final RandomAccessibleInterval< RealType< ? > > rai = ( RandomAccessibleInterval< RealType< ? > > ) source.getSource( timepoint, level );

				final SpatialIndex< Spot > toProcess = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
				for ( final Spot spot : toProcess )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					/*
					 * Skip if we are not force to recompute all and if a value
					 * is already computed.
					 */
					if ( !recomputeAll && output.medians.get( iSource ).isSet( spot ) )
						continue;

					// Spot location in pixel units.
					transform.applyInverse( center, spot );
					for ( int d = 0; d < pos.length; d++ )
						p[ d ] = Math.round( pos[ d ] );

					// Compute ROI.
					final double minRadius = minRadius( spot, cov );
					for ( int d = 0; d < 3; d++ )
					{
						roiMin[ d ] = ( long ) Math.max( rai.min( d ), p[ d ] - minRadius / calibration[ d ] - 0.5 );
						roiMax[ d ] = ( long ) Math.min( rai.max( d ), p[ d ] + minRadius / calibration[ d ] + 0.5 );
					}

					// Iterate over pixels.
					array.clear();
					for ( final RealType< ? > pixel : Views.interval( rai, roiMin, roiMax ) )
						array.addValue( pixel.getRealDouble() );

					// Store median.
					if ( !array.isEmpty() )
					{
						final double[] arr = array.getArray();
						Arrays.sort( arr, 0, array.size() );
						final double median = arr[ array.size() / 2 ];
						output.medians.get( iSource ).set( spot, median );
					}
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
