package nars.nal.nal8.operator;

import nars.nal.Truth;
import nars.nal.nal8.decide.Decider;
import nars.nal.nal8.decide.DecideAboveDecisionThresholdAndQuestions;
import nars.nal.nal8.operator.TermFunction;

/**
 * A termfunction which evaluates to a Truth value,
 * and allows invocation by question
 */
abstract public class TermPredicate extends TermFunction<Truth> {

    public Decider decider() {
        return DecideAboveDecisionThresholdAndQuestions.the;
    }

}
