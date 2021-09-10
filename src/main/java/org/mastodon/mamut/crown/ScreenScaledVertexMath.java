/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.crown;

import org.mastodon.views.bdv.overlay.OverlayVertex;
import org.mastodon.views.bdv.overlay.ScreenVertexMath;
import org.mastodon.views.bdv.overlay.ScreenVertexMath.Ellipse;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;

/**
 * Copied from {@link ScreenVertexMath}. Scale a vertex by a factor.
 *
 * @author Tobias Pietzsch
 */
public class ScreenScaledVertexMath
{

	private AffineTransform3D transform;

	/**
	 * spot position in global coordinate system.
	 */
	private final double[] pos = new double[ 3 ];

	/**
	 * spot covariance in global coordinate system.
	 */
	private final double[][] S = new double[ 3 ][ 3 ];

	/**
	 * spot position in viewer coordinate system.
	 */
	private final double[] vPos = new double[ 3 ];

	/**
	 * spot covariance in viewer coordinate system.
	 */
	private final double[][] vS = new double[ 3 ][ 3 ];

	/**
	 * 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final Ellipse intersectEllipse = new Ellipse();

	private boolean intersectionComputed;

	// tmp
	private final JamaEigenvalueDecomposition eig2 = new JamaEigenvalueDecomposition( 2 );

	// tmp
	private final JamaEigenvalueDecomposition eig3 = new JamaEigenvalueDecomposition( 3 );

	// tmp
	private final double[][] T = new double[ 3 ][ 3 ];

	// tmp
	private final double[][] TS = new double[ 3 ][ 3 ];

	// tmp
	private final double[] vn = new double[ 3 ];

	/**
	 * covariance of 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	private final double[][] iS = new double[ 2 ][ 2 ];

	private final double scale;

	public ScreenScaledVertexMath( final double scale )
	{
		this.scale = scale;
	}

	/**
	 * (Re-)initialize for a new {@code vertex} and the given viewer transform.
	 * Resets the state of this {@link ScreenScaledVertexMath}, discarding all cached
	 * computed values.
	 *
	 * @param vertex
	 *            the vertex.
	 * @param viewerTransform
	 *            the transform.
	 */
	public void init( final OverlayVertex< ?, ? > vertex, final AffineTransform3D viewerTransform )
	{
		this.transform = viewerTransform;

		// transform spot covariance into viewer coordinates => vS
		vertex.getCovariance( S );

		// Scale covariance.
		for ( int i = 0; i < S.length; i++ )
			for ( int j = 0; j < S[ 0 ].length; j++ )
				S[ i ][ j ] = S[ i ][ j ] * scale * scale;

		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = transform.get( r, c );
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, vS );

		// transform spot position into viewer coordinates => vPos
		vertex.localize( pos );
		transform.apply( pos, vPos );

		intersectionComputed = false;
	}

	/**
	 * Get spot position in viewer coordinate system.
	 *
	 * @return spot position in viewer coordinate system.
	 */
	public double[] getViewPos()
	{
		return vPos;
	}

	/**
	 * Get the 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 *
	 * @return 2D ellipse obtained by intersecting ellipsoid with z=0 plane.
	 */
	public Ellipse getIntersectEllipse()
	{
		computeIntersection();
		return intersectEllipse;
	}

	private void computeIntersection()
	{
		if ( intersectionComputed )
			return;

		eig3.decomposeSymmetric( vS );
		final double[] eigVals = eig3.getRealEigenvalues();
		final double[][] V = eig3.getV();
		for ( int i = 0; i < 3; ++i )
		{
			final double e = Math.sqrt( eigVals[ i ] );
			final double inve = 1.0 / e;
			for ( int j = 0; j < 3; ++j )
			{
				T[ j ][ i ] = e * V[ j ][ i ];
				TS[ j ][ i ] = inve * V[ j ][ i ];
			}
		}
		/*
		 * now T and TS^T transform from unit sphere to covariance ellipsoid and
		 * vice versa
		 */

		final double[] vx = TS[ 0 ];
		final double[] vy = TS[ 1 ];
		final double[] vz = TS[ 2 ];

		final double z = vPos[ 2 ];
		LinAlgHelpers.cross( vx, vy, vn );
		LinAlgHelpers.normalize( vn );
		LinAlgHelpers.scale( vz, z, vz );
		final double d = LinAlgHelpers.dot( vn, vz );
		if ( Math.abs( d ) < 1. )
		{
			final double radius2 = 1.0 - d * d;
			LinAlgHelpers.scale( vn, LinAlgHelpers.dot( vn, vz ), vn );
			LinAlgHelpers.subtract( vz, vn, vz );
			LinAlgHelpers.mult( T, vz, vn );
			final double xshift = vn[ 0 ];
			final double yshift = vn[ 1 ];

			final double c2 = LinAlgHelpers.squareLength( vx );
			final double c = Math.sqrt( c2 );
			final double a = LinAlgHelpers.dot( vx, vy ) / c;
			final double a2 = a * a;
			final double b2 = LinAlgHelpers.squareLength( vy ) - a2;
			iS[ 0 ][ 0 ] = radius2 * ( 1.0 / c2 + a2 / ( b2 * c2 ) );
			iS[ 0 ][ 1 ] = radius2 * -a / ( b2 * c );
			iS[ 1 ][ 0 ] = iS[ 0 ][ 1 ];
			iS[ 1 ][ 1 ] = radius2 / b2;
			/*
			 * now iS is the 2D covariance ellipsoid of transformed circle with radius
			 */

			eig2.decomposeSymmetric( iS );
			final double[] eigVals2 = eig2.getRealEigenvalues();
			final double w = Math.sqrt( eigVals2[ 0 ] );
			final double h = Math.sqrt( eigVals2[ 1 ] );
			final double ci = eig2.getV()[ 0 ][ 0 ];
			final double si = eig2.getV()[ 1 ][ 0 ];

			intersectEllipse.setTheta( Math.atan2( si, ci ) );
			intersectEllipse.setCenter( vPos[ 0 ] + xshift, vPos[ 1 ] + yshift );
			intersectEllipse.setAxisHalfLength( w, h );
		}

		intersectionComputed = true;
	}
}
