/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "MatrixEditor.java". Description:
"An UI component for editing matrices.

  TODO: don't really need to enforce equal column lengths
  TODO: allow copy/paste, allow insert/delete at specific rows/columns

  @author Bryan Tripp"

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
 * Created on Jan 30, 2004
 */
package ca.nengo.config.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

/**
 * An UI component for editing matrices.
 *
 * TODO: don't really need to enforce equal column lengths
 * TODO: allow copy/paste, allow insert/delete at specific rows/columns
 *
 * @author Bryan Tripp
 */
public class MatrixEditor extends JPanel {

	/**
	 * This cell editor selects the Text Cell Editor on edit, so that it's contents are replaced automatically
	 */
	private static class MyCellEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 7289808186710531L;
		private final JTextField myTextField;

		public MyCellEditor() {
			super(new JTextField());
			myTextField = (JTextField) this.getComponent();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value,
				boolean isSelected,
				int row,
				int column) {

			Component component = super.getTableCellEditorComponent(table,
					value,
					isSelected,
					row,
					column);

			myTextField.selectAll();
			return component;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			// TODO Auto-generated method stub
			return super.isCellEditable(anEvent);
		}
	}

	private static final long serialVersionUID = 1L;

	private float[][] myMatrix;
	private final MatrixTableModel myTableModel;
	private final RowHeaderTableModel myRowHeaderModel;
	private final JTable myTable;
	private final JPanel myControlPanel;

	/**
	 * Creates an editor for the given matrix.
	 *
	 * @param matrix
	 *            The matrix to be edited
	 */
	public MatrixEditor(float[][] matrix) {
		super(new BorderLayout());

		myMatrix = matrix;
		myTableModel = new MatrixTableModel(matrix);
		myTable = new JTable(myTableModel);
		myTable.setMinimumSize(new Dimension(matrix.length == 0 ? 50 : 50 * matrix[0].length,
				16 * matrix.length));
		myTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		myTable.setRowSelectionAllowed(false);
		myTable.setDefaultEditor(Object.class, new MyCellEditor());

		JScrollPane scroll = new JScrollPane(myTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		myRowHeaderModel = new RowHeaderTableModel(myTableModel);
		JTable rowHeader = getRowHeaderView(myRowHeaderModel);
		rowHeader.setBackground(myTable.getTableHeader().getBackground());
		scroll.setRowHeaderView(rowHeader);
		scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader());
		this.add(scroll, BorderLayout.CENTER);

		myControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		this.add(myControlPanel, BorderLayout.SOUTH);
	}

	/**
	 * @return Number of columns
	 */
	public int getColumnCount() {
		return myTable.getColumnCount();
	}

	/**
	 * @return Number of rows
	 */
	public int getRowCount() {
		return myTable.getRowCount();
	}

	/**
	 * @param matrix
	 *            The matrix to be edited
	 * @param numRowsFixed
	 *            If false, the user is able to change the number of matrix rows
	 * @param numColsFixed
	 *            If false, the user is able to change the number of matrix
	 *            columns
	 */
	public MatrixEditor(float[][] matrix, boolean numRowsFixed, boolean numColsFixed) {
		this(matrix);

		if (!numRowsFixed) {
			JButton resizeRowsButton = new JButton("Set # Rows");
			resizeRowsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int n = promptForNum("rows", myTableModel.getRowCount());
					if (n >= 0) {
						MatrixEditor.this.setNumRows(n);
					}
				}
			});
			myControlPanel.add(resizeRowsButton);
		}

		if (!numColsFixed) {
			JButton resizeColsButton = new JButton("Set # Columns");
			resizeColsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int n = promptForNum("columns", myTableModel.getColumnCount());
					if (n >= 0) {
						MatrixEditor.this.setNumCols(n);
					}
				}
			});
			myControlPanel.add(resizeColsButton);
		}
	}

	/**
	 * @return The panel containing controls (caller can add further controls in
	 *         a FlowLayout)
	 */
	public JPanel getControlPanel() {
		return myControlPanel;
	}

	private int promptForNum(String name, int initialValue) {
		int n = -1;

		String nString = JOptionPane.showInputDialog("Number of " + name + ':',
				String.valueOf(initialValue));
		try {
			if (nString != null) {
                n = Integer.parseInt(nString);
            }
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(MatrixEditor.this,
					"# " + name + " must be an integer",
					"Error",
					JOptionPane.ERROR_MESSAGE);
		}

		return n;
	}

	private void setNumRows(int n) {
		int rows = myMatrix.length;
		int cols = myMatrix.length > 0 ? myMatrix[0].length : 0;

		float[][] newMatrix = new float[n][];
		System.arraycopy(myMatrix, 0, newMatrix, 0, Math.min(rows, n));

		for (int i = rows; i < n; i++) {
			newMatrix[i] = new float[cols];
		}

		myMatrix = newMatrix;
		myTableModel.setMatrix(myMatrix);

		if (n > rows) {
			myTableModel.fireTableRowsInserted(rows - 1, n - 1);
			myRowHeaderModel.fireTableRowsInserted(rows - 1, n - 1);
		} else if (n < rows) {
			myTableModel.fireTableRowsDeleted(n, rows - 1);
			myRowHeaderModel.fireTableRowsDeleted(n, rows - 1);
		}
	}

	private void setNumCols(int n) {
		int rows = myMatrix.length;
		int cols = myMatrix.length > 0 ? myMatrix[0].length : 0;

		float[][] newMatrix = new float[rows][];
		for (int i = 0; i < rows; i++) {
			newMatrix[i] = new float[n];
			System.arraycopy(myMatrix[i], 0, newMatrix[i], 0, Math.min(cols, n));
		}

		myMatrix = newMatrix;
		myTableModel.setMatrix(myMatrix);

		myTableModel.fireTableStructureChanged();
	}

	private static JTable getRowHeaderView(TableModel model) {
		JTable result = new JTable(model);
		result.setPreferredScrollableViewportSize(new Dimension(40, 100));

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value,
					boolean isSelected,
					boolean hasFocus,
					int row,
					int column) {
				JLabel field = (JLabel) super.getTableCellRendererComponent(table,
						value,
						isSelected,
						hasFocus,
						row,
						column);
				field.setBorder(new EmptyBorder(0, 0, 0, 3));
				return field;
			}
		};
		renderer.setHorizontalAlignment(JTextField.RIGHT);
		result.getColumnModel().getColumn(0).setCellRenderer(renderer);
		result.setEnabled(false);

		JTableHeader corner = result.getTableHeader();
		corner.setReorderingAllowed(false);
		corner.setResizingAllowed(false);

		return result;
	}

	/**
	 * @return The matrix being edited
	 */
	public float[][] getMatrix() {
		return myMatrix;
	}

	/**
	 * Stops current cell editing
	 */
	public void finishEditing() {
		if (myTable.getCellEditor() != null) {
            myTable.getCellEditor().stopCellEditing();
        }
	}

	// a TableModel for the row header
	private static class RowHeaderTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private final TableModel myTableModel;

		public RowHeaderTableModel(TableModel model) {
			myTableModel = model;
		}

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return myTableModel.getRowCount();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return String.valueOf(rowIndex + 1);
		}

		@Override
		public String getColumnName(int column) {
			return "";
		}

	}

	// a TableModel for the main part of the table
	private static class MatrixTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private float[][] myMatrix;

		public MatrixTableModel(float[][] matrix) {
			setMatrix(matrix);
		}

		public void setMatrix(float[][] matrix) {
			int cols = matrix.length > 0 ? matrix[0].length : 0;
			for (int i = 1; i < matrix.length; i++) {
				if (matrix[i].length != cols) {
					throw new IllegalArgumentException(
							"Matrix must have the same number of columns in each row");
				}
			}
			myMatrix = matrix;
		}

		public int getColumnCount() {
			return myMatrix.length > 0 ? myMatrix[0].length : 0;
		}

		@Override
		public String getColumnName(int theColumn) {
			return String.valueOf(theColumn + 1);
		}

		public int getRowCount() {
			return myMatrix.length;
		}

		public Object getValueAt(int row, int column) {
			return new Float(myMatrix[row][column]);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			try {
				float val = Float.parseFloat(value.toString());
				myMatrix[row][column] = val;
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null,
						"Please enter a number",
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

	}
}
