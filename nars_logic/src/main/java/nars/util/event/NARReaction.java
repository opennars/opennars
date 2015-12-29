package nars.util.event;

import nars.Memory;
import nars.NAR;

/**
 * Class whch manages the registration and unregistration of event handlers
 * with an EventEmitter. it may be enabled and disabled repeatedly with
 * different event classes as selector keys for event bus messages.
 */
public abstract class NARReaction extends AbstractReaction<Class,Object[]> {



    protected NARReaction(NAR n, Class... events) {
        this(n.memory.event, true, events);
    }

    protected NARReaction(Memory m, boolean active, Class... events) {
        this(m.event, active, events);
    }

    protected NARReaction(Memory m, Class... events) {
        this(m.event, true, events);
    }
    protected NARReaction(EventEmitter n, Class... events) {
        this(n, true, events);
    }

    protected NARReaction(NAR n, boolean active, Class... events) {
        this(n.memory.event, active, events);
    }

    protected NARReaction(EventEmitter source, boolean active, Class... events) {
        super(source, active, events);
    }



}
