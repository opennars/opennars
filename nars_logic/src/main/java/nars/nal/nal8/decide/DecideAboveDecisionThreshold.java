package nars.nal.nal8.decide;

import nars.concept.Concept;
import nars.nal.nal8.Operation;


public class DecideAboveDecisionThreshold extends DecideAllGoals {

    public final static DecideAboveDecisionThreshold the = new DecideAboveDecisionThreshold();

    @Override
    public boolean decide(Concept c, Operation task) {
        if (super.decide(c, task))
            return c.isDesired();
        return false;
    }

}