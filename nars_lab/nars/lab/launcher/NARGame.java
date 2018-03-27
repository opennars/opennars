package nars.lab.launcher;

import nars.io.events.EventEmitter.EventObserver;
import nars.io.events.Events;
import nars.storage.Memory;
import nars.main.NAR;
import nars.gui.NARSwing;

/**
 * Game event-loop interface for NARS sensory and motor interaction
 */
abstract public class NARGame implements EventObserver {
    public final NAR nar;
    private int cyclesPerFrame;
    public NARSwing sw;

    public NARGame(NAR nar) {        
        this.nar = nar;        
        //if (nar.memory.param.getTiming()!=Memory.Timing.Simulation)
        //    throw new RuntimeException(this + " requires NAR use Simulation timing");
        
        nar.memory.event.on(Events.CyclesEnd.class, this);
        sw=new NARSwing(nar);
    }
    
    abstract public void init();
    abstract public void cycle();
    
    
    public void start(float fps, int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
        nar.start((long)(1000.0f / fps));
    }
    
    public void stop() {
        nar.stop();
    }


    @Override
    public void event(Class event, Object[] arguments) {
        if (event == Events.CyclesEnd.class) {
            cycle();
        }
    }
    
    
}
