package org.mastodon.mamut.nearest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.jdom2.IllegalAddException;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.scijava.listeners.Listeners;

import bdv.ui.settings.style.Style;
import gnu.trove.list.array.TDoubleArrayList;
import net.imglib2.util.Util;

public class NearestObjectStatModel implements Iterable< NearestObjectStatItem >, Style< NearestObjectStatModel >
{

	private static final int MAX_N_ITEMS = 20;

	public static final Collection< NearestObjectStatModel > defaults;
	static
	{
		final NearestObjectStatModel d = new NearestObjectStatModel( "default" );
		d.add( new NearestObjectStatItem( 6, NearestObjectStat.MEAN, false ) );
		defaults = new ArrayList<>( 1 );
		defaults.add( d );
	}

	private final List< NearestObjectStatItem > items = new ArrayList<>();

	private String name;

	public interface StatModelListener
	{
		public void statModelChanged();
	}

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

	private final Listeners.List< StatModelListener > updateListeners = new Listeners.List<>();

	private void notifyListeners()
	{
		for ( final StatModelListener l : updateListeners.list )
			l.statModelChanged();
	}

	public org.scijava.listeners.Listeners.List< StatModelListener > statModelListeners()
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

		public final int n;

		public final boolean includeItem;

		public final NearestObjectStat statStat;

		public NearestObjectStatItem( final int n, final NearestObjectStat stat, final boolean includeItem )
		{
			this.n = n;
			this.statStat = stat;
			this.includeItem = includeItem;
		}

		public static NearestObjectStatItem defaultValue()
		{
			return new NearestObjectStatItem( 6, NearestObjectStat.MEAN, false );
		}

		@Override
		public String toString()
		{
			return statStat.toString() + " of " + n + " NN" +
					( includeItem ? " including central item" : "" );
		}

		@Override
		public boolean equals( final Object obj )
		{
			if ( !NearestObjectStatItem.class.isInstance( obj ) )
				return false;

			final NearestObjectStatItem o = ( NearestObjectStatItem ) obj;
			if ( o.includeItem != includeItem )
				return false;
			if ( o.n != n )
				return false;
			if ( o.statStat != statStat )
				return false;

			return true;
		}

		public double eval( final Spot neighbor, final Spot center, final FeatureModel featureModel )
		{
			// TODO: this is just the distance.
			return Util.distance( neighbor, center );
		}

		public double summarize( final TDoubleArrayList arr )
		{
			switch ( statStat )
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
				throw new IllegalAddException( "This summarizing operation is unknown: " + statStat );
			}
		}
	}

	public enum NearestObjectStat
	{

		MEAN( "Mean" ),
		STD( "Std" ),
		MIN( "Min" ),
		MAX( "Max" ),
		MEDIAN( "Median" ),
		SUM( "Sum" );

		private final String name;

		NearestObjectStat( final String name )
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
}
