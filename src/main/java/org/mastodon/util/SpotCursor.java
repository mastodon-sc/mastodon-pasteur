package org.mastodon.util;

import java.util.Arrays;
import java.util.PrimitiveIterator.OfLong;

import org.mastodon.revised.bdv.overlay.ScreenVertexMath;
import org.mastodon.revised.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.mastodon.revised.model.mamut.Spot;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.Sampler;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

/**
 * A cursor that iterates over the pixels in a {@link Spot}, for a transformed
 * source.
 * <p>
 * I could not get a super-elegant analytical iterator, so here is the approach.
 * We favor iterating Z-plane per Z-plane.
 * <ol>
 * <li>First we compute the Zmin and Zmax for a spot (everything is in pixel
 * units).
 * <li>Then we compute for each Z plane the intersection of the spot with the
 * plane, which is a rotated ellipse.
 * <li>We compute the 2D bounding-box of the ellipse, and create a cursor over
 * this interval.
 * <li>The 2D cursor is made to iterate over this interval, but only stops when
 * it is inside the ellipse.
 * </ol>
 *
 * Adapted from {@link ScreenVertexMath}.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <T>
 */
public class SpotCursor< T > implements Cursor< T >
{

	/**
	 * Used for intersection ellipse computation.
	 */
	private final JamaEigenvalueDecomposition eig2 = new JamaEigenvalueDecomposition( 2 );

	/**
	 * Spot center position in pixel units.
	 */
	private final double[] vPos = new double[ 3 ];

	/**
	 * T transforms from unit sphere to covariance ellipsoid.
	 */
	private final double[][] T = new double[ 3 ][ 3 ];

	/**
	 * TS^T transforms from covariance ellipsoid to unit sphere.
	 */
	private final double[][] TS = new double[ 3 ][ 3 ];

	/**
	 * Random access (3D) into the source.
	 */
	private final RandomAccess< T > ra;

	/**
	 * The 3D source to iterate over.
	 */
	private final RandomAccessible< T > source;

	/**
	 * The cursor that iterates the current plane.
	 */
	private Cursor< T > currentPlaneCursor;

	/**
	 * The spot to iterate over, using the global coordinates system.
	 */
	private final Spot spot;

	/**
	 * The source transform into the global coordinates system.
	 */
	private final AffineTransform3D transform;

	/**
	 * Z planes (pixel units) to iterate to cover this ellipsoid.
	 */
	private final long[] zs;

	/**
	 * Iterator over all the Zs.
	 */
	private OfLong planeIterator;

	/**
	 * Creates a new cursor that will iterate over all the pixels of a spot.
	 *
	 * @param spot
	 *            the spot to iterate over.
	 * @param transform
	 *            the transform that maps the source into the global coordinates
	 *            system.
	 * @param source
	 *            the source to iterate.
	 */
	public SpotCursor( final Spot spot, final AffineTransform3D transform, final RandomAccessible< T > source )
	{
		this.spot = spot;
		this.transform = transform;
		this.source = source;
		this.ra = source.randomAccess();

		// Transform spot covariance into pixel coordinates => vS.
		final double[][] S = new double[ 3 ][ 3 ];
		final double[][] vS = new double[ 3 ][ 3 ];
		spot.getCovariance( S );
		for ( int r = 0; r < 3; ++r )
			for ( int c = 0; c < 3; ++c )
				T[ r ][ c ] = transform.inverse().get( r, c );
		LinAlgHelpers.mult( T, S, TS );
		LinAlgHelpers.multABT( TS, T, vS );

		// Transform spot position into pixel coordinates => vPos.
		final double[] pos = new double[ 3 ];
		spot.localize( pos );
		transform.inverse().apply( pos, vPos );

		final JamaEigenvalueDecomposition eig3 = new JamaEigenvalueDecomposition( 3 );
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
		 * Compute bounding box in Z.
		 */

		final double[] vz = TS[ 2 ];
		final double d = 1. / LinAlgHelpers.length( vz );
		final long minZ = ( long ) Math.ceil( vPos[ 2 ] - d );
		final long maxZ = ( long ) Math.floor( vPos[ 2 ] + d );
		final int nZ = ( int ) ( maxZ - minZ + 1 );

		this.zs = new long[ nZ ];
		for ( int i = 0; i < zs.length; i++ )
			zs[ i ] = minZ + i;

		reset();
	}

	@Override
	public void reset()
	{
		planeIterator = Arrays.stream( zs ).iterator();
		nextEllipse();
	}

