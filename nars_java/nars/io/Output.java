package nars.io;

import nars.util.EventEmitter;
import nars.util.Events.Answer;
import nars.storage.Memory;
import nars.core.NAR;
import nars.util.AbstractObserver;
import nars.console.Say;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public abstract class Output extends AbstractObserver {
    
    
    /** implicitly repeated input (a repetition of all input) */
    public static interface IN  { }
    
    /** conversational (judgments, questions, etc...) output */
    public static interface OUT  { }
    
    /** warnings, errors & exceptions */
    public static interface ERR { }
    
    /** explicitly repeated input (repetition of the content of input ECHO commands) */
    public static interface ECHO  { }
    
    /** operation execution */
    public static interface EXE  { }
    
        
    public static class ANTICIPATE {}
    
    public static class CONFIRM {}
    
    public static class DISAPPOINT {}

    public static final Class[] DefaultOutputEvents = new Class[] { IN.class, EXE.class, OUT.class, ERR.class, ECHO.class, Say.class, Answer.class, ANTICIPATE.class, CONFIRM.class, DISAPPOINT.class };
            
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
