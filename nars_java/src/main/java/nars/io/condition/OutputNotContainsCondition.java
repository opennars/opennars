/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import nars.NAR;

/**
 *
 * @author me
 */
public class OutputNotContainsCondition extends OutputContainsCondition {

    String failureReason = null;

    public OutputNotContainsCondition(NAR nar, String containing) {
        super(nar, containing, -1);
        succeeded = true;
    }

    @Override
    public String getFalseReason() {
        return "Output should not have contained: " + containing + ", in: " + failureReason;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        if (!succeeded) {
            return false;
        }
        if (cond(channel, signal)) {
            onFailure(channel, signal);
            failureReason = channel + ": " + signal;
            succeeded = false;
            return false;
        }
        return true;
    }

    @Override
    public boolean isInverse() {
        return true;
    }

    protected void onFailure(Class channel, Object signal) {
    }
    
}
