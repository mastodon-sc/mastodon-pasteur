package org.mastodon.mamut.nearest;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mastodon.app.MastodonIcons;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

@Plugin( type = NearestObjectStatPlugin.class )
public class NearestObjectStatPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	public static final String SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION = "compute nearest neighbors stats dialog";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	static
	{
		menuTexts.put( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION, "Stats on nearest neighbors" );
	}

	private final ToggleDialogAction toggleDialog = new ToggleDialogAction();

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
		if ( null == appModel || null == appModel.getAppModel() )
			return;

		toggleDialog.setAppModel( appModel );
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

	public static class ToggleDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private JDialog dialog;

		private NearestObjectStatController controller;

		public ToggleDialogAction()
		{
			super( SHOW_NEAREST_NEIGHBORS_STATS_DIALOG_ACTION );
		}

		public void setAppModel( final MamutPluginAppModel pluginModel )
		{
			final MamutAppModel appModel = pluginModel.getAppModel();
//			final Context context = pluginModel.getWindowManager().getContext();
//
//			final SharedBigDataViewerData bdvData = appModel.getSharedBdvData();
			final Model model = appModel.getModel();
//			final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();

			controller = new NearestObjectStatController( model, appModel.getMinTimepoint(), appModel.getMaxTimepoint() );
			dialog = new JDialog( ( Frame ) null, "Nearest neighbors" );
			dialog.setSize( 600, 300 );
			dialog.setIconImages( Arrays.asList( new Image[] {
					MastodonIcons.MASTODON_ICON_LARGE.getImage(),
					MastodonIcons.MASTODON_ICON_MEDIUM.getImage(),
					MastodonIcons.MASTODON_ICON_SMALL.getImage() } ) );
			dialog.getContentPane().add( controller.getView(), BorderLayout.CENTER );
			dialog.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
			dialog.setLocationRelativeTo( null );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( null == dialog )
				return;
			dialog.setVisible( !dialog.isVisible() );
		}
	}
}
