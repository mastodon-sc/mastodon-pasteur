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
package org.mastodon.mamut.spottrackimage;

import org.mastodon.mamut.model.Spot;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.position.transform.Round;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

public class SpotTrackImageUtils
{

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static Img< ? > collectInterval(
			final Interval patch,
			final AffineTransform3D patchTransform,
			final Source< ? > targetSource,
			final int frame )
	{
		final AffineTransform3D targetToGlobal = new AffineTransform3D();
		targetSource.getSourceTransform( frame, 0, targetToGlobal );

		final AffineTransform3D t1 = targetToGlobal.inverse();
		final AffineTransform3D t2 = t1.concatenate( patchTransform );
		
		// Squeeze singleton dimensions.
		Interval intervalTarget = patch;
		int d = 0;
		do
		{
			if ( intervalTarget.dimension( d ) < 2 )
				intervalTarget = Intervals.hyperSlice( intervalTarget, d );
			else
				d++;
		}
		while ( d < intervalTarget.numDimensions() );

		// Create a target image with no singleton dimension.
		final ImgFactory< ? > factory = Util.getArrayOrCellImgFactory( intervalTarget, ( NativeType ) targetSource.getType() );
		final Img< ? > target = factory.create( intervalTarget );

		final RealPoint targetPos = RealPoint.wrap( new double[ 3 ] );
		final RealPoint sourcePos = RealPoint.wrap( new double[ 3 ] );

		final RealRandomAccessible< ? > targetRai = targetSource.getInterpolatedSource( frame, 0, Interpolation.NEARESTNEIGHBOR );
		final RealRandomAccess< ? > targetRa = targetRai.realRandomAccess();

		final Cursor< ? > cursor = target.localizingCursor();
		final long[] min = patch.minAsLongArray();
		while ( cursor.hasNext() )
		{
			cursor.fwd();

			sourcePos.setPosition( min );
			for ( int d2 = 0; d2 < target.numDimensions(); d2++ )
				sourcePos.move( cursor.getLongPosition( d2 ), d2 );

			t2.apply( sourcePos, targetPos );
			targetRa.setPosition( targetPos );
			( ( Type ) cursor.get() ).set( ( Type ) targetRa.get() );
		}
		return target;
	}

	public static final Interval getIntervalAround( final Spot spot, final int width, final int height, final int depth, final Source< ? > source )
	{
		final int frame = spot.getTimepoint();
		// Get spot coords
		final AffineTransform3D sourceToGlobal = new AffineTransform3D();
		source.getSourceTransform( frame, 0, sourceToGlobal );
		final Point roundedSourcePos = new Point( 3 );
		sourceToGlobal.applyInverse( new Round<>( roundedSourcePos ), spot );
		final long x = roundedSourcePos.getLongPosition( 0 );
		final long y = roundedSourcePos.getLongPosition( 1 );
		final long z = roundedSourcePos.getLongPosition( 2 );
		// Create interval.
		final long[] min = new long[] { x - width / 2, y - height / 2, z - depth / 2 };
		final long[] max = new long[] { x + width / 2, y + height / 2, z + depth / 2 };
		return new FinalInterval( min, max );
	}

	private SpotTrackImageUtils()
	{}
}
