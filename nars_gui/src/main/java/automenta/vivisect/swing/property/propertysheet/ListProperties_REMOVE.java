package automenta.vivisect.swing.property.propertysheet;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;


@Deprecated
public class ListProperties_REMOVE {

	static class CustomTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -8352295145276923539L;

		private List<Object> keys = new ArrayList<Object>();
		private List<Object> values = new ArrayList<Object>();

		private static final String columnNames[] = { "Property String", "Value" };

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public int getRowCount() {
			return keys.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			Object returnValue = null;
			if (column == 0) {
				returnValue = keys.get(row);
			} else if (column == 1) {
				returnValue = values.get(row);
			}
			return returnValue;
		}

		public synchronized void uiDefaultsUpdate(UIDefaults defaults) {

			Enumeration<Object> newKeys = defaults.keys();
			keys.clear();
			keys.addAll(Collections.list(newKeys));

			Enumeration<Object> newValues = defaults.elements();
			values.clear();
			values.addAll(Collections.list(newValues));

			fireTableDataChanged();
		}
	}

	public static void main(String args[]) {
		final JFrame frame = new JFrame("List Properties");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final CustomTableModel model = new CustomTableModel();
		model.uiDefaultsUpdate(UIManager.getDefaults());
		TableSorter sorter = new TableSorter(model);

		JTable table = new JTable(sorter);
		TableHeaderSorter.install(sorter, table);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		UIManager.LookAndFeelInfo looks[] = UIManager
		.getInstalledLookAndFeels();

		ActionListener actionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final String lafClassName = actionEvent.getActionCommand();
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						try {
							UIManager.setLookAndFeel(lafClassName);
							SwingUtilities.updateComponentTreeUI(frame);
							// Added
							model.uiDefaultsUpdate(UIManager.getDefaults());
						} catch (Exception exception) {
							JOptionPane.showMessageDialog(frame,
							"Can't change look and feel",
							"Invalid PLAF", JOptionPane.ERROR_MESSAGE);
						}
					}
				};
				SwingUtilities.invokeLater(runnable);
			}
		};

		JToolBar toolbar = new JToolBar();
		for (int i = 0, n = looks.length; i < n; i++) {
			JButton button = new JButton(looks[i].getName());
			button.setActionCommand(looks[i].getClassName());
			button.addActionListener(actionListener);
			toolbar.add(button);
		}

		Container content = frame.getContentPane();
		content.add(toolbar, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(table);
		content.add(scrollPane, BorderLayout.CENTER);
		frame.setSize(400, 400);
		frame.setVisible(true);
	}
}

class TableSorter extends TableMap implements TableModelListener {

	private static final long serialVersionUID = 6627171931468194200L;

	private int indexes[] = new int[0];

	private List<Object> sortingColumns = new ArrayList<Object>();

	boolean ascending = true;

	public TableSorter() {
	}

	public TableSorter(TableModel model) {
		setModel(model);
	}

	@Override
	public void setModel(TableModel model) {
		super.setModel(model);
		reallocateIndexes();
		sortByColumn(0);
		fireTableDataChanged();
	}

	public int compareRowsByColumn(int row1, int row2, int column) {

		Class<?> type = model.getColumnClass(column);
		TableModel data = model;

		// Check for nulls

		Object o1 = data.getValueAt(row1, column);
		Object o2 = data.getValueAt(row2, column);

		// If both values are null return 0
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) { // Define null less than everything.
			return -1;
		} else if (o2 == null) {
			return 1;
		}

