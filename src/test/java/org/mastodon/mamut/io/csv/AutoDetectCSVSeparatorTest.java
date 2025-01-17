/*-
 * #%L
 * mastodon-pasteur
 * %%
 * Copyright (C) 2019 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.io.StringReader;

import org.junit.Test;

public class AutoDetectCSVSeparatorTest
{

	@Test
	public void testAutoDetect()
	{
		final String normal = "ID  ,  QUALITY  ,  POSITION_X  ,  POSITION_Y  ,  POSITION_Z  ,  FRAME  ,  RADIUS  ,  MEDIAN_INTENSITY\n" +
				"0  ,  50.943  ,  11.000  ,  11.000  ,  9.000  ,  0  ,  4.500  ,  51\n" +
				"1  ,  206.877  ,  30.000  ,  31.000  ,  9.000  ,  0  ,  4.500  ,  119\n" +
				"2  ,  130.664  ,  36.000  ,  12.000  ,  9.000  ,  0  ,  4.500  ,  197";

		final String tab = "ID  	  QUALITY  	  POSITION_X  	  POSITION_Y  	  POSITION_Z  	  FRAME  	  RADIUS  	  MEDIAN_INTENSITY\n" +
				"0  	  50.943  	  11.000  	  11.000  	  9.000  	  0  	  4.500  	  51\n" +
				"1  	  206.877  	  30.000  	  31.000  	  9.000  	  0  	  4.500  	  119\n" +
				"2  	  130.664  	  36.000  	  12.000  	  9.000  	  0  	  4.500  	  197";

		final String excel = "ID;QUALITY;POSITION_X;POSITION_Y;POSITION_Z;FRAME;RADIUS;MEDIAN_INTENSITY\n" +
				"0;50.943;11.000;11.000;9.000;0;4.500;51\n" +
				"1;206.877;30.000;31.000;9.000;0;4.500;119\n" +
				"2;130.664;36.000;12.000;9.000;0;4.500;197";

		final String frenchExcel =
				"0,0;50,943;11,000;11,000;9,000;0,0;4,500;51,0\n" +
						"1,0;206,877;30,000;31,000;9,000;0,0;4,500;119,0\n" +
						"2,0;130,664;36,000;12,000;9,000;0,0;4,500;197,0";

		final String frenchExcelSingleLine = "0,0;50,943;11,000;11,000;9,000;0,0;4,500;51,0";

		final String singleLine = "2;130.664;36.000;12.000;9.000;0;4.500;197";

		final String[] toTest = new String[] {
				normal,
				tab,
				excel,
				singleLine,
				frenchExcel,
				frenchExcelSingleLine };
		final char[] expectedSeparators = new char[] {
				',',
				'\t',
				';',
				';',
				';',
				';'
		};

		for ( int i = 0; i < toTest.length; i++ )
		{
			final String string = toTest[ i ];
			try
			{
				final char separator = AutoDetectCSVSeparator.autoDetect( new StringReader( string ) );
				assertEquals( "Unexpected separator detected.", expectedSeparators[ i ], separator );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
				fail( e.getMessage() );
			}
		}
	}
}
