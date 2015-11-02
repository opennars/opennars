package nars.nar;

import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.io.SortedTaskPerception;
import nars.io.TaskPerception;
import nars.nal.Deriver;
import nars.task.Task;
import nars.term.Term;

/**
 * Various extensions enabled
 */
public class Default2 extends Default {

    /**
     * max # of tasks to accumulate in sorted buffer
     */


    public Default2(int i, int i1, int i2, int i3) {
        this(new LocalMemory(), i, i1, i2, i3);
    }

    public Default2(Memory mem, int i, int i1, int i2, int i3) {
        super(mem, i, i1, i2, i3);

        //new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }

    @Override
    protected DefaultCycle2 initCore(int activeConcepts, Deriver deriver, Bag<Term, Concept> conceptBag, ConceptActivator activator) {
        return new DefaultCycle2(this, deriver, conceptBag, activator);
    }

    /** normalizes each derivation's tasks as a group before inputting into
     * the main perception buffer.
     * ex: this can ensure that a premise which produces many derived tasks
     * will not consume budget unfairly relative to another premise
     * with less tasks but equal budget.
     * */
    public static class DefaultCycle2 extends DefaultCycle {

        public DefaultCycle2(NAR nar, Deriver deriver, Bag<Term, Concept> concepts, ConceptActivator ca) {
            super(nar, deriver, concepts, ca);
        }


        /**
         * holds the resulting tasks of one derivation so they can
         * be normalized or some other filter or aggregation
         * applied collectively.
         */
        final ItemAccumulator<Task> derivationAccumulator = new ItemAccumulator(Budget.plus);

        @Override
        protected void fireConcept(Concept c) {

            //used to estimate the fraction this batch should be scaled but this is not accurate

            fireConcept(c, p -> {

                deriver.run(p, derivationAccumulator::add);

                if (!derivationAccumulator.isEmpty()) {

                    Task.normalize(
                            derivationAccumulator.keySet(),
                            p.getMeanPriority());

                    derivationAccumulator.forEach(nar::input);

                    derivationAccumulator.clear();
                }

            });

        }
    }


    @Override
    public TaskPerception initInput() {
        int perceptionCapacity = 64;

        SortedTaskPerception input = new SortedTaskPerception(
                this,
                task -> true /* allow everything */,
                task -> exec(task),
                perceptionCapacity,
                1
        );
        //input.inputsMaxPerCycle.set(conceptsFirePerCycle);;
        return input;
    }
}
