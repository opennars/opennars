/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ConfigurationTreePopupListener.java". Description: 
"Creates a popup menu for configuration tree nodes, to allow refreshing, adding/setting/removing, etc"

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

import ca.nengo.config.*;
import ca.nengo.config.ui.ConfigurationTreeModel.Value;
import ca.nengo.model.StructuralException;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Creates a popup menu for configuration tree nodes, to allow refreshing, adding/setting/removing, etc. 
 * as appropriate. 
 * 
 * @author Bryan Tripp
 */
public class ConfigurationTreePopupListener extends MouseAdapter {
	
	private final JTree myTree;
	private final ConfigurationTreeModel myModel;
	
	/**
	 * @param tree A tree that displays a Configuration
	 * @param model TreeModel underlying the above tree
	 */
	public ConfigurationTreePopupListener(JTree tree, ConfigurationTreeModel model) {
		myTree = tree;
		myModel = model;			
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}
	
	private void maybeShowPopup(final MouseEvent event) {
		final TreePath path = myTree.getPathForLocation(event.getX(), event.getY());		
		if (event.isPopupTrigger() && event.getComponent().equals(myTree) && path != null) {
			myTree.setSelectionPath(path);
			
			JPopupMenu popup = new JPopupMenu();
			if (path.getLastPathComponent() instanceof ListProperty) {
				final ListProperty p = (ListProperty) path.getLastPathComponent();
				if (!p.isFixedCardinality()) {
					JMenuItem addValueItem = new JMenuItem("Add");
					addValueItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent E) {
							myModel.addValue(path, getValue(p.getType()), null);
						}
					});
					popup.add(addValueItem);				
				}
			} else if (path.getLastPathComponent() instanceof NamedValueProperty) {
				final NamedValueProperty p = (NamedValueProperty) path.getLastPathComponent();
				if (!p.isFixedCardinality()) {
					JMenuItem addValueItem = new JMenuItem("Add");
					addValueItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent E) {
							String name = JOptionPane.showInputDialog(myTree, "Value Name");
							myModel.addValue(path, getValue(p.getType()), name);
						}
					});
					popup.add(addValueItem);
				}
			} else if (path.getParentPath() != null && path.getParentPath().getLastPathComponent() instanceof Property) {
				final Property p = (Property) path.getParentPath().getLastPathComponent();
				if (p.isMutable() && !MainHandler.getInstance().canHandle(p.getType())) {
					final JMenuItem replaceValueItem = new JMenuItem("Replace");
					replaceValueItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							Class<?> currentType = ((Value) path.getLastPathComponent()).getObject().getClass();
							Object o = NewConfigurableDialog.showDialog(replaceValueItem, p.getType(), currentType);
							if (o != null) {
								try {
									myModel.setValue(path, o);
								} catch (StructuralException ex) {
									ConfigExceptionHandler.handle(ex, ex.getMessage(), event.getComponent());
								}
							}
						}
					});
					popup.add(replaceValueItem);									
				}
				
				if (!p.isFixedCardinality()) {
					if (p instanceof ListProperty) {
						JMenuItem insertValueItem = new JMenuItem("Insert");
						insertValueItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								myModel.insertValue(path, getValue(p.getType()));
							}
						}); 
						popup.add(insertValueItem);						
					}
					JMenuItem removeValueItem = new JMenuItem("Remove");
					removeValueItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							myModel.removeValue(path);
						}
					});
					popup.add(removeValueItem);				
				}
				
			}
			
			JMenuItem refreshItem = new JMenuItem("Refresh");
			refreshItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					myModel.refresh(path);
				}
			});
			popup.add(refreshItem);
			
			//property help on either property or property value ... 
			Property property = null;
			if (path.getParentPath() != null && path.getParentPath().getLastPathComponent() instanceof Property) {
				property = (Property) path.getParentPath().getLastPathComponent();
			} else if (path.getLastPathComponent() instanceof Property) {
				property = (Property) path.getLastPathComponent();
			}			
			if (property != null) {
				JMenuItem helpItem = new JMenuItem("Help");
				final Property p = property;
				helpItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String documentation = p.getDocumentation();
						if (documentation != null)
							PropretiesUtil.showHelp(documentation);
					}				
				});
				popup.add(helpItem);
			}
			
//			if (path.getLastPathComponent() instanceof Value && ((Value) path.getLastPathComponent()).getObject() instanceof Function) {
//				final Function function = (Function) ((Value) path.getLastPathComponent()).getObject();
//				JMenuItem plotValueItem = new JMenuItem("Plot");
//				plotValueItem.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						Plotter.plot(function, -1, .001f, 1, "Function");
//					}
//				});
//				popup.add(plotValueItem);
//			}
			
			popup.show(event.getComponent(), event.getX(), event.getY());
		}
	}
	
	private Object getValue(Class<?> type) {
		Object result = PropretiesUtil.getDefaultValue(type);
		if (result == null) {
			result = NewConfigurableDialog.showDialog(myTree, type, null);
		}
		return result;
	}
	
}
