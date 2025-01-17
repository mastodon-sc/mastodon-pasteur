/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
import org.mastodon.mamut.nearest.NearestObjectStatModel.Value;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.tracking.linking.ProgressListeners;
import org.mastodon.ui.ProgressListener;

import gnu.trove.list.array.TDoubleArrayList;

public class NearestObjectStatComputation
{

	public static final NearestObjectStatFeature compute( final Model model, final NearestObjectStatModel statModel, final int minTimepoint, final int maxTimepoint )
	{
		return compute( model, statModel, minTimepoint, maxTimepoint, ProgressListeners.voidLogger() );
	}

	public static NearestObjectStatFeature compute( final Model model, final NearestObjectStatModel statModel, final int minTimepoint, final int maxTimepoint, final ProgressListener progressListener )
	{
		final NearestObjectStatFeature feature = NearestObjectStatFeature.createFeature( statModel, model );

		final ReadLock lock = model.getGraph().getLock().readLock();
		lock.lock();
		try
		{
			for ( int t = minTimepoint; t <= maxTimepoint; t++ )
			{
				progressListener.showProgress( t - minTimepoint, maxTimepoint - minTimepoint + 1 );
				process( model, statModel, t, feature ).run();
			}
			return feature;
		}
		finally
		{
			progressListener.clearStatus();
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
				// Max number of neighbors we need to collect when we specify N.
				int maxN = -1;
				// Max distance we need to search for when we specify max dist.
				double maxDistance = Double.NEGATIVE_INFINITY;

				final List< ToDoubleBiFunction< Spot, Spot > > evaluators = new ArrayList<>( nosModel.size() );
				for ( final NearestObjectStatItem item : nosModel )
				{
					final ToDoubleBiFunction< Spot, Spot > evaluator = item.eval( model.getFeatureModel() );
					evaluators.add( evaluator );

					switch ( item.collectBy )
					{
					case MAX_DISTANCE:
					{
						final double md = item.maxDistance;
						if ( md > maxDistance )
							maxDistance = md;
						break;
					}
					case SPECIFY_N:
					{
						final int n = item.n;
						if ( n > maxN )
							maxN = n;
						break;
					}
					default:
						throw new IllegalArgumentException( "Unknown collection method: " + item.collectBy );
					}
				}
				final RefList< Spot > list = RefCollections.createRefList( model.getGraph().vertices() );

				// Spatial search.
				final SpatialIndex< Spot > si = model.getSpatioTemporalIndex().getSpatialIndex( t );
				final IncrementalNearestNeighborSearch< Spot > search = si.getIncrementalNearestNeighborSearch();

				// Storage for numerical values fetched from neighbor.
				final TDoubleArrayList arr = new TDoubleArrayList();
				final TDoubleArrayList distances = new TDoubleArrayList();
				for ( final Spot spot : si )
				{
					// Collect the largest list of neighbors we need.
					search.search( spot );
					distances.clear();
					list.clear();
					int n = 0;
					while ( true )
					{
						final Spot neighbor = search.next();
						if ( neighbor == null )
							break;

						final double d = search.getDistance();
						if ( n > maxN && d > maxDistance )
							break;

						n++;
						distances.add( d );
						list.add( neighbor );
					}

					for ( int j = 0; j < nosModel.size(); j++ )
					{
						final NearestObjectStatItem item = nosModel.get( j );
						final ToDoubleBiFunction< Spot, Spot > evaluator = evaluators.get( j );

						// The first one is always the spot itself.
						final int start = item.include ? 0 : 1;

						arr.resetQuick();
						switch ( item.collectBy )
						{
						case MAX_DISTANCE:
						{
							// Are collecting the number of neighbors?
							if ( item.value == Value.DISTANCE_OR_N )
							{
								int nNeighbors = 0;
								for ( int k = start; k < list.size(); k++ )
								{
									final double d = distances.get( k );
									if ( d > item.maxDistance )
										break;

									nNeighbors++;
								}
								// No need to summarize.
								feature.set( spot, item, nNeighbors );
								continue;
							}

							// Stop collecting neighbors if we are beyond max
							// limit.

							for ( int k = start; k <= list.size() && k < distances.size(); k++ )
							{
								final double d = distances.get( k );
								if ( d > item.maxDistance )
									break;

								arr.add( evaluator.applyAsDouble( list.get( k ), spot ) );
							}
							break;
						}
						case SPECIFY_N:
						{
							// Stop collecting neighbors if we exceed the
							// specified number of neighbors.
							for ( int k = start; k <= Math.min( item.n, list.size() - 1 ); k++ )
								arr.add( evaluator.applyAsDouble( list.get( k ), spot ) );
							break;
						}
						default:
							throw new IllegalArgumentException( "Unknown collection method: " + item.collectBy );
						}

						// Summarize measurement on neighbors.
						final double val = item.summarize( arr );
						// Store.
						feature.set( spot, item, val );
					}
				}
			}
		};
	}
}