	private void nextEllipse()
	{
		final long z = planeIterator.nextLong();
		final Ellipse intersection = computeIntersection( z );
		if ( null == intersection )
		{
			nextEllipse();
			return;
		}
		ra.setPosition( z, 2 );
		final MixedTransformView< T > slice = Views.hyperSlice( source, 2, z );
		this.currentPlaneCursor = intersection.cursor( slice );
	}

	private Ellipse computeIntersection( final long z )
	{

		final double[] vx = TS[ 0 ];
		final double[] vy = TS[ 1 ];
		final double[] vz = TS[ 2 ];

		final double[] vn = new double[ 3 ];
		final double[] vztmp = new double[ 3 ];
		LinAlgHelpers.cross( vx, vy, vn );
		LinAlgHelpers.normalize( vn );
		LinAlgHelpers.scale( vz, z - vPos[ 2 ], vztmp );
		final double d = LinAlgHelpers.dot( vn, vztmp );

		if ( Math.abs( d ) >= 1 )
			return null;

		final double radius2 = 1.0 - d * d;
		LinAlgHelpers.scale( vn, LinAlgHelpers.dot( vn, vztmp ), vn );
		LinAlgHelpers.subtract( vztmp, vn, vztmp );
		LinAlgHelpers.mult( T, vztmp, vn );
		final double xshift = vn[ 0 ];
		final double yshift = vn[ 1 ];

		final double c2 = LinAlgHelpers.squareLength( vx );
		final double c = Math.sqrt( c2 );
		final double a = LinAlgHelpers.dot( vx, vy ) / c;
		final double a2 = a * a;
		final double b2 = LinAlgHelpers.squareLength( vy ) - a2;

		// Covariance of 2D ellipse obtained by intersecting ellipsoid with z=0
		// plane.
		final double[][] iS = new double[ 2 ][ 2 ];
		iS[ 0 ][ 0 ] = radius2 * ( 1.0 / c2 + a2 / ( b2 * c2 ) );
		iS[ 0 ][ 1 ] = radius2 * -a / ( b2 * c );
		iS[ 1 ][ 0 ] = iS[ 0 ][ 1 ];
		iS[ 1 ][ 1 ] = radius2 / b2;
		/*
		 * Now iS is the 2D covariance ellipsoid of transformed circle with
		 * radius
		 */

		eig2.decomposeSymmetric( iS );
		final double[] eigVals2 = eig2.getRealEigenvalues();
		final double w = Math.sqrt( eigVals2[ 0 ] );
		final double h = Math.sqrt( eigVals2[ 1 ] );
		final double ci = eig2.getV()[ 0 ][ 0 ];
		final double si = eig2.getV()[ 1 ][ 0 ];

		final Ellipse intersectEllipse = new Ellipse(
				vPos[ 0 ] + xshift,
				vPos[ 1 ] + yshift,
				w,
				h,
				Math.atan2( si, ci ) );
		return intersectEllipse;
	}

	private class Ellipse
	{
		/**
		 * center of 2D ellipse.
		 */
		private final double x;

		private final double y;

		/**
		 * rotation angle of 2D ellipse (in radians).
		 */
		private final double theta;

		/**
		 * half-width and half-height of axis-aligned 2D ellipse.
		 */
		private final double w;

		private final double h;

		public Ellipse( final double x, final double y, final double w, final double h, final double theta )
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.theta = theta;
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder( super.toString() );
			str.append( ", pos = " + x + ", " + y );
			str.append( String.format( ", theta = %.1f", theta ) );
			str.append( String.format( ", half-width = %.1f", w ) );
			str.append( String.format( ", half-height = %.1f", h ) );
			return str.toString();
		}

		public Cursor< T > cursor( final RandomAccessible< T > source )
		{
			return new EllipseCursor( this, source );
		}

