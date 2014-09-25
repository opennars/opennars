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
        nar.memory.event.on(Events.CycleEnd.class, this);
        new NARSwing(nar);
    }
    
    abstract public void init();
    abstract public void cycle();
    
    
    public void start(float fps) {
        nar.start(fpsToMS(fps));
    }
    
    public void stop() {
        nar.stop();
    }

    private long fpsToMS(float fps) {
        return (long)(1000.0f/fps);
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (event == Events.CycleEnd.class) {
            cycle();
        }
    }
    
    
}
