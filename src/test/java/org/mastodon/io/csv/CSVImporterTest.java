package org.mastodon.io.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.mastodon.detection.mamut.DetectionQualityFeature;
import org.mastodon.project.MamutProject;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class CSVImporterTest
{

	@Test
	public void test() throws IOException, SpimDataException
	{
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

		assertEquals( "Incorrect number of spots imported.", 3, model.getGraph().vertices().size() );

		final double[] expectedX = new double[] { 11., 30., 36. };
		final double[] expectedY = new double[] { 11., 31., 12. };
		final double[] expectedZ = new double[] { 9., 9., 9. };
		final double[] expectedQ = new double[] { 50.943, 206.877, 130.664 };
		
		final DetectionQualityFeature quality  = DetectionQualityFeature.getOrRegister( model.getFeatureModel(), model.getGraph().vertices().getRefPool() );
		model.getGraph().vertices().forEach( s -> {
			final int id = s.getInternalPoolIndex();
			assertEquals( "Imported incorrect value for X.", expectedX[ id ], s.getDoublePosition( 0 ), 1e-3 );
			assertEquals( "Imported incorrect value for Y.", expectedY[ id ], s.getDoublePosition( 1 ), 1e-3 );
			assertEquals( "Imported incorrect value for Z.", expectedZ[ id ], s.getDoublePosition( 2 ), 1e-3 );
			assertEquals( "Imported incorrect value for quality.", expectedQ[ id ], quality.value( s ), 1e-3 );
		} );
	}

}