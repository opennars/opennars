/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core.bag;

import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.language.Term;
import nars.storage.CurveBag;
import nars.util.sort.FractalSortedItemList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author me
 */
public class CurveBagConceptTest {

    public static class NullConcept extends Concept {

        public NullConcept(String id, float priority) {
            super(new BudgetValue(priority, priority, priority), new Term(id), null, null, null);
        }    

        @Override
        public float getQuality() {
            return 0;
        }
        
        
    }
    
    @Test
    public void testConcept() {
        CurveBag b = new CurveBag(2, new CurveBag.FairPriorityProbabilityCurve(), true, new FractalSortedItemList());
        //different id, different priority
        b.putIn(new NullConcept("a", 0.1f));
        b.putIn(new NullConcept("b", 0.15f));
        assertEquals(2, b.size());
        b.clear();
        
        //same priority, different id
        b.putIn(new NullConcept("a", 0.1f));
        b.putIn(new NullConcept("b", 0.1f));
        assertEquals(2, b.size());
        
        b.putIn(new NullConcept("c", 0.2f));
        assertEquals(2, b.size());
        assertEquals(0.1f, b.getMinPriority(),0.001f);
        assertEquals(0.2f, b.getMaxPriority(),0.001f);
        b.clear();
        
        b.putIn(new NullConcept("b", 0.4f));
        System.out.println(b.nameTable);
        System.out.println(b.items);
        assertEquals(2, b.size());
        assertEquals(0.2f, b.getMinPriority(),0.001f);
        assertEquals(0.4f, b.getMaxPriority(),0.001f);
        
        
    }
}
