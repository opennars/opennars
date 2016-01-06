//package nars.gui.output;
//
//import automenta.vivisect.swing.NPanel;
//import nars.NAR;
//import nars.event.NARReaction;
//import nars.io.out.Output;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.List;
//
//import static java.awt.BorderLayout.CENTER;
//
//
//public abstract class TablePanel extends NPanel  {
//
//    protected final NAR nar;
//    protected DefaultTableModel data;
//    protected final JTable table;
//    //private final NARReaction out;
//
//    public TablePanel(NAR nar) {
//        super(new BorderLayout());
//        this.nar = nar;
//        table = new JTable();
//        table.setFillsViewportHeight(true);
//
//        add(new JScrollPane(table), CENTER);
////        out = new NARReaction(nar, Output.DefaultOutputEvents) {
////
////            @Override
////            public void event(Class event, Object[] arguments) {
////                output(event, arguments.length > 1 ? arguments : arguments[0]);
////            }
////
////        };
//    }
//
//    abstract public void output(Class c, Object s);
//
//    @Override
//    protected void visibility(boolean appearedOrDisappeared) {
//        out.setActive(appearedOrDisappeared);
//    }
//
//    protected List<Object> getSelectedRows(int column) {
//        int[] selectedRows = table.getSelectedRows();
//        List<Object> l = new ArrayList(selectedRows.length);
//        for (int i : selectedRows) {
//            int selectedRow = table.convertRowIndexToModel(i);
//            l.add(data.getValueAt(selectedRow, column));
//        }
//        return l;
//    }
// }
