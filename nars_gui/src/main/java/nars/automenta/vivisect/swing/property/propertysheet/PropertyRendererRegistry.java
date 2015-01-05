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
import java.awt.Point;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableCellRenderer;

import automenta.vivisect.swing.property.beans.ExtendedPropertyDescriptor;
import automenta.vivisect.swing.property.sheet.renderer.BooleanRenderer;
import automenta.vivisect.swing.property.sheet.renderer.DimensionRenderer;
import automenta.vivisect.swing.property.sheet.renderer.EnumRenderer;
import automenta.vivisect.swing.property.sheet.renderer.NumberRenderer;
import automenta.vivisect.swing.property.sheet.renderer.PointRenderer;
import automenta.vivisect.swing.property.swing.renderer.ColorCellRenderer;
import automenta.vivisect.swing.property.swing.renderer.DateRenderer;
import automenta.vivisect.swing.property.swing.renderer.DefaultCellRenderer;


/**
 * Mapping between Properties, Property Types and Renderers.
 */
public class PropertyRendererRegistry implements PropertyRendererFactory {

	private static final Map<Class<?>, Class<?>> TYPE_TO_CLASS = new HashMap<Class<?>, Class<?>>();
	private static final Map<Class<?>, TableCellRenderer> TYPE_TO_RENDERER = new HashMap<Class<?>, TableCellRenderer>();

	private Map typeToRenderer;
	private Map propertyToRenderer;

	public PropertyRendererRegistry() {
		typeToRenderer = new HashMap();
		propertyToRenderer = new HashMap();
		registerDefaults();
	}

	@Override
	public TableCellRenderer createTableCellRenderer(Property property) {
		return getRenderer(property);
	}

	@Override
	public TableCellRenderer createTableCellRenderer(Class type) {
		return getRenderer(null, type);
	}

