package nars.nal.nal8.decide;

import nars.Memory;
import nars.concept.Concept;
import nars.nal.nal8.Operation;
import nars.task.Task;
import nars.truth.Truth;


abstract public class DecideAboveDecisionThreshold extends DecideAllGoals {

    public final Memory memory;

    public DecideAboveDecisionThreshold(Memory m) {
        this.memory = m;
    }

    public final static class DecideTaskDesireAboveDecisionThreshold extends DecideAboveDecisionThreshold {

        public DecideTaskDesireAboveDecisionThreshold(Memory m) {
            super(m);
        }

        @Override
        protected final float desire(Task<Operation> task) {
            Truth t = task.getTruth();
            if (t == null)
                throw new RuntimeException("null truth");

            return t.getExpectation();
        }
    }

    public final static class DecideConceptDesireAboveDecisionThreshold extends DecideAboveDecisionThreshold {

        public DecideConceptDesireAboveDecisionThreshold(Memory m) {
            super(m);
        }

        @Override
        protected final float desire(Task<Operation> task) {
            Concept c = memory.concept(task.getTerm());
            Truth t = c.getDesire();
            if (t == null)
                throw new RuntimeException("null truth");

            return t.getExpectation();
        }
    }

    @Override
    public boolean test(final Task<Operation> task) {
        if (super.test(task)) {
            return desire(task) > memory.executionThreshold.floatValue();
        }
        return false;
    }

    /** computes the effective desire value for a given task */
    protected abstract float desire(Task<Operation> task);

}
/*
public class DecideAboveDecisionThreshold extends DecideAllGoals {

    final AtomicDouble executionThreshold = new AtomicDouble();

    public DecideAboveDecisionThreshold(final float initialValue) {
        executionThreshold.set(initialValue);
    }

    @Override
    public boolean test(final Operation task) {
        if (super.test(task)) {
            return task.getConcept().isDesired(
                    executionThreshold.floatValue()
            );
        }
        return false;
    }

}
 */