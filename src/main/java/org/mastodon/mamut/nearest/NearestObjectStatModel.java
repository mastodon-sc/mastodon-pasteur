package org.mastodon.mamut.nearest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;

import org.jdom2.IllegalAddException;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.SpotIntensityFeature;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.TargetType;
import org.scijava.listeners.Listeners;

import bdv.ui.settings.style.Style;
import gnu.trove.list.array.TDoubleArrayList;
import net.imglib2.util.Util;

public class NearestObjectStatModel implements Iterable< NearestObjectStatItem >, Style< NearestObjectStatModel >
{

	public static final int MAX_N_ITEMS = 20;

	public static final Collection< NearestObjectStatModel > defaults;

	static
	{
		final NearestObjectStatModel d1 = new NearestObjectStatModel( "default" );
		d1.add( new NearestObjectStatItem() );
		final NearestObjectStatModel d2 = new NearestObjectStatModel( "Mean of neighbor intensities" );
		d2.add( new NearestObjectStatItem(
				CollectBy.SPECIFY_N,
				6,
				10.,
				Value.FEATURE,
				new FeatureProjectionId(
						SpotIntensityFeature.KEY,
						SpotIntensityFeature.MEAN_PROJECTION_SPEC.getKey(),
						TargetType.VERTEX,
						0 ),
				Stat.MEAN,
				false ) );
		defaults = new ArrayList<>( 2 );
		defaults.add( d1 );
		defaults.add( d2 );
	}

	private final List< NearestObjectStatItem > items = new ArrayList<>();

	private final Listeners.List< StatModelListener > updateListeners = new Listeners.List<>();

	private String name;

	public NearestObjectStatModel()
	{}

	public NearestObjectStatModel( final String name )
	{
		this.name = name;
	}

	@Override
	public NearestObjectStatModel copy( final String name )
	{
		final NearestObjectStatModel nosm = new NearestObjectStatModel( "copy" );
		nosm.set( this );
		if ( name != null )
			nosm.setName( name );
		return nosm;

	}

	public void set( final NearestObjectStatModel o )
	{
		name = o.name;
		items.clear();
		for ( final NearestObjectStatItem item : o.items )
			items.add( item );

		notifyListeners();
	}

	public synchronized void setItems( final List< NearestObjectStatItem > o )
	{
		if ( !this.items.equals( o ) )
		{
			this.items.clear();
			for ( final NearestObjectStatItem item : o )
				this.items.add( item );
			notifyListeners();
		}
	}

	public List< NearestObjectStatItem > getItems()
	{
		return Collections.unmodifiableList( items );
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public synchronized void setName( final String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
			notifyListeners();
		}
	}

	private void notifyListeners()
	{
		for ( final StatModelListener l : updateListeners.list )
			l.statModelChanged();
	}

	public Listeners.List< StatModelListener > statModelListeners()
	{
		return updateListeners;
	}

	public void add( final NearestObjectStatItem item )
	{
		if ( items.size() < MAX_N_ITEMS && !items.contains( item ) )
		{
			items.add( item );
			notifyListeners();
		}
	}

	public void remove( final NearestObjectStatItem item )
	{
		if ( items.remove( item ) )
			notifyListeners();
	}

	public int size()
	{
		return items.size();
	}

	public NearestObjectStatItem get( final int i )
	{
		return items.get( i );
	}

	public boolean contains( final NearestObjectStatItem item )
	{
		return items.contains( item );
	}

	@Override
	public Iterator< NearestObjectStatItem > iterator()
	{
		return items.iterator();
	}

	@Override
	public String toString()
	{
		return items.toString();
	}

	public static class NearestObjectStatItem
	{

		public final CollectBy collectBy;

		public final int n;

		public final double maxDistance;

		public final Value value;

		public final FeatureProjectionId featureID;

		public final Stat stat;

		public final boolean include;

		public NearestObjectStatItem()
		{
			this(
					CollectBy.SPECIFY_N,
					6,
					10.,
					Value.DISTANCE_OR_N,
					new FeatureProjectionId(
							SpotIntensityFeature.KEY,
							SpotIntensityFeature.MEAN_PROJECTION_SPEC.getKey(),
							TargetType.VERTEX,
							0 ),
					Stat.MEAN,
					false );
		}

		public NearestObjectStatItem( final CollectBy collectBy, final int n, final double maxDistance, final Value value, final FeatureProjectionId featureID, final Stat stat, final boolean include )
		{
			this.collectBy = collectBy;
			this.n = n;
			this.maxDistance = maxDistance;
			this.value = value;
			this.featureID = featureID;
			this.stat = stat;
			this.include = include;
		}

		public static NearestObjectStatItem defaultValue()
		{
			return new NearestObjectStatItem();
		}

