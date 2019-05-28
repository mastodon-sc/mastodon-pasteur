package org.mastodon.mamut.feature;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.io.FeatureSerializer;
import org.mastodon.io.FileIdToObjectMap;
import org.mastodon.io.ObjectToFileIdMap;
import org.mastodon.io.properties.DoublePropertyMapSerializer;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

@Plugin( type = SpotMedianIntensityFeatureSerializer.class )
public class SpotMedianIntensityFeatureSerializer implements FeatureSerializer< SpotMedianIntensityFeature, Spot >
{

	@Override
	public FeatureSpec< SpotMedianIntensityFeature, Spot > getFeatureSpec()
	{
		return SpotMedianIntensityFeature.SPEC;
	}

	@Override
	public void serialize( final SpotMedianIntensityFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final int nSources = feature.medians.size();
		oos.writeInt( nSources );
		for ( int i = 0; i < nSources; i++ )
			new DoublePropertyMapSerializer<>( feature.medians.get( i ) ).writePropertyMap( idmap, oos );
	}

	@Override
	public SpotMedianIntensityFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final int nSources = ois.readInt();
		final List< DoublePropertyMap< Spot > > medians = new ArrayList<>( nSources );
		for ( int i = 0; i < nSources; i++ )
		{
			final DoublePropertyMap< Spot > medianMap = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( medianMap ).readPropertyMap( idmap, ois );
			medians.add( medianMap );
		}
		return new SpotMedianIntensityFeature( medians );
	}

}
