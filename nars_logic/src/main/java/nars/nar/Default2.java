package nars.nar;

import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.concept.util.ConceptActivator;
import nars.nal.Deriver;
import nars.task.Task;
import nars.task.flow.SortedTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.Term;
import nars.time.FrameClock;
import nars.util.data.list.FasterList;

import java.util.List;

/**
 * Various extensions enabled
 */
public class Default2 extends Default {

    /**
     * max # of tasks to accumulate in sorted buffer
     */


    public Default2(int numConcepts,
                    int conceptsFirePerCycle,
                    int tasklinkFirePerConcept,
                    int termlinkFirePerConcept) {
        this(new Memory(new FrameClock(), CacheBag.memory(numConcepts)), numConcepts, conceptsFirePerCycle, tasklinkFirePerConcept, termlinkFirePerConcept);
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

        final int inputCapacity = activeConcepts/8; //HACK heuristic

        return new DefaultCycle2(this, deriver,
                conceptBag, activator,
                inputCapacity);
    }

    /**
     * normalizes each derivation's tasks as a group before inputting into
     * the main perception buffer.
     * ex: this can ensure that a premise which produces many derived tasks
     * will not consume budget unfairly relative to another premise
     * with less tasks but equal budget.
     */
    public static class DefaultCycle2 extends DefaultCycle {

        public DefaultCycle2(NAR nar, Deriver deriver, Bag<Term, Concept> concepts, ConceptActivator ca, int initialCapacity) {
            super(nar, deriver, concepts, ca);

            derivedTasksBuffer = new FasterList();

        }


        /**
         * holds the resulting tasks of one derivation so they can
         * be normalized or some other filter or aggregation
         * applied collectively.
         */
        final List<Task> derivedTasksBuffer;

        @Override
        protected void fireConcept(Concept c) {

            //used to estimate the fraction this batch should be scaled but this is not accurate

            fireConcept(c, p -> {


                deriver.run(p, derivedTasksBuffer::add);

                if (!derivedTasksBuffer.isEmpty()) {


                    Task.normalize(
                            derivedTasksBuffer,
                            p.getMeanPriority());

                    derivedTasksBuffer.forEach(
                        t -> nar.input(t)
                    );

                    derivedTasksBuffer.clear();
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
