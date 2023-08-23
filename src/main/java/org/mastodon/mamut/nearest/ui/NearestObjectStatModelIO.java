package org.mastodon.mamut.nearest.ui;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.FLOW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.mamut.nearest.NearestObjectStatModel.CollectBy;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Stat;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Value;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.TargetType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class NearestObjectStatModelIO
{


	/**
	 * Returns a YAML instance that can dump / load a collection of
	 * {@link NearestObjectStatModel} to / from a .yaml file.
	 *
	 * @return a new YAML instance.
	 */
	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new NearestObjectStatModelRepresenter();
		final Constructor constructor = new NearestObjectStatModelConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	private static final Tag FEATURE_PROJECTION_ID_TAG = new Tag( "!featureProjectionID" );

	private static final Tag NEAREST_OBJECT_STAT_MODEL_TAG = new Tag( "!nearestobjectstatmodel" );

	public static final Tag NEAREST_OBJECT_STAT_ITEM_TAG = new Tag( "!nearestobjectstatitem" );;

	private static class RepresentVertexFeatureProjectionId extends WorkaroundRepresent
	{

		public RepresentVertexFeatureProjectionId( final WorkaroundRepresenter r )
		{
			super( r, FEATURE_PROJECTION_ID_TAG, FeatureProjectionId.class );
		}

		@Override
		public Node representData( final Object obj )
		{
			final FeatureProjectionId fpi = ( FeatureProjectionId ) obj;
			final Map< String, Object > mapping = new LinkedHashMap<>();
			mapping.put( "featureKey", fpi.getFeatureKey() );
			mapping.put( "projectionKey", fpi.getProjectionKey() );
			mapping.put( "i0", fpi.getI0() );
			mapping.put( "i1", fpi.getI1() );
			final Node node = representMapping( getTag(), mapping, FLOW );
			return node;
		}
	}

	private static class ConstructVertexFeatureProjectionId extends AbstractWorkaroundConstruct
	{
		public ConstructVertexFeatureProjectionId( final WorkaroundConstructor c )
		{
			super( c, FEATURE_PROJECTION_ID_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String featureKey = getString( mapping, "featureKey" );
				final String projectionKey = getString( mapping, "projectionKey" );
				final int i0 = getInt( mapping, "i0" );
				final int i1 = getInt( mapping, "i1" );
				return new FeatureProjectionId( featureKey, projectionKey, TargetType.VERTEX, i0, i1 );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static class NearestObjectStatModelRepresenter extends WorkaroundRepresenter
	{

		public NearestObjectStatModelRepresenter()
		{
			putRepresent( new RepresentNearestObjectStatModel( this ) );
			putRepresent( new RepresentNearestObjectStatItem( this ) );
			putRepresent( new RepresentVertexFeatureProjectionId( this ) );
		}
	}

	private static class NearestObjectStatModelConstructor extends WorkaroundConstructor
	{

		public NearestObjectStatModelConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructVertexFeatureProjectionId( this ) );
			putConstruct( new ConstructNearestObjectStatItem( this ) );
			putConstruct( new ConstructNearestObjectStatModel( this ) );
		}
	}

	private static class ConstructNearestObjectStatModel extends AbstractWorkaroundConstruct
	{

		public ConstructNearestObjectStatModel( final WorkaroundConstructor c )
		{
			super( c, NEAREST_OBJECT_STAT_MODEL_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final String name = ( String ) mapping.get( "name" );
				@SuppressWarnings( "unchecked" )
				final List< NearestObjectStatItem > items = ( List< NearestObjectStatItem > ) mapping.get( "items" );
				final NearestObjectStatModel m = new NearestObjectStatModel( name );
				m.setItems( items );
				return m;
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	private static class RepresentNearestObjectStatModel extends WorkaroundRepresent
	{

		public RepresentNearestObjectStatModel( final NearestObjectStatModelRepresenter r )
		{
			super( r, NEAREST_OBJECT_STAT_MODEL_TAG, NearestObjectStatModel.class );
		}

		@Override
		public Node representData( final Object obj )
		{
			final NearestObjectStatModel s = ( NearestObjectStatModel ) obj;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			mapping.put( "name", s.getName() );
			mapping.put( "items", s.getItems() );
			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructNearestObjectStatItem extends AbstractWorkaroundConstruct
	{

		public ConstructNearestObjectStatItem( final WorkaroundConstructor c )
		{
			super( c, NEAREST_OBJECT_STAT_ITEM_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode ) node );
				final CollectBy collectBy = CollectBy.valueOf( getString( mapping, "collectBy" ) );
				final int n = getInt( mapping, "n" );
				final double maxDistance = getDouble( mapping, "maxDistance" );
				final Value value = Value.valueOf( getString( mapping, "value" ) );
				final FeatureProjectionId featureID = ( FeatureProjectionId ) mapping.get( "featureId" );
				final Stat stat = Stat.valueOf( getString( mapping, "stat" ) );
				final boolean include = getBoolean( mapping, "include" );
				return new NearestObjectStatItem( collectBy, n, maxDistance, value, featureID, stat, include );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}

	}

	private static class RepresentNearestObjectStatItem extends WorkaroundRepresent
	{

		public RepresentNearestObjectStatItem( final WorkaroundRepresenter r )
		{
			super( r, NEAREST_OBJECT_STAT_ITEM_TAG, NearestObjectStatItem.class );
		}

		@Override
		public Node representData( final Object obj )
		{
			final NearestObjectStatItem s = ( NearestObjectStatItem ) obj;
			final Map< String, Object > mapping = new LinkedHashMap<>();

			mapping.put( "collectBy", s.collectBy.name() );
			mapping.put( "n", s.n );
			mapping.put( "maxDistance", s.maxDistance );
			mapping.put( "value", s.value.name() );
			mapping.put( "featureId", s.featureID );
			mapping.put( "stat", s.stat.name() );
			mapping.put( "include", s.include );
			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}
}
