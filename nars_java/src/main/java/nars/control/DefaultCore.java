package nars.control;

import com.nurkiewicz.typeof.TypeOf;
import nars.core.Core;
import nars.core.Events;
import nars.core.Events.ConceptForget;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.BudgetFunctions;
import nars.logic.BudgetFunctions.Activating;
import nars.logic.FireConcept;
import nars.logic.entity.*;
import nars.util.bag.*;
import nars.util.bag.Bag.MemoryAware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultCore implements Core {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;
    public final CacheBag<Term, Concept> subcon;
    
    private final ConceptBuilder conceptBuilder;
    private Memory memory;
    
    List<Runnable> run = new ArrayList();
    

    public DefaultCore(Bag<Term, Concept> concepts, CacheBag<Term,Concept> subcon, ConceptBuilder conceptBuilder) {
        this.concepts = concepts;
        this.subcon = subcon;
        this.conceptBuilder = conceptBuilder;        
        
    }

    /** for removing a specific concept (if it's not putBack) */
    public Concept takeOut(Term t) {
        return concepts.TAKE(t);
    }
            
    @Override
    public void init(Memory m) {
        this.memory = m;
        if (concepts instanceof AttentionAware)
            ((AttentionAware)concepts).setAttention(this);
        if (concepts instanceof MemoryAware)
            ((MemoryAware)concepts).setMemory(m);
    }

    private static class DefaultFireConcept extends FireConcept {

        private final Bag<Term, Concept> bag;

        public DefaultFireConcept(Memory mem, Bag<Term, Concept> bag, Concept concept, int numTaskLinks) {
            super(mem, concept, numTaskLinks);
            this.bag = bag;
        }
        @Override
        public void beforeFinish() {
            float forgetCycles = memory.param.cycles(memory.param.conceptForgetDurations);

            bag.putBack(currentConcept, forgetCycles, memory);
        }
    }
    
    protected FireConcept nextConcept() {
        Concept currentConcept = concepts.TAKENEXT();
        if (currentConcept==null)
            return null;
            
        return new DefaultFireConcept(memory, concepts, currentConcept, 1);
    }

    /**
     * An atomic working cycle of the system: process new Tasks, then fire a
     * concept
     **/
    @Override
    public void cycle() {

        //1 input per cycle
        memory.nextPercept(1);

        //all new tasks
        int numNewTasks = memory.newTasks.size();
        for (int i = 0; i < numNewTasks; i++) {
            Runnable r = memory.nextNewTask();
            if (r != null) {
                r.run();
            }
            else break;
        }

        //all novel tasks
        /*if (memory.newTasks.isEmpty())*/ {
            int numNovelTasks = memory.novelTasks.size();
            for (int i = 0; i < numNovelTasks; i++) {
                Runnable novel = memory.nextNovelTask();
                if (novel != null) novel.run();
                else break;
            }

            //1 noveltask if no newtasks
            //Runnable novel = memory.nextNovelTask();
            //if (novel != null) novel.run();
        }

        //1 concept
        /*if (memory.newTasks.isEmpty())*/ {
            FireConcept f = nextConcept();
            if (f != null) {
                f.run();
            }

        }

        memory.dequeueOtherTasks(run);
        Core.run(run);
        run.clear();

    }



    
    public Iterable<Concept> getConcepts() {
         return concepts.values();
    }

    @Override
    public void reset() {
        concepts.clear();
    }

    @Override
    public Concept concept(final Term term) {
        return concepts.GET(term);
    }

    @Override
    public void conceptRemoved(Concept c) {
        
        if (subcon!=null) {            
            subcon.add(c);
            //System.out.println("forget: " + c + "   con=" + concepts.size() + " subcon=" + subcon.size());
        }
        else {
            memory.emit(ConceptForget.class, c);
            
        
            //explicitly destroy all concept data structures to free memory for GC
            //c.end();
        }
        
        
        
        
    }

    public class ConceptActivator extends BagActivator<Term,Concept> {

        final float relativeThreshold = Parameters.FORGET_QUALITY_RELATIVE;

        private boolean createIfMissing;
        private long now;

        public Concept updateItem(Concept c) {

            if (budget!=null) {
                BudgetValue cb = c.budget;

                BudgetFunctions.activate(cb, getBudget(), Activating.TaskLink);
            }

            long cyclesSinceLastForgotten = now - c.budget.getLastForgetTime();
            memory.forget(c, cyclesSinceLastForgotten, relativeThreshold);

            return c;
        }

        public ConceptActivator set(Term t, BudgetValue b, boolean createIfMissing, long now) {
            setKey(t);
            setBudget(b);
            this.createIfMissing = createIfMissing;
            this.now = now;
            return this;
        }

        @Override
        public Concept newItem() {

            //try remembering from subconscious
            if (subcon!=null) {
                Concept concept = subcon.take(getKey());
                if (concept!=null) {

                    //reset the forgetting period to zero so that its time while forgotten will not continue to penalize it during next forgetting iteration
                    concept.budget.setLastForgetTime(now);

                    memory.emit(Events.ConceptRemember.class, concept);

                    return concept;
                }
            }

            //create new concept, with the applied budget
            if (createIfMissing) {

                Concept concept = conceptBuilder.newConcept(budget, getKey(), memory);

                if (memory.logic!=null)
                    memory.logic.CONCEPT_NEW.hit();

                memory.emit(Events.ConceptNew.class, concept);

                return concept;
            }

            return null;
        }

        @Override
        public void overflow(Concept overflow) {
            conceptRemoved(overflow);
        }
    };

    final ConceptActivator activator = new ConceptActivator();


    @Override
    public Concept conceptualize(BudgetValue budget, final Term term, boolean createIfMissing) {

        return concepts.UPDATE( activator.set(term, budget, createIfMissing, memory.time()) );

    }
    
    
    @Override public void activate(final Concept c, final BudgetValue b, Activating mode) {
        concepts.TAKE(c.name());
        BudgetFunctions.activate(c.budget, b, mode);
        concepts.putBack(c, memory.param.cycles(memory.param.conceptForgetDurations), memory);
    }
    
//    @Override
//    public void forget(Concept c) {
//        concepts.take(c.name());        
//        concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);    
//    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.PEEKNEXT();
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    @Override
    public Memory getMemory() {
        return memory;
    }


    @Override
    public void forEach(Consumer<? super Concept> action) {
        //use experimental consumer for levelbag to avoid allocating so many iterators within iterators
        TypeOf.whenTypeOf(concepts).is(LevelBag.class).then(l -> {
            l.forEach(action);
        });

        //use default iterator
        //iterator().forEachRemaining(action);
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
