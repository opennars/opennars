package nars.core;

import javolution.context.ConcurrentContext;
import nars.logic.BudgetFunctions.Activating;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import nars.logic.entity.Term;

import java.io.PrintStream;
import java.util.Deque;


/** Core implements a central reasoning component which references a set of Concepts and activates them during a memory cycle.*/
public interface Core extends Iterable<Concept> /* TODO: implements Plugin */ {


    void addTask(Task t);

    int size();



    public interface CoreAware {
        public void setCore(Core a);
    }


    /** called once during the main memory cycle; 
     *  responsible for executing Memory's:
     *      --newTasks (ImmediateProcess)
     *      --novelTasks (ImmediateProcess)
     *      --fired concepts (FireConcept)
     *      --otherTasks
     */
    public void cycle();




    /** Invoked during a memory reset to empty all concepts */
    public void reset();

    /** Maps Term to associated Concept. May also be called 'recognize'
     * as it can be used to determine if a symbolic pattern (term) is known */
    public Concept concept(Term term);

    /**
     * Creates and adds new concept to the memory.  May also be called 'cognize' because
     * it is like a request to process a symbolic pattern (term).
     * @return the new concept, or null if the memory is full
     */
    public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing);

    /** Activates a concept, adjusting its budget.  
     *  May be invoked by the concept processor or at certain points in the reasoning process.
     */
    public void activate(Concept c, BudgetValue b, Activating mode);

    //public void forget(Concept c);
    
    /**
     * Provides a "next" concept for sampling during logic.
     */
    public Concept nextConcept();

    public void init(Memory m);

    /** used by the bag to explicitly forget an item asynchronously */
    public void conceptRemoved(Concept c);
    
    public Memory getMemory();
    
    
    /** Generic utility method for running a list of tasks in current thread */
    public static void run(final Deque<Runnable> tasks) {
        run(tasks, tasks.size());
    }

    /** Generic utility method for running a list of tasks in current thread (concurrency == 1) or in multiple threads (> 1, in which case it will block until they finish) */
    public static void run(final Deque<Runnable> tasks, int maxTasksToRun) {

        final int concurrency = Math.min(Parameters.THREADS, maxTasksToRun);

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

}
