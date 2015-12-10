package automenta.vivisect.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

/**
 * JPanel subclass that is aware of when it is shown. This allows event handlers to attach and reattach to NAR's
 * @author SeH
 */
public abstract class NPanel extends JPanel implements HierarchyListener {

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public NPanel() {
        initialize();
    }

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public NPanel(LayoutManager l) {
        super(l);
        initialize();
    }

    protected void initialize() {
        //setOpaque(false);
        //setBackground(Video.transparent);
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
            visibility(showing);
        }
    }    

    /** called when visibility changes */
    protected abstract void visibility(boolean appearedOrDisappeared);


}