		public boolean isInside( final double xp, final double yp )
		{
			final double cos = Math.cos( theta );
			final double sin = Math.sin( theta );
			final double rr1 = w * w;
			final double rr2 = h * h;
			final double a = cos * ( x - xp ) + sin * ( y - yp );
			final double aa = a * a;
			final double b = sin * ( x - xp ) - cos * ( y - yp );
			final double bb = b * b;
			final double ell = aa / rr1 + bb / rr2;
			return ell <= 1.;
		}
	}

	private class EllipseCursor implements Cursor< T >
	{

		private final Ellipse ellipse;

		private final RandomAccessible< T > source;

		private Cursor< T > cursor;

		private boolean hasNext = true;

		private EllipseCursor( final Ellipse ellipse, final RandomAccessible< T > source )
		{
			this.ellipse = ellipse;
			this.source = source;
			reset();
		}

		@Override
		public void reset()
		{
			// Compute bounding box.
			final double ux = ellipse.w * Math.cos( ellipse.theta );
			final double uy = ellipse.w * Math.sin( ellipse.theta );
			final double vx = ellipse.y * Math.cos( ellipse.theta + Math.PI / 2 );
			final double vy = ellipse.y * Math.sin( ellipse.theta + Math.PI / 2 );

			final double bboxHalfWidth = Math.sqrt( ux * ux + vx * vx );
			final double bboxHalfHeight = Math.sqrt( uy * uy + vy * vy );

			final double xmin = ellipse.x - bboxHalfWidth;
			final double xmax = ellipse.x + bboxHalfWidth;
			final double ymin = ellipse.y - bboxHalfHeight;
			final double ymax = ellipse.y + bboxHalfHeight;

			final long[] min = new long[] { Math.round( xmin ), Math.round( ymin ) };
			final long[] max = new long[] { Math.round( xmax ), Math.round( ymax ) };
			final Interval interval = new FinalInterval( min, max );
			this.cursor = Views.interval( source, interval ).localizingCursor();

			prefetch();
		}

		@Override
		public boolean hasNext()
		{
			return hasNext;
		}

		@Override
		public void jumpFwd( final long steps )
		{
			for ( long i = 0; i < steps; i++ )
				fwd();
		}

		@Override
		public void fwd()
		{
			ra.setPosition( cursor.getLongPosition( 0 ), 0 );
			ra.setPosition( cursor.getLongPosition( 1 ), 1 );
			prefetch();
		}

		private void prefetch()
		{
			while ( cursor.hasNext() )
			{
				cursor.fwd();
				if ( ellipse.isInside( cursor.getDoublePosition( 0 ), cursor.getDoublePosition( 1 ) ) )
					break;
			}
			hasNext = cursor.hasNext();
		}

		@Override
		public T next()
		{
			fwd();
			return ra.get();
		}

		@Override
		public T get()
		{
			return ra.get();
		}

		@Override
		public void localize( final float[] position )
		{
			ra.localize( position );
		}

		@Override
		public void localize( final double[] position )
		{
			ra.localize( position );
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return ra.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return ra.getDoublePosition( d );
		}

		@Override
		public int numDimensions()
		{
			return 2;
		}

		@Override
		public void localize( final int[] position )
		{
			ra.localize( position );
		}

		@Override
		public void localize( final long[] position )
		{
			ra.localize( position );
		}

		@Override
		public int getIntPosition( final int d )
		{
			return ra.getIntPosition( d );
		}

		@Override
		public long getLongPosition( final int d )
		{
			return ra.getLongPosition( d );
		}

		@Override
		public Sampler< T > copy()
		{
			return copyCursor();
		}

		@Override
		public Cursor< T > copyCursor()
		{
			throw new UnsupportedOperationException( "Cannot copy the ellipse cursor." );
		}
	}

	@Override
	public void localize( final float[] position )
	{
		currentPlaneCursor.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		currentPlaneCursor.localize( position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return currentPlaneCursor.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return currentPlaneCursor.getDoublePosition( d );
	}

	@Override
	public int numDimensions()
	{
		return 3;
	}

	@Override
	public T get()
	{
		return currentPlaneCursor.get();
	}

	@Override
	public void jumpFwd( final long steps )
	{
		throw new UnsupportedOperationException( "jumpFwd(long) is not supported for Spot cursor." );
	}

	@Override
	public void fwd()
	{
		if ( !currentPlaneCursor.hasNext() )
			nextEllipse();

		currentPlaneCursor.fwd();
	}

	@Override
	public boolean hasNext()
	{
		if ( currentPlaneCursor.hasNext() )
			return true;

		if ( !planeIterator.hasNext() )
			return false;

		/*
		 * Check if we have another plane to iterate, and if this plane has
		 * pixels.
		 */
		return ( null != computeIntersection( ra.getLongPosition( 2 ) + 1 ) );
	}

	@Override
	public T next()
	{
		fwd();
		return get();
	}

	@Override
	public void localize( final int[] position )
	{
		currentPlaneCursor.localize( position );
	}

	@Override
	public void localize( final long[] position )
	{
		currentPlaneCursor.localize( position );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return currentPlaneCursor.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return currentPlaneCursor.getLongPosition( d );
	}

	@Override
	public Cursor< T > copyCursor()
	{
		return new SpotCursor<>( spot, transform, source );
	}

	@Override
	public Sampler< T > copy()
	{
		return copyCursor();
	}
}
