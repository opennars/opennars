package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NPanel;
import nars.io.Output;



public class SentenceTablePanel extends NPanel implements Output {
    private final NAR nar;
    
    DefaultTableModel data;
    private final JButton graphButton;
    private final JTable t;

    public SentenceTablePanel(NAR nar) {
        super();
        this.nar = nar;
        
        setLayout(new BorderLayout());        
        
        data = newModel();
        
        t = new JTable(data);        
        t.setAutoCreateRowSorter(true);       
        t.validate();
        t.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                graphButton.setEnabled(t.getSelectedRowCount() > 0);
            }
        });
        t.getColumn("Type").setMaxWidth(48);
        t.getColumn("Frequency").setMaxWidth(64);
        t.getColumn("Confidence").setMaxWidth(64);
        t.getColumn("Priority").setMaxWidth(64);
        t.getColumn("Complexity").setMaxWidth(64);
        t.getColumn("Time").setMaxWidth(72);
        
        add(new JScrollPane(t), BorderLayout.CENTER);
        
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        {
            graphButton = new JButton("Graph");
            graphButton.setEnabled(false);
            graphButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    newSelectedGraphPanel();
                }
            });
            menu.add(graphButton);
            
            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    data = newModel();
                    t.setModel(data);
                }
            });
            menu.add(clearButton);
            
        }
        add(menu, BorderLayout.SOUTH);
    }
    
    public DefaultTableModel newModel() {
        DefaultTableModel data = new DefaultTableModel();
        data.addColumn("Time");
        data.addColumn("Sentence");
        data.addColumn("Type");
        data.addColumn("Frequency");
        data.addColumn("Confidence");        
        data.addColumn("Complexity");
        data.addColumn("Priority");
        data.addColumn("ParentTask");       
        data.fireTableStructureChanged();
        return data;
    }

    
    public void newSelectedGraphPanel() {
        new ProcessingGraphPanel(nar, getSelectedRows());        
    }
    
    @Override
    public void output(Class channel, Object o) {
        if (o instanceof Task) {
            Task t = (Task)o;
            float priority = t.getPriority();
            
            Sentence s = (Sentence)t.sentence;
            
            float freq = -1;
            float conf = -1;
            TruthValue truth = s.truth;
            if (truth!=null) {
                freq = truth.getFrequency();
                conf = truth.getConfidence();
            }
            
            String parentTask = (t.parentTask!=null) ? t.parentTask.toStringBrief() : ""; 
            
            //TODO use table sort instead of formatting numbers with leading '0's
            data.addRow(new Object[] {
                String.format("%08d",  nar.getTime()), 
                s,
                s.punctuation,
                freq == -1 ? "" : freq,
                conf == -1 ? "" : conf,
                String.format("%03d",  s.content.getComplexity()),
                priority,
                parentTask
            });
        }
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing)
            nar.addOutput(this);        
        else
            nar.removeOutput(this);                
    }

    private List<Object> getSelectedRows() {
        int[] selectedRows = t.getSelectedRows();
        List<Object> l = new ArrayList(selectedRows.length);
        for (int i : selectedRows) {            
            int selectedRow = t.convertRowIndexToModel(i);            
            l.add((Sentence)data.getValueAt(selectedRow, 0));
        }
        return l;        
    }
    
    
}
