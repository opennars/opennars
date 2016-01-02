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

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;


/**
 * ComboBoxPropertyEditor. <br>
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComboBoxPropertyEditor extends AbstractPropertyEditor {

	private Object oldValue;
	private Icon[] icons;

	public ComboBoxPropertyEditor() {

		JComboBox combo = new JComboBox() {

			private static final long serialVersionUID = -7048198994640023540L;

			@Override
			public void setSelectedItem(Object anObject) {
				oldValue = getSelectedItem();
				super.setSelectedItem(anObject);
			}
		};

		combo.setRenderer(new Renderer());
		combo.setBorder(null);
		combo.setOpaque(false);
		combo.setFocusable(false);

		BasicComboBoxEditor comboEditor = (BasicComboBoxEditor) combo.getEditor();
		JTextField textField = (JTextField) comboEditor.getEditorComponent();
		textField.setBorder(null);
		textField.setFocusable(false);
		textField.setOpaque(false);

		combo.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				firePropertyChange(oldValue, combo.getSelectedItem());
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});

		combo.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					firePropertyChange(oldValue,
					combo.getSelectedItem());
				}
			}
		});
		combo.setSelectedIndex(-1);

		editor = combo;
	}

	@Override
	public Object getValue() {
		Object selected = ((JComboBox) editor).getSelectedItem();
		return selected instanceof Value ? ((Value) selected).value : selected;
	}

	@Override
	public void setValue(Object value) {
		JComboBox combo = (JComboBox) editor;
		Object current = null;
		int index = -1;
		for (int i = 0, c = combo.getModel().getSize(); i < c; i++) {
			current = combo.getModel().getElementAt(i);
			if (Objects.equals(current, value)) {
				index = i;
				break;
			}
		}
		((JComboBox) editor).setSelectedIndex(index);
	}

	public void setAvailableValues(Object[] values) {
		((JComboBox) editor).setModel(new DefaultComboBoxModel(values));
	}

	public void setAvailableIcons(Icon[] icons) {
		this.icons = icons;
	}

	public class Renderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -419585447957652497L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {

			Object v = value instanceof Value ? ((Value) value).visualValue : value;
			Component component = super.getListCellRendererComponent(list, v, index, selected, focus);

			if (component instanceof JLabel) {

				JLabel label = (JLabel) component;

				if (icons != null && index >= 0) {
					label.setIcon(icons[index]);
				}

				label.setPreferredSize(new Dimension(component.getSize().width, 16));
			}

			return component;
		}
	}

	public static final class Value {

		private final Object value;
		private final Object visualValue;

		public Value(Object value, Object visualValue) {
			this.value = value;
			this.visualValue = visualValue;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			return Objects.equals(value, o);
		}

		@Override
		public int hashCode() {
			return value == null ? 0 : value.hashCode();
		}
	}
}
