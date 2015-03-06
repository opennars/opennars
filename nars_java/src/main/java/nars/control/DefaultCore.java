package nars.control;

import nars.core.Core;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.reason.ConceptFire;
import nars.logic.reason.ImmediateProcess;
import nars.util.bag.Bag;
import nars.util.bag.impl.CacheBag;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultCore extends UniCore {

    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final Bag<Sentence<CompoundTerm>, Task<CompoundTerm>> novelTasks;

    /* ---------- Short-term workspace for a single cycle ------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final Deque<Task> newTasks;


    public DefaultCore(Bag<Term, Concept> concepts, CacheBag<Term,Concept> subcon, ConceptBuilder conceptBuilder, Bag<Sentence<CompoundTerm>, Task<CompoundTerm>> novelTasks) {
        super(concepts, subcon, conceptBuilder);


        this.novelTasks = novelTasks;
        if (novelTasks instanceof CoreAware) {
            ((CoreAware) novelTasks).setCore(this);
        }

        this.newTasks = (Parameters.THREADS > 1)
                ? new ConcurrentLinkedDeque<>() : new ArrayDeque<>();


    }



    @Override
    public void addTask(Task t) {

        newTasks.add(t);
    }


    /**
     * An atomic working cycle of the system: process new Tasks, then fire a
     * concept
     **/
    @Override
    public void cycle() {

        memory.nextPercept(memory.param.inputsMaxPerCycle.get());

        //all new tasks
        int numNewTasks = newTasks.size();
        for (int i = 0; i < numNewTasks; i++) {
            Runnable r = nextNewTask();
            if (r != null) {
                r.run();
            }
            else
                break;
        }

        //all novel tasks
        /*if (memory.newTasks.isEmpty())*/ {
            int numNovelTasks = novelTasks.size();
            for (int i = 0; i < numNovelTasks; i++) {
                Runnable novel = nextNovelTask();
                if (novel != null) novel.run();
                else
                    break;
            }

            //1 noveltask if no newtasks
            //Runnable novel = memory.nextNovelTask();
            //if (novel != null) novel.run();
        }

        //1 concept
        /*if (memory.newTasks.isEmpty())*/
        int conceptsToFire = memory.param.conceptsFiredPerCycle.get();

        for (int i = 0; i < conceptsToFire; i++) {
            ConceptFire f = nextTaskLink(nextConcept());
            if (f != null) {
                f.run();
            }
        }



        concepts.peekNextForget(
                memory.param.conceptForgetDurations,
                Parameters.CONCEPT_FORGETTING_ACCURACY,
                memory);

        memory.dequeueOtherTasks(run);
        Core.run(run);
        run.clear();

    }

    private ConceptFire nextTaskLink(Concept concept) {
        if (concept == null) return null;


        TaskLink taskLink = concept.taskLinks.peekNextForget(memory.param.taskLinkForgetDurations, memory);
        if (taskLink!=null)
            return newFireConcept(concept, taskLink);
        else {
            return null;
        }

    }

    protected Runnable nextNewTask() {
        if (newTasks.isEmpty()) return null;

        Task task = newTasks.removeFirst();

        memory.emotion.adjustBusy(task.getPriority(), task.getDurability());

        if (task.isInput() || task.sentence.isQuest() || task.sentence.isGoal()
                || task.sentence.isQuestion()
                || (concept(task.sentence.term) != null)
                ) {
            //it is a question/quest or a judgment for a concept which exists:

            return new ImmediateProcess(memory, task);

        } else {
            //it is a judgment or goal which would create a new concept:


            final Sentence s = task.sentence;

            //if (s.isJudgment() || s.isGoal()) {

            final double exp = s.truth.getExpectation();
            if (exp > Parameters.DEFAULT_CREATION_EXPECTATION) {

                // new concept formation
                Task overflow = novelTasks.put(task);
                memory.logic.TASK_ADD_NOVEL.hit();

                if (overflow != null) {
                    if (overflow == task) {
                        memory.removeTask(task, "Ignored");
                    } else {
                        memory.removeTask(overflow, "Displaced novel task");
                    }
                }

            } else {
                memory.removeTask(task, "Neglected");
            }
            //}
        }
        return null;
    }

    /**
     * Select a novel task to process.
     */
    protected Runnable nextNovelTask() {
        if (novelTasks.isEmpty()) return null;

        final Task task = novelTasks.TAKENEXT();

        return new ImmediateProcess(memory, task);
    }


    @Override
    public void reset() {
        super.reset();
        novelTasks.clear();
        newTasks.clear();
    }


}
      /*
    //private final Cycle loop = new Cycle();
    class Cycle {
        public final AtomicInteger threads = new AtomicInteger();
        private final int numThreads;

        public Cycle() {
            this(Parameters.THREADS);
        }

        public Cycle(int threads) {
            this.numThreads = threads;
            this.threads.set(threads);

        }

        int t(int threads) {
            if (threads == 1) return 1;
            else {
                return threads;
            }
        }




        public int newTasksPriority() {
            return memory.newTasks.size();
        }

        public int novelTasksPriority() {
            if (memory.getNewTasks().isEmpty()) {
                return t(numThreads);
            } else {
                return 0;
            }
        }

        public int conceptsPriority() {
            if (memory.getNewTasks().isEmpty()) {
                return memory.param.conceptsFiredPerCycle.get();
            } else {
                return 0;
            }
        }


    }

    */
