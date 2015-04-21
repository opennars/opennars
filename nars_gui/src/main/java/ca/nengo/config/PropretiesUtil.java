/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ConfigUtil.java". Description:
"Configuration-related utility methods"

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
 * Created on 22-Dec-07
 */
package ca.nengo.config;

import automenta.vivisect.swing.ReflectPanel;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.impl.ListPropertyImpl;
import ca.nengo.config.impl.NamedValuePropertyImpl;
import ca.nengo.config.impl.SingleValuedPropertyImpl;
import ca.nengo.config.ui.*;
import nars.gui.output.VerticalPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Configuration-related utility methods.
 *
 * @author Bryan Tripp
 */
public class PropretiesUtil {

	/**
	 * Shows a tree in which object properties can be edited.
	 *
	 * @param owner Parent dialog in which to put GUI elements
	 * @param o Object to investigate
	 */
	public static void configure(Dialog owner, Object o) {
		configure(null, owner, o);
	}

	/**
	 * Shows a tree in which object properties can be edited.
	 *
	 * @param owner Parent frame in which to put GUI elements
	 * @param o Object to investigate
	 */
	public static void configure(Frame owner, Object o) {
		configure(owner, null, o);
	}

	/**
	 * Shows a tree in which object properties can be edited.
	 *
	 * @param o The Object to configure
	 */
	private static void configure(Frame owner, Dialog owner2, Object o) {

		final JDialog dialog;
		if (owner != null) {
			dialog = new JDialog(owner, o.toString());
		} else {
			dialog = new JDialog(owner2, o.toString());
		}

		dialog.getContentPane().setLayout(new BorderLayout());

        VerticalPanel centerPane = new VerticalPanel();
        centerPane.add(new ReflectPanel(o), 0);
        centerPane.add(createConfigurationPane(o), 1);

        dialog.setPreferredSize(new Dimension(400, 600));
		dialog.getContentPane().add(centerPane, BorderLayout.CENTER);

		JButton doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(doneButton);

//		dialog.setModal(true); //this prevents help popups
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		dialog.pack();
		if (owner != null) {
			dialog.setLocationRelativeTo(owner);// centers on screen
		} else {
			dialog.setLocationRelativeTo(owner2);// centers on screen
		}
		dialog.setVisible(true);
	}

	/**
	 *
	 *
	 */
	public static class ConfigurationPane extends JPanel {

		private static final long serialVersionUID = 1L;

		private ConfigurationTreeCellRenderer myCellRenderer;
		private JTree myTree;

		/**
		 * @param o Object to configure
		 */
		public ConfigurationPane(Object o) {
			super(new BorderLayout());
			init(o);

			//note: setting preferred size of tree itself prevents viewport from expanding
			this.setPreferredSize(new Dimension(300, 300));
            add(myTree, BorderLayout.CENTER);
		}

