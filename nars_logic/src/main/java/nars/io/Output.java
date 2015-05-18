package nars.io;

import nars.Events;
import nars.Events.Answer;
import nars.Memory;
import nars.NAR;
import nars.event.NARReaction;
import nars.util.event.EventEmitter;
import nars.nal.nal8.ImmediateOperation;
import nars.op.io.Echo;
import nars.op.io.say;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public abstract class Output extends NARReaction {


    public static final Class[] DefaultOutputEvents = new Class[] {
            Events.IN.class,
            Events.EXE.class,
            Events.OUT.class,
            Events.ERR.class,
            ImmediateOperation.class,
            Echo.class,
            say.class,
            Answer.class
            //Events.PluginsChange.class //this gets annoying
    };
            
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
