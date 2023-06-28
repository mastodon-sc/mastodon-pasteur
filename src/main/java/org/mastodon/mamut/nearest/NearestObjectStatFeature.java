package org.mastodon.mamut.nearest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.properties.DoublePropertyMap;

public class NearestObjectStatFeature implements Feature< Spot >
{


	public static final String KEY = "Stats on nearest neighbors";

	private static final String HELP_STRING =
			"Stores the values of the statistics on the N nearest neighbors plugin.";

	private final Map< FeatureProjectionKey, MyDoublePropertyProjection > featureProjections;

	private final Map< NearestObjectStatItem, FeatureProjectionKey > fSpecMap;

	private NearestObjectStatFeature(
			final Map< FeatureProjectionKey, MyDoublePropertyProjection > featureProjections,
			final Map< NearestObjectStatItem, FeatureProjectionKey > fSpecMap )
	{
		this.featureProjections = featureProjections;
		this.fSpecMap = fSpecMap;
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return featureProjections.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( featureProjections.values() );
	}

	@Override
	public FeatureSpec< NearestObjectStatFeature, Spot > getSpec()
	{
		final Set< FeatureProjectionKey > set = featureProjections.keySet();
		final FeatureProjectionSpec[] fps = new FeatureProjectionSpec[set.size()];
		final Iterator< FeatureProjectionKey > it = set.iterator();
		for ( int i = 0; i < set.size(); i++ )
			fps[ i ] = it.next().getSpec();
		return new MyFeatureSpec( fps  );
	}

	@Override
	public void invalidate( final Spot obj )
	{
		for ( final MyDoublePropertyProjection fp : featureProjections.values() )
			fp.invalidate( obj );
	}

	public void set( final Spot spot, final NearestObjectStatItem item, final double value )
	{
		final FeatureProjectionKey key = fSpecMap.get( item );
		if ( key == null )
			return;
		final MyDoublePropertyProjection fp = featureProjections.get( key );
		if ( fp == null )
			return;

		fp.set( spot, value );
	}

	public static NearestObjectStatFeature createFeature( final NearestObjectStatModel statModel, final Model model )
	{
		final Map< FeatureProjectionKey, MyDoublePropertyProjection > featureProjections = new LinkedHashMap<>( statModel.size() );
		final Map< NearestObjectStatItem, FeatureProjectionKey > fSpecMap = new HashMap<>();

		for ( final NearestObjectStatItem item : statModel )
		{
			// TODO other than distance.
			final FeatureProjectionSpec fpSpec = new FeatureProjectionSpec( item.toString(), Dimension.LENGTH );
			final FeatureProjectionKey key = FeatureProjectionKey.key( fpSpec );
			final DoublePropertyMap< Spot > map = new DoublePropertyMap<>( model.getGraph().vertices(), Double.NaN );
			final String units = Dimension.LENGTH.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
			final MyDoublePropertyProjection featureProjection = new MyDoublePropertyProjection( key, map, units );
			featureProjections.put( key, featureProjection );
			fSpecMap.put( item, key );
		}
		return new NearestObjectStatFeature( featureProjections, fSpecMap );
	}

	private static final class MyDoublePropertyProjection implements FeatureProjection< Spot >
	{

		private final FeatureProjectionKey key;

		private final DoublePropertyMap< Spot > map;

		private final String units;

		public MyDoublePropertyProjection( final FeatureProjectionKey key, final DoublePropertyMap< Spot > map,
				final String units )
		{
			this.key = key;
			this.map = map;
			this.units = units;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key;
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return map.isSet( obj );
		}

		@Override
		public double value( final Spot obj )
		{
			return map.getDouble( obj );
		}

		@Override
		public String units()
		{
			return units;
		}

		public void invalidate( final Spot spot )
		{
			map.remove( spot );
		}

		public void set( final Spot spot, final double value )
		{
			map.set( spot, value );
		}
	}

	private static class MyFeatureSpec extends FeatureSpec< NearestObjectStatFeature, Spot >
	{

		protected MyFeatureSpec( final FeatureProjectionSpec[] projectionSpecs )
		{
			super( KEY, HELP_STRING, NearestObjectStatFeature.class, Spot.class, Multiplicity.SINGLE, projectionSpecs );
		}
	}
}
