package org.mastodon.io.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.mastodon.detection.mamut.DetectionQualityFeature;
import org.mastodon.project.MamutProject;
import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.algorithm.Algorithm;

public class CSVImporter implements Algorithm
{

	private final String filePath;

	private String errorMessage;

	private final String xColumnName;

	private final String yColumnName;

	private final String zColumnName;

	private final String frameColumnName;

	private final String qualityColumn;

	private final String idColumn;

	private final double radius;

	private final double xOrigin;

	private final double yOrigin;

	private final double zOrigin;

	private final Model model;

	private final CSVFormat csvFormat = CSVFormat.EXCEL
			.withHeader()
			.withCommentMarker( '#' );

	private CSVImporter(
			final Model model,
			final String filePath,
			final double radius,
			final String xColumnName,
			final String yColumnName,
			final String zColumnName,
			final String frameColumnName,
			final String qualityColumName,
			final String idColumnName,
			final double xOrigin,
			final double yOrigin,
			final double zOrigin )
	{
		this.model = model;
		this.filePath = filePath;
		this.radius = radius;
		this.xColumnName = xColumnName;
		this.yColumnName = yColumnName;
		this.zColumnName = zColumnName;
		this.frameColumnName = frameColumnName;
		this.qualityColumn = qualityColumName;
		this.idColumn = idColumnName;
		this.xOrigin = xOrigin;
		this.yOrigin = yOrigin;
		this.zOrigin = zOrigin;
	}

	public static Builder create()
	{
		return new Builder();
	}

	@Override
	public boolean checkInput()
	{
		return true;
	}

	@Override
	public boolean process()
	{
		/*
		 * Open and parse file.
		 */

		try (Reader in = new FileReader( filePath );
				CSVParser records = csvFormat.parse( in );)
		{

			final Map< String, Integer > headerMap = records.getHeaderMap();

			/*
			 * Parse mandatory headers.
			 */

			final Integer xcol = headerMap.get( xColumnName );
			if ( null == xcol )
			{
				errorMessage = "Could not find X column in " + filePath + ". Was looking for " + xColumnName + ".";
				return false;
			}

			final Integer ycol = headerMap.get( yColumnName );
			if ( null == ycol )
			{
				errorMessage = "Could not find Y column in " + filePath + ". Was looking for " + yColumnName + ".";
				return false;
			}

			final Integer zcol = headerMap.get( zColumnName );
			if ( null == zcol )
			{
				errorMessage = "Could not find Z column in " + filePath + ". Was looking for " + zColumnName + ".";
				return false;
			}

			final Integer framecol = headerMap.get( frameColumnName );
			if ( null == framecol )
			{
				errorMessage = "Could not find frame column in " + filePath + ". Was looking for " + frameColumnName + ".";
				return false;
			}

			/*
			 * Parse optional headers.
			 */

			final ModelGraph graph = model.getGraph();

			final DetectionQualityFeature qualityFeature = new DetectionQualityFeature( graph.vertices().getRefPool() );
			Integer qualitycol = null;
			if ( null != qualityColumn && !qualityColumn.isEmpty() )
				qualitycol = headerMap.get( qualityColumn );

			Integer idcol = null;
			if ( null != idColumn && !idColumn.isEmpty() )
				idcol = headerMap.get( idColumn );

			/*
			 * Iterate over records.
			 */

			final WriteLock lock = graph.getLock().writeLock();
			lock.lock();
			final Spot vref = graph.vertexRef();
			final double[] pos = new double[ 3 ];
			try
			{
				for ( final CSVRecord record : records )
				{
					try
					{
						pos[ 0 ] = Double.parseDouble( record.get( xcol ) ) + xOrigin;
						pos[ 1 ] = Double.parseDouble( record.get( ycol ) ) + yOrigin;
						pos[ 2 ] = Double.parseDouble( record.get( zcol ) ) + zOrigin;
						final int t = Integer.parseInt( record.get( framecol ) );

						final Spot spot = graph.addVertex( vref ).init( t, pos, radius );
						if ( null != idcol )
						{
							final int id = Integer.parseInt( record.get( idcol ) );
							spot.setLabel( "" + id );
						}
						double q = 1.;
						if ( null != qualitycol )
						{
							q = Double.parseDouble( record.get( qualitycol ) );
							qualityFeature.set( spot, q );
						}
					}
					catch ( final NumberFormatException nfe )
					{
						nfe.printStackTrace();
						System.out.println( "Could not parse line " + record.getRecordNumber() + ". Malformed number, skipping.\n" + nfe.getMessage() );
						continue;
					}

				}

				if ( null != qualitycol )
					model.getFeatureModel().declareFeature( qualityFeature );
			}
			finally
			{
				lock.unlock();
			}
		}
		catch ( final FileNotFoundException e )
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
			return false;
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
			errorMessage = e.getMessage();
			return false;
		}

