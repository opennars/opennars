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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

/**
 * AbstractProperty. <br>
 *  
 */
public abstract class AbstractProperty implements Property {

  private Object value;
  
  // PropertyChangeListeners are not serialized.
  private transient PropertyChangeSupport listeners =
    new PropertyChangeSupport(this);

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  @SuppressWarnings("CloneReturnsClassType")
  public Object clone() {
    AbstractProperty clone = null;
    try {
      clone = (AbstractProperty)super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);      
    }
  }
  
  @Override
  public void setValue(Object value) {
    Object oldValue = this.value;
    this.value = value;
    if (!Objects.equals(value, oldValue))
      firePropertyChange(oldValue, getValue());
  }

  protected void initializeValue(Object value) {
    this.value = value;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    listeners.addPropertyChangeListener(listener);
    Property[] subProperties = getSubProperties();
    if (subProperties != null)
      for (Property subProperty : subProperties) subProperty.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    listeners.removePropertyChangeListener(listener);
    Property[] subProperties = getSubProperties();
    if (subProperties != null)
      for (Property subProperty : subProperties) subProperty.removePropertyChangeListener(listener);
  }

  protected void firePropertyChange(Object oldValue, Object newValue) {
    listeners.firePropertyChange("value", oldValue, newValue);
  }

  private void readObject(ObjectInputStream in) throws IOException,
    ClassNotFoundException {
    in.defaultReadObject();
    listeners = new PropertyChangeSupport(this);    
  }
  
  @Override
  public Property getParentProperty() {
  	return null;
  }
  
  @Override
  public Property[] getSubProperties() {
  	return null;
  }
}
