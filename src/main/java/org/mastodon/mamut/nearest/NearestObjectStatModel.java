package org.mastodon.mamut.nearest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.IllegalAddException;
import org.mastodon.feature.FeatureModel;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.scijava.listeners.Listeners;

import gnu.trove.list.array.TDoubleArrayList;
import net.imglib2.util.Util;

public class NearestObjectStatModel implements Iterable< NearestObjectStatItem >
{

	private static final int MAX_N_ITEMS = 20;

	private final List< NearestObjectStatItem > items = new ArrayList<>();

	public interface StatModelListener
	{
		public void statModelChanged();
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

		private int n = 6;

		private boolean includeItem = false;

		private NearestObjectStat nearestObjectStat = NearestObjectStat.MEAN;

		public int n()
		{
			return n;
		}

		public void setN( final int n )
		{
			this.n = n;
		}

		public boolean includeItem()
		{
			return includeItem;
		}

		public void setIncludeItem( final boolean includeItem )
		{
			this.includeItem = includeItem;
		}

		public NearestObjectStat nearestObjectStat()
		{
			return nearestObjectStat;
		}

		public void setNearestObjectStat( final NearestObjectStat nearestObjectStat )
		{
			this.nearestObjectStat = nearestObjectStat;
		}

		public static NearestObjectStatItem defaultValue()
		{
			return new NearestObjectStatItem();
		}

		public NearestObjectStatItem copy()
		{
			final NearestObjectStatItem copy = new NearestObjectStatItem();
			copy.setIncludeItem( includeItem );
			copy.setN( n );
			copy.setNearestObjectStat( nearestObjectStat );
			return copy;
		}

		@Override
		public String toString()
		{
			return nearestObjectStat.toString() + " of " + n + " NN" +
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
			if ( o.nearestObjectStat != nearestObjectStat )
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
			switch ( nearestObjectStat )
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
				throw new IllegalAddException( "This summarizing operation is unknown: " + nearestObjectStat );
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

	private static final int size( final int start, final int end )
	{
		return end - start + 1;
	}
}
