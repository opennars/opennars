package org.opennars.tasklet;

import org.opennars.entity.Item;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.interfaces.Timable;
import org.opennars.language.Term;

/**
 * Belief or task which can be schedualed for (independent) processing
 */
public class Tasklet extends  Item<Term> {
    private final EnumType type;
    public final Task task;
    public final Sentence belief;

    //////////////
    // attention
    public double basePriority;

    public double cachedUtility;

    public Tasklet(final Task task, Timable timable) {
        type = EnumType.TASK;
        this.task = task;
        this.belief = null;

        recalcUtility(timable);
    }

    public Tasklet(final Sentence belief, Timable timable) {
        type = EnumType.BELIEF;
        this.belief = belief;
        this.task = null;

        recalcUtility(timable);
    }

    public boolean isBelief() {
        return type == EnumType.BELIEF;
    }

    public boolean isTask() {
        return type == EnumType.TASK;
    }

    @Override
    public Term name() {
        if (isBelief()) {
            return belief.term;
        }
        return task.sentence.term;
    }

    public void recalcUtility(Timable timable) {
        cachedUtility = calcUtility(timable);
    }

    public double calcUtility(Timable timable) {
        Sentence sentence;
        if(isBelief()) {
            sentence = belief;
        }
        else { // is a task
            sentence = task.sentence;
        }

        TruthValue truth = sentence.truth;

        long deltaT = timable.time() - sentence.stamp.getOccurrenceTime();
        double decay = Math.pow(1.0, -deltaT);

        return truth.getExpectation() + decay;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += isBelief() ? belief.hashCode() : task.hashCode();
        hash += isBelief() ? 1 : -1;
        return hash;
    }

    public enum EnumType {
        BELIEF,
        TASK
    }
}
