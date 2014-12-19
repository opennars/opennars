/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.condition;

import java.util.LinkedList;
import java.util.List;
import nars.core.NAR;

/**
 *
 * @author me
 */
public class EmptyOutputCondition extends OutputCondition {
    List<String> output = new LinkedList();

    public EmptyOutputCondition(NAR nar) {
        super(nar);
        succeeded = true;
    }

    public String getFailureReason() {
        return "FAIL: output exists but should not: " + output;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        //any OUT or ERR output is a failure
        if ((channel == OUT.class) || (channel == ERR.class)) {
            output.add(channel.getSimpleName().toString() + ": " + signal.toString());
            succeeded = false;
            return false;
        }
        return false;
    }
    
}
