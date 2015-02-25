package ca.nengo.ui.lib.menu;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.StandardAction;

import javax.swing.*;

/**
 * Used to build a menu. The created menu can later be converted to a Swing
 * component.
 * 
 * @author Shu
 */
public abstract class AbstractMenuBuilder {
	private final boolean applyStyle;

	public AbstractMenuBuilder(boolean applyCustomStyle) {
		this.applyStyle = applyCustomStyle;

	}

	public abstract void addAction(StandardAction action);
	
	public abstract void addActionsRadio(StandardAction[] actions, int selected);

	public abstract void addLabel(String msg);

	public abstract void addSection(String name);

	public abstract AbstractMenuBuilder addSubMenu(String label);

	public boolean isCustomStyle() {
		return applyStyle;
	}

	public void style(JComponent item) {
		style(item, false);
	}

	public void style(JComponent item, boolean isTitle) {
		if (applyStyle) {
			NengoStyle.applyMenuStyle(item, isTitle);
		}
	}
}
