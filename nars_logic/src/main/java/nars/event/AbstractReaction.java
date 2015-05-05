package nars.event;

import nars.Memory;
import nars.NAR;

/**
 * Class whch manages the registration and unregistration of event handlers
 * with an EventEmitter. it may be enabled and disabled repeatedly with
 * different event classes as selector keys for event bus messages.
 */
public abstract class AbstractReaction implements Reaction {
    
    protected final EventEmitter source;
    protected EventEmitter.Registrations active;
    private final Class[] events;

    public AbstractReaction(NAR n, Class... events) {
        this(n.memory.event, true, events);
    }

    public AbstractReaction(Memory m, boolean active, Class... events) {
        this(m.event, active, events);
    }

    public AbstractReaction(Memory m, Class... events) {
        this(m.event, true, events);
    }
    public AbstractReaction(EventEmitter n, Class... events) {
        this(n, true, events);
    }

    public AbstractReaction(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }

    public AbstractReaction(EventEmitter source, boolean active, Class... events) {
        super();
        this.source = source;
        this.events = events;

        setActive(active);
    }

    public void setActive(boolean b) {
        
        if (b && (this.active==null)) {
            this.active = source.on(this, events);
        }
        else if (!b && (this.active!=null)) {
            this.active.off();
            this.active = null;
        }

    }

    public void off() { setActive(false); }

    protected void emit(Class channel, Object... signal) {
        source.emit(channel, signal);
    }

    public boolean isActive() {
        return active!=null;
    }

}
