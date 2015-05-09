package nars.model;

import javolution.context.ConcurrentContext;
import nars.Global;
import nars.Memory;
import nars.budget.BudgetFunctions.Activating;
import nars.budget.Budget;
import nars.nal.NALOperator;
import nars.nal.concept.Concept;
import nars.nal.Task;
import nars.nal.term.Term;

import java.util.Deque;


/** Core implements a central reasoning component which references a set of Concepts and activates them during a memory cycle.*/
public interface ControlCycle extends Iterable<Concept> /* TODO: implements Plugin */ {


    void addTask(Task t);

    int size();

    public double conceptMass();

    default public void conceptPriorityHistogram(double[] bins) {

    }

    /** sample concepts for a specific operator type
     *
     * @param implication
     * @param v percentage of bag size # of attempts to search before returning null
     */
    default Concept nextConcept(NALOperator op, float v) {
        int attempts = (int) Math.ceil(size() * v);
        for (int i = 0; i < attempts; i++) {
            Concept c = nextConcept();
            if (c.getTerm().operator() == op)
                return c;
        }
        return null;
    }


    public interface CoreAware {
        public void setCore(ControlCycle a);
    }


    /** called once during the main memory cycle; 
     *  responsible for executing Memory's:
     *      --newTasks (DirectProcess)
     *      --novelTasks (DirectProcess)
     *      --fired concepts (FireConcept)
     *      --otherTasks
     */
    public void cycle();


    /** Invoked during a memory reset to empty all concepts
     *  @param delete  whether to finalize everything (deallocate as much as possible)
     * */
    public void reset(boolean delete);

    /** Maps Term to associated Concept. May also be called 'recognize'
     * as it can be used to determine if a symbolic pattern (term) is known */
    public Concept concept(Term term);

    /**
     * Creates and adds new concept to the memory.  May also be called 'cognize' because
     * it is like a request to process a symbolic pattern (term).
     * @return the new concept, or null if the memory is full
     */
    public Concept conceptualize(Budget budget, Term term, boolean createIfMissing);

    /** Activates a concept, adjusting its budget.  
     *  May be invoked by the concept processor or at certain points in the reasoning process.
     */
    public void activate(Concept c, Budget b, Activating mode);

    //public void forget(Concept c);
    
    /**
     * Provides a "next" concept for sampling during logic.
     */
    public Concept nextConcept();

    public void init(Memory m);

    /** used by the bag to explicitly forget an item asynchronously
     *  returns true if the concept was completely deleted,
     *  false if it was forgotten (in subconcepts)
     * */
    public boolean conceptRemoved(Concept c);
    
    public Memory getMemory();
    
    
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

}
