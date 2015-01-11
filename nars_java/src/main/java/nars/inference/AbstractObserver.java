package nars.inference;

import nars.core.EventEmitter;
import nars.core.EventEmitter.Registrations;
import nars.core.NAR;

/**
 * Class whch manages the registration and unregistration of event handlers
 * with an EventEmitter. it may be enabled and disabled repeatedly with
 * different event classes as selector keys for event bus messages.
 */
public abstract class AbstractObserver implements EventEmitter.EventObserver {
    
    protected final EventEmitter source;
    protected Registrations active;
    private final Class[] events;
    
    public AbstractObserver(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }

    
    public AbstractObserver(EventEmitter source, boolean active, Class... events) {
        this.source = source;
        this.events = events;
        setActive(active);
    }

    public synchronized void setActive(boolean b) {        
        
        if (b && (this.active==null)) {
            this.active = source.on(this, events);
        }
        else if (!b && (this.active!=null)) {
            this.active.cancel();
            this.active = null;
        }
    }

    public boolean isActive() {
        return active!=null;
    }
    
}
