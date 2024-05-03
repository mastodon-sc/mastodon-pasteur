/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.SpotNLinksFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.nearest.Builder.AbstractBuilderWithStat;
import org.mastodon.mamut.nearest.Builder.CollectByDistanceMeasureFeatureBuilder;
import org.mastodon.mamut.nearest.Builder.CollectByNBuilder;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;

public class NearestNeighborsStatsTest
{

	private static Model model;

	private static RefList< Spot > spots;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		model = new Model();
		final ModelGraph graph = model.getGraph();
		final Spot ref = graph.vertexRef();

		final double radius = 0.5;

		// 5 spots spiralling in XY.
		final double[][] positions = new double[][] {
				{ 0., 0., 0. },
				{ 1., 0., 0. },
				{ 0., 2., 0. },
				{ -3., 0., 0. },
				{ 0., -4., 0. }
		};

		spots = RefCollections.createRefList( graph.vertices(), positions.length );
		Spot source = null;
		final int tp = 0;
		for ( int t = 0; t < positions.length; t++ )
		{
			final Spot target = graph.addVertex( ref ).init( tp, positions[ t ], radius );
			spots.add( target );
			if ( source == null )
			{
				source = graph.vertexRef();
			}
			else
			{
				graph.addEdge( source, target ).init();
			}
			source.refTo( target );
		}
		graph.releaseRef( ref );
		graph.releaseRef( source );
	}

	/**
	 * Test values when measuring stats on distance for various number of
	 * neighbors, including center or not.
	 */
	@Test
	public void testDistanceToNeighbors()
	{
		final double[] expected4WithoutCenter = new double[] {
				2.5,
				4.,
				1.,
				3.,
				10.,
				1.2909944487358056
		};
		final double[] expected4WithCenter = new double[] {
				2.,
				4.,
				0.,
				2.,
				10.,
				1.5811388300841898
		};
		final double[] expected2WithoutCenter = new double[] {
				1.5,
				2.,
				1.,
				2.,
				3.,
				0.7071067811865476
		};
		final double[] expected2WithCenter = new double[] {
				1.,
				2.,
				0.,
				1.,
				3.,
				1.0
		};
		final double[] expected1WithoutCenter = new double[] {
				1,
				1.,
				1.,
				1.,
				1.,
				Double.NaN
		};
		final double[] expected1WithCenter = new double[] {
				.5,
				1.,
				0.,
				1.,
				1.,
				0.7071067811865476
		};
		final double[][] expecteds = new double[][] {
				expected4WithoutCenter,
				expected4WithCenter,
				expected2WithoutCenter,
				expected2WithCenter,
				expected1WithoutCenter,
				expected1WithCenter };

		final int[] ns = new int[] { 4, 4, 2, 2, 1, 1 };
		final boolean[] withOrWithoutYou = new boolean[] { false, true, false, true, false, true };

		for ( int j = 0; j < withOrWithoutYou.length; j++ )
		{
			final CollectByNBuilder builder = NearestObjectStatItem
					.create()
					.collectByN( ns[ j ] )
					.includeCenter( withOrWithoutYou[ j ] )
					.measureDistance();
			test( builder, expecteds[ j ] );
		}
	}

	/**
	 * Stat on feature values for neighbors, collected by number of neighbors.
	 */
	@Test
	public void testFeatureOfNeighbors()
	{
		final CollectByNBuilder builder = NearestObjectStatItem
				.create()
				.collectByN( 2 )
				.measureFeature( SpotNLinksFeature.KEY )
				.includeCenter( false );
		final double[] expectedWithout = new double[] {
				2.,
				2.,
				2.,
				2.,
				4.,
				0. };
		test( builder, expectedWithout );

		final CollectByNBuilder builder2 = builder.includeCenter( true );
		final double[] expectedWith = new double[] {
				1.6666666666666667,
				2.,
				1.,
				2.,
				5.,
				0.5773502691896257 };
		test( builder2, expectedWith );
	}

	/**
	 * Test counting the number of neighbors when collecting neighbors by max
	 * distance.
	 */
	@Test
	public void testCollectByDistanceMeasureN()
	{
		final double[] dists = new double[] { 0.5, 1., 2., 3., 4., 5., 100000 };
		final int[] expecteds = new int[] { 0, 1, 2, 3, 4, 4, 4 };

		for ( final boolean include : new boolean[] { false, true } )
		{
			for ( int i = 0; i < expecteds.length; i++ )
			{
				final int expected = expecteds[ i ] + ( ( include ) ? 1 : 0 );

				final NearestObjectStatItem item = NearestObjectStatItem
						.create()
						.collectByDistance( dists[ i ] )
						.includeCenter( include )
						.measureN()
						.get();

				final NearestObjectStatModel stat = new NearestObjectStatModel( "test" );
				stat.add( item );

				final NearestObjectStatFeature feature = NearestObjectStatComputation.compute( model, stat, 0, 0 );
				final FeatureProjection< Spot > projection = feature.get( item );
				final Spot center = spots.get( 0 );
				final int val = ( int ) projection.value( center );
				assertEquals( "Incorrect number of neighbors collected with " + item, expected, val );
			}
		}
	}

	/**
	 * Test counting the number of neighbors when collecting neighbors by max
	 * distance.
	 */
	@Test
	public void testCollectByDistanceMeasureFeature()
	{
		final double[] dists = new double[] { 0.5, 1., 2., 3., 4. };
		final double[][] expecteds = new double[][] {
				{ // No neighbors.
						Double.NaN,
						Double.NaN,
						Double.NaN,
						Double.NaN,
						Double.NaN,
						Double.NaN
				},
				{ // 1 neighbor
						2.,
						2.,
						2.,
						2.,
						2.,
						Double.NaN
				},
				{ // 2 neighbors (distance limit is inclusive)
						2.,
						2.,
						2.,
						2.,
						4.,
						0
				},
				{ // 3 neighbors
						2.,
						2.,
						2.,
						2.,
						6.,
						0
				},
				{ // 4 neighbors
						1.75,
						2.,
						1.,
						2.,
						7.,
						0.5
				},

		};

		for ( int i = 0; i < expecteds.length; i++ )
		{

			final CollectByDistanceMeasureFeatureBuilder builder = NearestObjectStatItem
					.create()
					.collectByDistance( dists[ i ] )
					.includeCenter( false )
					.measureFeature( SpotNLinksFeature.KEY );

			test( builder, expecteds[ i ] );
		}
	}

	private static final void test( final AbstractBuilderWithStat< ? > builder, final double[] expected )
	{
		final List< NearestObjectStatItem > items = Arrays.asList( new NearestObjectStatItem[] {
				builder.mean().get(),
				builder.max().get(),
				builder.min().get(),
				builder.median().get(),
				builder.sum().get(),
				builder.std().get()
		} );
		final NearestObjectStatModel stat = new NearestObjectStatModel( "test" );
		items.forEach( stat::add );

		final NearestObjectStatFeature feature = NearestObjectStatComputation.compute( model, stat, 0, 0 );
		final Spot center = spots.get( 0 );

		for ( int i = 0; i < items.size(); i++ )
		{
			final NearestObjectStatItem item = items.get( i );
			final double val = feature.get( item ).value( center );
			final double exp = expected[ i ];
			assertEquals( "Incorrect value for " + item, exp, val, 1e-6 );
		}
	}
}
