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

@Plugin( type = SpotSumIntensityFeatureSerializer.class )
public class SpotSumIntensityFeatureSerializer implements FeatureSerializer< SpotSumIntensityFeature, Spot >
{

	@Override
	public FeatureSpec< SpotSumIntensityFeature, Spot > getFeatureSpec()
	{
		return SpotSumIntensityFeature.SPEC;
	}

	@Override
	public void serialize( final SpotSumIntensityFeature feature, final ObjectToFileIdMap< Spot > idmap, final ObjectOutputStream oos ) throws IOException
	{
		final int nSources = feature.sums.size();
		oos.writeInt( nSources );
		for ( int i = 0; i < nSources; i++ )
			new DoublePropertyMapSerializer<>( feature.sums.get( i ) ).writePropertyMap( idmap, oos );
	}

	@Override
	public SpotSumIntensityFeature deserialize( final FileIdToObjectMap< Spot > idmap, final RefCollection< Spot > pool, final ObjectInputStream ois ) throws IOException, ClassNotFoundException
	{
		final int nSources = ois.readInt();
		final List< DoublePropertyMap< Spot > > sums = new ArrayList<>( nSources );
		for ( int i = 0; i < nSources; i++ )
		{
			final DoublePropertyMap< Spot > medianMap = new DoublePropertyMap<>( pool, Double.NaN );
			new DoublePropertyMapSerializer<>( medianMap ).readPropertyMap( idmap, ois );
			sums.add( medianMap );
		}
		return new SpotSumIntensityFeature( sums );
	}

}
