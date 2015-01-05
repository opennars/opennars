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
package automenta.vivisect.swing.property.swing.renderer;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

import automenta.vivisect.swing.property.model.DefaultObjectRenderer;
import automenta.vivisect.swing.property.model.ObjectRenderer;


/**
 * DefaultCellRenderer.<br>
 * 
 */
public class DefaultCellRenderer extends DefaultTableCellRenderer implements ListCellRenderer {

	private static final long serialVersionUID = -6142292027983690799L;

	private ObjectRenderer objectRenderer = new DefaultObjectRenderer();

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean selected, boolean focus) {

		setBorder(null);

		if (selected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setValue(value);

		return this;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int column) {
		super.getTableCellRendererComponent(table, value, selected, focus, row, column);
		setValue(value);
		return this;
	}

	@Override
	public void setValue(Object value) {
		String text = convertToString(value);
		Icon icon = convertToIcon(value);

		setText(text == null ? "" : text);
		setIcon(icon);
		setDisabledIcon(icon);
	}

	/**
	 * Converts cell value to string.
	 * 
	 * @param value the value to be displayed in cell
	 * @return String representation of given value
	 */
	protected String convertToString(Object value) {
		return objectRenderer.getText(value);
	}

	protected Icon convertToIcon(Object value) {
		return null;
	}

}