	/**
	 * Gets a renderer for the given property. The lookup is as follow:
	 * <ul>
	 * <li>if a renderer was registered with
	 * {@link ExtendedPropertyDescriptor#setPropertyTableRendererClass(Class)} -
	 * BeanInfo, it is returned, else</li>
	 * <li>if a renderer was registered with
	 * {@link #registerRenderer(Property, TableCellRenderer)}, it is returned,
	 * else</li>
	 * <li>if a renderer class was registered with
	 * {@link #registerRenderer(Property, Class)}, it is returned, else
	 * <li>
	 * <li>look for renderer for the property type using
	 * {@link #getRenderer(Class)}.</li>
	 * </ul>
	 * 
	 * @param property
	 * @return a renderer suitable for the Property.
	 */
	public synchronized TableCellRenderer getRenderer(Property property) {

		// editors bound to the property descriptor have the highest priority
		TableCellRenderer renderer = null;

		if (property instanceof PropertyDescriptorAdapter) {

			PropertyDescriptorAdapter pda = (PropertyDescriptorAdapter) property;
			PropertyDescriptor descriptor = pda.getDescriptor();

			if (descriptor instanceof ExtendedPropertyDescriptor) {

				ExtendedPropertyDescriptor epd = (ExtendedPropertyDescriptor) descriptor;
				Class<?> clazz = epd.getPropertyTableRendererClass();

				if (clazz != null) {
					return createRenderer(property, clazz);
				}
			}
		}

		Object value = propertyToRenderer.get(property);

		if (value instanceof TableCellRenderer) {
			renderer = (TableCellRenderer) value;
		} else if (value instanceof Class) {
			try {
				renderer = createRenderer(property, (Class<?>) value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			renderer = getRenderer(property, property.getType());
		}

		return renderer;
	}

	/**
	 * Create renderer for specific property from specific class.
	 * 
	 * @param property the property for which renderer should be created
	 * @param clazz the class of renderer to create
	 * @return New table cell renderer instance
	 */
	private TableCellRenderer createRenderer(Property property, Class<?> clazz) {

		TableCellRenderer renderer = null;
		Constructor<?> ctor = null;

		try {
			ctor = clazz.getConstructor(new Class[] { Object.class });
		} catch (Exception ex) {
			// fall through
		}

		try {
			if (ctor == null) {
				renderer = (TableCellRenderer) clazz.newInstance();
			} else {
				renderer = (TableCellRenderer) ctor.newInstance(new Object[] { property });
			}
		} catch (Exception e) {
			throw new RuntimeException("PropertyEditor not instantiated", e);
		}

		return renderer;
	}

	/**
	 * Gets a renderer for the given property type. The lookup is as follow:
	 * <ul>
	 * <li>if a renderer was registered with
	 * {@link #registerRenderer(Class, TableCellRenderer)}, it is returned, else
	 * </li>
	 * <li>if a renderer class was registered with
	 * {@link #registerRenderer(Class, Class)}, it is returned, else
	 * <li>
	 * <li>it returns null.</li>
	 * </ul>
	 * 
	 * @param propertyType
	 * @return a renderer editor suitable for the Property type or null if none
	 *         found
	 */
	public synchronized TableCellRenderer getRenderer(Property property, Class<?> propertyType) {

		TableCellRenderer renderer = null;
		Object value = typeToRenderer.get(propertyType);

		if (value instanceof TableCellRenderer) {
			renderer = (TableCellRenderer) value;
		} else if (value instanceof Class) {

			Class<?> clazz = (Class<?>) value;
			Constructor<?> ctor = null;

			try {
				ctor = clazz.getConstructor(new Class[] { Object.class });
			} catch (Exception ex) {
				// fall through
			}

			try {
				if (ctor == null) {
					renderer = (TableCellRenderer) clazz.newInstance();
				} else {
					renderer = (TableCellRenderer) ctor.newInstance(new Object[] { property });
				}
			} catch (Exception e) {
				throw new RuntimeException("PropertyEditor not instantiated", e);
			}
		}

		if (renderer == null) {
			if (property != null) {
				Class<?> type = property.getType();
				if (type != null && type.isEnum()) {
					return new EnumRenderer();
				}
			}
		}

		return renderer;
	}

	public synchronized void registerRenderer(Class type, Class rendererClass) {
		typeToRenderer.put(type, rendererClass);
	}

	public synchronized void registerRenderer(Class type, TableCellRenderer renderer) {
		typeToRenderer.put(type, renderer);
	}

	public synchronized void unregisterRenderer(Class type) {
		typeToRenderer.remove(type);
	}

	public synchronized void registerRenderer(Property property, Class rendererClass) {
		propertyToRenderer.put(property, rendererClass);
	}

	public synchronized void registerRenderer(Property property,
	TableCellRenderer renderer) {
		propertyToRenderer.put(property, renderer);
	}

	public synchronized void unregisterRenderer(Property property) {
		propertyToRenderer.remove(property);
	}

	/**
	 * Adds default renderers. This method is called by the constructor but may
	 * be called later to reset any customizations made through the
	 * <code>registerRenderer</code> methods. <b>Note: if overriden,
	 * <code>super.registerDefaults()</code> must be called before plugging
	 * custom defaults. </b>
	 */
	public void registerDefaults() {
		typeToRenderer.clear();
		propertyToRenderer.clear();

		BooleanRenderer booleanRenderer = new BooleanRenderer();
		registerRenderer(boolean.class, booleanRenderer);
		registerRenderer(Boolean.class, booleanRenderer);

		DefaultCellRenderer renderer = new DefaultCellRenderer();
		registerRenderer(Object.class, renderer);
		registerRenderer(char.class, renderer);
		registerRenderer(Character.class, renderer);

		// numbers
		NumberRenderer intRenderer = new NumberRenderer(0);
		NumberRenderer floatRenderer = new NumberRenderer(4);
		NumberRenderer doubleRenderer = new NumberRenderer(10);
		registerRenderer(byte.class, intRenderer);
		registerRenderer(Byte.class, intRenderer);
		registerRenderer(double.class, doubleRenderer);
		registerRenderer(Double.class, doubleRenderer);
		registerRenderer(float.class, floatRenderer);
		registerRenderer(Float.class, floatRenderer);
		registerRenderer(int.class, intRenderer);
		registerRenderer(Integer.class, intRenderer);
		registerRenderer(long.class, intRenderer);
		registerRenderer(Long.class, intRenderer);
		registerRenderer(short.class, intRenderer);
		registerRenderer(Short.class, intRenderer);

		registerRenderer(Date.class, new DateRenderer());

		// awt classes
		registerRenderer(Color.class, new ColorCellRenderer());
		registerRenderer(Point.class, new PointRenderer());
		registerRenderer(Dimension.class, new DimensionRenderer());
	}

}
