package org.mastodon.mamut.nearest;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;

public class NearestObjectStatListPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final JPanel mainPanel;

	public NearestObjectStatListPanel( final NearestObjectStatModel model )
	{
		super( new BorderLayout() );
		model.statModelListeners().add( () -> refresh( model ) );
		this.mainPanel = new JPanel();
		mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS ) );

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder( new LineBorder( getBackground().darker() ) );
		scrollPane.setOpaque( false );
		scrollPane.getViewport().setOpaque( false );
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setViewportView( mainPanel );

		add( scrollPane, BorderLayout.CENTER );
		refresh( model );
	}

	private void refresh( final NearestObjectStatModel model )
	{
		mainPanel.removeAll();
		for ( final NearestObjectStatItem item : model )
		{
			final JPanel panel = new JPanel();
			panel.setLayout( new BoxLayout( panel, BoxLayout.LINE_AXIS ) );
			panel.add( new JLabel( item.toString() ) );
			panel.add( Box.createHorizontalGlue() );
			final JButton btn = new JButton( MastodonIcons.REMOVE_ICON );
			btn.addActionListener( e -> model.remove( item ) );
			panel.add( btn );
			mainPanel.add( panel );
		}
		mainPanel.revalidate();
	}
}
