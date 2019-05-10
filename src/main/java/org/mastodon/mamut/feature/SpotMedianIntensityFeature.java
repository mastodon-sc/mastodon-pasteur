package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.RefPool;
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

public class SpotMedianIntensityFeature implements Feature< Spot >
{

	private static final String KEY = "Spot median intensity";

	private static final String HELP_STRING = "Computes the median intensity inside a spot, "
			+ "for the pixels inside the largest box that fits into the spot ellipsoid.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( "Median", Dimension.INTENSITY );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotMedianIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotMedianIntensityFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					PROJECTION_SPEC );
		}
	}

	final List< DoublePropertyMap< Spot > > medians;

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	SpotMedianIntensityFeature( final int nSources, final RefPool< Spot > pool )
	{
		this.medians = new ArrayList<>( nSources );
		this.projectionMap = new LinkedHashMap<>( nSources );
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			final DoublePropertyMap< Spot > m = new DoublePropertyMap<>( pool, Double.NaN );
			medians.add( m );
			final FeatureProjectionKey mkey = key( PROJECTION_SPEC, iSource );
			projectionMap.put( mkey, FeatureProjections.project( mkey, m, Dimension.COUNTS_UNITS ) );
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
