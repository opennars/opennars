package nars.nar.experimental;

import nars.Memory;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.cycle.AbstractCycle;
import nars.io.Perception;
import nars.io.in.Input;
import nars.task.Task;
import nars.task.TaskAccumulator;
import nars.task.TaskComparator;
import nars.term.Term;

import java.util.Iterator;

/**
 * Spiking continuous-time model designed by TonyLo
 *
 */
public class Spinnar extends AbstractCycle {

    /** holds original (user-input) tasks */
    protected final TaskAccumulator inputs = new TaskAccumulator(TaskComparator.Merging.Plus);

    @Override
    public boolean accept(Task t) {
        if (t.isInput())
            inputs.add(t);

        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void conceptPriorityHistogram(double[] bins) {

    }

    @Override
    public void cycle() {

    }

    @Override
    public Concept conceptualize(Term term, Budget budget, boolean createIfMissing) {
        return null;
    }

    @Override
    public Concept nextConcept() {
        return null;
    }

    @Override
    public boolean reprioritize(Term term, float newPriority) {
        return false;
    }

    @Override
    public Concept remove(Concept c) {
        return null;
    }

    @Override
    protected void on(Concept c) {

    }

    @Override
    protected void off(Concept c) {

    }

    @Override
    protected boolean active(Term t) {
        return false;
    }

    @Override
    public Iterator<Concept> iterator() {
        return null;
    }
}
