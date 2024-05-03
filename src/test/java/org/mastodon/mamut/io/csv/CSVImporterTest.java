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
package org.mastodon.mamut.io.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.tracking.mamut.detection.DetectionQualityFeature;

import mpicbg.spim.data.SpimDataException;

public class CSVImporterTest
{

	@Test
	public void test() throws IOException, SpimDataException
	{
		final URL urlCSV = CSVImporterTest.class.getResource( "TestMedianCSVImport.csv" );
		final String csvFilePath = urlCSV.getPath();

		final Model model = new Model();

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
