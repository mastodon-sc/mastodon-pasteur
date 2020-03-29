package org.mastodon.io.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Utility that tries to detect the separator of a CSV file.
 * <p>
 * Its logic is the following: If we have several lines, the correct separator
 * should be present the same amount of times at each line. So we parse
 * {@value #MAX_LINES} to test that it is the case.
 * <p>
 * If we have one line, we return the most frequent separator.
 * <p>
 * Finally, there is the case of wicked French CSV files with only numbers. They
 * use the ',' as decimal separator and ';' as CSV separator. We want the
 * latter. In that case if there is ',' occurs N times, there will be N-1 ';'.
 * 
 * @author Jean-Yves Tinevez
 */
public class AutoDetectCSVSeparator
{

	private final static int MAX_LINES = 7;

	public static char autoDetect( final String filePath ) throws IOException
	{
		return autoDetect( new FileReader( filePath ) );
	}

	public static char autoDetect( final Reader in ) throws IOException
	{
		// Sorted by decreasing priority.
		final String separators = ",\t;|:";
		final int commaIndex = separators.indexOf( ',' );
		final int semiColonIndex = separators.indexOf( ';' );

		/*
		 * Collect occurrences.
		 */
		final int[][] occurrences = new int[ MAX_LINES ][ separators.length() ];
		int currentLine = -1;
		try (BufferedReader reader = new BufferedReader( in ))
		{
			String line;
			while ( ( line = reader.readLine() ) != null && ++currentLine < MAX_LINES )
			{
				final int[] occs = occurrences[ currentLine ];
				for ( final char ch : line.toCharArray() )
				{
					final int sepIndex = separators.indexOf( ch );
					if ( sepIndex >= 0 )
						occs[ sepIndex ]++;
				}
			}
		}

		/*
		 * Try to guess from a single line.
		 */
		if ( currentLine == 1 )
		{
			final int[] occs = occurrences[ 0 ];

			/*
			 * Wicked French CSV files with only number case. They use the ','
			 * as decimal separator and ';' as CSV separator. We want the
			 * latter. In that case if there is N ',', there will be N-1 ';'.
			 */
			if ( occs[ commaIndex ] == occs[ semiColonIndex ] + 1 )
				return ';';

			/*
			 * If not, we take the most frequent, giving priority to the
			 * separators that are early in the list.
			 */
			int maxOccs = -1;
			int maxIndex = 0;
			for ( int i = 0; i < occs.length; i++ )
			{
				if ( occs[ i ] > maxOccs )
				{
					maxOccs = occs[ i ];
					maxIndex = i;
				}
			}
			return separators.charAt( maxIndex );
		}

		/*
		 * Multiple lines. We try to identify for what separators we have the
		 * same number of occurrences over all lines. Larger than 0 of course.
		 */
		final int[] commonOccurrences = new int[ separators.length() ];
		for ( int sep = 0; sep < separators.length(); sep++ )
		{
			final int commonOccurrence = occurrences[ 0 ][ sep ];
			for ( int line = 1; line < Math.min( occurrences.length, currentLine ); line++ )
			{
				final int occ = occurrences[ line ][ sep ];
				if ( occ != commonOccurrence || occ == 0 )
				{
					commonOccurrences[ sep ] = -1;
					break;
				}
			}
			commonOccurrences[ sep ] = commonOccurrence;
		}

		/*
		 * Again the wicked French CSV files with only number case. They use the
		 * ',' as decimal separator and ';' as CSV separator. We want the
		 * latter. In that case if there is N ',', there will be N-1 ';'.
		 */
		if ( commonOccurrences[ commaIndex ] == commonOccurrences[ semiColonIndex ] + 1 )
			return ';';

		/*
		 * Otherwise we take the most frequent, giving priority to the
		 * separators that are early in the list.
		 */
		int maxOccs = -1;
		int maxIndex = 0;
		for ( int i = 0; i < commonOccurrences.length; i++ )
		{
			if ( commonOccurrences[ i ] > maxOccs )
			{
				maxOccs = commonOccurrences[ i ];
				maxIndex = i;
			}
		}
		return separators.charAt( maxIndex );
	}
}
