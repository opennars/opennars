/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import java.util.Collection;
import java.util.Iterator;
import nars.core.Attention;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.inference.BudgetFunctions;
import nars.language.Term;
import nars.storage.Bag;
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
       
            
    public AntAttention(ConceptBuilder conceptBuilder) {
        this.concepts = new DelayBag<>(1000);        
        this.conceptBuilder = conceptBuilder;        
        //this.subcon = subcon;        
    }    

    @Override
    public void cycle() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    @Override
    public FireConcept next() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Concept concept(Term term) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Concept conceptualize(BudgetValue budget, Term term, boolean createIfMissing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void activate(Concept c, BudgetValue b, BudgetFunctions.Activating mode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Concept sampleNextConcept() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(Memory m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void conceptRemoved(Concept c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Concept> iterator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getInputPriority() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
