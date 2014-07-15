package nars.gui;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.JPanel;

/**
 * JPanel subclass that is aware of when it is shown. This allows event handlers to attach and reattach to NAR's
 * @author SeH
 */
abstract public class NPanel extends JPanel implements HierarchyListener {

    public void addNotify() {
        super.addNotify();
        addHierarchyListener(this);
    }

    public void removeNotify() {
        removeHierarchyListener(this);
        super.removeNotify();
    }

    public void hierarchyChanged(HierarchyEvent e) {
        if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
            boolean showing = isShowing();
            onShowing(showing);
        }
    }    
    
    abstract protected void onShowing(boolean showing);
    
}
