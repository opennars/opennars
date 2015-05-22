package nars.nal.nal8;

import nars.Symbols;
import nars.nal.concept.Concept;

/**
 * true if the task is a goal, even if it is negative (frequency < 0.5)
 */
public class DecideGoals implements Decider {

    public final static DecideGoals the = new DecideGoals();

    @Override
    public boolean decide(Concept c, Operation task) {
        return (task.getTask().getPunctuation() == Symbols.GOAL);
    }

}
