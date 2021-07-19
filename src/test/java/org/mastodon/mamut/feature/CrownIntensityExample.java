package org.mastodon.mamut.feature;

import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import ij.ImageJ;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class CrownIntensityExample
{

	public static void main( final String[] args )
	{
		// Create a 2D empty image, 60 x 60 x 30.
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( 60, 60, 30 );

		// Wrap it in a BDV source (this is the way we get images in Mastodon).
		final Source< UnsignedShortType > source = new RandomAccessibleIntervalSource<>( img, img.firstElement(), "Blank" );

		// Create an empty mamut model.
		final Model model = new Model( "pixel", "frame" );

		// Create an ellipsoid in this model.
		final int timepoint = 0;
		final double[] center = new double[] { 30., 30., 15. };
		final double[][] covariance = new double[ 3 ][ 3 ];
		covariance[ 0 ][ 0 ] = covariance[ 1 ][ 1 ] = 200.;
		covariance[ 2 ][ 2 ] = 100.;
		covariance[ 0 ][ 1 ] = covariance[ 1 ][ 0 ] = 40.;
		covariance[ 1 ][ 2 ] = covariance[ 2 ][ 1 ] = 40.;
		final Spot spot = model.getGraph().addVertex().init( timepoint, center, covariance );

		// Create the ellipsoid iterable over the source.
		final EllipsoidIterable< UnsignedShortType > iterable = new EllipsoidIterable<>( source );

		// Set the iterable to iterate over the ellipsoid (spot) we created.
		iterable.reset( spot );

		// Iterate inside this ellipsoid and set value to 1000.
		for ( final UnsignedShortType p : iterable )
			p.set( 1000 );

		// Shrink the spot by half (to the square for covariance).
		for ( int i = 0; i < covariance.length; i++ )
			for ( int j = 0; j < covariance[ i ].length; j++ )
				covariance[ i ][ j ] /= ( 2. * 2. );

		spot.setCovariance( covariance );

		// Iterate inside the shrunk ellipsoid and set value to 0.
		iterable.reset( spot );
		for ( final UnsignedShortType p : iterable )
			p.set( 0 );
		
		// Wrap the Img into a ImgPlus (so that we can set the calibration)
		// (does not work).
		final String name = "Test crown intensity";;
		final AxisType[] axisTypes = new AxisType[] { Axes.X, Axes.Y, Axes.Z };
		final double[] cal = new double[] { 1., 1., 1. };
		final ImgPlus< UnsignedShortType > imgPlus = new ImgPlus<>( img, name, axisTypes, cal );

		// Show the resulting image in ImageJ.
		ImageJ.main( args );
		final ImagePlus imp = ImageJFunctions.wrap( imgPlus, name );
		imp.setDimensions( 1, ( int ) img.dimension( 2 ), 1 );
		imp.setSlice( ( int ) ( img.dimension( 2 ) / 2 ) );
		imp.resetDisplayRange();
		imp.show();
	}
}
