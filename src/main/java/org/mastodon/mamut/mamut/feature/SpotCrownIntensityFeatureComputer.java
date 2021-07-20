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
package org.mastodon.mamut.mamut.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.feature.EllipsoidIterable;
import org.mastodon.mamut.feature.MamutFeatureComputer;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.DoubleArray;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.type.numeric.RealType;

@Plugin( type = MamutFeatureComputer.class )
public class SpotCrownIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
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
	private SpotCrownIntensityFeature output;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotCrownIntensityFeature.SPEC );
			if ( null != feature )
			{
				output = ( SpotCrownIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List< DoublePropertyMap< Spot > > crownmeans = new ArrayList<>( nSources );
			for ( int i = 0; i < nSources; i++ )
			{
				crownmeans.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );
			}
			output = new SpotCrownIntensityFeature( crownmeans );
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
			for ( final DoublePropertyMap< Spot > map : output.crownmeans )
				map.beforeClearPool();
		}

		// Dummy model to create and store scaled spots.
		final Model modelScaled = new Model( model.getSpaceUnits(), model.getTimeUnits() );

		final int numTimepoints = bdvData.getNumTimepoints();
		final int numDimensions = bdvData.is2D() ? 2 : 3;
		final int nSourcesToCompute = bdvData.getSources().size();
		final int todo = numTimepoints * nSourcesToCompute;

		final ArrayList< SourceAndConverter< ? > > sources = bdvData.getSources();
		final int nSources = sources.size();
		int done = 0;
		MAIN_LOOP: for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			@SuppressWarnings( "unchecked" )
			final Source< RealType< ? > > source = ( Source< RealType< ? > > ) sources.get( iSource ).getSpimSource();
			// Calculation are made on resolution level 0 by default.
			final EllipsoidIterable< RealType< ? > > ellipsoidIter = new EllipsoidIterable<>( source );
			final DoubleArray store = new DoubleArray();

			for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
			{

				status.notifyProgress( ( double ) done++ / todo );

				final SpatialIndex< Spot > toProcess = model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
				for ( final Spot spot : toProcess )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					/*
					 * Skip if we are not force to recompute all and if a value
					 * is already computed.
					 */
					if ( !recomputeAll && output.crownmeans.get( iSource ).isSet( spot ) )
						continue;

					// Get pixel intensities of spot
					ellipsoidIter.reset( spot );
					store.clear();
					ellipsoidIter.forEach( p -> store.addValue( p.getRealDouble() ) );

					final int size = store.size();
					if ( size < 1 )
						continue;

					// Compute sum
					final double[] array = store.getArray();
					double sum = 0.;
					for ( int i = 0; i < size; i++ )
						sum += array[ i ];

					// Spot position
					final double[] pos = new double[ numDimensions ];
					spot.delegate().localize( pos );

					// Covariance matrix
					final double[][] cov = new double[ numDimensions ][ numDimensions ];
					spot.getCovariance( cov );

					// Covariance matrix scaled by delta
					final double[][] covScaled = new double[ numDimensions ][ numDimensions ];
					final double delta = 1.5;
					for ( int i = 0; i < numDimensions; i++ )
						for ( int j = 0; j < numDimensions; j++ )
							covScaled[ i ][ j ] = cov[ i ][ j ] * delta * delta;

					// New scaled spot
					final Spot spotScaled = modelScaled.getGraph().addVertex().init( timepoint, pos, covScaled );

					// Get pixel intensities of scaled spot
					ellipsoidIter.reset( spotScaled );
					store.clear();
					ellipsoidIter.forEach( p -> store.addValue( p.getRealDouble() ) );

					final int sizeScaled = store.size();

					if ( sizeScaled < 1 )
						continue;

					// Compute sum
					final double[] arrayScaled = store.getArray();
					double sumScaled = 0.;
					for ( int i = 0; i < sizeScaled; i++ )
						sumScaled += arrayScaled[ i ];

					if ( ( sizeScaled - size ) == 0 )
						continue;

					// Compute crown mean intensities
					final double mean = Math.abs( ( sumScaled - sum ) / ( sizeScaled - size ) );

					output.crownmeans.get( iSource ).set( spot, mean );
				}
			}
		}
	}

	public static final long nSpots( final IntFunction< Iterable< Spot > > index, final int numTimepoints )
	{
		long nSpots = 0l;
		for ( int timepoint = 0; timepoint < numTimepoints; timepoint++ )
		{
			final Iterable< Spot > iterable = index.apply( timepoint );
			for ( @SuppressWarnings( "unused" )
			final Spot spot : iterable )
				nSpots++;
		}
		return nSpots;
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
