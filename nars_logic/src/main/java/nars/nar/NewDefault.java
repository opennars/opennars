package nars.nar;

import nars.nal.LogicPolicy;
import nars.nal.NALExecuter;

/**
 * Temporary class which uses the new rule engine for ruletables
 */
public class NewDefault extends Default {
    @Override
    public LogicPolicy getLogicPolicy() {
        return newPolicy(NALExecuter.defaults);
    }
}
