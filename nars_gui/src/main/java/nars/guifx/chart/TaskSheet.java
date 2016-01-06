package nars.guifx.chart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nars.NAR;
import org.controlsfx.control.spreadsheet.*;

/**
 * 2D spreadsheet of tasks, X and Y axis are configurable sorts
 */
public class TaskSheet extends BorderPane {

	private final NAR nar;
	private final SpreadsheetView sheet;
	private final FlowPane header;

	public TaskSheet(NAR n) {

        nar = n;

        Button refreshButton = new Button("Update");
        refreshButton.setOnAction(e -> update());

        setTop(header = new FlowPane(
            refreshButton
        ));

        sheet = new SpreadsheetView(getSampleGrid());

        sheet.setMaxWidth(Double.MAX_VALUE);
        sheet.setMaxHeight(Double.MAX_VALUE);

        setCenter(sheet);
    }
	protected void update() {
		sheet.getGrid().setCellValue(0, 0, "x" + Math.random());
	}

	private static Grid getSampleGrid() {
		GridBase gridBase = new GridBase(16, 16);

		ObservableList rows = FXCollections.observableArrayList();

		for (int row = 0; row < gridBase.getRowCount(); ++row) {
			ObservableList currentRow = FXCollections.observableArrayList();

			for (int column = 0; column < gridBase.getColumnCount(); ++column) {
				SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(
						row, column, 1, 1, " ? ");
				cell.setWrapText(true);
				/*
				 * cell.getStyleClass().clear();
				 * cell.getStyleClass().add("sheetcell");
				 */

				currentRow.add(cell);
			}

			rows.add(currentRow);
		}

		gridBase.setRows(rows);
		return gridBase;
	}
}
