package nars.nal.nal8;

import nars.Symbols;
import nars.nal.concept.Concept;


public class DesireThresholdExecutive extends DecideGoals {

    public final static DesireThresholdExecutive the = new DesireThresholdExecutive();

    @Override
    public boolean decide(Concept c, Operation task) {
        if (super.decide(c, task))
            return c.isDesired();
        return false;
    }

}