package nars.io.events;

import nars.main.NAR;

/**
 *
 */
public abstract class EventHandler implements EventEmitter.EventObserver {
    protected final EventEmitter source;
    protected boolean active = false;
    private final Class[] events;

    public EventHandler(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }
    
    public EventHandler(EventEmitter source, boolean active, Class... events) {
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
