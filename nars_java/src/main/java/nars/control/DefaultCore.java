package nars.control;

import nars.core.Core;
import nars.core.Events;
import nars.core.Events.ConceptForget;
import nars.core.Memory;
import nars.logic.BudgetFunctions;
import nars.logic.BudgetFunctions.Activating;
import nars.logic.FireConcept;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Concept;
import nars.logic.entity.ConceptBuilder;
import nars.logic.entity.Term;
import nars.util.bag.Bag;
import nars.util.bag.Bag.MemoryAware;
import nars.util.bag.CacheBag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultCore implements Core {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Concept,Term> concepts;
    public final CacheBag<Term, Concept> subcon;
    
    private final ConceptBuilder conceptBuilder;
    private Memory memory;
    
    List<Runnable> run = new ArrayList();
    

    public DefaultCore(Bag<Concept,Term> concepts, CacheBag<Term,Concept> subcon, ConceptBuilder conceptBuilder) {
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

        private final Bag<Concept, Term> bag;

        public DefaultFireConcept(Memory mem, Bag<Concept,Term> bag, Concept concept, int numTaskLinks) {
            super(mem, concept, numTaskLinks);
            this.bag = bag;
        }
        @Override
        public void onFinished() {
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
    
    @Override
    public Concept conceptualize(BudgetValue budget, final Term term, boolean createIfMissing) {
        
        //see if concept is active
        Concept concept = concepts.TAKE(term);
        
        //try remembering from subconscious
        if ((concept == null) && (subcon!=null)) {
            concept = subcon.take(term);
            if (concept!=null) {                
                
                //reset the forgetting period to zero so that its time while forgotten will not continue to penalize it during next forgetting iteration
                concept.budget.setLastForgetTime(memory.time());
                
                memory.emit(Events.ConceptRemember.class, concept);                

                //System.out.println("retrieved: " + concept + "  subcon=" + subcon.size());
            }
        }               
        
        
        if ((concept == null) && (createIfMissing)) {                            
            //create new concept, with the applied budget
            
            concept = conceptBuilder.newConcept(budget, term, memory);

            if (memory.logic!=null)
                memory.logic.CONCEPT_NEW.hit();
            memory.emit(Events.ConceptNew.class, concept);                
        }
        else if (concept!=null) {            
            
            //apply budget to existing concept
            //memory.logic.CONCEPT_ACTIVATE.commit(term.getComplexity());
            if (budget!=null)
                BudgetFunctions.activate(concept.budget, budget, Activating.TaskLink);
        }
        else {
            //unable to create, ex: has variables
            return null;
            //throw new RuntimeException("Unable to conceptualize " + term);
        }

        
        Concept displaced = concepts.putBack(concept, memory.param.cycles(memory.param.conceptForgetDurations), memory);
                
        if (displaced == null) {
            //added without replacing anything
            
            //but we need to get the actual stored concept in case it was merged
            return concept;
        }        
        else if (displaced == concept) {
            //not able to insert
            //System.out.println("can not insert: " + concept);   
            
            conceptRemoved(displaced);
            return null;
        }        
        else {
            //replaced something else
            //System.out.println("replace: " + removed + " -> " + concept);            

            conceptRemoved(displaced);
            return concept;
        }

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
