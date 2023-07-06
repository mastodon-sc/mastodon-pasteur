package org.mastodon.mamut.nearest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.function.ToDoubleBiFunction;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.tracking.linking.ProgressListeners;
import org.mastodon.ui.ProgressListener;

import gnu.trove.list.array.TDoubleArrayList;

public class NearestObjectStatComputation
{

	public static final NearestObjectStatFeature compute( final Model model, final NearestObjectStatModel selectedStyle, final int minTimepoint, final int maxTimepoint )
	{
		return compute( model, selectedStyle, minTimepoint, maxTimepoint, ProgressListeners.voidLogger() );
	}

	public static NearestObjectStatFeature compute( final Model model, final NearestObjectStatModel selectedStyle, final int minTimepoint, final int maxTimepoint, final ProgressListener progressListener )
	{
		final NearestObjectStatFeature feature = NearestObjectStatFeature.createFeature( selectedStyle, model );

		final ReadLock lock = model.getGraph().getLock().readLock();
		lock.lock();
		try
		{
			for ( int t = minTimepoint; t <= maxTimepoint; t++ )
			{
				progressListener.showProgress( t - minTimepoint, maxTimepoint - minTimepoint + 1 );
				process( model, selectedStyle, t, feature ).run();
			}
			progressListener.clearStatus();
			return feature;
		}
		finally
		{
			lock.unlock();
		}
	}

	private static final Runnable process( final Model model, final NearestObjectStatModel nosModel, final int t, final NearestObjectStatFeature feature )
	{
		return new Runnable()
		{

			@Override
			public void run()
			{
				// Max number of neighbors we need to collect.
				int maxN = -1;
				final List< ToDoubleBiFunction< Spot, Spot > > evaluators = new ArrayList<>( nosModel.size() );
				for ( final NearestObjectStatItem item : nosModel )
				{
					final ToDoubleBiFunction< Spot, Spot > evaluator = item.eval( model.getFeatureModel() );
					evaluators.add( evaluator );
					final int n = item.n;
					if ( n > maxN )
						maxN = n;
				}
				final RefList< Spot > list = RefCollections.createRefList( model.getGraph().vertices(), maxN );

				// Spatial search.
				final SpatialIndex< Spot > si = model.getSpatioTemporalIndex().getSpatialIndex( t );
				final IncrementalNearestNeighborSearch< Spot > search = si.getIncrementalNearestNeighborSearch();

				// Storage for numerical values fetched from neighbor.
				final TDoubleArrayList arr = new TDoubleArrayList( maxN );
				for ( final Spot spot : si )
				{
					// Build list of neighbors.
					search.search( spot );
					list.clear();
					for ( int i = 0; i <= maxN; i++ )
						list.add( search.next() );

					for ( int j = 0; j < nosModel.size(); j++ )
					{
						final NearestObjectStatItem item = nosModel.get( j );
						final ToDoubleBiFunction< Spot, Spot > evaluator = evaluators.get( j );

						// The first one is always the spot itself.
						final int start = item.include ? 0 : 1;

						arr.resetQuick();
						for ( int i = start; i <= item.n; i++ )
							arr.add( evaluator.applyAsDouble( list.get( i ), spot ) );

						final double val = item.summarize( arr );
						feature.set( spot, item, val );
					}
				}
			}
		};
	}
}
