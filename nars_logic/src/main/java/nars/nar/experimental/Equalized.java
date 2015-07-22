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
import nars.task.TaskAccumulator;
import nars.task.TaskComparator;
import nars.term.Term;

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

        public EqualizedCycle(TaskAccumulator taskAccumulator, Bag<Term, Concept> concepts, AtomicInteger conceptsFiredPerCycle) {
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

            float inputPriorityFactor = 1f/(conceptsToFire);

            //new tasks
            int newTasksToFire = Math.min(newTasks.size(), conceptsFiredPerCycle.get());
            for (int n = newTasksToFire;  n > 0; n--) {
                Task highest = newTasks.removeHighest();
                if (highest == null) break;

                TaskProcess tp = TaskProcess.get(memory, highest, inputPriorityFactor);
                if (tp!=null)
                    tp.run();
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
                });

                //System.out.print("discarded=" + removed + "  ");
            }

            //System.out.println();


        }



    }

    @Override
    public CycleProcess newCycleProcess() {
        return new EqualizedCycle(
                new TaskAccumulator(TaskComparator.Merging.Or),
                newConceptBag(),
                conceptsFiredPerCycle
        );
    }

}
