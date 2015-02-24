/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConfigurationTreeCellRenderer.java". Description: 
"Renderer for cells in a configuration tree"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on 10-Dec-07
 */
package ca.nengo.config.ui;

import ca.nengo.config.IconRegistry;
import ca.nengo.config.MainHandler;
import ca.nengo.config.Property;
import ca.nengo.config.ui.ConfigurationTreeModel.Value;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Renderer for cells in a configuration tree. 
 * 
 * @author Bryan Tripp
 */
public class ConfigurationTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component result = this;
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		Icon icon = getCustomIcon(value);
		if (icon != null) setIcon(icon);		
		
		if (value instanceof Value && ((Value) value).getConfiguration() != null) {
			setText(((Value) value).getObject().getClass().getSimpleName());
			setToolTipText(((Value) value).getObject().getClass().getCanonicalName());
		} else if (value instanceof Property) {
			Property property = (Property) value;
			
			StringBuilder text = new StringBuilder(property.getName());
			text.append(" (");
			text.append(property.getType().getSimpleName());
			text.append(')');
			setText(text.toString());
			
			setToolTipText(null);
		} else if (value instanceof Value) { //with null getConfiguration (a leaf) 
			Object o = ((Value) value).getObject();
			Component customRenderer = MainHandler.getInstance().getRenderer(o);
			
			if (customRenderer == null) {
				setText("UNKNOWN TYPE (" + o.toString() + ')');
				setToolTipText(o.getClass().getCanonicalName());			
			} else {
				customRenderer.setBackground(sel ? this.getBackgroundSelectionColor() : this.getBackgroundNonSelectionColor());
				result = customRenderer;
			}				
		} else {
			setToolTipText(value.getClass().getCanonicalName());
		}
		
		//show name 
		if (value instanceof Value && ((Value) value).getName() != null && result instanceof JLabel) {
			JLabel label = (JLabel) result;
			label.setText(((Value) value).getName() + ": " + label.getText());
		}
		
		return result;
	}
	
	private Icon getCustomIcon(Object node) {
		if (node instanceof Property) {
			return IconRegistry.getInstance().getIcon(((Property) node).getType());
		} else {
			Object value = (node instanceof Value) ? ((Value) node).getObject() : node;
			return IconRegistry.getInstance().getIcon(value);
		}
	}
	
}
