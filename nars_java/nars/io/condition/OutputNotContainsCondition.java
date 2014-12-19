/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import nars.core.NAR;

/**
 *
 * @author me
 */
public class OutputNotContainsCondition extends OutputContainsCondition {

    public OutputNotContainsCondition(NAR nar, String containing) {
        super(nar, containing, false);
        succeeded = true;
    }

    @Override
    public String getFailureReason() {
        return "incorrect output: " + containing;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        if (!succeeded) {
            return false;
        }
        if (cond(channel, signal)) {
            onFailure(channel, signal);
            succeeded = false;
            return false;
        }
        return true;
    }

    public boolean isInverse() {
        return true;
    }

    protected void onFailure(Class channel, Object signal) {
    }
    
}
