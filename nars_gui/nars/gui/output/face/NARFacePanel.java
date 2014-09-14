package nars.gui.output.face;

import nars.core.NAR;

/**
 *
 * @author me
 */


public class NARFacePanel extends HumanoidFacePanel {
    private final NAR nar;

    public NARFacePanel(NAR n) {
        super();
        this.nar = n;
    }

    @Override
    public void update(double t) {
        happy = nar.memory.emotion.happy() > 0.6;
        busy = nar.memory.emotion.busy() > 0.95;
        
        super.update(t);        
    }
    
}
