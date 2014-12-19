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
 *
 * @author me
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
                setSucceeded();
            }
        }
    }

    protected void setSucceeded() {
        if (successAt == -1) {
            successAt = nar.time();
        }
        succeeded = true;
    }

    public boolean success() {
        return succeeded;
    }

    /** returns true if condition was satisfied */
    public abstract boolean condition(Class channel, Object signal);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + (succeeded ? "OK: " + exact : getFailureReason());
    }

    public abstract String getFailureReason();

    public long getSuccessAt() {
        return successAt;
    }
    
    
}
