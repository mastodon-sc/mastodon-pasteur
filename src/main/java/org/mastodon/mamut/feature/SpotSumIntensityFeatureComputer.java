package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.feature.DefaultFeatureComputerService.FeatureComputationStatus;
import org.mastodon.feature.Feature;
import org.mastodon.feature.update.Update;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.util.SpotCursor;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

@Plugin( type = SpotSumIntensityFeatureComputer.class, name = "Spot sum intensity" )
public class SpotSumIntensityFeatureComputer implements MamutFeatureComputer, Cancelable
{

	@Parameter
	private SharedBigDataViewerData bdvData;

	@Parameter
	private Model model;

	@Parameter
	private SpotUpdateStack update;

	@Parameter
	private FeatureComputationStatus status;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotSumIntensityFeature output;

	private boolean[] processSource;

	private String cancelReason;

	@Override
	public void createOutput()
	{
		if ( null == output )
		{
			// Try to get it from the FeatureModel, if we deserialized a model.
			final Feature< ? > feature = model.getFeatureModel().getFeature( SpotSumIntensityFeature.SPEC );
			if ( null != feature )
			{
				output = ( SpotSumIntensityFeature ) feature;
				return;
			}

			// Create a new one.
			final int nSources = bdvData.getSources().size();
			final List<DoublePropertyMap< Spot >> sums = new ArrayList<>( nSources );
			for ( int i = 0; i < nSources; i++ )
				sums.add( new DoublePropertyMap<>( model.getGraph().vertices().getRefPool(), Double.NaN ) );

			output = new SpotSumIntensityFeature( sums );
		}
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
		final Update< Spot > changes = update.changesFor( SpotSumIntensityFeature.SPEC );

		if ( null == changes )
		{
			// Redo all.
			index = ( timepoint ) -> model.getSpatioTemporalIndex().getSpatialIndex( timepoint );
			// Clear all.
			for ( final DoublePropertyMap< Spot > map : output.sums )
				map.beforeClearPool();
		}
		else
		{
			// Only process modified spots.
			index = new MyIndex( changes, model.getGraph().vertices().getRefPool() );
		}

		// Calculation are made on resolution level 0.
		final int level = 0;
		// Affine transform holder.
		final AffineTransform3D transform = new AffineTransform3D();

		final int numTimepoints = bdvData.getNumTimepoints();
		int nSourcesToCompute = 0;
		for ( final boolean process : processSource )
			if ( process )
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

				@SuppressWarnings( "rawtypes" )
				final RandomAccessibleInterval rai =  source.getSource( timepoint, level );
				@SuppressWarnings( "unchecked" )
				final RandomAccessible< RealType< ? > > extended = Views.extendZero( rai );

				for ( final Spot spot : index.apply( timepoint ) )
				{
					if ( isCanceled() )
						break MAIN_LOOP;

					final SpotCursor< RealType< ? > > cursor = new SpotCursor<>( spot, transform, extended );
					double val = 0.;
					while ( cursor.hasNext() )
						val += cursor.next().getRealDouble();

					// Store sum.
					output.sums.get( iSource ).set( spot, val );
				}
			}
		}
	}

	private static final class MyIndex implements IntFunction< Iterable< Spot > >
	{

		private final Map< Integer, Collection< Spot > > index;

		public MyIndex( final Update< Spot > update, final RefPool< Spot > pool )
		{
			this.index = new HashMap<>();
			for ( final Spot spot : update.get() )
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
