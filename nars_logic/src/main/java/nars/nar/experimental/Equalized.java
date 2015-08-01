package nars.nar.experimental;

import nars.Global;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.cycle.DefaultCycle;
import nars.nal.nal8.ImmediateOperation;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.term.Term;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by me on 7/21/15.
 */
public class Equalized extends Default {



    public Equalized(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    public static class EqualizedCycle extends DefaultCycle {

        /** stores sorted tasks temporarily */
        private List<Task> temporary = Global.newArrayList();

        public EqualizedCycle(ItemAccumulator taskAccumulator, Bag<Term, Concept> concepts, AtomicInteger conceptsFiredPerCycle) {
            super(taskAccumulator, concepts, null, null, null, conceptsFiredPerCycle);
        }

        /**
         * An atomic working cycle of the system:
         *  0) optionally process inputs
         *  1) optionally process new task(s)
         *  2) optionally process novel task(s)
         *  2) optionally fire a concept
         **/
        @Override
        public synchronized void cycle() {

            int conceptsToFire = conceptsFiredPerCycle.get();

            concepts.forgetNext(
                    memory.param.conceptForgetDurations,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    memory);

            //inputs
            if (memory.isInputting()) {

                //input all available percepts
                Task t;
                while ((t = percepts.get())!=null) {
                    if (t instanceof ImmediateOperation.ImmediateTask)
                        memory.add(t);
                    else
                        newTasks.add(t);
                }
            }

            queueNewTasks();



            //new tasks
            int newTasksToFire = Math.min(newTasks.size(), conceptsFiredPerCycle.get());
            Iterator<Task> ii = newTasks.iterateHighestFirst();

            for (int n = newTasksToFire;  ii.hasNext() && n > 0; n--) {
                Task next = ii.next();
                if (next == null) break;

                TaskProcess tp = TaskProcess.get(memory, next, getPriority(next, conceptsToFire));
                if (tp!=null)
                    tp.run();

                ii.remove();
            }



            //1 concept if (memory.newTasks.isEmpty())*/

            float tasklinkForgetDurations = memory.param.taskLinkForgetDurations.floatValue();
            float conceptForgetDurations = memory.param.conceptForgetDurations.floatValue();
            for (int i = 0; i < conceptsToFire; i++) {
                ConceptProcess f = newProcess(nextConceptToProcess(conceptForgetDurations), tasklinkForgetDurations);
                if (f != null) {
                    f.run();
                }
            }

            int added = commitNewTasks();

            //System.out.print("newTasks=" + newTasksToFire + " + " + added + "  ");

            //System.out.print("concepts=" + conceptsToFire + "  ");

            memory.runNextTasks();

            final int maxNewTasks = conceptsToFire * memory.duration();
            if (newTasks.size() > maxNewTasks) {
                int removed = newTasks.limit(maxNewTasks, new Consumer<Task>() {
                    @Override public void accept(Task task) {
                        memory.removed(task, "Ignored");
                    }
                }, temporary);

                //System.out.print("discarded=" + removed + "  ");
            }

            //System.out.println();


        }

        //final Frequency termTypes = new Frequency();

        private float getPriority(final Task task, int conceptsToFire) {
            float f = 1f/(conceptsToFire);

//            final Compound term = task.getTerm();
//            long includedTerms = (term.structuralHash() & 0xffffffff);
//            long v = 1;
//            for (int i = 0; i < 32; i++) {
//                v = v << 1;
//                if ((includedTerms & v) > 0) {
//                    termTypes.addValue(Op.values()[i]);
//                }
//            }



//            //EXPERIMENTAL
//            double p = termTypes.getPct(term.operator());
//            f *= Math.max(0.1, 1.0 - p);


//            switch (term.operator()) {
//                case IMAGE_EXT:
//                case IMAGE_INT:
//                    f *= 0.5f;
//                    break;
//            }
//
//            if (Math.random() < 0.01) {
//                System.out.println(termTypes);
//            }

            return f;
        }


    }

    @Override
    public CycleProcess newCycleProcess() {
        return new EqualizedCycle(
                new ItemAccumulator(new ItemComparator.Plus()),
                newConceptBag(),
                conceptsFiredPerCycle
        );
    }

}
