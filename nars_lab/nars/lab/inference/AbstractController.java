package nars.lab.inference;

import nars.core.EventEmitter;
import nars.core.Events;
import nars.core.NAR;


public abstract class AbstractController implements EventEmitter.EventObserver {

    public final NAR nar;
    /** how many cycles to wait before action, then wait again.. */
    private int period;

    /** how many cycles to wait before action, then wait again.. */
    public AbstractController(NAR n, int period) {
        this.nar = n;
        this.period = period;
        //nar.memory.logic.setActive(true);
        start();
    }
    /** read sensor values as input */
    /** adjust parameter values */

    public void start() {
        nar.on(Events.CycleEnd.class, this);
    }

    /** read sensor values as input */
    public abstract void getSensors();

    /** adjust parameter values */
    public abstract void setParameters();

    @Override
    public void event(final Class event, final Object... arguments) {
        //TODO use relative time (not modulo) for non-sequence time modes
        long cycle = nar.time();
        //nar.memory.logic.update(nar.memory);
        getSensors();
        if (cycle % period == (period - 1)) {
            setParameters();
        }
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void stop() {
        nar.off(Events.CycleEnd.class, this);
    }
}