		if (type.getSuperclass() == Number.class) {
			Number n1 = (Number) data.getValueAt(row1, column);
			double d1 = n1.doubleValue();
			Number n2 = (Number) data.getValueAt(row2, column);
			double d2 = n2.doubleValue();

			if (d1 < d2)
				return -1;
			else if (d1 > d2)
				return 1;
			else
				return 0;
		} else if (type == String.class) {
			String s1 = (String) data.getValueAt(row1, column);
			String s2 = (String) data.getValueAt(row2, column);
			int result = s1.compareTo(s2);

			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			else
				return 0;
		} else if (type == java.util.Date.class) {
			Date d1 = (Date) data.getValueAt(row1, column);
			long n1 = d1.getTime();
			Date d2 = (Date) data.getValueAt(row2, column);
			long n2 = d2.getTime();

			if (n1 < n2)
				return -1;
			else if (n1 > n2)
				return 1;
			else
				return 0;
		} else if (type == Boolean.class) {
			Boolean bool1 = (Boolean) data.getValueAt(row1, column);
			boolean b1 = bool1.booleanValue();
			Boolean bool2 = (Boolean) data.getValueAt(row2, column);
			boolean b2 = bool2.booleanValue();

			if (b1 == b2)
				return 0;
			else if (b1) // Define false < true
				return 1;
			else
				return -1;
		} else {
			Object v1 = data.getValueAt(row1, column);
			String s1 = v1.toString();
			Object v2 = data.getValueAt(row2, column);
			String s2 = v2.toString();
			int result = s1.compareTo(s2);

			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			else
				return 0;
		}
	}

	public int compare(int row1, int row2) {
		for (int level = 0, n = sortingColumns.size(); level < n; level++) {
			Integer column = (Integer) sortingColumns.get(level);
			int result = compareRowsByColumn(row1, row2, column.intValue());
			if (result != 0) {
				return (ascending ? result : -result);
			}
		}
		return 0;
	}

	public void reallocateIndexes() {
		int rowCount = model.getRowCount();
		indexes = new int[rowCount];
		for (int row = 0; row < rowCount; row++) {
			indexes[row] = row;
		}
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent) {
		super.tableChanged(tableModelEvent);
		reallocateIndexes();
		sortByColumn(0);
		fireTableStructureChanged();
	}

	public void checkModel() {
		if (indexes.length != model.getRowCount()) {
			System.err.println("Sorter not informed of a change in model.");
		}
	}

	public void sort() {
		checkModel();
		shuttlesort(indexes.clone(), indexes, 0, indexes.length);
		fireTableDataChanged();
	}

	public void shuttlesort(int from[], int to[], int low, int high) {
		if (high - low < 2) {
			return;
		}
		int middle = (low + high) / 2;
		shuttlesort(to, from, low, middle);
		shuttlesort(to, from, middle, high);

		int p = low;
		int q = middle;

		for (int i = low; i < high; i++) {
			if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
				to[i] = from[p++];
			} else {
				to[i] = from[q++];
			}
		}
	}

	@Override
	public Object getValueAt(int row, int column) {
		checkModel();
		return model.getValueAt(indexes[row], column);
	}

	@Override
	public void setValueAt(Object aValue, int row, int column) {
		checkModel();
		model.setValueAt(aValue, indexes[row], column);
	}

	public void sortByColumn(int column) {
		sortByColumn(column, true);
	}

	public void sortByColumn(int column, boolean ascending) {
		this.ascending = ascending;
		sortingColumns.clear();
		sortingColumns.add(new Integer(column));
		sort();
		super.tableChanged(new TableModelEvent(this));
	}
}

class TableHeaderSorter extends MouseAdapter {

	private TableSorter sorter;

	private JTable table;

	private TableHeaderSorter() {
	}

	public static void install(TableSorter sorter, JTable table) {
		TableHeaderSorter tableHeaderSorter = new TableHeaderSorter();
		tableHeaderSorter.sorter = sorter;
		tableHeaderSorter.table = table;
		JTableHeader tableHeader = table.getTableHeader();
		tableHeader.addMouseListener(tableHeaderSorter);
	}

	@Override
	public void mouseClicked(MouseEvent mouseEvent) {
		TableColumnModel columnModel = table.getColumnModel();
		int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
		int column = table.convertColumnIndexToModel(viewColumn);
		if (mouseEvent.getClickCount() == 1 && column != -1) {
			System.out.println("Sorting ...");
			int shiftPressed = (mouseEvent.getModifiers() & InputEvent.SHIFT_MASK);
			boolean ascending = (shiftPressed == 0);
			sorter.sortByColumn(column, ascending);
		}
	}
}

class TableMap extends AbstractTableModel implements TableModelListener {

	private static final long serialVersionUID = -2648081177216556320L;
	protected TableModel model;

	public TableModel getModel() {
		return model;
	}

	public void setModel(TableModel model) {
		if (this.model != null) {
			this.model.removeTableModelListener(this);
		}
		this.model = model;
		if (this.model != null) {
			this.model.addTableModelListener(this);
		}
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return model.getColumnClass(column);
	}

	@Override
	public int getColumnCount() {
		return ((model == null) ? 0 : model.getColumnCount());
	}

	@Override
	public String getColumnName(int column) {
		return model.getColumnName(column);
	}

	@Override
	public int getRowCount() {
		return ((model == null) ? 0 : model.getRowCount());
	}

	@Override
	public Object getValueAt(int row, int column) {
		return model.getValueAt(row, column);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		model.setValueAt(value, row, column);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return model.isCellEditable(row, column);
	}

	@Override
	public void tableChanged(TableModelEvent tableModelEvent) {
		fireTableChanged(tableModelEvent);
	}
}