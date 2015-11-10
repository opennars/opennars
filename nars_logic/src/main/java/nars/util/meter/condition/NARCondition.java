package nars.util.meter.condition;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * a condition which can be observed as being true or false
 * in the observed behavior of a NAR
 */
public interface NARCondition extends Serializable {



    long getSuccessTime();

    boolean isTrue();

    String toConditionString();

    default void toString(PrintStream out) {
        out.print(toConditionString());
    }

    /** max possible cycle time in which this condition could possibly be satisfied. */
    long getFinalCycle();

    void report();
}
