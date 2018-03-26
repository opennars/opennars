package nars.gui.output;

import automenta.vivisect.swing.NPanel;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import nars.main.NAR;
import nars.io.handlers.OutputHandler;


public abstract class TablePanel extends NPanel  {

    protected final NAR nar;
    protected DefaultTableModel data;
    protected final JTable table;
    private final OutputHandler out;

    public TablePanel(NAR nar) {
        super(new BorderLayout());
        this.nar = nar;
        table = new JTable();   
        table.setFillsViewportHeight(true);
        
        add(new JScrollPane(table), CENTER);
        out = new OutputHandler(nar, false) {

            @Override
            public void event(Class event, Object[] arguments) {
                output(event, arguments.length > 1 ? arguments : arguments[0]);            
            }
            
        };        
    }

    abstract public void output(Class c, Object s);
    
    @Override
    protected void onShowing(boolean showing) {
        out.setActive(showing);
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
