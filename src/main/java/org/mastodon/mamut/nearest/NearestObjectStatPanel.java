package org.mastodon.mamut.nearest;

import static org.mastodon.app.ui.StyleElements.booleanElement;
import static org.mastodon.app.ui.StyleElements.enumElement;
import static org.mastodon.app.ui.StyleElements.intElement;
import static org.mastodon.app.ui.StyleElements.linkedCheckBox;
import static org.mastodon.app.ui.StyleElements.linkedComboBoxEnumSelector;
import static org.mastodon.app.ui.StyleElements.linkedSpinner;
import static org.mastodon.app.ui.StyleElements.separator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.mastodon.app.ui.StyleElements.BooleanElement;
import org.mastodon.app.ui.StyleElements.EnumElement;
import org.mastodon.app.ui.StyleElements.IntElement;
import org.mastodon.app.ui.StyleElements.Separator;
import org.mastodon.app.ui.StyleElements.StyleElement;
import org.mastodon.app.ui.StyleElements.StyleElementVisitor;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStat;
import org.mastodon.mamut.nearest.NearestObjectStatModel.NearestObjectStatItem;

public class NearestObjectStatPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public NearestObjectStatPanel( final NearestObjectStatItem model )
	{
		super( new GridBagLayout() );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		styleElements( model ).forEach( element -> element.accept(
				new StyleElementVisitor()
				{
					@Override
					public void visit( final Separator element )
					{
						add( Box.createVerticalStrut( 10 ), c );
						++c.gridy;
					}

					@Override
					public void visit( final BooleanElement element )
					{
						final JCheckBox checkbox = linkedCheckBox( element, "" );
						checkbox.setFocusable( true );
						addToLayout(
								checkbox,
								new JLabel( element.getLabel() ) );
					}

					@Override
					public void visit( final IntElement element )
					{
						final JSpinner spinner = linkedSpinner( element );
						addToLayout(
								spinner,
								new JLabel( element.getLabel() ) );
					}

					@Override
					public < E > void visit( final EnumElement< E > enumElement )
					{
						final JComboBox< E > cb = linkedComboBoxEnumSelector( enumElement );
						addToLayout( cb,
								new JLabel( enumElement.getLabel() ) );
					};

					private void addToLayout( final JComponent comp1, final JComponent comp2 )
					{
						c.gridy++;
						c.gridx = 0;
						c.weightx = 0.0;
						c.anchor = GridBagConstraints.EAST;
						add( comp2, c );

						c.gridx++;
						c.weightx = 1.0;
						c.anchor = GridBagConstraints.WEST;
						add( comp1, c );
					}
				} ) );
	}

	private List< StyleElement > styleElements( final NearestObjectStatItem model )
	{
		return Arrays.asList(
				separator(),
				enumElement( "statistics", NearestObjectStat.values(), model::nearestObjectStat, model::setNearestObjectStat ),
				intElement( "n neighbors", 0, 1000, model::n, model::setN ),
				booleanElement( "include center item", model::includeItem, model::setIncludeItem ) );
	}
}
