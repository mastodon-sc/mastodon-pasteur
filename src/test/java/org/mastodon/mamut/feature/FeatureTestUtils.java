package org.mastodon.mamut.feature;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mastodon.views.bdv.SharedBigDataViewerData;

import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImgPlusViews;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.Type;

public class FeatureTestUtils
{

	public static final < T extends Type< T > > SharedBigDataViewerData wrapAsSharedBdvData( final ImgPlus< T > img )
	{
		// Time.
		final int timeDim = img.dimensionIndex( Axes.TIME );
		final long nt = timeDim < 0 ? 1 : img.dimension( timeDim );

		final File basePath = new File( "." );
		final TimePoints timepoints = new TimePoints(
				IntStream.range( 0, ( int ) nt ).mapToObj( TimePoint::new ).collect( Collectors.toList() ) );
		final Map< Integer, BasicViewSetup > setups = new HashMap<>();
		setups.put( 0, new BasicViewSetup( 0, img.getName(), null, null ) );

		// Spatial calibration.
		final int xdim = img.dimensionIndex( Axes.X );
		final int ydim = img.dimensionIndex( Axes.Y );
		final int zdim = img.dimensionIndex( Axes.Z );
		final AffineTransform3D calib = new AffineTransform3D();
		calib.set( img.averageScale( xdim ), 0, 0 );
		calib.set( img.averageScale( ydim ), 1, 1 );
		calib.set( zdim < 0 ? 1. : img.averageScale( zdim ), 2, 2 );

		final BasicImgLoader imgLoader = new InRamBasicImgLoader<>( img );
		final SequenceDescriptionMinimal sequenceDescription = new SequenceDescriptionMinimal( timepoints, setups, imgLoader, null );
		final ViewRegistrations viewRegistrations = new ViewRegistrations(
				IntStream.range( 0, ( int ) nt ).mapToObj( tp -> new ViewRegistration( tp, 0, calib ) ).collect( Collectors.toList() ) );
		final SpimDataMinimal spimData = new SpimDataMinimal( basePath, sequenceDescription, viewRegistrations );

		// Shared bdv.
		final ViewerOptions options = ViewerOptions.options();
		return new SharedBigDataViewerData( basePath.getAbsolutePath(), spimData, options, null );
	}

	public static final < T extends Type< T > > RandomAccessibleInterval< T > hyperslice( final ImgPlus< T > img, final int channel, final int timepointId )
	{
		final ImgPlus< T > singleTimePoint;
		if ( img.dimensionIndex( Axes.TIME ) < 0 )
			singleTimePoint = img;
		else
			singleTimePoint = ImgPlusViews.hyperSlice( img, img.dimensionIndex( Axes.TIME ), timepointId );

		final ImgPlus< T > singleChannel;
		if ( singleTimePoint.dimensionIndex( Axes.CHANNEL ) < 0 )
			singleChannel = singleTimePoint;
		else
			singleChannel = ImgPlusViews.hyperSlice( singleTimePoint, singleTimePoint.dimensionIndex( Axes.CHANNEL ), channel );
		return singleChannel;
	}

	private static class InRamBasicImgLoader< T extends Type< T > > implements BasicImgLoader
	{

		private final List< BasicSetupImgLoader< T > > setupImgLoaders;

		private final ImgPlus< T > img;

		public InRamBasicImgLoader( final ImgPlus< T > img )
		{
			this.img = img;
			final int cdim = img.dimensionIndex( Axes.CHANNEL );
			final long nc = cdim < 0 ? 1 : img.dimension( cdim );
			setupImgLoaders = new ArrayList<>( ( int ) nc );

			for ( int c = 0; c < nc; c++ )
				setupImgLoaders.add( new MyBasicSetupImgLoader( c ) );
		}

		@Override
		public BasicSetupImgLoader< ? > getSetupImgLoader( final int setupId )
		{
			return setupImgLoaders.get( setupId );
		}

		private final class MyBasicSetupImgLoader implements BasicSetupImgLoader< T >
		{

			private final int channel;

			public MyBasicSetupImgLoader( final int channel )
			{
				this.channel = channel;
			}

			@Override
			public RandomAccessibleInterval< T > getImage( final int timepointId, final ImgLoaderHint... hints )
			{
				return hyperslice( img, channel, timepointId );
			}

			@Override
			public T getImageType()
			{
				return img.firstElement();
			}
		}
	}

}
