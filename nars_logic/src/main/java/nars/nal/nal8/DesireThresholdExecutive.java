package nars.nal.nal8;

import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.term.Term;


public class DesireThresholdExecutive implements Decider {

    public final static DesireThresholdExecutive the = new DesireThresholdExecutive();

    @Override
    public boolean decide(Concept c, Operation task) {
        return c.isDesired();
    }

}
