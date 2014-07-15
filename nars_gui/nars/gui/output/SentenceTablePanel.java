package nars.gui.output;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import nars.entity.Sentence;
import nars.entity.TruthValue;
import nars.gui.NARSwing;
import nars.gui.NPanel;
import nars.io.Output;

/**
 *
 * @author me
 */


public class SentenceTablePanel extends NPanel implements Output {
    private final NARSwing nar;
    
    DefaultTableModel data = new DefaultTableModel();

    public SentenceTablePanel(NARSwing nar) {
        super();
        this.nar = nar;
        
        setLayout(new BorderLayout());        
        
        data.addColumn("Time");
        data.addColumn("Sentence");
        data.addColumn("Punctuation");
        data.addColumn("Frequency");
        data.addColumn("Confidence");        
        data.fireTableStructureChanged();
        
        JTable t = new JTable(data);        
        t.setAutoCreateRowSorter(true);

        t.validate();
        
        add(new JScrollPane(t), BorderLayout.CENTER);
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
            
            data.addRow(new Object[] {
                String.format("%08d",  nar.getTime()),
                s.toString(),
                s.getPunctuation(),
                freq == -1 ? "" : freq,
                conf == -1 ? "" : conf
            });
        }
    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing)
            nar.addOutputChannel(this);        
        else
            nar.removeOutputChannel(this);                
    }
    
    
}