		/*
		 * Return.
		 */

		return true;
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public static class Builder
	{

		private Model model;

		private String csvFilePath;

		private double radius = Double.NaN;

		private String xColumnName;

		private String yColumnName;

		private String zColumnName;

		private String frameColumnName;

		private String qualityColumnName;

		private String idColumnName;

		private double xOrigin = 0.;

		private double yOrigin = 0.;

		private double zOrigin = 0.;

		public Builder model( final Model model )
		{
			this.model = model;
			return this;
		}

		public Builder csvFilePath( final String csvFilePath )
		{
			this.csvFilePath = csvFilePath;
			return this;
		}

		public Builder radius( final double radius )
		{
			this.radius = radius;
			return this;
		}

		public Builder xColumnName( final String xColumnName )
		{
			this.xColumnName = xColumnName;
			return this;
		}

		public Builder yColumnName( final String yColumnName )
		{
			this.yColumnName = yColumnName;
			return this;
		}

		public Builder zColumnName( final String zColumnName )
		{
			this.zColumnName = zColumnName;
			return this;
		}

		public Builder frameColumnName( final String frameColumnName )
		{
			this.frameColumnName = frameColumnName;
			return this;
		}

		public Builder qualityColumnName( final String qualityColumnName )
		{
			this.qualityColumnName = qualityColumnName;
			return this;
		}

		public Builder idColumnName( final String idColumnName )
		{
			this.idColumnName = idColumnName;
			return this;
		}

		public Builder xOrigin( final double xOrigin )
		{
			this.xOrigin = xOrigin;
			return this;
		}

		public Builder yOrigin( final double yOrigin )
		{
			this.yOrigin = yOrigin;
			return this;
		}

		public Builder zOrigin( final double zOrigin )
		{
			this.zOrigin = zOrigin;
			return this;
		}

		public CSVImporter get()
		{
			final StringBuilder errorMessage = new StringBuilder("Invalid CSV importer definition:\n");
			boolean valid = true;

			if (model == null) {
				errorMessage.append(" - Missing model.\n");
				valid = false;
			}
			if (csvFilePath == null) {
				errorMessage.append(" - Missing CSV file path.\n");
				valid = false;
			}
			if (Double.isNaN(radius)) {
				errorMessage.append(" - Missing spot radius.\n");
				valid = false;
			}
			if (xColumnName == null) {
				errorMessage.append(" - Missing X column name.\n");
				valid = false;
			}
			if (yColumnName == null) {
				errorMessage.append(" - Missing Y column name.\n");
				valid = false;
			}
			if (zColumnName == null) {
				errorMessage.append(" - Missing Z column name.\n");
				valid = false;
			}
			if (frameColumnName == null) {
				errorMessage.append(" - Missing frame column name.\n");
				valid = false;
			}

			if (!valid) {
				throw new IllegalArgumentException(errorMessage.toString());
			}

			return new CSVImporter(
					model,
					csvFilePath,
					radius,
					xColumnName,
					yColumnName,
					zColumnName,
					frameColumnName,
					qualityColumnName,
					idColumnName,
					xOrigin,
					yOrigin,
					zOrigin );
		}
	}

	/*
	 * MAIN METHOD
	 */

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final WindowManager wm = new WindowManager( new Context() );

		final String bdvFile = "samples/190123_crop_channel_1.xml";
		final MamutProject project = new MamutProject( null, new File( bdvFile ) );
		wm.getProjectManager().open( project );
		new MainWindow( wm ).setVisible( true );

		final Model model = wm.getAppModel().getModel();
		final String csvFilePath = "samples/forTrackMate.csv";
		final CSVImporter importer = CSVImporter.create()
				.model( model )
				.csvFilePath( csvFilePath )
				.radius( 2. )
				.xColumnName( "x" )
				.yColumnName( "y" )
				.zColumnName( "z" )
				.frameColumnName( "t" )
				.idColumnName( "index" )
				.get();

		System.out.println( "Starting import" );
		final long start = System.currentTimeMillis();
		if ( !importer.checkInput() || !importer.process() )
		{
			System.out.println( importer.getErrorMessage() );
			return;
		}
		System.out.println( String.format( "Finished import of %d spots in %.1f s.",
				model.getGraph().vertices().size(),
				( System.currentTimeMillis() - start ) / 1000. ) );
	}
}