		private void init(Object o) {
			ConfigurationTreeModel model = new ConfigurationTreeModel(o);

			myTree = new JTree(model);

			if (myTree.getUI().getClass().getName().contains("apple.laf")) {
				AquaTreeUI aui = new AquaTreeUI();
				myTree.setUI(aui);
				aui.setRowHeight(0); //must be done after setUI(...)
			}

			myTree.setScrollsOnExpand(true);

			myTree.setEditable(true);
			myTree.setCellEditor(new ConfigurationTreeCellEditor(myTree));

			myTree.addMouseListener(new ConfigurationTreePopupListener(myTree, model));

			// shows help when F1 is pressed
			myTree.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					Object selected = (myTree.getSelectionPath() == null) ? null : myTree
							.getSelectionPath().getLastPathComponent();
					if (e.getKeyCode() == 112 && selected instanceof Property) {
						String documentation = ((Property) selected).getDocumentation();
						if (documentation != null) {
                            PropretiesUtil.showHelp(documentation);
                        }
					}
				}
			});
			myCellRenderer = new ConfigurationTreeCellRenderer();

			myTree.setCellRenderer(myCellRenderer);

			ToolTipManager.sharedInstance().registerComponent(myTree);
			myTree.setRowHeight(0);
		}

		/**
		 * @return Cell Renderer object
		 */
		public ConfigurationTreeCellRenderer getCellRenderer() {
			return myCellRenderer;
		}

		/**
		 * @return Tree object
		 */
		public JTree getTree() {
			return myTree;
		}
	}

	/**
	 * Shows a tree in which object properties can be edited.
	 *
	 * @param o The Object to configure
	 * @return A Scroll Pane containing the configuration properties
	 */
	public static ConfigurationPane createConfigurationPane(Object o) {

		return new ConfigurationPane(o);
	}

	/**
	 * @param configurable An object
	 * @return configurable.getConfiguration() : Configuration if such a method is defined for configurable,
	 * 		otherwise ConfigUtil.defaultConfiguration(configurable).
	 */
	public static Configuration getConfiguration(Object configurable) {
		Configuration result = null;
		Method[] methods = configurable.getClass().getMethods();
		for (int i = 0; i < methods.length && result == null; i++) {
			if (methods[i].getName().equals("getConfiguration")
					&& methods[i].getParameterTypes().length == 0
					&& Configuration.class.isAssignableFrom(methods[i].getReturnType())) {

				try {
					result = (Configuration) methods[i].invoke(configurable);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (result == null) {
			result = defaultConfiguration(configurable);
		}

		return result;
	}

	/**
	 * @param configurable An Object
	 * @return A default Configuration with properties of the object, based on reflection of the
	 * 		object's getters and setters.
	 */
	public static ConfigurationImpl defaultConfiguration(Object configurable) {
		ConfigurationImpl result = new ConfigurationImpl(configurable);

		Method[] methods = configurable.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Class<?> returnType = methods[i].getReturnType();
			String propName = getPropertyName(methods[i]);

			if (isSingleValueGetter(methods[i])
					&& !methods[i].getName().equals("getClass")
					&& !methods[i].getName().equals("getConfiguration")
					&& !isCounter(methods[i])) {

				result.defineSingleValuedProperty(propName, returnType, false);
			} else if (isIndexedGetter(methods[i]) && !result.getPropertyNames().contains(propName)) {
				Property p = ListPropertyImpl.getListProperty(result, propName, returnType);
				if (p != null) {
                    result.defineProperty(p);
                }
			} else if (isNamedGetter(methods[i]) && !result.getPropertyNames().contains(propName)) {
				Property p = NamedValuePropertyImpl.getNamedValueProperty(result, propName, returnType);
				if (p != null) {
                    result.defineProperty(p);
                }
			}
		}

		//look for additional array, list, and map getters
		for (int i = 0; i < methods.length; i++) {
			Type returnType = methods[i].getGenericReturnType();
			String propName = getPropertyName(methods[i]);

			if (isGetter(methods[i]) && !isNamesGetter(methods[i]) && !result.getPropertyNames().contains(propName)
					&& !result.getPropertyNames().contains(stripSuffix(propName, "s"))
					&& !result.getPropertyNames().contains(stripSuffix(propName, "es"))) {

				Property p = null;
				if (returnType instanceof Class<?> && MainHandler.getInstance().canHandle((Class<?>) returnType)) {
					p = SingleValuedPropertyImpl.getSingleValuedProperty(result, propName, (Class<?>) returnType);
				} else if (returnType instanceof Class<?> && ((Class<?>) returnType).isArray()) {
					p = ListPropertyImpl.getListProperty(result, propName, ((Class<?>) returnType).getComponentType());
				} else if (returnType instanceof ParameterizedType) {
					Type rawType = ((ParameterizedType) returnType).getRawType();
					Type[] typeArgs = ((ParameterizedType) returnType).getActualTypeArguments();
					if (rawType instanceof Class<?> && List.class.isAssignableFrom((Class<?>) rawType)
							&& typeArgs[0] instanceof Class<?>) {
						p = ListPropertyImpl.getListProperty(result, propName, (Class<?>) typeArgs[0]);
					} else if (rawType instanceof Class<?> && Map.class.isAssignableFrom((Class<?>) rawType)
							&& typeArgs[0] instanceof Class<?> && typeArgs[1] instanceof Class<?>) {
						p = NamedValuePropertyImpl.getNamedValueProperty(result, propName, (Class<?>) typeArgs[1]);
					}
				}
				if (p != null) {
                    result.defineProperty(p);
                }
			}
		}

		return result;
	}

	private static boolean isCounter(Method method) {
		String name = method.getName();
		if (method.getReturnType().equals(Integer.TYPE)
				&& (name.matches("getNum.+") || name.matches("get.+Count")) ) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isNamesGetter(Method method) {
		String name = method.getName();

		boolean returnsStringArray = method.getReturnType().isArray()
			&& String.class.isAssignableFrom(method.getReturnType().getComponentType());
		boolean returnsStringList = List.class.isAssignableFrom(method.getReturnType())
			&& (method.getGenericReturnType() instanceof ParameterizedType)
			&& ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0] instanceof Class<?>
			&& String.class.isAssignableFrom((Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);

		if (name.matches("get.+Names") && (returnsStringArray || returnsStringList)) {
			return true;
		} else {
			return false;
		}
	}

	private static String getPropertyName(Method method) {
		String result = method.getName();

		result = stripPrefix(result, "get");
		result = stripPrefix(result, "All");
		result = stripSuffix(result, "Array");
		result = stripSuffix(result, "List");

		if (result.length()>0) {

			// don't do a lower case for methods like getPDF()
			if (result.length()>1 && Character.isUpperCase(result.charAt(1))) {
				return result;
			}

			return Character.toLowerCase(result.charAt(0)) + result.substring(1);

		}
		return "";
	}

	/**
	 * @param s A String
	 * @param suffix Something that the string might end with
	 * @return The string with the given suffix removed (if it was there)
	 */
	public static String stripSuffix(String s, String suffix) {
		if (s.endsWith(suffix)) {
			return s.substring(0, s.length() - suffix.length());
		} else {
			return s;
		}
	}

	private static String stripPrefix(String s, String prefix) {
		if (s.startsWith(prefix)) {
			return s.substring(prefix.length());
		} else {
			return s;
		}
	}

	private static boolean isSingleValueGetter(Method m) {
		if (m.getName().startsWith("get")
				&& m.getParameterTypes().length == 0
				&& !Collection.class.isAssignableFrom(m.getReturnType())
				&& !Map.class.isAssignableFrom(m.getReturnType())
				&& !m.getReturnType().isArray()) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isGetter(Method m) {
		if (m.getName().startsWith("get")
				&& m.getParameterTypes().length == 0) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isIndexedGetter(Method m) {
		Class<?>[] paramTypes = m.getParameterTypes();
		if (m.getName().startsWith("get") && paramTypes.length == 1 && paramTypes[0].equals(Integer.TYPE)) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isNamedGetter(Method m) {
		Class<?>[] paramTypes = m.getParameterTypes();
		if (m.getName().startsWith("get") && paramTypes.length == 1 && paramTypes[0].equals(String.class)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @param c Any class
	 * @return Either c or if c is a primitive class (eg Integer.TYPE), the corresponding wrapper class
	 */
	public static Class<?> getPrimitiveWrapperClass(Class<?> c) {
		if (Integer.TYPE.isAssignableFrom(c)) {
			c = Integer.class;
		} else if (Float.TYPE.isAssignableFrom(c)) {
			c = Float.class;
		} else if (Boolean.TYPE.isAssignableFrom(c)) {
			c = Boolean.class;
		} else if (Long.TYPE.isAssignableFrom(c)) {
			c = Long.class;
		} else if (Double.TYPE.isAssignableFrom(c)) {
			c = Double.class;
		} else if (Character.TYPE.isAssignableFrom(c)) {
			c = Character.class;
		} else if (Byte.TYPE.isAssignableFrom(c)) {
			c = Byte.class;
		} else if (Short.TYPE.isAssignableFrom(c)) {
			c = Short.class;
		}

		return c;
	}

	/**
	 * @param type A class
	 * @return If there is a ConfigurationHandler for the class, then getDefaultValue() from that
	 * 		handler, otherwise if there is a zero-arg constructor then the result of that
	 * 		constructor, otherwise null.
	 */
	public static Object getDefaultValue(Class<?> type) {
		Object result = null;

		if (MainHandler.getInstance().canHandle(type)) {
			result = MainHandler.getInstance().getDefaultValue(type);
		}

		if (result == null) {
			Constructor<?>[] constructors = type.getConstructors();
			Constructor<?> zeroArgConstructor = null;
			for (int i = 0; i < constructors.length && zeroArgConstructor == null; i++) {
				if (constructors[i].getParameterTypes().length == 0) {
					zeroArgConstructor = constructors[i];
				}
			}
			if (zeroArgConstructor != null) {
				try {
					result = zeroArgConstructor.newInstance();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	/**
	 * Displays given text in a help window.
	 *
	 * @param text Help text (html body)
	 */
	public static void showHelp(String text) {
		String document = "<html><head></head><body bgcolor='#000000'><font color='#FFFFFF' face='arial'>"
			+ text + "</font></body></html>";
		JEditorPane pane = new JEditorPane("text/html", document);
		pane.setEditable(false);
		pane.setBorder(BorderFactory.createEmptyBorder());

		JFrame frame = new JFrame("Help");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);
	}
}
