package nars.gui;

import automenta.vivisect.swing.NPanel;
import nars.NAR;
import nars.util.event.EventEmitter;
import nars.util.event.Reaction;

import java.awt.*;

@SuppressWarnings("AbstractClassNeverImplemented")
public abstract class ReactionPanel extends NPanel implements Reaction<Class,Object[]> {

    public final NAR nar;
    private EventEmitter.Registrations reg;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ReactionPanel(NAR n) {
        nar = n;
    }
    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ReactionPanel(NAR n, LayoutManager l) {
        this(n);
        setLayout(l);
    }


    @Override
    protected void visibility(boolean appearedOrDisappeared) {
        if (appearedOrDisappeared) {
            if (reg==null)
                reg = nar.memory.event.on(this, getEvents());
        }
        else {
            if (reg!=null)
                reg.off();
        }
    }

    public abstract Class[] getEvents();

}
