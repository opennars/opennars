package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.TruthValue;
import nars.util.NARGraph;
import nars.util.NARGraph.DefaultGraphizer;
import nars.util.NARGraph.Filter;
import nars.util.NARGraph.SentenceContent;
import nars.gui.NPanel;
import nars.io.Output;
import nars.language.CompoundTerm;
import nars.language.Term;

/**
 *
 * @author me
 */


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
        data.addColumn("Sentence");
        data.addColumn("Time");
        data.addColumn("Punctuation");
        data.addColumn("Frequency");
        data.addColumn("Confidence");        
        data.addColumn("Complexity");
        data.fireTableStructureChanged();
        return data;
    }

    
    public void newSelectedGraphPanel() {
        new ProcessingGraphPanel(nar, getSelectedRows());        
    }
    
    @Override
    public void output(Class channel, Object o) {
        if (o instanceof Sentence) {
            Sentence s = (Sentence)o;
            
            float freq = -1;
            float conf = -1;
            TruthValue truth = s.getTruth();
            if (truth!=null) {
                freq = truth.getFrequency();
                conf = truth.getConfidence();
            }
            
            //TODO use table sort instead of formatting numbers with leading '0's
            data.addRow(new Object[] {
                s,
                String.format("%08d",  nar.getTime()),
                s.getPunctuation(),
                freq == -1 ? "" : freq,
                conf == -1 ? "" : conf,
                String.format("%03d",  s.getContent().getComplexity())     
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

    private List<Sentence> getSelectedRows() {
        int[] selectedRows = t.getSelectedRows();
        List<Sentence> l = new ArrayList(selectedRows.length);
        for (int i : selectedRows) {            
            int selectedRow = t.convertRowIndexToModel(i);            
            l.add((Sentence)data.getValueAt(selectedRow, 0));
        }
        return l;        
    }
    
    
}
