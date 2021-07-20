package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.mamut.feature.SpotCrownIntensityFeature;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;

public class RunningSpotCrownIntensityFeatureComputerExample
{

	public static void main( final String[] args ) throws Exception
	{
		// Create the context of this test with only 1 service.
		final Context context = new Context( MamutFeatureComputerService.class );

		// Create empty test image.
		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( 60, 60, 30 );

		// Fill it with a value.
		for ( final UnsignedShortType p : img )
			p.set( 500 );

		// Wrap the Img into a ImgPlus.
		final String name = "Test crown intensity";;
		final AxisType[] axisTypes = new AxisType[] { Axes.X, Axes.Y, Axes.Z };
		final double[] cal = new double[] { 1., 1., 1. };
		final ImgPlus< UnsignedShortType > imgPlus = new ImgPlus<>( img, name, axisTypes, cal );

		// Wrap it into a shared BDV data (used by Mastodon).
		final SharedBigDataViewerData bdvData = FeatureTestUtils.wrapAsSharedBdvData( imgPlus );

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

		// Create the feature computer service.
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );

		// Pass model and image to the service.
		service.setModel( model );
		service.setSharedBdvData( bdvData );

		// Perform feature computation for only one feature.
		compute( service, model, SpotCrownIntensityFeature.SPEC );

		// Get results.
		final FeatureModel featureModel = model.getFeatureModel();
		final SpotCrownIntensityFeature feature = ( SpotCrownIntensityFeature ) featureModel.getFeature( SpotCrownIntensityFeature.SPEC );
		System.out.println( "Spot crown intensity value for spot " + spot + ": " + feature.getMean( spot, 0 ) );

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