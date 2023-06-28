package org.mastodon.mamut.nearest;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.nearest.ui.NearestObjectStatController;
import org.mastodon.mamut.nearest.ui.NearestObjectStatModelManager;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.settings.SelectAndEditProfileSettingsPage;
import bdv.ui.settings.style.StyleProfile;
import bdv.ui.settings.style.StyleProfileManager;

@Plugin( type = NearestObjectStatPlugin.class )
public class NearestObjectStatPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	public static final String SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION = "generate statistics on nearest neighbors";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, "Generate statistics on nearest neighbors" );
	}

	private final ShowDialogAction toggleDialog = new ShowDialogAction();

	private MamutPluginAppModel appModel;

	private SelectAndEditProfileSettingsPage< StyleProfile< NearestObjectStatModel > > page;

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Collections.singletonList( MamutMenuBuilder.makeFullMenuItem(
				SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, MENU_PATH ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( toggleDialog, ACTION_1_KEYS );
	}

	@Override
	public void setAppPluginModel( final MamutPluginAppModel appModel )
	{
		this.appModel = appModel;

		if ( null == appModel || null == appModel.getAppModel() )
			return;

		final NearestObjectStatController controller = new NearestObjectStatController(
				appModel.getAppModel().getModel(),
				appModel.getAppModel().getMinTimepoint(),
				appModel.getAppModel().getMaxTimepoint() );

		this.page = new SelectAndEditProfileSettingsPage<>(
				"Stats on nearest-neighbors",
				new StyleProfileManager<>( new NearestObjectStatModelManager(), new NearestObjectStatModelManager( false ) ),
				controller.getProfileEditor() );
		appModel.getWindowManager().getPreferencesDialog().addPage( page );
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
			descriptions.add( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, ACTION_1_KEYS,
					"Cpmpute statistics on nearest neighbors and store them as feature values." );
		}
	}

	private class ShowDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		public ShowDialogAction()
		{
			super( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( appModel == null )
				return;

			final PreferencesDialog dialog = appModel.getWindowManager().getPreferencesDialog();
			if ( null == dialog )
				return;

			dialog.showPage( page.getTreePath() );
			dialog.setVisible( true );
		}
	}
}
