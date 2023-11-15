package org.mastodon.mamut.nearest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.CollectBy;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;

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

	/**
	 * Returns the projection corresponding to the specified stat item, or
	 * <code>null</code> if the results for the stat item are not stored in this
	 * feature.
	 * 
	 * @param item
	 *            the stat item.
	 * @return the projection, or <code>null</code>.
	 */
	public FeatureProjection< Spot > get( final NearestObjectStatItem item )
	{
		final FeatureProjectionKey key = fSpecMap.get( item );
		if ( key == null )
			return null;
		return featureProjections.get( key );
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
			Dimension projectionDimension;
			switch ( item.value )
			{
			case DISTANCE_OR_N:
			{
				projectionDimension = ( item.collectBy == CollectBy.SPECIFY_N ) ? Dimension.LENGTH : Dimension.NONE;
				break;
			}
			case FEATURE:
			{
				final FeatureProjectionSpec projection = getProjectionSpec( item.featureID, model.getFeatureModel() );
				// Deal with the feature not being present in the model.
				if ( projection == null )
					continue;

				projectionDimension = projection.projectionDimension;
				break;
			}
			default:
				throw new IllegalArgumentException( "Unknown value definition: " + item.value );
			}
			final FeatureProjectionSpec fpSpec = new FeatureProjectionSpec( item.echo( model.getSpaceUnits() ), projectionDimension );
			final FeatureProjectionKey key = FeatureProjectionKey.key( fpSpec );
			final DoublePropertyMap< Spot > map = new DoublePropertyMap<>( model.getGraph().vertices(), Double.NaN );
			final String units = projectionDimension.getUnits( model.getSpaceUnits(), model.getTimeUnits() );
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

	public static FeatureProjectionSpec getProjectionSpec( final FeatureProjectionId featureID, final FeatureModel featureModel )
	{
		FeatureSpec< ?, ? > spec = null;
		for ( final FeatureSpec< ?, ? > fs : featureModel.getFeatureSpecs() )
		{
			if ( fs.getKey().equals( featureID.getFeatureKey() ) )
			{
				spec = fs;
				break;
			}
		}
		if ( spec == null )
			return null;

		for ( final FeatureProjectionSpec fps : spec.getProjectionSpecs() )
		{
			if ( fps.getKey().equals( featureID.getProjectionKey() ) )
				return fps;
		}
		return null;
	}

	public static final FeatureProjection< Spot > getProjection( final FeatureProjectionId featureID, final FeatureModel featureModel )
	{
		FeatureSpec< ?, ? > spec = null;
		for ( final FeatureSpec< ?, ? > fs : featureModel.getFeatureSpecs() )
		{
			if ( fs.getKey().equals( featureID.getFeatureKey() ) )
			{
				spec = fs;
				break;
			}
		}
		if ( spec == null )
			return DUMMY_PROJECTION;

		@SuppressWarnings( "unchecked" )
		final Feature< Spot > feature = ( Feature< Spot > ) featureModel.getFeature( spec );
		FeatureProjectionSpec fpSpec = null;
		for ( final FeatureProjectionSpec fps : feature.getSpec().getProjectionSpecs() )
		{
			if ( fps.getKey().equals( featureID.getProjectionKey() ) )
				fpSpec = fps;
		}
		if ( fpSpec == null )
			return DUMMY_PROJECTION;

		final int i0 = featureID.getI0();
		final int i1 = featureID.getI1();
		final FeatureProjectionKey key;
		if ( i0 < 0 )
			key = FeatureProjectionKey.key( fpSpec );
		else if ( i1 < 0 )
			key = FeatureProjectionKey.key( fpSpec, i0 );
		else
			key = FeatureProjectionKey.key( fpSpec, i0, i1 );

		final FeatureProjection< Spot > projection = feature.project( key );
		return projection;
	}

	/**
	 * A feature projection which always return NaN.
	 */
	private static final FeatureProjection< Spot > DUMMY_PROJECTION = new FeatureProjection< Spot >()
	{

		@Override
		public double value( final Spot obj )
		{
			return Double.NaN;
		}

		@Override
		public String units()
		{
			return "";
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return false;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return null;
		}
	};
}
