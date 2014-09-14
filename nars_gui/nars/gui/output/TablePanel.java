package nars.gui.output;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import nars.core.NAR;
import nars.gui.NPanel;
import nars.io.Output;


public abstract class TablePanel extends NPanel implements Output {

    protected final NAR nar;
    protected DefaultTableModel data;
    protected final JTable table;

    public TablePanel(NAR nar) {
        super();
        this.nar = nar;
        table = new JTable();        
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            nar.addOutput(this);
        } else {
            nar.removeOutput(this);
        }
    }

    protected List<Object> getSelectedRows(int column) {
        int[] selectedRows = table.getSelectedRows();
        List<Object> l = new ArrayList(selectedRows.length);
        for (int i : selectedRows) {
            int selectedRow = table.convertRowIndexToModel(i);
            l.add(data.getValueAt(selectedRow, column));
        }
        return l;
    }
}
