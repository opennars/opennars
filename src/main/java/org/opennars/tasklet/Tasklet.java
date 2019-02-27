package org.opennars.tasklet;

import org.opennars.entity.Item;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.language.Term;

/**
 * Belief or task which can be schedualed for (independent) processing
 */
public class Tasklet extends  Item<Term> {
    private final EnumType type;
    public final Task task;
    public final Sentence belief;

    public Tasklet(final Task task) {
        type = EnumType.TASK;
        this.task = task;
        this.belief = null;
    }

    public Tasklet(final Sentence belief) {
        type = EnumType.BELIEF;
        this.belief = belief;
        this.task = null;
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

    public enum EnumType {
        BELIEF,
        TASK
    }
}
