package org.mastodon.mamut.feature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotMedianIntensityFeature;
import org.mastodon.mamut.io.csv.CSVImporter;
import org.mastodon.mamut.io.csv.CSVImporterTest;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

public class SpotMedianIntensityFeatureComputerTest
{

	@Test
	public void test() throws IOException, SpimDataException
	{
		/*
		 * Load image and model.
		 */

		final URL urlFile = CSVImporterTest.class.getResource( "TestMedianCSVImport.xml" );
		final String bdvFilePath = urlFile.getPath();

		final URL urlCSV = CSVImporterTest.class.getResource( "TestMedianCSVImport.csv" );
		final String csvFilePath = urlCSV.getPath();

		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( bdvFilePath );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData(
				bdvFilePath,
				spimData,
				ViewerOptions.options(),
				() -> {} );

		final MamutProject project = new MamutProject( null, new File( bdvFilePath ) );
		final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );

		final CSVImporter importer = CSVImporter.create()
				.model( model )
				.csvFilePath( csvFilePath )
				.radius( 3. )
				.xColumnName( "POSITION_X" )
				.yColumnName( "POSITION_Y" )
				.zColumnName( "POSITION_Z" )
				.frameColumnName( "FRAME" )
				.idColumnName( "ID" )
				.qualityColumnName( "QUALITY" )
				.get();
		if ( !importer.checkInput() || !importer.process() )
			fail( importer.getErrorMessage() );

		/*
		 * Compute median value.
		 */
		final Context context = new Context( MamutFeatureComputerService.class );
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( sharedBdvData );
		System.out.println( "Computing spot intensity..." );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute(
				SpotMedianIntensityFeature.SPEC );
		final SpotMedianIntensityFeature feature = ( SpotMedianIntensityFeature ) features.get( SpotMedianIntensityFeature.SPEC );

		/*
		 * Compare to expected values. I got these by applying a 3D median
		 * filter with a radius of 3 to the raw image in Fiji.
		 */
		
		final int[] expectedMedian = new int[] {
				8738,
				30583,
				17476 };

		for ( final Spot spot : model.getGraph().vertices() )
		{
			assertEquals(
					"Unexpected median value for spot " + spot,
					expectedMedian[ spot.getInternalPoolIndex() ],
					( int ) feature.medians.get( 0 ).getDouble( spot ) );
		}
	}
}
