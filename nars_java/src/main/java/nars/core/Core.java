package nars.core;

import javolution.context.ConcurrentContext;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.inference.BudgetFunctions.Activating;
import nars.language.Term;

import java.util.List;


/** Core implements a central reasoning component which references a set of Concepts and activates them during a memory cycle.*/
public interface Core extends Iterable<Concept> /* TODO: implements Plugin */ {


    public interface AttentionAware {
        public void setAttention(Core a);
    }


    /** called once during the main memory cycle; 
     *  responsible for executing Memory's:
     *      --newTasks (ImmediateProcess)
     *      --novelTasks (ImmediateProcess)
     *      --fired concepts (FireConcept)
     *      --otherTasks
     */
    public void cycle();

    /** how many input tasks to process per cycle.  this allows Attention to regulate
     *  input relative to other kinds of mental activity
     * @return 
     */
    public int getInputPriority();


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
     * Provides a "next" concept for sampling during inference. 
     */
    public Concept sampleNextConcept();

    public void init(Memory m);

    /** used by the bag to explicitly forget an item asynchronously */
    public void conceptRemoved(Concept c);
    
    public Memory getMemory();
    
    
    /** Generic utility method for running a list of tasks in current thread */
    public static void run(final List<Runnable> tasks) {
        run(tasks, 1);
    }

    /** Generic utility method for running a list of tasks in current thread (concurrency == 1) or in multiple threads (> 1, in which case it will block until they finish) */
    public static void run(final List<Runnable> tasks, int concurrency) {

        if ((tasks == null) || (tasks.isEmpty())) {
            return;
        } else if (tasks.size() == 1) {
            tasks.get(0).run();
        } else if (concurrency == 1) {
            //single threaded
            for (final Runnable t : tasks) {
                t.run();
            }
        } else {
            
            //execute in parallel, multithreaded
            final ConcurrentContext ctx = ConcurrentContext.enter();

            ctx.setConcurrency(concurrency);
            try {
                for (final Runnable r : tasks) {
                    ctx.execute(r);
                }
            } finally {
                // Waits for all concurrent executions to complete.
                // Re-exports any exception raised during concurrent executions. 
                ctx.exit();
            }
        }
    }
    
}
