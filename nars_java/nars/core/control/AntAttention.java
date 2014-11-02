/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nars.core.Attention;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.Bag.MemoryAware;
import nars.storage.DelayBag;

/**
 *
 * @author me
 */
public class AntAttention implements Attention {

    public final Bag<Concept,Term> concepts;
    //public final CacheBag<Term, Concept> subcon;
    
    private final ConceptBuilder conceptBuilder;
    private Memory memory;
    final List<Runnable> run = new ArrayList();
    
    int inputPriority = 2;
    int newTaskPriority = 2;
    int novelTaskPriority = 2;
    int conceptPriority = 2;
               
    public AntAttention(int maxConcepts, ConceptBuilder conceptBuilder) {
        this.concepts = new DelayBag<>(maxConcepts);        
        this.conceptBuilder = conceptBuilder;        
        //this.subcon = subcon;        
    }    

    @Override
    public void cycle() {

        run.clear();
        
        memory.processNewTasks(newTaskPriority, run);
                
        memory.processNovelTasks(novelTaskPriority, run);
        
        for (int i = 0; i < conceptPriority; i++) {
            Concept c = concepts.takeNext();
            if (c == null)
                break;
            run.add(new FireConcept(memory, c, 1) {
                @Override public void onFinished() {
                    //putIn, not putBack; DelayBag has its own forget function
                    concepts.putIn(currentConcept);
                }
            });
        }

        memory.run(run, Parameters.THREADS);
        
        /*if (!run.isEmpty())
            System.out.println("run: "+ run.size() + " " + run + " " + concepts.size());*/
        
        
    }
    


    @Override
    public void reset() {
        concepts.clear();
    }

    @Override
    public Concept concept(Term term) {
        return concepts.get(term);
    }

    @Override
    public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing) {
        Concept c = concept(term);
        if (c!=null) {
            //existing
            BudgetFunctions.activate(c.budget, budget, BudgetFunctions.Activating.Max);
        }
        else {
            if (createIfMissing)
                c = conceptBuilder.newConcept(budget, term, memory);
            concepts.putIn(c);
        }
        return c;
    }

    @Override
    public void activate(Concept c, BudgetValue b, BudgetFunctions.Activating mode) {
        conceptualize(b, c.term, false);
    }

    @Override
    public Concept sampleNextConcept() {
        return concepts.takeNext();
    }

    @Override
    public void init(Memory m) {
        this.memory = m;
        ((MemoryAware)concepts).setMemory(m);
        ((AttentionAware)concepts).setAttention(this);
    }

    @Override
    public void conceptRemoved(Concept c) {
    
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    @Override
    public int getInputPriority() {
        return inputPriority;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + concepts.toString() + "]";
    }
    
    
}
