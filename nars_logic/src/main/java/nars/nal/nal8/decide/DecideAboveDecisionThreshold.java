package nars.nal.nal8.decide;

import nars.concept.Concept;
import nars.nal.nal8.Operation;


public class DecideAboveDecisionThreshold extends DecideAllGoals {

    public final static DecideAboveDecisionThreshold the = new DecideAboveDecisionThreshold();

    @Override
    public boolean test(final Operation task) {
        if (super.test(task)) {
            return task.getConcept().isDesired(
                    task.getMemory().param.executionThreshold.floatValue()
            );
        }
        return false;
    }

}
/*
public class DecideAboveDecisionThreshold extends DecideAllGoals {

    final AtomicDouble executionThreshold = new AtomicDouble();

    public DecideAboveDecisionThreshold(final float initialValue) {
        executionThreshold.set(initialValue);
    }

    @Override
    public boolean test(final Operation task) {
        if (super.test(task)) {
            return task.getConcept().isDesired(
                    executionThreshold.floatValue()
            );
        }
        return false;
    }

}
 */