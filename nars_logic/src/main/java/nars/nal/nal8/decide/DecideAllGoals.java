package nars.nal.nal8.decide;

import nars.Symbols;
import nars.nal.concept.Concept;
import nars.nal.nal8.Operation;

/**
 * true if the task is a goal, even if it is negative (frequency < 0.5)
 */
public class DecideAllGoals implements Decider {

    public final static DecideAllGoals the = new DecideAllGoals();

    @Override
    public boolean decide(Concept c, Operation task) {
        return (task.getTask().getPunctuation() == Symbols.GOAL);
    }

}
