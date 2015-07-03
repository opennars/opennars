/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.meter.condition;

import nars.Events;
import nars.NAR;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author me
 */
public class OutputEmptyCondition extends OutputCondition {
    List<String> output;

    public OutputEmptyCondition(NAR nar) {
        super(nar);
        succeeded = true;
    }

    @Override
    public String getFalseReason() {
        return "FAIL: output exists but should not: " + output;
    }

    @Override
    public boolean condition(Class channel, Object signal) {
        //any OUT or ERR output is a failure
        if ((channel == Events.OUT.class) || (channel == Events.ERR.class)) {
            if (output == null)
                output = new LinkedList();
            output.add(channel.getSimpleName() + ": " + signal.toString());
            succeeded = false;
            return false;
        }
        return false;
    }
    
}
