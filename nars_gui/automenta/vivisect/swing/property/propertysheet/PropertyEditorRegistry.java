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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import automenta.vivisect.swing.property.beans.editor.InsetsPropertyEditor;
import automenta.vivisect.swing.property.beans.editor.JCalendarDatePropertyEditor;
import automenta.vivisect.swing.property.beans.editor.RectanglePropertyEditor;
import automenta.vivisect.swing.property.beans.editor.StringPropertyEditor;
import automenta.vivisect.swing.property.sheet.editor.BooleanEditor;
import automenta.vivisect.swing.property.sheet.editor.CharacterEditor;
import automenta.vivisect.swing.property.sheet.editor.ColorEditor;
import automenta.vivisect.swing.property.sheet.editor.DimensionEditor;
import automenta.vivisect.swing.property.sheet.editor.EnumEditor;
import automenta.vivisect.swing.property.sheet.editor.FileEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.ByteEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.DoubleEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.FloatEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.IntegerEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.LongEditor;
import automenta.vivisect.swing.property.sheet.editor.NumberEditor.ShortEditor;


/**
 * Mapping between Properties, Property Types and Property Editors.
 */
public class PropertyEditorRegistry implements PropertyEditorFactory {

	private Map<Class<?>, Object> typeToEditor;
	private Map<Property, Object> propertyToEditor;

	public PropertyEditorRegistry() {
		typeToEditor = new HashMap<Class<?>, Object>();
		propertyToEditor = new HashMap<Property, Object>();
		registerDefaults();
	}

	@Override
	public PropertyEditor createPropertyEditor(Property property) {
		return getEditor(property);
	}

	/**
	 * Gets an editor for the given property. The lookup is as follow:
	 * <ul>
	 * <li>if propertyDescriptor.getPropertyEditorClass() returns a valid value,
	 * it is returned, else,
	 * <li>if an editor was registered with
	 * {@link #registerEditor(Property, PropertyEditor)}, it is returned, else</li>
	 * <li>if an editor class was registered with
	 * {@link #registerEditor(Property, Class)}, it is returned, else
	 * <li>
	 * <li>look for editor for the property type using {@link #getEditor(Class)}
	 * .it is returned, else</li>
	 * <li>look for editor using PropertyEditorManager.findEditor(Class);</li>
	 * </ul>
	 * 
	 * @param property
	 * @return an editor suitable for the Property.
	 */
	public synchronized PropertyEditor getEditor(Property property) {

		PropertyEditor editor = null;
		if (property instanceof PropertyDescriptorAdapter) {
			PropertyDescriptor descriptor = ((PropertyDescriptorAdapter) property).getDescriptor();
			if (descriptor != null) {
				Class<?> clz = descriptor.getPropertyEditorClass();
				if (clz != null) {
					editor = descriptor.createPropertyEditor(property);
				}
			}
		}

		if (editor == null) {
			Object value = propertyToEditor.get(property);
			if (value instanceof PropertyEditor) {
				editor = (PropertyEditor) value;
			} else if (value instanceof Class) {
				editor = loadPropertyEditor((Class<?>) value);
			} else {
				editor = createEditor(property);
			}
		}
		if ((editor == null) && (property instanceof PropertyDescriptorAdapter)) {
			PropertyDescriptor descriptor = ((PropertyDescriptorAdapter) property).getDescriptor();
			Class<?> clz = descriptor.getPropertyType();
			editor = PropertyEditorManager.findEditor(clz);
		}
		return editor;
	}

