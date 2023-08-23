package org.mastodon.mamut.nearest.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mastodon.app.ui.AbstractStyleManagerYaml;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.yaml.snakeyaml.Yaml;

public class NearestObjectStatModelManager extends AbstractStyleManagerYaml< NearestObjectStatModelManager, NearestObjectStatModel >
{

	private static final String STYLE_FILE = System.getProperty( "user.home" ) + "/.mastodon/nearestneighborstatsettings.yaml";

	public NearestObjectStatModelManager()
	{
		this( true );
	}

	public NearestObjectStatModelManager( final boolean loadStyles )
	{
		if ( loadStyles )
			loadStyles();
	}

	@Override
	public void saveStyles()
	{
		saveStyles( STYLE_FILE );
	}

	@Override
	protected List< NearestObjectStatModel > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( new ArrayList<>( NearestObjectStatModel.defaults ) );
	}

	public void loadStyles()
	{
		loadStyles( STYLE_FILE );
	}

	@Override
	protected Yaml createYaml()
	{
		return NearestObjectStatModelIO.createYaml();
	}
}
