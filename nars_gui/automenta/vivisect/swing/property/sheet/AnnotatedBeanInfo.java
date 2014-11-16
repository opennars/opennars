/**
 * Copyright (C) 2012 Bartosz Firyn (SarXos)
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package automenta.vivisect.swing.property.sheet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableCellRenderer;

import automenta.vivisect.swing.property.beans.BaseBeanInfo;
import automenta.vivisect.swing.property.beans.ExtendedPropertyDescriptor;
import automenta.vivisect.swing.property.beans.editor.AbstractPropertyEditor;
import automenta.vivisect.swing.property.sheet.annotation.PropertyInfo;


public class AnnotatedBeanInfo extends BaseBeanInfo {

	private static final class PropertyPair {

		private PropertyInfo property;
		private Field field;

		public PropertyPair(PropertyInfo property, Field field) {
			super();
			this.property = property;
			this.field = field;
		}

		public PropertyInfo getProperty() {
			return property;
		}

		public Field getField() {
			return field;
		}

	}

	private static final String DEFAULT_CATEGORY = "General";

	public AnnotatedBeanInfo(Class<?> type) {

		super(type);

		for (PropertyPair pair : getProperties(type)) {

			PropertyInfo property = pair.getProperty();
			Field field = pair.getField();

			String name = field.getName();

			ExtendedPropertyDescriptor descriptor = addProperty(name);

			String displayName = property.name();
			if ("#default".equals(displayName)) {
				displayName = name;
			}

			descriptor.setDisplayName(displayName);

			String category = property.category();
			if (category.isEmpty()) {
				category = DEFAULT_CATEGORY;
			}

			descriptor.setCategory(category);

			String description = property.description();
			if (!description.isEmpty()) {
				descriptor.setShortDescription(description);
			}

			Class<? extends AbstractPropertyEditor> editorClass = property.editor();
			if (editorClass != AbstractPropertyEditor.class) {
				descriptor.setPropertyEditorClass(editorClass);
			}

			Class<? extends TableCellRenderer> renderer = property.renderer();
			if (renderer != TableCellRenderer.class) {
				descriptor.setPropertyTableRendererClass(renderer);
			}

			if (property.readonly()) {
				descriptor.setReadOnly();
			}

			descriptor.setExpert(property.expert());
			descriptor.setPreferred(property.important());
			descriptor.setConstrained(property.constrained());
			descriptor.setHidden(property.hidden());
			descriptor.setBound(true);
		}
	}

	private List<PropertyPair> getProperties(Class<?> type) {

		List<PropertyPair> pairs = new ArrayList<PropertyPair>();
		Field[] fields = type.getDeclaredFields();

		for (Field field : fields) {
			PropertyInfo property = field.getAnnotation(PropertyInfo.class);
			if (property != null) {
				pairs.add(new PropertyPair(property, field));
			}
		}

		return pairs;

	}
}
