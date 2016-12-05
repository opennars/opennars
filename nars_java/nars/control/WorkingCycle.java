package nars.control;

import java.util.Iterator;
import nars.util.Events;
import nars.util.Events.ConceptForget;
import nars.storage.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.inference.BudgetFunctions;
import nars.inference.BudgetFunctions.Activating;
import nars.language.CompoundTerm;
import nars.language.Interval;
import nars.language.Term;
import nars.storage.Bag;
import nars.config.Plugins;

/**
 * The original deterministic memory cycle implementation that is currently used as a standard
 * for development and testing.
 */
public class WorkingCycle implements Iterable<Concept> {


    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Concept,Term> concepts;
    
    private Memory memory;
         
    public WorkingCycle(Bag<Concept,Term> concepts) {
        this.concepts = concepts;   
    }

    /** for removing a specific concept (if it's not putBack) */
    public Concept takeOut(Term t) {
        return concepts.take(t);
    }
            
    public void init(Memory m) {
        this.memory = m;
    }
    
    protected FireConcept next() {       

        Concept currentConcept = concepts.takeNext();
        if (currentConcept==null)
            return null;
            
        return new FireConcept(memory, currentConcept, 1) {
            
            @Override public void onFinished() {
                float forgetCycles = memory.cycles(memory.param.conceptForgetDurations);

                concepts.putBack(currentConcept, forgetCycles, memory);
            }
        };
        
    }

    public void cycle() {
        cycleSequential();
    }

    public boolean noResult() {
        return memory.newTasks.isEmpty();
    }
    
    public void cycleSequential() {
        
        memory.processNewTasks();
        if(noResult())
            memory.processNovelTask();
        if(noResult())
            processConcept();
    }
    
    public void processConcept() {
        FireConcept f = next();
            if (f!=null)
                f.run();  
        
    }

    public void reset() {
        concepts.clear();
    }

    public Concept concept(Term term) {
        term = CompoundTerm.cloneDeepReplaceIntervals(term);
        return concepts.get(term);
    }

    public void conceptRemoved(Concept c) {
        memory.emit(ConceptForget.class, c);
    }
    
    public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing) {
        
        if(term instanceof Interval) {
            return null;
        }
        
        term = CompoundTerm.cloneDeepReplaceIntervals(term);
        
        //see if concept is active
        Concept concept = concepts.take(term);
        
        if ((concept == null) && (createIfMissing)) {                            
            //create new concept, with the applied budget
            
            concept = new Concept(budget, term, memory);

            //if (memory.logic!=null)
            //    memory.logic.CONCEPT_NEW.commit(term.getComplexity());
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

        
        Concept displaced = concepts.putBack(concept, memory.cycles(memory.param.conceptForgetDurations), memory);
                
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
    
    
    public void activate(final Concept c, final BudgetValue b, Activating mode) {
        concepts.take(c.name());
        BudgetFunctions.activate(c.budget, b, mode);
        concepts.putBack(c, memory.cycles(memory.param.conceptForgetDurations), memory);
    }

    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    public Memory getMemory() {
        return memory;
    }
    
}
