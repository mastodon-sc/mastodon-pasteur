package org.mastodon.mamut.feature;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.mamut.feature.SpotCrownIntensityFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import bdv.util.RandomAccessibleIntervalSource;
import bdv.viewer.Source;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class SpotCrownIntensityFeatureComputerTest {

	@Test
	public void test() throws Exception {
		// Create the context of this test with only 1 service.
		final Context context = new Context( MamutFeatureComputerService.class );
		
		/*
		 * Create test image.
		 * The image consists of 3D ellipsoids of the same center but at different widths.
		 */

		// Create empty test image.
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( 60, 60, 30 );
		
		// Create an empty mamut model.
		final Model model = new Model( "pixel", "frame" );
		
		// Wrap it in a BDV source (this is the way we get images in Mastodon).
		final Source< UnsignedShortType > source = new RandomAccessibleIntervalSource<>( img, img.firstElement(), "Blank" );

		// Create an ellipsoid in this model.
		final int timepoint = 0;
		final double[] center = new double[] { 30., 30., 15. };
		final double[][] covariance = new double[ 3 ][ 3 ];
		covariance[ 0 ][ 0 ] = covariance[ 1 ][ 1 ] = 400.;
		covariance[ 2 ][ 2 ] = 225.;
		covariance[ 0 ][ 1 ] = covariance[ 1 ][ 0 ] = 40.;
		covariance[ 1 ][ 2 ] = covariance[ 2 ][ 1 ] = 40.;
		final Spot spot = model.getGraph().addVertex().init( timepoint, center, covariance );

		// Create the ellipsoid iterable over the source.
		final EllipsoidIterable< UnsignedShortType > iterable = new EllipsoidIterable<>( source );

		// Iterate inside this ellipsoid and set value to 1500.
		iterable.reset( spot );
		for ( final UnsignedShortType p : iterable )
			p.set( 1500 );

		// Shrink the spot by 1.5 (to the square for covariance).
		for ( int i = 0; i < covariance.length; i++ )
			for ( int j = 0; j < covariance[ i ].length; j++ )
				covariance[ i ][ j ] /= ( 1.5 * 1.5 );
		spot.setCovariance( covariance );

		// Iterate inside the shrunk ellipsoid and set value to 1000.
		iterable.reset( spot );
		for ( final UnsignedShortType p : iterable )
			p.set( 1000 );
		
		// Shrink again the spot by 1.5 (to the square for covariance).
		for ( int i = 0; i < covariance.length; i++ )
			for ( int j = 0; j < covariance[ i ].length; j++ )
				covariance[ i ][ j ] /= ( 1.5 * 1.5 );
		spot.setCovariance( covariance );

		// Iterate inside the shrunk ellipsoid and set value to 500.
		iterable.reset( spot );
		for ( final UnsignedShortType p : iterable )
			p.set( 500 );
		
		// Wrap the Img into a ImgPlus.
		final String name = "Test crown intensity";;
		final AxisType[] axisTypes = new AxisType[] { Axes.X, Axes.Y, Axes.Z };
		final double[] cal = new double[] { 1., 1., 1. };
		final ImgPlus< UnsignedShortType > imgPlus = new ImgPlus<>( img, name, axisTypes, cal );
		
		/*
		 * Create service to compute the crown intensity feature.
		 */

		// Wrap it into a shared BDV data (used by Mastodon).
		final SharedBigDataViewerData bdvData = FeatureTestUtils.wrapAsSharedBdvData( imgPlus );

		// Create the feature computer service.
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );

		// Pass model and image to the service.
		service.setModel( model );
		service.setSharedBdvData( bdvData );

		/*
		 * Test
		 * The spot should localize at the smallest ellipsoid.
		 * The output must = 1000.
		 */
		
		// Perform feature computation for only one feature.
		compute( service, model, SpotCrownIntensityFeature.SPEC );

		// Get results.
		final FeatureModel featureModel = model.getFeatureModel();
		final SpotCrownIntensityFeature feature = ( SpotCrownIntensityFeature ) featureModel.getFeature( SpotCrownIntensityFeature.SPEC );
		
		assertEquals( "Result must be equal 1000", 1000, (long) feature.getMean( spot, 0 ) );
		
		/*
		 * Test
		 * Expand the ellipsoid by 1.5, it should localize now at the middle ellipsoid.
		 * The output must = 1500
		 */
		
		for ( int i = 0; i < covariance.length; i++ )
			for ( int j = 0; j < covariance[ i ].length; j++ )
				covariance[ i ][ j ] *= ( 1.5 * 1.5 );
		spot.setCovariance( covariance );
		
		
		// Perform feature computation for only one feature.
		compute( service, model, SpotCrownIntensityFeature.SPEC );

		// Get results.
		final FeatureModel featureModel2 = model.getFeatureModel();
		final SpotCrownIntensityFeature feature2 = ( SpotCrownIntensityFeature ) featureModel2.getFeature( SpotCrownIntensityFeature.SPEC );
		assertEquals( "Result must be equal 1500", 1500, (long) feature2.getMean( spot, 0 ) );
		
		/*
		 * Test
		 * Expand again the ellipsoid by 1.5, it should localize now at the largest ellipsoid.
		 * The output must = 0
		 */
		
		for ( int i = 0; i < covariance.length; i++ )
			for ( int j = 0; j < covariance[ i ].length; j++ )
				covariance[ i ][ j ] *= ( 1.5 * 1.5 );
		spot.setCovariance( covariance );
		
		
		// Perform feature computation for only one feature.
		compute( service, model, SpotCrownIntensityFeature.SPEC );

		// Get results.
		final FeatureModel featureModel3 = model.getFeatureModel();
		final SpotCrownIntensityFeature feature3 = ( SpotCrownIntensityFeature ) featureModel3.getFeature( SpotCrownIntensityFeature.SPEC );
		assertEquals( "Result must be equal 0", 0, (long) feature3.getMean( spot, 0 ) );
		
		// Finish
		context.close();
	}
	
	private static final void compute( final MamutFeatureComputerService service, final Model model, final FeatureSpec< ?, ? >... featureKeys )
	{
		// Perform computation, forcing to recompte everything.
		final boolean forceRecompute = true;
		final Map< FeatureSpec< ?, ? >, Feature< ? > > map = service.compute( forceRecompute, featureKeys );
		if ( service.isCanceled() )
			return;

		// Pass results to the feature model of the model.
		final FeatureModel featureModel = model.getFeatureModel();
		featureModel.pauseListeners();
		// Clear feature we can compute
		final Collection< FeatureSpec< ?, ? > > featureSpecs = featureModel.getFeatureSpecs();
		final Collection< FeatureSpec< ?, ? > > toClear = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > featureSpec : featureSpecs )
			if ( null != service.getFeatureComputerFor( featureSpec ) )
				toClear.add( featureSpec );

		for ( final FeatureSpec< ?, ? > featureSpec : toClear )
			featureModel.clear( featureSpec );

		// Pass the feature map to the feature model.
		map.values().forEach( featureModel::declareFeature );

		featureModel.resumeListeners();
	}

}
