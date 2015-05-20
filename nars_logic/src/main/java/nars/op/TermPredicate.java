package nars.op;

import nars.nal.Truth;
import nars.nal.nal8.Decider;
import nars.nal.nal8.DesireThresholdExecutivePredicate;
import nars.nal.nal8.TermFunction;

/**
 * A termfunction which evaluates to a Truth value,
 * and allows invocation by question
 */
abstract public class TermPredicate extends TermFunction<Truth> {

    public Decider decider() {
        return DesireThresholdExecutivePredicate.the;
    }

}
