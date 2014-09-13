package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;



public class SentenceTablePanel extends TablePanel {
    private final JButton graphButton;

    public SentenceTablePanel(NAR nar) {
        super(nar);
        
        setLayout(new BorderLayout());        
        
        data = newModel();
                
        table.setModel(data);
        table.setAutoCreateRowSorter(true);       
        table.validate();
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                graphButton.setEnabled(table.getSelectedRowCount() > 0);
            }
        });
        table.getColumn("Type").setMaxWidth(48);
        table.getColumn("Frequency").setMaxWidth(64);
        table.getColumn("Confidence").setMaxWidth(64);
        table.getColumn("Priority").setMaxWidth(64);
        table.getColumn("Complexity").setMaxWidth(64);
        table.getColumn("Time").setMaxWidth(72);
        
        add(new JScrollPane(table), BorderLayout.CENTER);
        
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
                    table.setModel(data);
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
            
            Sentence s = t.sentence;
            
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

    
    
}
