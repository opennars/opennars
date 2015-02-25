package ca.nengo.ui.lib.menu;

import ca.nengo.ui.lib.NengoStyle;
import ca.nengo.ui.lib.action.StandardAction;

import javax.swing.*;
import java.awt.*;

/**
 * Used to build a popup menu. The created menu can later be converted to a
 * Swing component.
 * 
 * @author Shu Wu
 */
public class PopupMenuBuilder extends AbstractMenuBuilder {

	boolean isFirstSection = true;

	final JPopupMenu menu;

	public PopupMenuBuilder(String label) {
		super(false);
		menu = new JPopupMenu(label);
		style(menu);

		if (label != null && label.compareTo("") != 0) {
			addSection(label, NengoStyle.FONT_LARGE);
		}
	}

	@Override
	public void addAction(StandardAction standardAction) {
		addAction(standardAction, menu.getComponentCount());
	}
	
	public void addAction(StandardAction standardAction, int index)	{
		JMenuItem item;
		item = new JMenuItem(standardAction.toSwingAction());
		style(item);
		menu.insert(item, index);
	}
	
	public void addAction(String section, StandardAction action) {
		Component[] comps = menu.getComponents(); 
		int index = -1;
		for(int i = 0; i < comps.length; i++) {
			if(comps[i] instanceof JLabel) {
				if(((JLabel)comps[i]).getText().equals(section)) {
					index = i+1;
				}
			}
		}
		if(index == -1) {
			index = comps.length;
		}
		addAction(action, index);
	}

	public void addActionsRadio(StandardAction[] actions, int selected) {
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem item;
		
		for (int i = 0; i < actions.length; i++) {
			item = new JRadioButtonMenuItem(actions[i].toSwingAction());
			item.setSelected(i == selected);
			style(item);
			group.add(item);
			menu.insert(item, menu.getComponentCount());
		}
	}

	public void addSection(String name) {
		addSection(name, NengoStyle.FONT_BOLD);
	}

	/**
	 * Creates a new section in the Popup menu
	 * 
	 * @param name The name of the new section
	 * @param fontStyle style of font for the subsection label
	 */
	public void addSection(String name, Font fontStyle) {
		if (isFirstSection) {
			isFirstSection = false;
		} else {
			menu.add(new JSeparator());
		}

		JLabel label = new JLabel(name);
		label.setLocation(4, 4);
		label.setFont(fontStyle);
		style(label);
		menu.add(label);
	}

	@Override
	public MenuBuilder addSubMenu(String label) {
		MenuBuilder mb = new MenuBuilder(label, isCustomStyle());
		toJPopupMenu().add(mb.getJMenu());
		return mb;
	}

	/**
	 * @return Swing component
	 */
	public JPopupMenu toJPopupMenu() {
		return menu;
	}

	@Override
	public void addLabel(String msg) {
		JLabel item = new JLabel(msg);
		style(item);
		menu.add(item);
	}

}
