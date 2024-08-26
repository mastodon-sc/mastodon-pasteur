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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.mastodon.RefPool;
import org.mastodon.collection.IntRefMap;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntScalarFeature;
import org.mastodon.feature.IntScalarFeatureSerializer;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.mamut.io.importer.ModelImporter;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.tracking.mamut.detection.DetectionQualityFeature;
import org.mastodon.ui.coloring.GlasbeyLut;
import org.mastodon.util.TagSetUtils;
import org.scijava.plugin.Plugin;
import org.scijava.util.VersionUtils;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import net.imglib2.algorithm.Algorithm;

public class CSVImporter extends ModelImporter implements Algorithm
{

	public static final String PLUGIN_VERSION = VersionUtils.getVersion( CSVImporter.class );

	private final String filePath;

	private String errorMessage;

	private final String xColumnName;

	private final String yColumnName;

	private final String zColumnName;

	private final String frameColumnName;

	private final String qualityColumnName;

	private final String radiusColumnName;

	private final String idColumnName;

	private final String parentIdColumnName;

	private final String labelColumnName;

	private final String tagColumnName;

	private final double radius;

	private final double xOrigin;

	private final double yOrigin;

	private final double zOrigin;

	private final Model model;

	private char separator;

	private CSVImporter(
			final Model model,
			final String filePath,
			final char separator,
			final double radius,
			final String xColumnName,
			final String yColumnName,
			final String zColumnName,
			final String frameColumnName,
			final String qualityColumName,
			final String radiusColumnName,
			final String idColumnName,
			final String parentIdColumnName,
			final String labelColumnName,
			final String tagColumnName,
			final double xOrigin,
			final double yOrigin,
			final double zOrigin )
	{
		super( model );
		this.model = model;
		this.filePath = filePath;
		this.separator = separator;
		this.radius = radius;
		this.xColumnName = xColumnName;
		this.yColumnName = yColumnName;
		this.zColumnName = zColumnName;
		this.frameColumnName = frameColumnName;
		this.qualityColumnName = qualityColumName;
		this.radiusColumnName = radiusColumnName;
		this.idColumnName = idColumnName;
		this.parentIdColumnName = parentIdColumnName;
		this.labelColumnName = labelColumnName;
		this.tagColumnName = tagColumnName;
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
		if ( separator == '\0' )
		{
			try
			{
				separator = AutoDetectCSVSeparator.autoDetect( filePath );
			}
			catch ( final IOException e1 )
			{
				separator = ',';
			}
		}

		final CSVParser parser =
				new CSVParserBuilder()
						.withSeparator( separator )
						.withIgnoreQuotations( true )
						.build();
		try
		{
			new CSVReaderBuilder( new FileReader( filePath ) )
					.withCSVParser( parser )
					.build();
		}
		catch ( final FileNotFoundException e )
		{
			errorMessage = "Could not find file: " + filePath;
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean process()
	{
		/*
		 * Open and parse file.
		 */

		if ( separator == '\0' )
		{
			try
			{
				separator = AutoDetectCSVSeparator.autoDetect( filePath );
			}
			catch ( final IOException e1 )
			{
				separator = ',';
			}
		}

		final CSVParser parser =
				new CSVParserBuilder()
						.withSeparator( separator )
						.withIgnoreQuotations( true )
						.build();
		try (final CSVReader reader = new CSVReaderBuilder( new FileReader( filePath ) )
				.withCSVParser( parser )
				.build())
		{
			final Iterator< String[] > it = reader.iterator();

			/*
			 * Parse first line and reads it as the header of the file.
			 */

			if ( !it.hasNext() )
			{
				errorMessage = "CSV file is empty.";
				return false;
			}

			final String[] firstLine = it.next();
			final Map< String, Integer > headerMap = new HashMap<>( firstLine.length );
			for ( int i = 0; i < firstLine.length; i++ )
			{
				final String cleanKey = firstLine[ i ].trim().replaceAll( "\\p{C}", "" );
				headerMap.put( cleanKey, Integer.valueOf( i ) );
			}

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

			Integer qualitycol = null;
			if ( null != qualityColumnName && !qualityColumnName.isEmpty() )
				qualitycol = headerMap.get( qualityColumnName );

			final DetectionQualityFeature qualityFeature = null == qualitycol
					? null
					: DetectionQualityFeature.getOrRegister( model.getFeatureModel(), graph.vertices().getRefPool() );

			Integer radiuscol = null;
			if ( null != radiusColumnName && !radiusColumnName.isEmpty() )
				radiuscol = headerMap.get( radiusColumnName );

			Integer idcol = null;
			if ( null != idColumnName && !idColumnName.isEmpty() )
				idcol = headerMap.get( idColumnName );

			final OriginalIdFeature originalIdFeature = null == idcol
					? null
					: OriginalIdFeature.getOrRegister( model.getFeatureModel(), graph.vertices().getRefPool() );

			Integer parentIdcol = null;
			if ( null != parentIdColumnName && !parentIdColumnName.isEmpty() )
				parentIdcol = headerMap.get( parentIdColumnName );

			Integer labelcol = null;
			if ( null != labelColumnName && !labelColumnName.isEmpty() )
				labelcol = headerMap.get( labelColumnName );

			Integer tagcol = null;
			if ( null != tagColumnName && !tagColumnName.isEmpty() )
				tagcol = headerMap.get( tagColumnName );

			TagSetStructure.TagSet importedTagSet = null;
			if ( null != tagcol )
				importedTagSet = parseTagsFromFile( parser, tagcol );

			IntRefMap< Spot > spotMap = new IntRefHashMap<>( model.getGraph().vertices().getRefPool(), -1 );

			/*
			 * Iterate over the rest of lines.
			 */

			final WriteLock lock = graph.getLock().writeLock();
			lock.lock();
			final Spot vref = graph.vertexRef();
			final Spot parentVertexRef = graph.vertexRef();
			final Link edgeRef = graph.edgeRef();
			final double[] pos = new double[ 3 ];
			startImport();
			if ( null != tagcol )
				model.getTagSetModel().pauseListeners();

			try
			{
				int lineNumber = 1;
				while ( it.hasNext() )
				{
					final String[] record = it.next();
					lineNumber++;

					try
					{
						pos[ 0 ] = Double.parseDouble( record[ xcol ].trim() ) + xOrigin;
						pos[ 1 ] = Double.parseDouble( record[ ycol ].trim() ) + yOrigin;
						pos[ 2 ] = Double.parseDouble( record[ zcol ].trim() ) + zOrigin;
						final int t = Integer.parseInt( record[ framecol ].trim() );

						double r = radius;
						if ( null != radiuscol )
							r = Double.parseDouble( record[ radiuscol ].trim() );

						final Spot spot = graph.addVertex( vref ).init( t, pos, r );
						if ( null != idcol )
						{
							final int id = Integer.parseInt( record[ idcol ].trim() );
							originalIdFeature.set( spot, id );
							if ( null != parentIdcol )
								spotMap.put( id, spot );
							if ( null == labelcol )
								spot.setLabel( "" + id );
						}

						if ( null != labelcol )
						{
							String label = record[ labelcol ].trim();
							spot.setLabel( label );
						}

						double q = 1.;
						if ( null != qualitycol )
						{
							q = Double.parseDouble( record[ qualitycol ].trim() );
							qualityFeature.set( spot, q );
						}

						if ( null != parentIdcol )
						{
							final int parentId = Integer.parseInt( record[ parentIdcol ].trim() );
							final Spot parent = spotMap.get( parentId, motherVertexRef );
							if ( parent != null )
								model.getGraph().addEdge( parent, spot, edgeRef ).init();
						}

						if ( null != tagcol )
						{
							String label = record[ tagcol ].trim();
							TagSetStructure.Tag tag = TagSetUtils.findTag( importedTagSet, label );
							TagSetUtils.tagSpot( model, importedTagSet, tag, spot );
							TagSetUtils.tagLinks( model, importedTagSet, tag, spot.incomingEdges() );
						}
					}
					catch ( final NumberFormatException nfe )
					{
						nfe.printStackTrace();
						System.out.println( "Could not parse line " + lineNumber + ". Malformed number, skipping.\n" + nfe.getMessage() );
						continue;
					}
				}
			}
			finally
			{
				lock.unlock();
				graph.releaseRef( vref );
				graph.releaseRef( parentVertexRef );
				graph.releaseRef( edgeRef );
				if ( null != tagcol )
					model.getTagSetModel().resumeListeners();
				finishImport();
			}
		}
		catch ( final FileNotFoundException e )
		{
			errorMessage = "Cannot find file " + filePath;
			e.printStackTrace();
			return false;
		}
		catch ( final IOException e )
		{
			errorMessage = "Error reading file " + filePath;
			e.printStackTrace();
			return false;
		}

		/*
		 * Return.
		 */

		return true;
	}

	private TagSetStructure.TagSet parseTagsFromFile( final CSVParser parser, int labelcol )
	{
		try (final CSVReader readerTags = new CSVReaderBuilder( new FileReader( filePath ) )
				.withCSVParser( parser )
				.build())
		{
			Iterator< String[] > csvIterator = readerTags.iterator();

			Set< String > tags = new HashSet<>();
			csvIterator.next();
			while ( csvIterator.hasNext() )
			{
				final String[] line = csvIterator.next();
				String tag = line[ labelcol ].trim();
				tags.add( tag );
			}
			GlasbeyLut glasbeyLut = new GlasbeyLut();
			List< Pair< String, Integer > > tagsAndColors =
					tags.stream().map( tag -> Pair.of( tag, glasbeyLut.next() ) ).collect( Collectors.toList() );

			return TagSetUtils.addNewTagSetToModel( model, "Imported Tags", tagsAndColors );
		}
		catch ( final IOException e )
		{
			errorMessage = "Error reading file " + filePath;
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getErrorMessage()
	{
		return errorMessage;
	}

	public static class OriginalIdFeature extends IntScalarFeature< Spot >
	{

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< OriginalIdFeature, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						HELP_STRING,
						OriginalIdFeature.class,
						Spot.class,
						Multiplicity.SINGLE,
						new FeatureProjectionSpec( KEY, Dimension.NONE ) );
			}
		}

		public static final Spec SPEC = new Spec();

		public static final String KEY = "Original id";

		private static final String HELP_STRING = "Store the id specified in the file that was imported.";

		private OriginalIdFeature( final RefPool< Spot > pool )
		{
			super( KEY, Dimension.NONE, Dimension.NONE_UNITS, pool );
		}

		private OriginalIdFeature( final IntPropertyMap< Spot > map )
		{
			super( KEY, Dimension.NONE, Dimension.NONE_UNITS, map );
		}

		public static final OriginalIdFeature getOrRegister( final FeatureModel featureModel, final RefPool< Spot > pool )
		{
			final OriginalIdFeature feature = new OriginalIdFeature( pool );
			final OriginalIdFeature retrieved = ( OriginalIdFeature ) featureModel.getFeature( feature.getSpec() );
			if ( null == retrieved )
			{
				featureModel.declareFeature( feature );
				return feature;
			}
			return retrieved;
		}

		@Override
		public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
		{
			return SPEC;
		}
	}

	@Plugin( type = FeatureSerializer.class )
	public static class OriginalIdFeatureSerializer extends IntScalarFeatureSerializer< OriginalIdFeature, Spot >
	{

		@Override
		public OriginalIdFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
		{
			final DeserializedStruct struct = read( idmap, pool, ois );
			return new OriginalIdFeature( struct.map );
		}

		@Override
		public FeatureSpec< OriginalIdFeature, Spot > getFeatureSpec()
		{
			return OriginalIdFeature.SPEC;
		}
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

		private String radiusColumnName;

		private String idColumnName;

		private String parentIdColumnName;

		private String labelColumnName;

		private String tagColumnName;

		private double xOrigin = 0.;

		private double yOrigin = 0.;

		private double zOrigin = 0.;

		private char separator = '\0';

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

		public Builder radiusColumnName( final String radiusColumnName )
		{
			this.radiusColumnName = radiusColumnName;
			return this;
		}

		public Builder idColumnName( final String idColumnName )
		{
			this.idColumnName = idColumnName;
			return this;
		}

		public Builder parentIdColumnName( final String parentIdColumnName )
		{
			this.parentIdColumnName = parentIdColumnName;
			return this;
		}

		public Builder labelColumnName( final String labelColumnName )
		{
			this.labelColumnName = labelColumnName;
			return this;
		}

		public Builder tagColumnName( final String tagColumnName )
		{
			this.tagColumnName = tagColumnName;
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

		/**
		 * Specifies the separator character to use to read the CSV file. If it
		 * is left unset, or set to the character '\0', then the separator is
		 * automatically determined by inspecting the first few lines of the CSV
		 * file.
		 *
		 * @param separator
		 *            the separator to use.
		 * @return this builder.
		 */
		public Builder separator( final char separator )
		{
			this.separator = separator;
			return this;
		}

		public CSVImporter get()
		{
			final StringBuilder errorMessage = new StringBuilder( "Invalid CSV importer definition:\n" );
			boolean valid = true;

			if ( model == null )
			{
				errorMessage.append( " - Missing model.\n" );
				valid = false;
			}
			if ( csvFilePath == null )
			{
				errorMessage.append( " - Missing CSV file path.\n" );
				valid = false;

			}
			if ( Double.isNaN( radius ) )
			{
				errorMessage.append( " - Missing spot radius.\n" );
				valid = false;
			}
			if ( xColumnName == null )
			{
				errorMessage.append( " - Missing X column name.\n" );
				valid = false;
			}
			if ( yColumnName == null )
			{
				errorMessage.append( " - Missing Y column name.\n" );
				valid = false;
			}
			if ( zColumnName == null )
			{
				errorMessage.append( " - Missing Z column name.\n" );
				valid = false;
			}
			if ( frameColumnName == null )
			{
				errorMessage.append( " - Missing frame column name.\n" );
				valid = false;
			}

			if ( !valid )
				throw new IllegalArgumentException( errorMessage.toString() );

			return new CSVImporter(
					model,
					csvFilePath,
					separator,
					radius,
					xColumnName,
					yColumnName,
					zColumnName,
					frameColumnName,
					qualityColumnName,
					radiusColumnName,
					idColumnName,
					parentIdColumnName,
					labelColumnName,
					tagColumnName,
					xOrigin,
					yOrigin,
					zOrigin );
		}
	}
}
