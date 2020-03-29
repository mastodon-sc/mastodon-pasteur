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
import org.mastodon.io.csv.CSVImporter;
import org.mastodon.io.csv.CSVImporterTest;
import org.mastodon.project.MamutProject;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

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

		final WindowManager wm = new WindowManager( new Context() );
		final MamutProject project = new MamutProject( null, new File( bdvFilePath ) );
		wm.getProjectManager().open( project );

		final Model model = wm.getAppModel().getModel();
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

		final Context context = wm.getContext();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( wm.getAppModel().getSharedBdvData() );
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
