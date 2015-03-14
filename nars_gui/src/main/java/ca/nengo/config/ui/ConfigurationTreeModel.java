/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigurationTreeModel.java". Description:
"Data model underlying JTree user interface for a Configurable.

  @author Bryan Tripp"

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
 * Created on 9-Dec-07
 */
package ca.nengo.config.ui;

//import java.awt.BorderLayout;
//import java.awt.Dimension;

import ca.nengo.config.*;
import ca.nengo.model.StructuralException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data model underlying JTree user interface for a Configurable.
 *
 * @author Bryan Tripp
 */
public class ConfigurationTreeModel implements TreeModel {

	private final Value myRoot;
	private final List<TreeModelListener> myListeners;

	/**
	 * @param configurable Root of the configuration tree
	 */
	public ConfigurationTreeModel(Object configurable) {
		myRoot = new Value(0, configurable);
		myListeners = new ArrayList<TreeModelListener>(5);
	}

	/**
	 * @param parentPath Path in configuration tree of a property to which a value is to be added
	 * @param value New value to add
	 * @param name Name of new value (only used if parent is a NamedValueProperty; can be null otherwise)
	 */
	public void addValue(TreePath parentPath, Object value, String name) {
		try {
			Object parent = parentPath.getLastPathComponent();
			if (parent instanceof ListProperty) {
				ListProperty property = (ListProperty) parent;
				property.addValue(value);

				TreeModelEvent event = new TreeModelEvent(this, parentPath, new int[]{property.getNumValues()-1}, new Object[]{value});
				for (int i = 0; i <myListeners.size(); i++) {
					myListeners.get(i).treeNodesInserted(event);
				}
			} else if (parent instanceof NamedValueProperty) {
				NamedValueProperty property = (NamedValueProperty) parent;
				property.setValue(name, value);
				refresh(parentPath);
			} else {
				throw new RuntimeException("Can't add child to a " + parent.getClass().getName());
			}
		} catch (StructuralException e) {
			ConfigExceptionHandler.handle(e, "Can't add value: " + e.getMessage(), null);
		}
	}

	/**
	 * @param path Path to root of subtree to refresh
	 */
	public void refresh(TreePath path) {
		TreeModelEvent event = new TreeModelEvent(this, path);
		for (int i = 0; i <myListeners.size(); i++) {
			myListeners.get(i).treeStructureChanged(event);
		}
	}

	/**
	 * @param path Path to the tree node to insert before
	 * @param value Value to insert
	 */
	public void insertValue(TreePath path, Object value) {
		try {
			Object parent = path.getParentPath().getLastPathComponent();
			if (parent instanceof ListProperty && path.getLastPathComponent() instanceof Value) {
				ListProperty property = (ListProperty) parent;
				Value toInsertBefore = (Value) path.getLastPathComponent();
				property.insert(toInsertBefore.getIndex(), value);

				Value node = new Value(toInsertBefore.getIndex(), value);
				TreeModelEvent insertEvent = new TreeModelEvent(this, path.getParentPath(),
						new int[]{toInsertBefore.getIndex()}, new Object[]{node});
				TreeModelEvent changeEvent = getIndexUpdateEvent(this, path.getParentPath(),
						toInsertBefore.getIndex()+1, property.getNumValues());
				for (int i = 0; i <myListeners.size(); i++) {
					myListeners.get(i).treeNodesInserted(insertEvent);
					myListeners.get(i).treeNodesChanged(changeEvent);
				}

			} else {
				throw new RuntimeException("Can't insert value on child of " + parent.getClass().getName());
			}
		} catch (StructuralException e) {
			ConfigExceptionHandler.handle(e, "Can't insert value: " + e.getMessage(), null);
		}
	}

	//creates an event to update a range of child indices with given parent
	private TreeModelEvent getIndexUpdateEvent(Object source, TreePath parentPath, int fromIndex, int toIndex) {
		Object parent = parentPath.getLastPathComponent();
		int[] changedIndices = new int[toIndex-fromIndex];
		Object[] changedValues = new Object[changedIndices.length];
		for (int i = 0; i < changedIndices.length; i++) {
			changedIndices[i] = fromIndex + i;
			changedValues[i] = getChild(parent, changedIndices[i]); //creates new Value object with correct index
		}
		return new TreeModelEvent(source, parentPath, changedIndices, changedValues);
	}

