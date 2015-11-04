package nars.util.event;

import nars.Memory;
import nars.NAR;

/**
 * Class whch manages the registration and unregistration of event handlers
 * with an EventEmitter. it may be enabled and disabled repeatedly with
 * different event classes as selector keys for event bus messages.
 */
public abstract class NARReaction extends AbstractReaction<Class,Object[]> {



    public NARReaction(NAR n, Class... events) {
        this(n.memory.event, true, events);
    }

    public NARReaction(Memory m, boolean active, Class... events) {
        this(m.event, active, events);
    }

    public NARReaction(Memory m, Class... events) {
        this(m.event, true, events);
    }
    public NARReaction(EventEmitter n, Class... events) {
        this(n, true, events);
    }

    public NARReaction(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }

    public NARReaction(EventEmitter source, boolean active, Class... events) {
        super(source, active, events);
    }



}
