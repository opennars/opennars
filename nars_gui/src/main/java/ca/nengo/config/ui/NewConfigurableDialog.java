/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NewConfigurableDialog.java". Description:
"A dialog box through which the user can construct a new object"

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
 * Created on 14-Dec-07
 */
package ca.nengo.config.ui;

import ca.nengo.config.*;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.config.ui.ConfigurationTreeModel.NullValue;
import ca.nengo.config.ui.ConfigurationTreeModel.Value;
import ca.nengo.model.StructuralException;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A dialog box through which the user can construct a new object.
 *
 * @author Bryan Tripp
 */
public class NewConfigurableDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final String CANCEL_ACTION_COMMAND = "cancel";

	private Object myResult;

	private Configuration myConfiguration;
	private final JTree myConfigurationTree;
	private ConfigurationTreePopupListener myPopupListener;
	private final JButton myPreviousButton;
	private final JButton myNextButton;
	private final JButton myOKButton;
	private Constructor<?>[] myConstructors;
	private int myConstructorIndex;

	/**
	 * Opens a NewConfigurableDialog through which the user can construct a new object, and
	 * returns the constructed object.
	 *
	 * @param comp UI component from which a dialog is to be launched
	 * @param type Class of object to be constructed
	 * @param specificType An optional more specific type to be initially selected (if there is more than
	 * 		one implementation of the more general type above)
	 * @return User-constructed object (or null if construction aborted)
	 */
	public static Object showDialog(Component comp, Class<?> type, Class<?> specificType) {

		List<Class<?>> types = ClassRegistry.getInstance().getImplementations(type);
		if (specificType != null && !NullValue.class.isAssignableFrom(specificType) && !types.contains(specificType)) {
			types.add(0, specificType);
		}

		NewConfigurableDialog dialog = null;
		if (types.size() > 0) {
			dialog = new NewConfigurableDialog(comp, type, types);
			dialog.setVisible(true);
		} else {
			String errorMessage = "There are no registered implementations of type " + type.getName();
			ConfigExceptionHandler.handle(new RuntimeException(errorMessage), errorMessage, comp);
		}

		return dialog.getResult();
	}

	private NewConfigurableDialog(Component comp, final Class<?> type, List<Class<?>> types) {
		super(JOptionPane.getFrameForComponent(comp), "New " + type.getSimpleName(), true);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(CANCEL_ACTION_COMMAND);
		cancelButton.addActionListener(this);
		JButton createButton = new JButton("Create");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					NewConfigurableDialog.this.setResult();
				} catch (StructuralException ex) {
					ConfigExceptionHandler.handle(ex,
							"A programming bug was encountered while trying to create the new " + type.getSimpleName()
							+ ". The error log may contain more information.", NewConfigurableDialog.this);
				}
			}
		});
		myOKButton = new JButton("OK");
		myOKButton.addActionListener(this);
		myOKButton.setEnabled(false);
		getRootPane().setDefaultButton(myOKButton);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setAlignmentX(FlowLayout.RIGHT);
		buttonPanel.add(cancelButton);
		buttonPanel.add(createButton);
		buttonPanel.add(myOKButton);

		myConfigurationTree = new JTree(new Object[0]);

		if (myConfigurationTree.getUI().getClass().getName().contains("apple.laf")) {
			AquaTreeUI aui = new AquaTreeUI();
			myConfigurationTree.setUI(aui);
			aui.setRowHeight(-1); //must be done after setUI(...)
		}

		myConfigurationTree.setEditable(true);
		myConfigurationTree.setRootVisible(true);
		myConfigurationTree.setCellEditor(new ConfigurationTreeCellEditor(myConfigurationTree));
		ConfigurationTreeCellRenderer cellRenderer = new ConfigurationTreeCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof Value && ((Value) value).getObject() instanceof ConstructionProperties
						&& result instanceof JLabel) {
					String text = "Constructor Arguments";
					if (((ConstructionProperties) ((Value) value).getObject()).getConfiguration().getPropertyNames().size() == 0) {
						text = "Zero-Argument Constructor";
					}
					((JLabel) result).setText(text);
				}
				return result;
			}

		};
		myConfigurationTree.setCellRenderer(cellRenderer);

		JScrollPane treeScroll = new JScrollPane(myConfigurationTree);

		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));

		final JComboBox typeBox = new JComboBox(types.toArray());
		typeBox.setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				((JLabel) result).setText(((Class<?>) value).getSimpleName());
				return result;
			}
		});
		typePanel.add(typeBox);

		myPreviousButton = new JButton("<");
		myPreviousButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewConfigurableDialog.this.changeConstructor(-1);
			}
		});
		myNextButton = new JButton(">");
		myNextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewConfigurableDialog.this.changeConstructor(1);
			}
		});
		typePanel.add(myPreviousButton);
		typePanel.add(myNextButton);

		typeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewConfigurableDialog.this.setSelectedType((Class<?>) typeBox.getSelectedItem());
			}
		});

		treeScroll.setPreferredSize(new Dimension(typeBox.getPreferredSize().width, 200));
		typeBox.setSelectedIndex(0);

		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(typePanel, BorderLayout.NORTH);
		contentPane.add(treeScroll, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(comp);
	}

	/**
	 * @return Resulting object
	 */
	public Object getResult() {
		return myResult;
	}

	private void setSelectedType(Class<?> type) {
		myConstructors = type.getConstructors();
		setConstructor(0);
		myOKButton.setEnabled(false);
	}

	private void changeConstructor(int increment) {
		int newIndex = myConstructorIndex + increment;
		if (newIndex >= 0 && newIndex < myConstructors.length) {
			setConstructor(newIndex);
		}
	}

	private void setConstructor(int index) {
		myConstructorIndex = index;
		Constructor<?> constructor = myConstructors[index];

		if (myConstructorIndex == 0) {
			myPreviousButton.setEnabled(false);
		} else {
			myPreviousButton.setEnabled(true);
		}

		if (myConstructorIndex == myConstructors.length - 1) {
			myNextButton.setEnabled(false);
		} else {
			myNextButton.setEnabled(true);
		}

		if (myPopupListener != null) {
			myConfigurationTree.removeMouseListener(myPopupListener);
		}

		myConfiguration = makeTemplate(constructor);
		ConfigurationTreeModel model = new ConfigurationTreeModel(new ConstructionProperties(myConfiguration));
		myConfigurationTree.setModel(model);
		myPopupListener = new ConfigurationTreePopupListener(myConfigurationTree, model);
		myConfigurationTree.addMouseListener(myPopupListener);
	}

	private static Configuration makeTemplate(Constructor<?> constructor) {
		ConfigurationImpl result = new ConfigurationImpl(null);
		Class<?>[] types = constructor.getParameterTypes();
        /*
		String[] names = JavaSourceParser.getArgNames(constructor);
		for (int i = 0; i < types.length; i++) {
			if (types[i].isPrimitive()) {
                types[i] = ConfigUtil.getPrimitiveWrapperClass(types[i]);
            }
			AbstractProperty p = null;
			if (types[i].isArray() && !MainHandler.getInstance().canHandle(types[i])) {
				p = new TemplateArrayProperty(result, names[i], types[i].getComponentType());
			} else {
				p = new TemplateProperty(result, names[i], types[i], ConfigUtil.getDefaultValue(types[i]));
			}
			p.setDocumentation(JavaSourceParser.getArgDocs(constructor, i));
			result.defineProperty(p);
		}

		*/
		return result;
	}

	private void setResult() throws StructuralException {
		List<Object> args = new ArrayList<Object>(myConfiguration.getPropertyNames().size());
		for (String string : myConfiguration.getPropertyNames()) {
			Property p = myConfiguration.getProperty(string);
			if (p instanceof SingleValuedProperty) {
				args.add(((SingleValuedProperty) p).getValue());
			} else if (p instanceof ListProperty) {
				ListProperty lp = (ListProperty) p;
				Object array = Array.newInstance(p.getType(), lp.getNumValues());
				for (int i = 0; i < lp.getNumValues(); i++) {
					Array.set(array, i, lp.getValue(i));
				}
				args.add(array);
			}
		}

		String errorMessage = "Can't create new "
			+ myConstructors[myConstructorIndex].getDeclaringClass().getName() + ". See error log for further detail. ";

		try {
			myResult = myConstructors[myConstructorIndex].newInstance(args.toArray(new Object[args.size()]));

			if (myPopupListener != null) {
				myConfigurationTree.removeMouseListener(myPopupListener);
			}
			ConfigurationTreeModel model = new ConfigurationTreeModel(myResult);
			myConfigurationTree.setModel(model);
			myPopupListener = new ConfigurationTreePopupListener(myConfigurationTree, model);
			myConfigurationTree.addMouseListener(myPopupListener);

			myOKButton.setEnabled(true);
		} catch (IllegalArgumentException e) {
			ConfigExceptionHandler.handle(e, errorMessage, myConfigurationTree);
		} catch (InstantiationException e) {
			ConfigExceptionHandler.handle(e, errorMessage, myConfigurationTree);
		} catch (IllegalAccessException e) {
			ConfigExceptionHandler.handle(e, errorMessage, myConfigurationTree);
		} catch (InvocationTargetException e) {
			ConfigExceptionHandler.handle(e, errorMessage + " (" + e.getCause().getClass().getName() + ')',
					myConfigurationTree);
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (CANCEL_ACTION_COMMAND.equals(e.getActionCommand())) {
			myResult = null;
		}
		setVisible(false);
	}

	/**
	 * Class used to pass configuration properties to created classes.
	 */
	public static class ConstructionProperties {

		private final Configuration myConfiguration;

		private ConstructionProperties(Configuration configuration) {
			myConfiguration = configuration;
		}

		/**
		 * @return the Configuration to use
		 */
		public Configuration getConfiguration() {
			return myConfiguration;
		}

	}
}
