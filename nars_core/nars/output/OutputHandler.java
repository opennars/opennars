package nars.output;

import nars.output.EventHandler;
import nars.util.EventEmitter;
import nars.util.Events.Answer;
import nars.storage.Memory;
import nars.NAR;

/**
 * Output Channel: Implements this and NAR.addOutput(..) to receive output signals on various channels
 */
public abstract class OutputHandler extends EventHandler {
    
    
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

    public static final Class[] DefaultOutputEvents = new Class[] { IN.class, EXE.class, OUT.class, ERR.class, ECHO.class, Answer.class, ANTICIPATE.class, CONFIRM.class, DISAPPOINT.class };
            
    public OutputHandler(EventEmitter source, boolean active) {
        super(source, active, DefaultOutputEvents );
    }
    
    public OutputHandler(Memory m, boolean active) {
        this(m.event, active);
    }

    public OutputHandler(NAR n, boolean active) {
        this(n.memory.event, active);
    }

    public OutputHandler(NAR n) {
        this(n, true);
    }

}
