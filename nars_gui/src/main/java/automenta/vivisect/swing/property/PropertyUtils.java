package automenta.vivisect.swing.property;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import automenta.vivisect.swing.property.propertysheet.PropertySheet;
import automenta.vivisect.swing.property.propertysheet.PropertySheetDialog;
import automenta.vivisect.swing.property.propertysheet.PropertySheetPanel;
import automenta.vivisect.swing.property.swing.BannerPanel;

public class PropertyUtils {

	private static SerializableProperty createProperty(Object obj, Field f,
			boolean forEdit) {
		Property a = f.getAnnotation(Property.class);

		if (a != null) {

			f.setAccessible(true);
			String name = f.getName();
			String displayName = a.name();
			String desc = a.description();
			Class<? extends PropertyEditor> editClass = null;
			String category = a.category();

			if (a.name().length() == 0)
				displayName = f.getName();

			if (a.description().length() == 0) {
				desc = displayName;
			}

			if (a.editorClass() != PropertyEditor.class) {
				editClass = a.editorClass();
			}

			if (category == null || category.length() == 0) {
				category = "Base";
			}

			Object o = null;
			try {
				o = f.get(null);
			} catch (Exception e) {
				// nothing
			}
			if (o == null) {
				try {
					o = f.get(obj);
				} catch (Exception e) {
					// nothing
				}
			}

			SerializableProperty pp = new SerializableProperty(name,
					f.getType(), o);
			pp.setShortDescription(desc);
			pp.setEditable(a.editable());
			pp.setDisplayName(displayName);
			pp.setEditor(editClass);
			if (category != null && category.length() > 0) {
				pp.setCategory(category);
			}
			return pp;
		}
		return null;
	}

	private static Field[] getFields(Object o) {
		Class<?> c;
		if (o instanceof Class<?>)
			c = (Class<?>) o;
		else
			c = o.getClass();

		HashSet<Field> fields = new HashSet<>();
		
		while (c != Object.class) {
			for (Field f : c.getFields())
				fields.add(f);
			for (Field f : c.getDeclaredFields()) {
				f.setAccessible(true);
				fields.add(f);
			}
			c = c.getSuperclass(); 
		} 
		
		return fields.toArray(new Field[0]);
	}

	public static LinkedHashMap<String, SerializableProperty> getProperties(
			Object obj, boolean editable) {
		LinkedHashMap<String, SerializableProperty> props = new LinkedHashMap<String, SerializableProperty>();

		for (Field f : getFields(obj)) {
			SerializableProperty pp = createProperty(obj, f, editable);
			if (pp != null)
				props.put(f.getName(), pp);
		}
		return props;
	}

	public static void setProperties(Object obj,
			LinkedHashMap<String, SerializableProperty> props) {
		setProperties(obj, props, true);
	}

