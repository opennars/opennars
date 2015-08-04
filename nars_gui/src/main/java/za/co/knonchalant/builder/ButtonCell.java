package za.co.knonchalant.builder;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

/**
 * Table cell containing a button
 */
public class ButtonCell<T> extends TableCell<T, Boolean> {
    final TableButton<T> cellButton;

    public ButtonCell(EventHandler<ActionEvent> eventHandler, TableView<T> tableView, String actionName) {
        cellButton = new TableButton<>(actionName, tableView, this);
        cellButton.setOnAction(eventHandler);
    }

    @Override
    protected void updateItem(Boolean t, boolean empty) {
        super.updateItem(t, empty);

        if (!empty) {
            setGraphic(cellButton);
        }
    }
}