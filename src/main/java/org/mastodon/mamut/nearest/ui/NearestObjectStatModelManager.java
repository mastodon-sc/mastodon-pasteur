package org.mastodon.mamut.nearest.ui;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.io.IOUtils;
import org.mastodon.mamut.nearest.NearestObjectStatModel;
import org.mastodon.views.bdv.overlay.RenderSettings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import bdv.ui.settings.style.AbstractStyleManager;

public class NearestObjectStatModelManager extends AbstractStyleManager< NearestObjectStatModelManager, NearestObjectStatModel >
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

	public void saveStyles( final String filename )
	{
		try
		{
			IOUtils.mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( selectedStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
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

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( NearestObjectStatModel::getName ).collect( Collectors.toSet() );
		try
		{
			final FileReader input = new FileReader( filename );
			final Yaml yaml = createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
				}
				else if ( obj instanceof RenderSettings )
				{
					final NearestObjectStatModel ts = ( NearestObjectStatModel ) obj;
					if ( null != ts )
					{
						// sanity check: style names must be unique
						if ( names.add( ts.getName() ) )
							userStyles.add( ts );
					}
				}
			}
			setSelectedStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{}
	}

	/**
	 * Returns a YAML instance that can dump / load a collection of
	 * {@link NearestObjectStatModel} to / from a .yaml file.
	 *
	 * @return a new YAML instance.
	 */
	private static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Yaml yaml = new Yaml( dumperOptions );
		return yaml;
	}
}