	public static void setProperties(Object obj,
			LinkedHashMap<String, SerializableProperty> props,
			boolean triggerEvents) {
		Class<? extends Object> providerClass = obj instanceof Class<?> ? (Class<?>) obj
				: obj.getClass();

		String name;
		SerializableProperty property;
		Object propertyValue;
		for (Field f : getFields(providerClass)) {
			Property a = f.getAnnotation(Property.class);
			if (a == null)
				continue;

			name = f.getName();

			property = props.get(name);
			if (property == null) {
				Logger.getGlobal().log(Level.WARNING,
						"Property " + name + " will not be saved.");
				continue;
			}
			try {
				propertyValue = property.getValue();
				Object oldValue = f.get(obj);

				try {
					f.set(obj, propertyValue);
				} catch (Exception e) {
					switch (f.getGenericType().toString()) {
					case "int":
					case "Integer":
						f.setInt(obj, (int) Double.parseDouble(propertyValue
								.toString()));
						propertyValue = (int) Double.parseDouble(propertyValue
								.toString());
						break;
					case "long":
					case "Long":
						f.setLong(obj, (long) Double.parseDouble(propertyValue
								.toString()));
						propertyValue = (long) Double.parseDouble(propertyValue
								.toString());
						break;
					case "short":
					case "Short":
						f.setShort(obj, (short) Double
								.parseDouble(propertyValue.toString()));
						propertyValue = (short) Double
								.parseDouble(propertyValue.toString());
						break;
					case "byte":
					case "Byte":
						f.setByte(obj, (byte) Double.parseDouble(propertyValue
								.toString()));
						propertyValue = (byte) Double.parseDouble(propertyValue
								.toString());
						break;
					case "float":
					case "Float":
						f.setFloat(obj, (float) Double
								.parseDouble(propertyValue.toString()));
						propertyValue = (float) Double
								.parseDouble(propertyValue.toString());
						break;
					case "double":
					case "Double":
						f.setDouble(obj,
								Double.parseDouble(propertyValue.toString()));
						break;
					default:
						break;
					}
				}

				if (triggerEvents && !oldValue.equals(propertyValue)
						&& obj instanceof PropertyChangeListener)
					((PropertyChangeListener) obj)
							.propertyChange(new PropertyChangeEvent(
									PropertyUtils.class, f.getName(), oldValue,
									propertyValue));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

	}

	public static void saveProperties(Object obj, File f) throws IOException {
		LinkedHashMap<String, SerializableProperty> props = getProperties(obj,
				true);
		Properties p = new Properties();
		for (SerializableProperty prop : props.values())
			p.setProperty(prop.getName(), prop.toString());

		p.store(new FileWriter(f), "Properties saved on " + new Date());
	}

	public static String saveProperties(Object obj) throws IOException {
		LinkedHashMap<String, SerializableProperty> props = getProperties(obj,
				true);
		Properties p = new Properties();
		for (SerializableProperty prop : props.values())
			p.setProperty(prop.getName(), prop.toString());

		StringWriter writer = new StringWriter();
		p.store(writer, null);
		return writer.toString().replaceAll("^\\#.*", "").trim()+"\n";
	}
	
	public static void setProperties(Object obj, Properties p,
			boolean triggerEvents) {
		LinkedHashMap<String, SerializableProperty> props = getProperties(obj,
				true);
		for (Entry<Object, Object> entry : p.entrySet()) {
			if (props.containsKey(entry.getKey())) {
				SerializableProperty sp = props.get(entry.getKey());
				sp.fromString("" + entry.getValue());
			}
		}
		setProperties(obj, props, triggerEvents);
	}

	public static void loadProperties(Object obj, String properties,
			boolean triggerEvents) throws IOException {
		Properties p = new Properties();
		StringReader reader = new StringReader(properties);
		p.load(reader);
		setProperties(obj, p, triggerEvents);
	}

	public static void loadProperties(Object obj, File f) throws IOException {
		loadProperties(obj, f, true);
	}

	public static void loadProperties(Object obj, File f, boolean triggerEvents)
			throws IOException {
		Properties p = new Properties();
		p.load(new FileReader(f));
		setProperties(obj, p, triggerEvents);
	}

	public static PropertySheetPanel getPropsPanel(Object obj, boolean editable) {
		PropertySheetPanel psp = new PropertySheetPanel();
		psp.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		psp.setToolBarVisible(false);
		psp.setEnabled(true);
		psp.setSortingCategories(true);
		psp.setDescriptionVisible(true);
		Collection<SerializableProperty> props = getProperties(obj, editable)
				.values();

		for (SerializableProperty p : props) {
			p.setEditable(editable && p.isEditable());
			psp.addProperty(p);
		}

		return psp;
	}

	public static void editProperties(Window parent, Object obj,
			boolean editable) {

		final PropertySheetPanel psp = getPropsPanel(obj, editable);

		final PropertySheetDialog propertySheetDialog = createWindow(parent,
				editable, psp, "Properties of "
						+ obj.getClass().getSimpleName());

		if (!propertySheetDialog.ask()) {
			// cancelled
			return;
		}

		LinkedHashMap<String, SerializableProperty> newProps = new LinkedHashMap<>();
		for (automenta.vivisect.swing.property.propertysheet.Property p : psp.getProperties())
			newProps.put(p.getName(), new SerializableProperty(p));

		setProperties(obj, newProps, true);

	}

	public static PropertySheetDialog createWindow(Window parent,
			boolean editable, final PropertySheetPanel psp, String title) {
		final PropertySheetDialog propertySheetDialog;
		if (parent instanceof Dialog) {
			Dialog pDialog = (Dialog) parent;
			propertySheetDialog = new PropertySheetDialog(pDialog);
		} else if (parent instanceof Frame) {
			Frame pFrame = (Frame) parent;
			propertySheetDialog = new PropertySheetDialog(pFrame);
		} else {
			propertySheetDialog = new PropertySheetDialog() {
				private static final long serialVersionUID = 1L;

				@Override
				public void ok() {
					if (psp.getTable().getEditorComponent() != null)
						psp.getTable().commitEditing();
					super.ok();
				};
			};
		}
		if (editable) {
			propertySheetDialog
					.setDialogMode(PropertySheetDialog.OK_CANCEL_DIALOG);
		} else {
			propertySheetDialog.setDialogMode(PropertySheetDialog.CLOSE_DIALOG);
		}

		int sb = 1;
		for (Component compL0 : propertySheetDialog.getRootPane()
				.getComponents()) {
			if (compL0 instanceof JLayeredPane) {
				for (Component compL01 : ((JLayeredPane) compL0)
						.getComponents()) {
					if (!(compL01 instanceof JPanel))
						continue;
					for (Component compL1 : ((JPanel) compL01).getComponents()) {
						if (compL1 instanceof BannerPanel)
							continue;
						if (compL1 instanceof JPanel) {
							for (Component compL2 : ((JPanel) compL1)
									.getComponents()) {
								for (Component compL3 : ((JPanel) compL2)
										.getComponents()) {
									if (compL3 instanceof JButton) {
										if (propertySheetDialog.getDialogMode() == PropertySheetDialog.OK_CANCEL_DIALOG
												&& sb == 1) {
											((JButton) compL3).setText("OK");
											sb--;
										} else if (propertySheetDialog
												.getDialogMode() == PropertySheetDialog.CLOSE_DIALOG
												|| sb == 0) {
											((JButton) compL3)
													.setText(propertySheetDialog
															.getDialogMode() == PropertySheetDialog.CLOSE_DIALOG ? "Close"
															: "Cancel");
											sb--;
											break;
										}
										if (sb < 0)
											break;
									}
								}
								if (sb < 0)
									break;
							}
						}
					}
				}
			}
		}

		if (title != null) {
			propertySheetDialog.getBanner().setTitle(title);
			propertySheetDialog.setTitle(title);
		}
		// propertySheetDialog.setIconImage(ImageUtils.getImage("images/menus/settings.png"));
		// propertySheetDialog.getBanner().setIcon(ImageUtils.getIcon("images/settings.png"));
		propertySheetDialog.getContentPane().add(psp);
		propertySheetDialog.pack();

		propertySheetDialog.setLocationRelativeTo(null);
		propertySheetDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		return propertySheetDialog;
	}
}