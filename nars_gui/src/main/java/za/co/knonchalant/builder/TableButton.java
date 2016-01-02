package za.co.knonchalant.builder;

import javafx.scene.control.Button;
import javafx.scene.control.TableView;


/**
 * A button used on a table, linked ButtonCell and TableView
 */
public class TableButton<T> extends Button {
    private final TableView<T> tableView;
    private final ButtonCell<T> buttonCell;

    public TableButton(String name, TableView<T> tableView, ButtonCell<T> buttonCell) {
        super(name);
        this.tableView = tableView;
        this.buttonCell = buttonCell;
    }

    public ButtonCell<T> getButtonCell() {
        return buttonCell;
    }

    public TableView<T> getTableView() {
        return tableView;
    }
}
