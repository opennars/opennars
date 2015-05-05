package nars.tuprolog.gui.ide;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class TextAreaRenderer
    extends JTextArea
    implements TableCellRenderer
{
  private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
  /**
 * map from table to map of rows to map of column heights
 */
  private final Map<JTable,Map<Integer,Map<Integer,Integer>>> cellSizes = new HashMap<>();
  private boolean[] isExpandedCellArray;
  private boolean[] isBorderedCellArray;

  public TextAreaRenderer(boolean[] isExpandedCellArray, boolean[] isBorderedCellArray)
  {
    setLineWrap(true);
    setWrapStyleWord(true);
    this.isExpandedCellArray = isExpandedCellArray;
    this.isBorderedCellArray = isBorderedCellArray;
  }

  public Component getTableCellRendererComponent(//
      JTable table, Object obj, boolean isSelected,
      boolean hasFocus, int row, int column) {
    // set the colours, etc. using the standard for that platform
    adaptee.getTableCellRendererComponent(table, obj,
        isSelected, hasFocus, row, column);
    setForeground(adaptee.getForeground());
    setBackground(adaptee.getBackground());
    setBorder(adaptee.getBorder());
    setFont(adaptee.getFont());
    setText(adaptee.getText());

    // This line was very important to get it working with JDK1.4
    TableColumnModel columnModel = table.getColumnModel();
    setSize(columnModel.getColumn(column).getWidth(), 100000);
    int height_wanted = (int) getPreferredSize().getHeight();
    addSize(table, row, column, height_wanted);

    /** it isn't necessary "else" becouse border isn't dynamic. it is always the same, despite user interation*/
    if (isBorderedCellArray[row])
    {
        /** 2, 0, 0, 0 --> border only on cell top*/
        adaptee.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.black),getBorder()));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.black),getBorder()));
    }

    height_wanted = findTotalMaximumRowSize(table, row);
    if (!isExpandedCellArray[row])
    {
        return adaptee;
    }
    else
    {
        table.setRowHeight(row, height_wanted);
        return this;
    }
  }

  private void addSize(JTable table, int row, int column,
                       int height) {
    Map<Integer,Map<Integer,Integer>> rows = cellSizes.get(table);
    if (rows == null) {
      cellSizes.put(table, rows = new HashMap<>());
    }
    Map<Integer,Integer> rowheights = rows.get(row);
    if (rowheights == null) {
      rows.put(row, rowheights = new HashMap<>());
    }
    rowheights.put(column, height);
  }

  /**
   * Look through all columns and get the renderer.  If it is
   * also a TextAreaRenderer, we look at the maximum height in
   * its hash table for this row.
   */
  private int findTotalMaximumRowSize(JTable table, int row) {
    int maximum_height = 0;
    Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
    while (columns.hasMoreElements()) {
      TableColumn tc = columns.nextElement();
      TableCellRenderer cellRenderer = tc.getCellRenderer();
      if (cellRenderer instanceof TextAreaRenderer) {
        TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
        maximum_height = Math.max(maximum_height,
            tar.findMaximumRowSize(table, row));
      }
    }
    return maximum_height;
  }

  private int findMaximumRowSize(JTable table, int row) {
    Map<Integer,Map<Integer,Integer>> rows = cellSizes.get(table);
    if (rows == null) return 0;
    Map<Integer,Integer> rowheights = rows.get(row);
    if (rowheights == null) return 0;
    int maximum_height = 0;
    for( Map.Entry<Integer, Integer> entry:rowheights.entrySet()){
      int cellHeight = entry.getValue();
      maximum_height = Math.max(maximum_height, cellHeight);
    }
    return maximum_height;
  }

}