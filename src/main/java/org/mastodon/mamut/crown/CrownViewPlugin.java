package org.mastodon.mamut.crown;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = CrownViewPlugin.class )
public class CrownViewPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	public static final String NEW_CROWN_VIEW_ACTION = "new crown view";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	private final RunnableAction newCrownViewAction = new RunnableAction( NEW_CROWN_VIEW_ACTION, this::createCrownViewer );

	private MamutPluginAppModel appModel;

	static
	{
		menuTexts.put( NEW_CROWN_VIEW_ACTION, "New crown view" );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Arrays.asList( makeFullMenuItem( MamutMenuBuilder.item( NEW_CROWN_VIEW_ACTION ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( newCrownViewAction, ACTION_1_KEYS );
	}

	/**
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( NEW_CROWN_VIEW_ACTION, ACTION_1_KEYS, "Create and show a new crown view." );
		}
	}

	@Override
	public void setAppPluginModel( final MamutPluginAppModel appModel )
	{
		this.appModel = appModel;
	}

	private void createCrownViewer()
	{
		if ( null == appModel.getAppModel().getModel() )
			return;
		
		new MamutCrownViewBdv( appModel.getAppModel() );
	}

	// TODO Put it in a Util class.
	private static final MenuItem makeFullMenuItem( final MenuItem item )
	{
		MenuItem menuPath = item;
		for ( int i = MENU_PATH.length - 1; i >= 0; i-- )
			menuPath = MamutMenuBuilder.menu( MENU_PATH[ i ], menuPath );
		return menuPath;
	}
}
