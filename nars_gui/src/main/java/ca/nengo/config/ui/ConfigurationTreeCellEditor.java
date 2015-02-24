/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConfigurationTreeCellEditor.java". Description: 
"TreeCellEditor for configuration trees"

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

import ca.nengo.config.MainHandler;
import ca.nengo.config.Property;
import ca.nengo.config.ui.ConfigurationTreeModel.NullValue;
import ca.nengo.config.ui.ConfigurationTreeModel.Value;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * TreeCellEditor for configuration trees. Gets editor components for property values from MainHandler.
 *   
 * @author Bryan Tripp
 */
public class ConfigurationTreeCellEditor extends DefaultCellEditor {

	private static final long serialVersionUID = 1L;
	
	private final JTree myTree;
	
	/**
	 * @param tree Configuration tree to which this cell editor is to belong
	 */
	public ConfigurationTreeCellEditor(JTree tree) {
		super(new JTextField());
		myTree = tree;
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		boolean result = false;

		if (e instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) e;
			if (me.getClickCount() > 1) {
				TreePath path = myTree.getPathForLocation(me.getX(), me.getY()); 
				if (path.getLastPathComponent() instanceof Value) {
					Object o = ((Value) path.getLastPathComponent()).getObject();
					Class<?> c = o.getClass();
					
					Object parent = null;
					if (path.getPath().length > 1) {
						parent = path.getParentPath().getLastPathComponent();
					}
					
					if (o instanceof NullValue && parent != null && parent instanceof Property) {
						c = ((Property) parent).getType();
					}
					
					if (MainHandler.getInstance().canHandle(c)) {						
						if (parent instanceof Property && ((Property) parent).isMutable()) {
							result = true;					
						}						
					}
				}				
			}
		}
		
		return result;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
		Component result = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		
		TreePath path = tree.getPathForRow(row);
		if (path.getParentPath().getLastPathComponent() instanceof Property && value instanceof Value) {
			Value node = (Value) value;
			Object o = node.getObject();
			
			if (o instanceof NullValue) {
				Class<?> type = ((Property) path.getParentPath().getLastPathComponent()).getType();
				o = MainHandler.getInstance().getDefaultValue(type);
			}
			
			ConfigurationChangeListener listener = new ConfigurationChangeListener(tree, path);
			Component customEditor = MainHandler.getInstance().getEditor(o, listener, myTree);
			if (customEditor != null) result = customEditor;
		}
		
		return result;
	}

}
