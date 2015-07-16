package nars.process;

import javolution.context.ConcurrentContext;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.io.Perception;
import nars.io.in.Input;
import nars.task.Task;
import nars.term.Term;

import java.util.Deque;
import java.util.function.Predicate;


/** a central reasoning policy that defines processes performed during and between each memory cycle.*/
public interface CycleProcess extends Iterable<Concept> /* TODO: implements Plugin */ {


    boolean addTask(Task t);

    int size();



    void conceptPriorityHistogram(double[] bins);

    /** sample concepts for a specific operator type
     *
     * @param implication
     * @param v percentage of bag size # of attempts to search before returning null
     */
    default Concept nextConcept(Predicate<Concept> pred, float v) {
        int attempts = (int) Math.ceil(size() * v);
        for (int i = 0; i < attempts; i++) {
            Concept c = nextConcept();
            if (pred.test(c)) return c;
        }
        return null;
    }


    public Memory getMemory();



    /** called once during the main memory cycle; 
     *  responsible for executing Memory's:
     *      --newTasks (DirectProcess)
     *      --novelTasks (DirectProcess)
     *      --fired concepts (FireConcept)
     *      --otherTasks
     */
    public void cycle();


    /** Invoked during a memory reset to empty all concepts
     * @param delete  whether to finalize everything (deallocate as much as possible)
     * @param perception */
    public void reset(Memory memory, Perception perception);

    /** Maps Term to a Concept active in this Cycle. May also be called 'recognize'
     * as it can be used to determine if a symbolic pattern (term) is known and active.
     *
     * Note this does not retrieve Concepts that are not active - for that, use Memory.concept(term)
     * */
    public Concept concept(Term term);

    /**
     * Creates and adds new concept to the memory.  May also be called 'cognize' because
     * it is like a request to process a symbolic pattern (term).
     * @return the new concept, or null if the memory is full
     *
     */
    public Concept conceptualize(Term term, Budget budget, boolean createIfMissing);

    /**
     * Provides a "next" concept for sampling during logic.
     */
    public Concept nextConcept();


    /** set the priority of a concept. returns false if the concept is no longer active after the change */
    public boolean reprioritize(Term term, float newPriority);
    
    /** Generic utility method for running a list of tasks in current thread */
    public static void run(final Deque<Runnable> tasks) {
        run(tasks, tasks.size());
    }

    /** Generic utility method for running a list of tasks in current thread (concurrency == 1) or in multiple threads (> 1, in which case it will block until they finish) */
    public static void run(final Deque<Runnable> tasks, int maxTasksToRun) {

        final int concurrency = Math.min(Global.THREADS, maxTasksToRun);

            ConcurrentContext ctx = null;
            if (concurrency > 1)  {
                //execute in parallel, multithreaded
                ctx = ConcurrentContext.enter();

                ctx.setConcurrency(concurrency);
            }

            try {
                while (!tasks.isEmpty() && maxTasksToRun-- > 0) {
                    Runnable tt = tasks.removeFirst();
                    if (ctx!=null)
                        ctx.execute(tt);
                    else
                        tt.run();
                }

            } finally {
                // Waits for all concurrent executions to complete.
                // Re-exports any exception raised during concurrent executions.
                if (ctx!=null)
                    ctx.exit();
            }

    }


    Concept remove(Concept c);

    default public void delete() {

    }

    void perceive(Input ii);

}