	/**
	 * Load PropertyEditor from clazz through reflection.
	 * 
	 * @param clazz Class to load from.
	 * @return Loaded propertyEditor
	 */
	private PropertyEditor loadPropertyEditor(Class<?> clazz) {
		PropertyEditor editor = null;
		try {
			editor = (PropertyEditor) clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return editor;
	}

	/**
	 * Gets an editor for the given property type. The lookup is as follow:
	 * <ul>
	 * <li>if an editor was registered with
	 * {@link #registerEditor(Class, PropertyEditor)}, it is returned, else</li>
	 * <li>if an editor class was registered with
	 * {@link #registerEditor(Class, Class)}, it is returned, else
	 * <li>
	 * <li>it returns null.</li>
	 * </ul>
	 * 
	 * @param clazz
	 * @return an editor suitable for the Property type or null if none found
	 */
	public synchronized PropertyEditor createEditor(Property property) {

		Class<?> clazz = property.getType();

		PropertyEditor editor = null;
		Object value = typeToEditor.get(clazz);

		if (value instanceof PropertyEditor) {
			editor = (PropertyEditor) value;
		} else if (value instanceof Class) {

			Class<?> cls = (Class<?>) value;

			Constructor<?> ctor = null;
			try {
				ctor = cls.getConstructor(new Class[] { Object.class });
			} catch (Exception ex) {
				// fall through
			}

			try {
				if (ctor == null) {
					editor = (PropertyEditor) cls.newInstance();
				} else {
					editor = (PropertyEditor) ctor.newInstance(new Object[] { property });
				}
			} catch (Exception e) {
				throw new RuntimeException("PropertyEditor not instantiated", e);
			}
		}

		if (editor == null) {
			if (clazz.isEnum()) {
				editor = new EnumEditor(property);
			}
		}

		return editor;
	}

	public synchronized void registerEditor(Class<?> type, Class<?> editorClass) {
		typeToEditor.put(type, editorClass);
	}

	public synchronized void registerEditor(Class<?> type, PropertyEditor editor) {
		typeToEditor.put(type, editor);
	}

	public synchronized void unregisterEditor(Class<?> type) {
		typeToEditor.remove(type);
	}

	public synchronized void registerEditor(Property property, Class<?> editorClass) {
		propertyToEditor.put(property, editorClass);
	}

	public synchronized void registerEditor(Property property, PropertyEditor editor) {
		propertyToEditor.put(property, editor);
	}

	public synchronized void unregisterEditor(Property property) {
		propertyToEditor.remove(property);
	}

	/**
	 * Adds default editors. This method is called by the constructor but may be
	 * called later to reset any customizations made through the
	 * <code>registerEditor</code> methods. <b>Note: if overriden,
	 * <code>super.registerDefaults()</code> must be called before plugging
	 * custom defaults. </b>
	 */
	public void registerDefaults() {

		typeToEditor.clear();
		propertyToEditor.clear();

		// our editors
		registerEditor(String.class, StringPropertyEditor.class);

		registerEditor(char.class, CharacterEditor.class);
		registerEditor(Character.class, CharacterEditor.class);

		registerEditor(double.class, DoubleEditor.class);
		registerEditor(Double.class, DoubleEditor.class);
		registerEditor(float.class, FloatEditor.class);
		registerEditor(Float.class, FloatEditor.class);
		registerEditor(int.class, IntegerEditor.class);
		registerEditor(Integer.class, IntegerEditor.class);
		registerEditor(long.class, LongEditor.class);
		registerEditor(Long.class, LongEditor.class);
		registerEditor(short.class, ShortEditor.class);
		registerEditor(Short.class, ShortEditor.class);
		registerEditor(byte.class, ByteEditor.class);
		registerEditor(Byte.class, ByteEditor.class);
		registerEditor(BigInteger.class, LongEditor.class);
		registerEditor(BigDecimal.class, DoubleEditor.class);

		registerEditor(boolean.class, BooleanEditor.class);
		registerEditor(Boolean.class, BooleanEditor.class);

		registerEditor(File.class, FileEditor.class);

		// awt object editors
		registerEditor(Color.class, ColorEditor.class);
		registerEditor(Dimension.class, DimensionEditor.class);
		registerEditor(Insets.class, InsetsPropertyEditor.class);
		registerEditor(Rectangle.class, RectanglePropertyEditor.class);
		registerEditor(Date.class, JCalendarDatePropertyEditor.class);

		try {
			Class<?> fontEditor = Class.forName("com.l2fprod.common.beans.editor.FontPropertyEditor");
			registerEditor(Font.class, fontEditor);
		} catch (Exception e) {
			// FontPropertyEditor might not be there when using the split jars
		}

	}
}