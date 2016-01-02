/**
 * L2FProd Common v9.2 License.
 *
 * Copyright 2005 - 2009 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package automenta.vivisect.swing.property.propertysheet;

import automenta.vivisect.swing.property.swing.ObjectTableModel;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * PropertySheetTableModel. <br>
 *  
 */
public class PropertySheetTableModel
  extends AbstractTableModel
  implements PropertyChangeListener, PropertySheet, ObjectTableModel {
  public static final int NAME_COLUMN = 0;
  public static final int VALUE_COLUMN = 1;
  public static final int NUM_COLUMNS = 2;

  private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
  private final List model;
  private final List publishedModel;
  private final List properties;
  private int mode;
  private boolean sortingCategories;
  private boolean sortingProperties;
  private boolean restoreToggleStates;
  private Comparator categorySortingComparator;
  private Comparator propertySortingComparator;
  private final Map toggleStates;

  public PropertySheetTableModel() {
    model = new ArrayList();
    publishedModel = new ArrayList();
    properties = new ArrayList();
    mode = PropertySheet.VIEW_AS_FLAT_LIST;
    sortingCategories = false;
    sortingProperties = false;
    restoreToggleStates = false;
    toggleStates=new HashMap();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#setProperties(com.l2fprod.common.propertysheet.Property[])
   */
  @Override
  public void setProperties(Property[] newProperties) {
    // unregister the listeners from previous properties
    for (Object property1 : properties) {
      Property prop = (Property) property1;
      prop.removePropertyChangeListener(this);
    }

    // replace the current properties
    properties.clear();
    properties.addAll(Arrays.asList(newProperties));

    // add listeners
    for (Object property : properties) {
      Property prop = (Property) property;
      prop.addPropertyChangeListener(this);
    }

    buildModel();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#getProperties()
   */
  @Override
  public Property[] getProperties() {
    return (Property[]) properties.toArray(new Property[properties.size()]);
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#addProperty(com.l2fprod.common.propertysheet.Property)
   */
  @Override
  public void addProperty(Property property) {
    properties.add(property);
    property.addPropertyChangeListener(this);
    buildModel();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#addProperty(int, com.l2fprod.common.propertysheet.Property)
   */
  @Override
  public void addProperty(int index, Property property) {
    properties.add(index, property);
    property.addPropertyChangeListener(this);
    buildModel();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#removeProperty(com.l2fprod.common.propertysheet.Property)
   */
  @Override
  public void removeProperty(Property property) {
    properties.remove(property);
    property.removePropertyChangeListener(this);
    buildModel();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#getPropertyCount()
   */
  @Override
  public int getPropertyCount() {
    return properties.size();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.propertysheet.PropertySheet#propertyIterator()
   */
  @Override
  public Iterator propertyIterator() {
    return properties.iterator();
  }

  /**
   * Set the current mode, either {@link PropertySheet#VIEW_AS_CATEGORIES}
   * or {@link PropertySheet#VIEW_AS_FLAT_LIST}. 
   */
  public void setMode(int mode) {
    if (this.mode == mode) {
      return;
    }
    this.mode = mode;
    buildModel();
  }

  /**
   * Get the current mode, either {@link PropertySheet#VIEW_AS_CATEGORIES}
   * or {@link PropertySheet#VIEW_AS_FLAT_LIST}.
   */
  public int getMode() {
    return mode;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnClass(int)
   */
  @Override
  public Class getColumnClass(int columnIndex) {
    return super.getColumnClass(columnIndex);
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return NUM_COLUMNS;
  }

  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount() {
    return publishedModel.size();
  }

  /* (non-Javadoc)
   * @see com.l2fprod.common.swing.ObjectTableModel#getObject(int)
   */
  @Override
  public Object getObject(int rowIndex) {
    return getPropertySheetElement(rowIndex);
  }

  /**
   * Get the current property sheet element, of type {@link Item}, at
   * the specified row.
   */
  public Item getPropertySheetElement(int rowIndex) {
    return (Item) publishedModel.get(rowIndex);
  }

  /**
   * Get whether this model is currently sorting categories.
   */
  public boolean isSortingCategories() {
    return sortingCategories;
  }

  /**
   * Set whether this model is currently sorting categories.
   * If this changes the sorting, the model will be rebuilt.
   */
  public void setSortingCategories(boolean value) {
    boolean old = sortingCategories;
    sortingCategories = value;
    if (sortingCategories != old)
      buildModel();
  }

  /**
   * Get whether this model is currently sorting properties.
   */
  public boolean isSortingProperties() {
    return sortingProperties;
  }

  /**
   * Set whether this model is currently sorting properties.
   * If this changes the sorting, the model will be rebuilt.
   */
  public void setSortingProperties(boolean value) {
    boolean old = sortingProperties;
    sortingProperties = value;
    if (sortingProperties != old)
      buildModel();
  }

  /**
   * Set the comparator used for sorting categories.  If this
   * changes the comparator, the model will be rebuilt.
   */
  public void setCategorySortingComparator(Comparator comp) {
    Comparator old = categorySortingComparator;
    categorySortingComparator = comp;
    if (categorySortingComparator != old)
      buildModel();
  }

  /**
   * Set the comparator used for sorting properties.  If this
   * changes the comparator, the model will be rebuilt.
   */
  public void setPropertySortingComparator(Comparator comp) {
    Comparator old = propertySortingComparator;
    propertySortingComparator = comp;
    if (propertySortingComparator != old)
      buildModel();
  }

  /**
   * Set whether or not this model will restore the toggle states when new
   * properties are applied.
   */
  public void setRestoreToggleStates(boolean value) {
    restoreToggleStates = value;
    if (!restoreToggleStates) {
      toggleStates.clear();
    }
  }

  /**
   * Get whether this model is restoring toggle states
   */
  public boolean isRestoreToggleStates() {
    return restoreToggleStates;
  }

   /**
    * @return the category view toggle states.
    */
   public Map getToggleStates() {
     // Call visibilityChanged to populate the toggleStates map
     visibilityChanged(restoreToggleStates);
     return toggleStates;
   }

   /**
    * Sets the toggle states for the category views. Note this <b>MUST</b> be
    * called <b>BEFORE</b> setting any properties.
    * @param toggleStates the toggle states as returned by getToggleStates
    */
   public void setToggleStates(Map toggleStates) {
     // We are providing a toggleStates map - so by definition we must want to
     // store the toggle states
     setRestoreToggleStates(true);
     this.toggleStates.clear();
     this.toggleStates.putAll(toggleStates);
   }
     
  /**
   * Retrieve the value at the specified row and column location.
   * When the row contains a category or the column is
   * {@link #NAME_COLUMN}, an {@link Item} object will be returned.
   * If the row is a property and the column is {@link #VALUE_COLUMN},
   * the value of the property will be returned.
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object result = null;
    Item item = getPropertySheetElement(rowIndex);

    if (item.isProperty()) {
      switch (columnIndex) {
        case NAME_COLUMN:
          result = item;
          break;
          
        case VALUE_COLUMN:
          try {
            result = item.getProperty().getValue();
          } catch (Exception e) {
            e.printStackTrace();
          }
          break;
          
        default:
          // should not happen
      }
    }
    else {
      result = item;
    }
    return result;
  }

  /**
   * Sets the value at the specified row and column.  This will have
   * no effect unless the row is a property and the column is
   * {@link #VALUE_COLUMN}.
   * 
   * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
   */
  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
    Item item = getPropertySheetElement(rowIndex);
    if (item.isProperty() ) {
      if (columnIndex == VALUE_COLUMN) {
        try {
          item.getProperty().setValue(value);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Add a {@link PropertyChangeListener} to the current model.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    listeners.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    listeners.removePropertyChangeListener(listener);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // forward the event to registered listeners
    listeners.firePropertyChange(evt);
  }

  protected void visibilityChanged(boolean restoreOldStates) {
    // Store the old visibility states
    if (restoreOldStates) {
      for (Object aPublishedModel : publishedModel) {
        Item item = (Item) aPublishedModel;
        toggleStates.put(item.getKey(), item.isVisible() ? Boolean.TRUE : Boolean.FALSE);
      }
    }
    publishedModel.clear();
    for (Object aModel : model) {
      Item item = (Item) aModel;
      Item parent = item.getParent();
      if (restoreOldStates) {
        Boolean oldState = (Boolean) toggleStates.get(item.getKey());
        if (oldState != null) {
          item.setVisible(oldState);
        }
        if (parent != null) {
          oldState = (Boolean) toggleStates.get(parent.getKey());
          if (oldState != null) {
            parent.setVisible(oldState);
          }
        }
      }
      if (parent == null || parent.isVisible())
        publishedModel.add(item);
    }
  }
  
  private void buildModel() {
    model.clear();

    if (properties != null && !properties.isEmpty()) {
      List sortedProperties = sortProperties(properties);
      
      switch (mode) {
        case PropertySheet.VIEW_AS_FLAT_LIST:
          // just add all the properties without categories
          addPropertiesToModel(sortedProperties, null);
          break;
          
        case PropertySheet.VIEW_AS_CATEGORIES:
          // add properties by category
          List categories = sortCategories(getPropertyCategories(sortedProperties));

          for (Object category1 : categories) {
            String category = (String) category1;
            Item categoryItem = new Item(category, null);
            model.add(categoryItem);
            addPropertiesToModel(
                    sortProperties(getPropertiesForCategory(properties, category)),
                    categoryItem);
          }
          break;

        default:
          // should not happen
      }
    }

    visibilityChanged(restoreToggleStates);
    fireTableDataChanged();
  }

  protected List sortProperties(List localProperties) {
    List sortedProperties = new ArrayList(localProperties);
    if (sortingProperties) {
      if (propertySortingComparator == null) {
        // if no comparator was defined by the user, use the default
        propertySortingComparator = new PropertyComparator();
      }
      Collections.sort(sortedProperties, propertySortingComparator);
    }
    return sortedProperties;
  }
  
  protected List sortCategories(List localCategories) {
    List sortedCategories = new ArrayList(localCategories);
    if (sortingCategories) {
      if (categorySortingComparator == null) {
        // if no comparator was defined by the user, use the default
        categorySortingComparator = STRING_COMPARATOR;
      }
      Collections.sort(sortedCategories, categorySortingComparator);
    }
    return sortedCategories;
  }
  
  protected List getPropertyCategories(List localProperties) {
    List categories = new ArrayList();
    for (Object localProperty : localProperties) {
      Property property = (Property) localProperty;
      if (!categories.contains(property.getCategory()))
        categories.add(property.getCategory());
    }
    return categories;
  }

  /**
   * Add the specified properties to the model using the specified parent.
   *
   * @param localProperties the properties to add to the end of the model
   * @param parent the {@link Item} parent of these properties, null if none
   */
  private void addPropertiesToModel(List localProperties, Item parent) {
    for (Object localProperty : localProperties) {
      Property property = (Property) localProperty;
      Item propertyItem = new Item(property, parent);
      model.add(propertyItem);

      // add any sub-properties
      Property[] subProperties = property.getSubProperties();
      if (subProperties != null && subProperties.length > 0)
        addPropertiesToModel(Arrays.asList(subProperties), propertyItem);
    }
  }

  /**
   * Convenience method to get all the properties of one category.
   */
  private List getPropertiesForCategory(List localProperties, String category) {
    List categoryProperties = new ArrayList();
    for (Object localProperty : localProperties) {
      Property property = (Property) localProperty;
      if ((category == property.getCategory())
              || (category != null && category.equals(property.getCategory()))) {
        categoryProperties.add(property);
      }
    }
    return categoryProperties;
  }
  
  public class Item {
    private final String name;
    private Property property;
    private final Item parent;
    private boolean hasToggle = true;
    private boolean visible = true;

    private Item(String name, Item parent) {
      this.name = name;
      this.parent = parent;
      // this is not a property but a category, always has toggle
      hasToggle = true;
    }
    
    private Item(Property property, Item parent) {
      name = property.getDisplayName();
      this.property = property;
      this.parent = parent;
      visible = (property == null);
      
      // properties toggle if there are sub-properties
      Property[] subProperties = property.getSubProperties();
      hasToggle = subProperties != null && subProperties.length > 0;
    }

    public String getName() {
      return name;
    }
    
    public boolean isProperty() {
      return property != null;
    }
    
    public Property getProperty() {
      return property;
    }
    
    public Item getParent() {
      return parent;
    }
    
    public int getDepth() {
      int depth = 0;
      if (parent != null) {
        depth = parent.getDepth();
        if (parent.isProperty())
          ++depth;
      }
      return depth;
    }
    
    public boolean hasToggle() {
      return hasToggle;
    }

    public void toggle() {
      if (hasToggle()) {
        visible = !visible;
        visibilityChanged(false);
        fireTableDataChanged();
      }
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    public boolean isVisible() {
      return (parent == null || parent.isVisible()) && (!hasToggle || visible);
    }
    
    public String getKey() {
      StringBuilder key = new StringBuilder(name);
      Item itemParent = parent;
      while (itemParent != null) {
        key.append(':');
        key.append(itemParent.getName());
        itemParent = itemParent.getParent();
      }
      return key.toString();
    }

  }
  
  /**
   * The default comparator for Properties. Used if no other comparator is
   * defined.
   */
  public static class PropertyComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
      if (o1 instanceof Property && o2 instanceof Property) {
        Property prop1 = (Property) o1;
        Property prop2 = (Property) o2;
        if (prop1 == null) {
          return prop2==null?0:-1;
        } else {
          return STRING_COMPARATOR.compare(prop1.getDisplayName()==null?null:prop1.getDisplayName().toLowerCase(),
              prop2.getDisplayName() == null ? null : prop2.getDisplayName().toLowerCase());
        }
      } else {
        return 0;
      }
    }
  }

  private static final Comparator STRING_COMPARATOR =
    new NaturalOrderStringComparator();

  public static class NaturalOrderStringComparator implements Comparator {    
    @Override
    public int compare(Object o1, Object o2) {
      String s1 = (String) o1;
      String s2 = (String) o2;
      if (s1 == null) {
        return s2==null?0:-1;
      } else {
        return s2 == null ? 1 : s1.compareTo(s2);
      }
    }
  }
}
