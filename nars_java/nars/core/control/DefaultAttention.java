package nars.core.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import nars.core.Attention;
import nars.core.Events;
import nars.core.Events.ConceptForget;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.inference.BudgetFunctions.Activating;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.Bag.MemoryAware;
import nars.storage.CacheBag;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class DefaultAttention implements Attention {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Concept,Term> concepts;
    public final CacheBag<Term, Concept> subcon;
    
    private final ConceptBuilder conceptBuilder;
    private Memory memory;
    
    private Cycle loop = new Cycle();
    final List<Runnable> run = new ArrayList();
       
    public class Cycle {
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

        public int inputTasksPriority() {
            return t(numThreads);
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
    
            
    public DefaultAttention(Bag<Concept,Term> concepts, CacheBag<Term,Concept> subcon, ConceptBuilder conceptBuilder) {
        this.concepts = concepts;
        this.subcon = subcon;
        this.conceptBuilder = conceptBuilder;        
        
    }

    @Override
    public void init(Memory m) {
        this.memory = m;
        if (concepts instanceof AttentionAware)
            ((AttentionAware)concepts).setAttention(this);
        if (concepts instanceof MemoryAware)
            ((MemoryAware)concepts).setMemory(m);
    }

    @Override
    public int getInputPriority() {
        return loop.inputTasksPriority();
    }
    
    
    protected FireConcept next() {       

        Concept currentConcept = concepts.takeNext();
        if (currentConcept==null)
            return null;
            
        return new FireConcept(memory, currentConcept, 1) {
            
            @Override public void onFinished() {
                float forgetCycles = memory.param.cycles(memory.param.conceptForgetDurations);

                concepts.putBack(currentConcept, forgetCycles, memory);
            }
        };
        
    }

    @Override
    public void cycle() {
        if (Parameters.THREADS == 1)
            cycleSequential();
        else
            cycleParallel();
    }

    
    
    public void cycleSequential() {

        run.clear();
        memory.processNewTasks(loop.newTasksPriority(), run);
        memory.run(run);
        
        run.clear();
        memory.processNovelTasks(loop.novelTasksPriority(), run);
        memory.run(run); 
        
        run.clear();        
        processConcepts(loop.conceptsPriority(), run);
        memory.run(run);
        
        run.clear();

    }

    public void cycleParallel() {

        run.clear();
        
        memory.processNewTasks(loop.newTasksPriority(), run);
        
        memory.processNovelTasks(loop.novelTasksPriority(), run);
        
        processConcepts(loop.conceptsPriority(), run);
                
        memory.run(run, Parameters.THREADS);
        
        run.clear();

    }    
    
    public void processConcepts(int c, Collection<Runnable> run) {
        if (c == 0) return;                
        
        for (int i = 0; i < c; i++) {
            FireConcept f = next();
            
            if (f!=null)
                run.add(f);                            
            else
                break;
        }
        
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
        return concepts.get(term);
    }

    @Override
    public void conceptRemoved(Concept c) {
        
        if (subcon!=null) {            
            subcon.add(c);
            //System.out.println("forget: " + c + "   con=" + concepts.size() + " subcon=" + subcon.size());
        }
        
        memory.emit(ConceptForget.class, c);
    }
    
    @Override
    public Concept conceptualize(BudgetValue budget, final Term term, boolean createIfMissing) {
        
        //see if concept is active
        Concept concept = concepts.take(term);
        
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
                memory.logic.CONCEPT_NEW.commit(term.getComplexity());
            memory.emit(Events.ConceptNew.class, concept);                
        }
        else if (concept!=null) {            
            
            //apply budget to existing concept
            //memory.logic.CONCEPT_ACTIVATE.commit(term.getComplexity());
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
        concepts.take(c.name());
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
        return concepts.peekNext();
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
