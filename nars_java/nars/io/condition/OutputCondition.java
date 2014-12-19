/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import java.util.ArrayList;
import java.util.List;
import nars.core.NAR;
import nars.io.Output;

/**
 * Monitors an output stream for certain conditions. Used in testing and
 * analysis
 */
public abstract class OutputCondition extends Output {
    public boolean succeeded = false;
    public List<CharSequence> exact = new ArrayList();
    public final NAR nar;
    long successAt = -1;

    public OutputCondition(NAR nar) {
        super(nar);
        this.nar = nar;
    }

    /** whether this is an "inverse" condition */
    public boolean isInverse() {
        return false;
    }

    @Override
    public void event(Class channel, Object... args) {
        if ((succeeded) && (!isInverse())) {
            return;
        }
        if ((channel == OUT.class) || (channel == EXE.class)) {
            Object signal = args[0];
            if (condition(channel, signal)) {
                setTrue();
            }
        }
    }

    protected void setTrue() {
        if (successAt == -1) {
            successAt = nar.time();
        }
        succeeded = true;
    }

    public boolean isTrue() {
        return succeeded;
    }

    /** returns true if condition was satisfied */
    public abstract boolean condition(Class channel, Object signal);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + (succeeded ? "OK: " + exact : getFalseReason());
    }

    /** if false, a reported reason why this condition is false */
    public abstract String getFalseReason();

    /** if true, when it became true */
    public long getTrueTime() {
        return successAt;
    }
    
    
}