	/**
	 * @param path Path to object to be replaced with new value
	 * @param value New value
	 * @throws StructuralException if the setValue functions fail
	 */
	public void setValue(TreePath path, Object value) throws StructuralException  {
		Object parent = path.getParentPath().getLastPathComponent();
		if (parent instanceof Property && path.getLastPathComponent() instanceof Value) {
			int index = ((Value) path.getLastPathComponent()).getIndex();

			if (parent instanceof SingleValuedProperty) {
				((SingleValuedProperty) parent).setValue(value);
			} else if (parent instanceof ListProperty) {
				((ListProperty) parent).setValue(index, value);
			} else if (parent instanceof NamedValueProperty) {
				String name = ((Value) path.getLastPathComponent()).getName();
				((NamedValueProperty) parent).setValue(name, value);
			}

			Value child = (Value) path.getLastPathComponent();
			child.setObject(value);

			if (child.getObject() instanceof Configurable) {
				TreeModelEvent event = new TreeModelEvent(this, path);
				for (int i = 0; i <myListeners.size(); i++) {
					myListeners.get(i).treeStructureChanged(event);
				}
			} else {
				TreePath shortPath = new TreePath(new Object[]{parent, child});
				TreeModelEvent event = new TreeModelEvent(this, shortPath, new int[]{index}, new Object[]{child});
				for (int i = 0; i <myListeners.size(); i++) {
					myListeners.get(i).treeNodesChanged(event);
				}
			}
		} else {
			throw new RuntimeException("Can't set value on child of "
					+ parent.getClass().getName() + " (this is probably a bug).");
		}
	}

