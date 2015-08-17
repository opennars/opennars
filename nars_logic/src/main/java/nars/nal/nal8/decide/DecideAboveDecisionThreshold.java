package nars.nal.nal8.decide;

import nars.concept.Concept;
import nars.nal.nal8.Operation;


public class DecideAboveDecisionThreshold extends DecideAllGoals {

    public final static DecideAboveDecisionThreshold the = new DecideAboveDecisionThreshold();

    @Override
    public boolean test(Operation task) {
        if (super.test(task)) {
            return task.getConcept().isDesired();
        }
        return false;
    }

}