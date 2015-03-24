/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control.experimental;

import nars.Core;
import nars.Memory;
import nars.Memory.MemoryAware;
import nars.nal.BudgetFunctions;
import nars.energy.Budget;
import nars.nal.entity.Concept;
import nars.nal.entity.ConceptBuilder;
import nars.nal.entity.Term;
import nars.energy.bag.experimental.DelayBag;
import nars.energy.bag.experimental.FairDelayBag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Uses DelayBag to emulate a massively parallel spiking neural network of concept activation
 * 
 * Designed for use in parallel processing
 * 
 * Named "Wave" core because its concept-firing timing resembles spiking
 * brainwaves 
 */
abstract public class ConceptWaveCore implements Core {
    

    public DelayBag<Term, Concept> concepts;
    //public final CacheBag<Term, Concept> subcon;
    
    private final ConceptBuilder conceptBuilder;
    Memory memory;
    List<Runnable> run = new ArrayList();

    private final int maxConcepts;
               
    public ConceptWaveCore(int maxConcepts, ConceptBuilder conceptBuilder) {
        this.maxConcepts = maxConcepts;
        this.conceptBuilder = conceptBuilder;        
        //this.subcon = subcon
    }    

    @Override
    abstract public void cycle();


    @Override
    public void reset() {
        concepts.clear();
    }

    @Override
    public Concept concept(Term term) {
        return concepts.get(term);
    }

    @Override
    public int size() {
        return concepts.size();
    }

    @Override
    public Concept conceptualize(Budget budget, Term term, boolean createIfMissing) {
        Concept c = concept(term);
        if (c!=null) {
            //existing
            BudgetFunctions.activate(c.budget, budget, BudgetFunctions.Activating.Max);
        }
        else {
            if (createIfMissing)
                c = conceptBuilder.newConcept(budget, term, memory);
            if (c == null)
                return null;
            concepts.put(c);
        }
        return c;
    }

    @Override
    public void activate(Concept c, Budget b, BudgetFunctions.Activating mode) {
        conceptualize(b, c.term, false);
    }

    @Override
    public Concept nextConcept() {
        return concepts.peekNext();
    }

    @Override
    public void init(Memory m) {
        this.memory = m;
        
        this.concepts = new FairDelayBag(memory.param.conceptForgetDurations, maxConcepts);      
        
        if (concepts instanceof MemoryAware)
            concepts.setMemory(m);
        if (concepts instanceof CoreAware)
            concepts.setCore(this);
    }

    @Override
    public void conceptRemoved(Concept c) {
    
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }


    @Override
    public String toString() {
        return super.toString() + "[" + concepts.toString() + "]";
    }

    @Override
    public Memory getMemory() {
        return memory;
    }
    
}
