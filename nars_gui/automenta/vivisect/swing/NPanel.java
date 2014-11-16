package automenta.vivisect.swing;

import java.awt.LayoutManager;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JPanel;

/**
 * JPanel subclass that is aware of when it is shown. This allows event handlers to attach and reattach to NAR's
 * @author SeH
 */
abstract public class NPanel extends JPanel implements HierarchyListener {

    public NPanel() {
        super();
    }

    public NPanel(LayoutManager l) {
        super(l);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        addHierarchyListener(this);
    }

    @Override
    public void removeNotify() {
        removeHierarchyListener(this);
        super.removeNotify();
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            boolean showing = isShowing();
            onShowing(showing);
        }
    }    
    
    abstract protected void onShowing(boolean showing);
    
}