		public String echo( final String units )
		{
			final String featureKey = featureID.getFeatureKey();
			final String projectionKey = featureID.getProjectionKey();
			final String featureStr = featureKey.equals( projectionKey )
					? featureKey.toLowerCase()
					: featureKey.toLowerCase() + " - " + projectionKey.toLowerCase();
			final String suffixStr;
			switch ( featureID.getMultiplicity() )
			{
			case SINGLE:
				suffixStr = "";
				break;
			case ON_SOURCES:
				suffixStr = " ch" + ( featureID.getI0() + 1 );
				break;
			case ON_SOURCE_PAIRS:
				suffixStr = " ch" + ( featureID.getI0() + 1 ) + "/ch" + ( featureID.getI1() + 1 );
				break;
			default:
				throw new IllegalAddException( "Unknown multiplicity: " + featureID.getMultiplicity() );

			}
			final String statStr = stat.toString();

			switch ( collectBy )
			{
			case MAX_DISTANCE:
			{
				if ( value == Value.FEATURE )
				{
					final String valueStr = String.format( " of " + featureStr + suffixStr + " over neighbors within %.2f %s", maxDistance, units );
					return statStr + valueStr + ( include ? " including center" : "" );
				}
				else
				{
					final String valueStr = String.format( "Number of neighbors within %.2f %s", maxDistance, units );
					return valueStr + ( include ? " including center" : "" );
				}
			}
			case SPECIFY_N:
			{
				final String valueStr = ( value == Value.DISTANCE_OR_N )
						? ( " distance to " ) : ( " of " + featureStr + suffixStr + " over " );
				return statStr + valueStr + n + " NN" +
						( include ? " including center" : "" );
			}
			default:
				throw new IllegalAddException( "Unknown collection method: " + collectBy );
			}
		}

		@Override
		public String toString()
		{
			return echo( "" );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if ( !NearestObjectStatItem.class.isInstance( obj ) )
				return false;

			final NearestObjectStatItem o = ( NearestObjectStatItem ) obj;
			if ( o.collectBy != collectBy )
				return false;

			switch ( collectBy )
			{
			case MAX_DISTANCE:
				if ( o.maxDistance != maxDistance )
					return false;
				break;
			case SPECIFY_N:
			{
				if ( o.n != n )
					return false;
				break;
			}
			default:
				throw new IllegalArgumentException( "Unknown collection method: " + collectBy );
			}
			if ( o.value != value )
				return false;

			if ( value == Value.FEATURE )
				if ( !o.featureID.equals( featureID ) )
					return false;

			// Test stat only if we are not collecting number of neighbors.
			if ( !( collectBy == CollectBy.MAX_DISTANCE && value == Value.DISTANCE_OR_N ) )
				if ( o.stat != stat )
					return false;

			if ( o.include != include )
				return false;

			return true;
		}

		/**
		 * Returns a function that map this stat specification to a double
		 * value. The function is specified as <code>f(neighbor, center)</code>
		 * where <code>neighbor</code> is one of the neighbors considered, and
		 * <code>center</code> the spot which neighborhood is being collected.
		 * 
		 * @param featureModel
		 *            a feature model from which to read feature values.
		 * @return a new {@link ToDoubleBiFunction}.
		 */
		public ToDoubleBiFunction< Spot, Spot > eval( final FeatureModel featureModel )
		{
			switch ( value )
			{
			case DISTANCE_OR_N:
				switch ( collectBy )
				{
				case MAX_DISTANCE:
					return null;
				case SPECIFY_N:
					return ( s, center ) -> Util.distance( s, center );
				default:
					throw new IllegalArgumentException( "Unknown collection method: " + collectBy );
				}
			case FEATURE:
			{
				final FeatureProjection< Spot > projection = NearestObjectStatFeature.getProjection( this.featureID, featureModel );
				return ( s, center ) -> projection.value( s );
			}
			default:
				throw new IllegalArgumentException( "Unknown value definition: " + value );
			}
		}

		public double summarize( final TDoubleArrayList arr )
		{
			if ( arr.isEmpty() )
				return Double.NaN;
			switch ( stat )
			{
			case MEAN:
				return arr.sum() / arr.size();
			case MEDIAN:
				arr.sort();
				return arr.getQuick( arr.size() / 2 );
			case STD:
				return Math.sqrt( variance( arr ) );
			case MIN:
				return arr.min();
			case MAX:
				return arr.max();
			case SUM:
				return arr.sum();
			default:
				throw new IllegalAddException( "This summarizing operation is unknown: " + stat );
			}
		}

		public static Builder create()
		{
			return new Builder();
		}
	}

	public enum Value
	{
		DISTANCE_OR_N( "Distance or N" ),
		FEATURE( "Feature" );

		private final String name;

		Value( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public enum Stat
	{

		MEAN( "Mean" ),
		STD( "Std" ),
		MIN( "Min" ),
		MAX( "Max" ),
		MEDIAN( "Median" ),
		SUM( "Sum" );

		private final String name;

		Stat( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	public enum CollectBy
	{

		SPECIFY_N( "Specifying N" ),
		MAX_DISTANCE( "Max distance" );

		private final String name;

		CollectBy( final String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static final double variance( final TDoubleArrayList arr )
	{
		final int size = arr.size();
		if ( size < 2 )
			return Double.NaN;

		final double mean = arr.sum() / size;
		double sum2 = 0.;
		for ( int i = 0; i < size; i++ )
		{
			final double dx = arr.getQuick( i ) - mean;
			sum2 += dx * dx;
		}
		final double variance = sum2 / ( size - 1 );
		return variance;
	}

	public interface StatModelListener
	{
		public void statModelChanged();
	}
}
