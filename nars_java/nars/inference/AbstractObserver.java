package nars.inference;

import nars.util.EventEmitter;
import nars.core.NAR;

/**
 *
 */
public abstract class AbstractObserver implements EventEmitter.EventObserver {
    protected final EventEmitter source;
    protected boolean active = false;
    private final Class[] events;

    
    public AbstractObserver(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }

    
    public AbstractObserver(EventEmitter source, boolean active, Class... events) {
        this.source = source;
        this.events = events;
        setActive(active);
    }

    public void setActive(boolean b) {
        if (this.active == b) return;
        
        this.active = b;
        source.set(this, b, events);
    }

    public boolean isActive() {
        return active;
    }
    
}
