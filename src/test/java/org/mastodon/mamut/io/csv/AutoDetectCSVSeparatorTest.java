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
