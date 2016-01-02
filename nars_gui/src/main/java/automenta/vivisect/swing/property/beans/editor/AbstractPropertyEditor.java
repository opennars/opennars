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
package automenta.vivisect.swing.property.beans.editor;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

/**
 * AbstractPropertyEditor. <br>
 *  
 */
public class AbstractPropertyEditor implements PropertyEditor {

  protected Component editor;
  private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);

  @Override
  public boolean isPaintable() {
    return false;
  }

  @Override
  public boolean supportsCustomEditor() {
    return false;
  }

  @Override
  public Component getCustomEditor() {
    return editor;
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    listeners.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    listeners.removePropertyChangeListener(listener);
  }

  protected void firePropertyChange(Object oldValue, Object newValue) {
    listeners.firePropertyChange("value", oldValue, newValue);
  }
    
  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public void setValue(Object value) {
  }

  @Override
  public String getAsText() {
    return null;
  }

  @Override
  public String getJavaInitializationString() {
    return null;
  }

  @Override
  public String[] getTags() {
    return null;
  }

  @Override
  public void setAsText(String text) throws IllegalArgumentException {
  }

  @Override
  public void paintValue(Graphics gfx, Rectangle box) {
  }

}
