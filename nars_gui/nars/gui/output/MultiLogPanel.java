package nars.gui.output;

import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nars.entity.Task;
import nars.gui.NARControls;

/**
 *
 * @author me
 */
public class MultiLogPanel extends JPanel {

    public Map<Task, LogPanel> taskPanel = new HashMap();
    private final SwingLogPanel rootTaskPanel;
    private final NARControls controls;

    public MultiLogPanel(NARControls c) {
        super(new GridLayout(1, 0));

        this.controls = c;

        rootTaskPanel = new SwingLogPanel(c) {

            @Override
            public void output(Class c, Object o) {
                System.out.println(o);
                if (o instanceof Task) {
                    Task t = (Task) o;
                    Task parent = t.getRootTask();
                    LogPanel p = getLogPanel(parent);
                    if (p == this)
                        super.output(c, o);
                    else
                        p.output(c, o);
                } else {
                    super.output(c, o);
                }
            }

        };
        add(rootTaskPanel);
    }

    LogPanel getLogPanel(Task parent) {
        if (parent == null) {
            return rootTaskPanel;
        } else {
            LogPanel p = taskPanel.get(parent);
            if (p == null) {
                p = new SwingLogPanel(controls);
                add(p);
                System.out.println("new task" + parent);
                taskPanel.put(parent, p);
            }
            return p;
        }
    }

    
    public void add(LogPanel p) {
        add(new JScrollPane(p));
    }
}