	/**
	 * @param path Tree path to property value to remove
	 */
	public void removeValue(TreePath path) {
		try {
			Object parent = path.getParentPath().getLastPathComponent();
			if (path.getLastPathComponent() instanceof Value && (parent instanceof ListProperty || parent instanceof NamedValueProperty)) {
				Value toRemove = (Value) path.getLastPathComponent();
				int numValues = 0;

				if (parent instanceof ListProperty) {
					ListProperty property = (ListProperty) parent;
					property.remove(toRemove.getIndex());
					numValues = property.getNumValues();
				} else if (parent instanceof NamedValueProperty) {
					NamedValueProperty property = (NamedValueProperty) parent;
					property.removeValue(toRemove.getName());
					numValues = property.getValueNames().size();
				}

				TreeModelEvent removeEvent = new TreeModelEvent(this, path.getParentPath(),
						new int[]{toRemove.getIndex()}, new Object[]{toRemove});
				TreeModelEvent changeEvent = getIndexUpdateEvent(this, path.getParentPath(),
						toRemove.getIndex(), numValues);
				for (int i = 0; i < myListeners.size(); i++) {
					myListeners.get(i).treeNodesRemoved(removeEvent);
					myListeners.get(i).treeNodesChanged(changeEvent);
				}
			} else {
				throw new RuntimeException("Can't remove child of "
						+ parent.getClass().getName() + " (this is probably a bug)");
			}
		} catch (StructuralException e) {
			ConfigExceptionHandler.handle(e, "Can't remove value: " + e.getMessage(), null);
		}
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index) {
		Object result = null;

		try {
			if (parent instanceof Value) {
				Configuration c = ((Value) parent).getConfiguration();
				if (c != null) {
					List<String> propertyNames = c.getPropertyNames();
					Collections.sort(propertyNames);
					result = c.getProperty(propertyNames.get(index));
				}
			} else if (parent instanceof ListProperty) {
				ListProperty p = (ListProperty) parent;
				Object o = p.getValue(index);
				result = new Value(index, o);
			} else if (parent instanceof NamedValueProperty) {
				NamedValueProperty p = (NamedValueProperty) parent;
				String name = p.getValueNames().get(index);
				Object o = p.getValue(name);
				result = new Value(index, o);
				((Value) result).setName(name);
			} else if (parent instanceof SingleValuedProperty) {
				if (index == 0) {
					Object o = ((SingleValuedProperty) parent).getValue();
					result = new Value(index, o);
				} else {
					ConfigExceptionHandler.handle(
							new StructuralException("SingleValuedProperty doesn't have child " + index),
							ConfigExceptionHandler.DEFAULT_BUG_MESSAGE, null);
				}
			}
		} catch (StructuralException e) {
			ConfigExceptionHandler.handle(e, ConfigExceptionHandler.DEFAULT_BUG_MESSAGE, null);
		}

		return result;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent) {
		int result = 0;

		if (parent instanceof Value) {
			Configuration configuration = ((Value) parent).getConfiguration();
			if (configuration != null) {
                result = configuration.getPropertyNames().size();
            }
		} else if (parent instanceof SingleValuedProperty) {
			result = 1;
		} else if (parent instanceof ListProperty) {
			result = ((ListProperty) parent).getNumValues();
		} else if (parent instanceof NamedValueProperty) {
			result = ((NamedValueProperty) parent).getValueNames().size();
		}

		return result;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	public int getIndexOfChild(Object parent, Object child) {
		int index = -1;

		try {
			if (child instanceof Property) {
				Configuration c = ((Value) parent).getConfiguration();
				Property p = (Property) child;
				List<String> propertyNames = c.getPropertyNames();
				Collections.sort(propertyNames);
				index = propertyNames.indexOf(p.getName());
			} else if (parent instanceof SingleValuedProperty) {
				SingleValuedProperty p = (SingleValuedProperty) parent;
				Value v = (Value) child;
				if (p.getValue() != null && p.getValue().equals(v.getObject())) {
					index = 0;
				}
			} else if (parent instanceof ListProperty) {
				ListProperty p = (ListProperty) parent;
				Value v = (Value) child;
				for (int i = 0; i < p.getNumValues() && index == -1; i++) {
					if (p.getValue(i) != null && p.getValue(i).equals(v.getObject())) {
                        index = i;
                    }
				}
			} else if (parent instanceof NamedValueProperty) {
				NamedValueProperty p = (NamedValueProperty) parent;
				String name = ((Value) child).getName();
				for (int i = 0; i < p.getValueNames().size() && index == -1; i++) {
					if (p.getValueNames().get(i).equals(name)) {
                        index = i;
                    }
				}
			}
		} catch (StructuralException e) {
			ConfigExceptionHandler.handle(e, ConfigExceptionHandler.DEFAULT_BUG_MESSAGE, null);
		}

		return index;
	}

	/**
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot() {
		return myRoot;
	}

	/**
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	public boolean isLeaf(Object o) {
		return ( !(o instanceof Value && ((Value) o).getConfiguration() != null) && !(o instanceof Property) );
	}

	/**
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener listener) {
		myListeners.add(listener);
	}

	/**
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener listener) {
		myListeners.remove(listener);
	}

	/**
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	public void valueForPathChanged(TreePath path, Object newValue) {
		if (newValue instanceof Value) {
			TreeModelEvent event = new TreeModelEvent(null, path.getParentPath(),
					new int[]{((Value) newValue).getIndex()}, new Object[]{newValue});
			for (int i = 0; i < myListeners.size(); i++) {
				myListeners.get(i).treeNodesChanged(event);
			}
		}
	}

	/**
	 * A wrapper for property values: stores index and configuration (if applicable)
	 */
	public static class Value {

		private int myIndex;
		private Object myObject;
		private String myName;
		private Configuration myConfiguration;

		/**
		 * @param index Property location
		 * @param object Owner object
		 */
		public Value(int index, Object object) {
			myIndex = index;
			myObject = (object == null) ? new NullValue() : object;

			if (object != null && !MainHandler.getInstance().canHandle(object.getClass())) {
				myConfiguration = PropretiesUtil.getConfiguration(object);
			}
		}

		/**
		 * @return Location
		 */
		public int getIndex() {
			return myIndex;
		}

		/**
		 * @param index Location
		 */
		public void setIndex(int index) {
			myIndex = index;
		}

		/**
		 * @param o Owner
		 */
		public void setObject(Object o) {
			myObject = o;
		}

		/**
		 * @return Owner
		 */
		public Object getObject() {
			return myObject;
		}

		/**
		 * @return Name
		 */
		public String getName() {
			return myName;
		}

		/**
		 * @param name Name
		 */
		public void setName(String name) {
			myName = name;
		}

		/**
		 * @return Configuration object
		 */
		public Configuration getConfiguration() {
			return myConfiguration;
		}
	}

	/**
	 * For the configuration UI to use in place of a null parameter value.
	 *
	 * @author Bryan Tripp
	 */
	public static class NullValue {
		public String toString() {
			return "NULL";
		}
	}
}
