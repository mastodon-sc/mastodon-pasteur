package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotSumIntensityFeature implements Feature< Spot >
{

	private static final String KEY = "Spot sum intensity";

	private static final String HELP_STRING = "Computes the total intensity inside a spot, "
			+ "for the pixels inside the spot ellipsoid.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( "Median", Dimension.INTENSITY );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotSumIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotSumIntensityFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					PROJECTION_SPEC );
		}
	}

	final List< DoublePropertyMap< Spot > > sums;

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	SpotSumIntensityFeature( final List< DoublePropertyMap< Spot > > sums )
	{
		final int nSources = sums.size();
		this.sums = sums;
		this.projectionMap = new LinkedHashMap<>( nSources  );
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			final FeatureProjectionKey mkey = key( PROJECTION_SPEC, iSource );
			projectionMap.put( mkey, FeatureProjections.project( mkey, sums.get( iSource ), Dimension.COUNTS_UNITS ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}
}
