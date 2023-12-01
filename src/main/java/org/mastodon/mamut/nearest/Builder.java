/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.mastodon.mamut.feature.SpotIntensityFeature;
import org.mastodon.mamut.nearest.NearestObjectStatModel.CollectBy;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Stat;
import org.mastodon.mamut.nearest.NearestObjectStatModel.Value;
import org.mastodon.ui.coloring.feature.FeatureProjectionId;
import org.mastodon.ui.coloring.feature.TargetType;

public class Builder
{

	public static abstract class AbstractBuilder< T extends AbstractBuilder< T > >
	{

		protected Stat stat = Stat.MEAN;

		protected Value value = Value.DISTANCE_OR_N;

		protected final CollectBy collectBy;
		
		protected FeatureProjectionId featureID = new FeatureProjectionId(
				SpotIntensityFeature.KEY,
				SpotIntensityFeature.MEAN_PROJECTION_SPEC.getKey(),
				TargetType.VERTEX,
				0 );

		protected boolean include = false;

		public AbstractBuilder( final CollectBy collectBy )
		{
			this.collectBy = collectBy;
		}

		@SuppressWarnings( "unchecked" )
		public T includeCenter( final boolean include )
		{
			this.include = include;
			return ( T ) this;
		}

		public abstract NearestObjectStatItem get();
	}

	public static class CollectByDistanceMeasureFeatureBuilder extends AbstractBuilderWithStat< CollectByDistanceMeasureFeatureBuilder >
	{

		private final double dist;

		public CollectByDistanceMeasureFeatureBuilder( final double dist, final FeatureProjectionId featureID, final boolean include )
		{
			super( CollectBy.MAX_DISTANCE );
			this.dist = dist;
			this.featureID = featureID;
			this.include = include;
			this.value = Value.FEATURE;
		}

		@Override
		public NearestObjectStatItem get()
		{
			return new NearestObjectStatItem(
					collectBy,
					6,
					dist,
					value,
					featureID,
					stat,
					include );
		}
	}

	public static abstract class AbstractBuilderWithStat< T extends AbstractBuilderWithStat< T > > extends AbstractBuilder< T >
	{

		public AbstractBuilderWithStat( final CollectBy collectBy )
		{
			super( collectBy );
		}

		@SuppressWarnings( "unchecked" )
		public T mean()
		{
			this.stat = Stat.MEAN;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T median()
		{
			this.stat = Stat.MEDIAN;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T max()
		{
			this.stat = Stat.MAX;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T min()
		{
			this.stat = Stat.MIN;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T sum()
		{
			this.stat = Stat.SUM;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T std()
		{
			this.stat = Stat.STD;
			return ( T ) this;
		}

		@SuppressWarnings( "unchecked" )
		public T measureFeature( final String featureKey, final String projectionKey )
		{
			this.featureID = new FeatureProjectionId( featureKey, projectionKey, TargetType.VERTEX );
			this.value = Value.FEATURE;
			return ( T ) this;
		}

		public T measureFeature( final String key )
		{
			return measureFeature( key, key );
		}
	}

	public static class CollectByNBuilder extends AbstractBuilderWithStat< CollectByNBuilder >
	{

		private final int n;

		private CollectByNBuilder( final int n )
		{
			super( CollectBy.SPECIFY_N );
			this.n = n;
		}

		public CollectByNBuilder measureDistance()
		{
			this.value = Value.DISTANCE_OR_N;
			return this;
		}

		@Override
		public NearestObjectStatItem get()
		{
			return new NearestObjectStatItem(
					collectBy,
					n,
					10.,
					value,
					featureID,
					stat,
					include );
		}
	}

	public static class CollectByDistanceBuilder extends AbstractBuilder< CollectByDistanceBuilder >
	{

		private final double dist;

		public CollectByDistanceBuilder( final double dist )
		{
			super( CollectBy.MAX_DISTANCE );
			this.dist = dist;
		}

		public CollectByDistanceBuilder measureN()
		{
			this.value = Value.DISTANCE_OR_N;
			return this;
		}

		public CollectByDistanceMeasureFeatureBuilder measureFeature( final String featureKey, final String projectionKey )
		{
			final FeatureProjectionId featureID = new FeatureProjectionId( featureKey, projectionKey, TargetType.VERTEX );
			return new CollectByDistanceMeasureFeatureBuilder( dist, featureID, include );
		}

		public CollectByDistanceMeasureFeatureBuilder measureFeature( final String key )
		{
			return measureFeature( key, key );
		}
		@Override
		public NearestObjectStatItem get()
		{
			return new NearestObjectStatItem(
					collectBy,
					6,
					dist,
					value,
					featureID,
					stat,
					include );
		}
	}

	Builder()
	{}

	public CollectByNBuilder collectByN( final int n )
	{
		if ( n < 1 )
			throw new IllegalArgumentException( "Number of neighbors N cannot be lower than 1." );
		return new CollectByNBuilder( n );
	}

	public CollectByDistanceBuilder collectByDistance( final double dist )
	{
		if ( dist <= 0 )
			throw new IllegalArgumentException( "Max distance cannot be lower than or equal to 0." );
		return new CollectByDistanceBuilder( dist );
	}
}
