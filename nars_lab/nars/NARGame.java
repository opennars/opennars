package nars;

import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.gui.NARSwing;

/**
 * Game event-loop interface for NARS sensory and motor interaction
 */
abstract public class NARGame implements Observer {
    public final NAR nar;

    public NARGame(NAR nar) {        
        this.nar = nar;        
        nar.memory.event.on(Events.FrameEnd.class, this);
        new NARSwing(nar);
    }
    
    abstract public void init();
    abstract public void cycle();
    
    
    public void start(float fps, int cyclesPerFrame) {
        nar.start(fps, cyclesPerFrame);
    }
    
    public void stop() {
        nar.stop();
    }


    @Override
    public void event(Class event, Object[] arguments) {
        if (event == Events.FrameEnd.class) {
            cycle();
        }
    }
    
    
}
