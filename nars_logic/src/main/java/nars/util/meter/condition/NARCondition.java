package nars.util.meter.condition;

import org.slf4j.Logger;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * a condition which can be observed as being true or false
 * in the observed behavior of a NAR
 */
public interface NARCondition extends Serializable {



    long getSuccessTime();

    boolean isTrue();

    default void toString(PrintStream out) {
        out.print(toString());
    }

    /** max possible cycle time in which this condition could possibly be satisfied. */
    long getFinalCycle();

    default void toLogger(Logger logger) {
        String s = toString();
        if (isTrue())
            logger.info(s);
        else
            logger.warn(s);
    }
}
