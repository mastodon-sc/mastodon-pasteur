package org.mastodon.mamut.crown;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPluginAppModel;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

@Plugin( type = CrownIntensityPlugin.class )
public class CrownIntensityPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins", "Crown intensity" };

	public static final String NEW_CROWN_VIEW_ACTION = "new crown view";
	public static final String SET_CROWN_SCALE_ACTION = "set crown scale";

	private static final String[] ACTION_1_KEYS = new String[] { "not mapped" };
	private static final String[] ACTION_2_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	private final RunnableAction newCrownViewAction = new RunnableAction( NEW_CROWN_VIEW_ACTION, this::createCrownViewer );
	private final ToggleCrownScaleDialogAction crownScaleDialogAction = new ToggleCrownScaleDialogAction();

	private MamutPluginAppModel appModel;

	/**
	 * Static value for now. We need this to be visible from the feature
	 * computer too. Might be problematic if the user launches two mastodon
	 * sessions at the same time, but even with that it would not be a great
	 * issue. A proper way to do it would probably be to have some way of having
	 * some kind of model storage from the app.
	 */
	private static double scale = 0.5;

	static
	{
		menuTexts.put( NEW_CROWN_VIEW_ACTION, "New crown view" );
		menuTexts.put( SET_CROWN_SCALE_ACTION, "Set crown scale" );
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Arrays.asList(
				makeFullMenuItem( MamutMenuBuilder.item( NEW_CROWN_VIEW_ACTION ) ),
				makeFullMenuItem( MamutMenuBuilder.item( SET_CROWN_SCALE_ACTION ) ) );
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( newCrownViewAction, ACTION_1_KEYS );
		actions.namedAction( crownScaleDialogAction, ACTION_2_KEYS );
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
			descriptions.add( SET_CROWN_SCALE_ACTION, ACTION_2_KEYS, "Set the scale of spot crown for intensity measurement." );
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

	public static void setScale( final double scale )
	{
		CrownIntensityPlugin.scale = scale;
	}

	public static double getScale()
	{
		return scale;
	}
	
	private class ToggleCrownScaleDialogAction extends AbstractNamedAction
	{

		private static final long serialVersionUID = 1L;

		private CrownScaleDialog crownScaleDialog;

		public ToggleCrownScaleDialogAction()
		{
			super( SET_CROWN_SCALE_ACTION );
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( null == crownScaleDialog )
			{
				final DoubleConsumer scaleSetter = s -> setScale( s );
				final DoubleSupplier scaleGetter = () -> getScale();
				crownScaleDialog = new CrownScaleDialog( scaleSetter, scaleGetter );
			}
			crownScaleDialog.setVisible( !crownScaleDialog.isVisible() );
		}
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
