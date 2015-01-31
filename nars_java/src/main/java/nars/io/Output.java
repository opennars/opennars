package nars.io;

import nars.core.Events;
import nars.core.Events.Answer;
import nars.core.Memory;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.event.EventEmitter;
import nars.operator.io.Echo;
import nars.operator.io.Say;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public abstract class Output extends AbstractReaction {


    public static final Class[] DefaultOutputEvents = new Class[] { Events.IN.class, Events.EXE.class, Events.OUT.class, Events.ERR.class, Echo.class, Say.class, Answer.class, Events.PluginsChange.class };
            
    public Output(EventEmitter source, boolean active) {
        super(source, active, DefaultOutputEvents );
    }
    
    public Output(Memory m, boolean active) {
        this(m.event, active);
    }

    public Output(NAR n, boolean active) {
        this(n.memory.event, active);
    }

    public Output(NAR n) {
        this(n, true);
    }

}
