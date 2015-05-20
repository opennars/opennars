package nars.nal.nal8;

import nars.Symbols;
import nars.nal.concept.Concept;


public class DesireThresholdExecutive implements Decider {

    public final static DesireThresholdExecutive the = new DesireThresholdExecutive();

    @Override
    public boolean decide(Concept c, Operation task) {
        if (task.getTask().getPunctuation() == Symbols.GOAL)
            return c.isDesired();
        return false;
    }

}