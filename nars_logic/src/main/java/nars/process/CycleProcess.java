package nars.process;

import javolution.context.ConcurrentContext;
import nars.Global;
import nars.Memory;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;

import java.util.Deque;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * a cyclically invoked process.
 * a central reasoning policy controller that defines processes performed
 * during and between each memory cycle.
 * may correspond to a region of activation or some other process which
 * is iteratively called a more or less continual basis
 * */
public interface CycleProcess<M> extends Iterable<Concept>, Consumer<Memory> { //CacheBag<Term,Concept>, Iterable<Concept> /* TODO: implements Plugin */ {


    /** accept the task, return whether it was accepted */
    boolean accept(Task t);

    /** number of concepts active in this controller */
    int size();


    void conceptPriorityHistogram(double[] bins);

    /** sample concepts for a specific operator type
     *
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

/*    default NAR forEachConcept(final Consumer<Concept> action) {
        forEachConcept(Integer.MAX_VALUE, action);
    }
*/

    default void forEachConcept(int max, Consumer<Concept> action) {
        Iterator<Concept> ii = iterator();
        int n = 0;
        while (ii.hasNext() && n < max) {
            action.accept(ii.next());
            n++;
        }
    }





    /** called once during the main memory cycle; 
     *  responsible for executing Memory's:
     *      --newTasks (DirectProcess)
     *      --novelTasks (DirectProcess)
     *      --fired concepts (FireConcept)
     *      --otherTasks
     */
    public void cycle();



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
    public Concept conceptualize(Termed term, Budget budget, boolean createIfMissing);

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


//    /** sums the priorities of items in different active areas of the memory */
//    public static double getActivePrioritySum(CycleProcess p, final boolean concept, final boolean tasklink, final boolean termlink) {
//        final double[] total = {0};
//        p.forEachConcept(c-> {
//            if (concept)
//                total[0] += c.getBudget().getPriority();
//            if (tasklink)
//                total[0] += c.getBudget().getTaskLinks().getPrioritySum();
//            if (termlink)
//                total[0] += c.getBudget().getTermLinks().getPrioritySum();
//        });
//        return total[0];
//    }

    default public double getActivePriorityPerConcept(final Memory memory, final boolean tasklink, final boolean termlink) {
        return 0;
//        int c = numConcepts(true, false);
//        if (c == 0) return 0;
//        return getActivePrioritySum(concept, tasklink, termlink)/c;
    }

}
