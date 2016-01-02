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

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyEditor;
import java.util.EventObject;

/**
 * Allows to use any PropertyEditor as a Table or Tree cell editor. <br>
 */
public class CellEditorAdapter
  extends AbstractCellEditor
  implements TableCellEditor, TreeCellEditor {

  protected PropertyEditor editor;
  protected int clickCountToStart = 1;

  class CommitEditing implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      stopCellEditing();
    }
  }

  class CancelEditing implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      cancelCellEditing();
    }
  }

  /**
   * Select all text when focus gained, deselect when focus lost.
   */
  static class SelectOnFocus implements FocusListener {
    @Override
    public void focusGained(FocusEvent e) {
      if (! (e.getSource() instanceof JTextField))
        return;
      SwingUtilities.invokeLater(() -> ((JTextField) e.getSource()).selectAll());
    }
    @Override
    public void focusLost(FocusEvent e) {
      if (! (e.getSource() instanceof JTextField))
        return;
      SwingUtilities.invokeLater(() -> ((JTextField) e.getSource()).select(0, 0));
    }
  }
  
  public CellEditorAdapter(PropertyEditor editor) {
    this.editor = editor;
    Component component = editor.getCustomEditor();
    if (component instanceof JTextField) {
      JTextField field = (JTextField)component;
      field.addFocusListener(new SelectOnFocus());
      field.addActionListener(new CommitEditing());
      field.registerKeyboardAction(
        new CancelEditing(),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_FOCUSED);
    }

    // when the editor notifies a change, commit the changes
    editor.addPropertyChangeListener(evt -> stopCellEditing());
  }
  
  @Override
  public Component getTreeCellEditorComponent(
    JTree tree,
    Object value,
    boolean selected,
    boolean expanded,
    boolean leaf,
    int row) {
    return getEditor(value);
  }

  @Override
  public Component getTableCellEditorComponent(
    JTable table,
    Object value,
    boolean selected,
    int row,
    int column) {
    return getEditor(value);
  }

  public void setClickCountToStart(int count) {
    clickCountToStart = count;
  }

  public int getClickCountToStart() {
    return clickCountToStart;
  }

  @Override
  public Object getCellEditorValue() {
    return editor.getValue();
  }

  @Override
  public boolean isCellEditable(EventObject event) {
    if (event instanceof MouseEvent) {
      return ((MouseEvent)event).getClickCount() >= clickCountToStart;
    }
    return true;
  }

  @Override
  public boolean shouldSelectCell(EventObject event) {
    return true;
  }

  @Override
  public boolean stopCellEditing() {
    fireEditingStopped();
    return true;
  }

  @Override
  public void cancelCellEditing() {
    fireEditingCanceled();
  }

  private Component getEditor(Object value) {
    editor.setValue(value);

    Component cellEditor = editor.getCustomEditor();

    // request focus later so the editor can be used to enter value as soon as
    // made visible
    SwingUtilities.invokeLater(cellEditor::requestFocus);

    return cellEditor;
  }

}
