package nars.guifx.chart;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import nars.NAR;
import nars.task.Task;
import nars.truth.Truth;
import org.controlsfx.control.spreadsheet.*;

import java.util.Comparator;

/**
 * 2D spreadsheet of tasks, X and Y axis are configurable sorts
 */
public class TaskSheet extends BorderPane {

    private final NAR nar;
    private final SpreadsheetView sheet;
   // private final FlowPane header;

    class Sorting implements Comparator<Float> {
        @Override
        public int compare(Float t, Float t1) {
            return t < t1 ? -1 : t == t1 ? 0 : 1;
        }
    }

    public TaskSheet(NAR n) {
        super();

        this.nar = n;

        //Button refreshButton = new Button("Update");
      //  refreshButton.setOnAction(e -> update());

       // setTop(this.header = new FlowPane(
      //      refreshButton
      //  ));

        this.sheet = new SpreadsheetView(getSampleGrid());
        int k=0;
        for(SpreadsheetColumn column : this.sheet.getColumns()) {
            column.setPrefWidth(50);
            if(k==3) {
                column.setPrefWidth(100);
            }
            k++;
        }

        sheet.setMaxWidth(Double.MAX_VALUE);
        sheet.setMaxHeight(Double.MAX_VALUE);

        setCenter(sheet);

        nar.memory.eventInput.on(this::derived);
        sheet.getGrid().getColumnHeaders().addAll("Priority", "Durability", "Quality","Term","Frequency","Confidence","Expectation","Occurence","Creation");
    }

    int i=0;
    public void derived(Task t) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {

                    sheet.getGrid().setCellValue(i, 0, t.getPriority());
                    sheet.getGrid().setCellValue(i, 1, t.getDurability());
                    sheet.getGrid().setCellValue(i, 2, t.getQuality());
                    sheet.getGrid().setCellValue(i, 3, t.getTerm().toString());

                    Truth tr = t.getTruth();
                    if (tr != null) {
                        sheet.getGrid().setCellValue(i, 4, tr.getFrequency());
                        sheet.getGrid().setCellValue(i, 5, tr.getConfidence());
                        sheet.getGrid().setCellValue(i, 6, tr.getExpectation());
                    }

                    sheet.getGrid().setCellValue(i,7,t.getOccurrenceTime());
                    sheet.getGrid().setCellValue(i,8,t.getCreationTime());

                    i = (i + 1) % 160;
                } catch (Exception ex){}
            }
        });
    }

    protected void update() {


        sheet.getGrid().setCellValue(0,0,"x"+Math.random());
    }


    private static Grid getSampleGrid() {
        GridBase gridBase = new GridBase(160, 9);

        ObservableList rows = FXCollections.observableArrayList();

        for(int row = 0; row < gridBase.getRowCount(); ++row) {
            ObservableList currentRow = FXCollections.observableArrayList();

            for(int column = 0; column < gridBase.getColumnCount(); ++column) {
                SpreadsheetCell cell = null;
                if(column != 3) {
                    cell = SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, 0.0);
                } else {
                    cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, " ? ");
                }

                cell.setWrapText(false);
                /*cell.getStyleClass().clear();
                cell.getStyleClass().add("sheetcell");*/

                currentRow.add(cell);
            }

            rows.add(currentRow);
        }

        gridBase.setRows(rows);

        return gridBase;
    }
}
