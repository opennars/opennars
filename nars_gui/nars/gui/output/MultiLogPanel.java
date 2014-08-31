package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import nars.core.NAR;
import nars.entity.Task;
import nars.gui.NARControls;
import nars.gui.output.SwingLogPanel.SwingLogText;
import nars.io.Output;

/**
 *
 * @author me
 */
public class MultiLogPanel extends JPanel implements Output {

    public Map<Task, SwingLogText> taskPanel = new HashMap();
    private final SwingLogText rootTaskPanel;
    private final NAR nar;
    private final JPanel content;

    public MultiLogPanel(NARControls c) {
        super(new BorderLayout());
        
        content = new JPanel(new GridLayout(1, 0));
        add(content, BorderLayout.CENTER);

        this.nar = c.nar;
        nar.addOutput(this);
        
        rootTaskPanel = new SwingLogText() {

        };
        add("Root", rootTaskPanel);
    }

    @Override
    public void output(Class channel, Object o) {
        if (o instanceof Task) {
            Task t = (Task) o;
            Task parent = t.getRootTask();
            SwingLogText p = getLogPanel(parent);
            p.print(channel, o, false, nar);
        } else {
            rootTaskPanel.print(channel, o, false, nar);
        }
    }


    SwingLogText getLogPanel(Task parent) {
        if (parent == null) {
            return rootTaskPanel;
        } else {
            SwingLogText p = taskPanel.get(parent);
            if (p == null) {
                p = new SwingLogText();
                add(parent.toStringBrief(), p);
                taskPanel.put(parent, p);
            }
            return p;
        }
    }
    
    public void add(String title, SwingLogText p) {
        int columnWidth = 400;
        SwingLogPanel.setConsoleStyle(p, true);
                
        JPanel x = new JPanel(new BorderLayout());
        x.add(new JButton(title), BorderLayout.NORTH);
        x.add(new JScrollPane(p), BorderLayout.CENTER);
        
        
        x.setMinimumSize(new Dimension(columnWidth, 0));
        x.setMaximumSize(new Dimension(columnWidth, Integer.MAX_VALUE/2));
        x.setPreferredSize(new Dimension(columnWidth, 0));
        x.validate();
        
        content.add(x);
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                revalidate();
                repaint();
            }            
        });
    }
}
