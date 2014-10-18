package nars.inference;

import nars.core.EventEmitter;
import nars.core.Events;
import nars.entity.Concept;
import nars.io.Output;

/**
 *
 */
public abstract class AbstractObserver implements EventEmitter.Observer {
    protected final EventEmitter source;
    protected boolean active = false;
    private final Class[] events;

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
