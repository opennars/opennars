package nars.control;

import nars.Global;
import nars.nal.*;
import nars.budget.Bag;
import nars.budget.bag.CacheBag;
import nars.nal.concept.Concept;
import nars.nal.tlink.TaskLink;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultCore extends SequentialCore {

    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final Bag<Sentence<Compound>, Task<Compound>> novelTasks;

    /* ---------- Short-term workspace for a single cycle ------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final Deque<Task> newTasks;




    public DefaultCore(Bag<Term, Concept> concepts, CacheBag<Term,Concept> subcon, Bag<Sentence<Compound>, Task<Compound>> novelTasks) {
        super(concepts, subcon);


        this.novelTasks = novelTasks;
        if (novelTasks instanceof CoreAware) {
            ((CoreAware) novelTasks).setCore(this);
        }

        this.newTasks = (Global.THREADS > 1)
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

        //inputs
        memory.perceiveNext(memory.param.inputsMaxPerCycle.get());

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

        //1 novel tasks if numNewTasks empty
        if (newTasks.isEmpty()) {
            int numNovelTasks = 1;
            for (int i = 0; i < numNovelTasks; i++) {
                Runnable novel = nextNovelTask();
                if (novel != null) novel.run();
                else
                    break;
            }
        }


        //1 concept if (memory.newTasks.isEmpty())*/
        int conceptsToFire = newTasks.isEmpty() ? memory.param.conceptsFiredPerCycle.get() : 0;
        for (int i = 0; i < conceptsToFire; i++) {
            ConceptProcess f = nextTaskLink(nextConceptToProcess());
            if (f != null) {
                f.run();
            }
        }



        concepts.forgetNext(
                memory.param.conceptForgetDurations,
                Global.CONCEPT_FORGETTING_ACCURACY,
                memory);

        memory.runNextTasks();
        run.clear();

    }

    private ConceptProcess nextTaskLink(Concept concept) {
        if (concept == null) return null;


        TaskLink taskLink = concept.taskLinks.forgetNext(memory.param.taskLinkForgetDurations, memory);
        if (taskLink!=null)
            return newConceptProcess(concept, taskLink);
        else {
            return null;
        }

    }

    protected Runnable nextNewTask() {
        if (newTasks.isEmpty()) return null;

        final Task task = newTasks.removeFirst();
        final Sentence sentence = task.sentence;

        memory.emotion.adjustBusy(task.getPriority(), task.getDurability());

        if (task.isInput() || sentence.isQuest() || sentence.isGoal()
                || sentence.isQuestion()
                || (concept(sentence.term) != null)
                ) {
            //it is a question/quest or a judgment for a concept which exists:

            return new DirectProcess(memory, task);

        } else {
            //it is a judgment or goal which would create a new concept:


            final Sentence s = sentence;

            //if (s.isJudgment() || s.isGoal()) {

            final double exp = s.truth.getExpectation();
            if (exp > Global.DEFAULT_CREATION_EXPECTATION) {

                // new concept formation
                Task overflow = novelTasks.put(task);
                memory.logic.TASK_ADD_NOVEL.hit();

                if (overflow != null) {
                    if (overflow == task) {
                        memory.taskRemoved(task, "Ignored");
                    } else {
                        memory.taskRemoved(overflow, "Displaced novel task");
                    }
                }

            } else {
                memory.taskRemoved(task, "Neglected");
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

        final Task task = novelTasks.pop();

        return new DirectProcess(memory, task);
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
