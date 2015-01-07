/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.bag;

import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.CurveBag;
import nars.storage.GearBag;
import nars.storage.LevelBag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class BagOperationsTest {

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
        //testBagSequence(new CurveBag(2, new CurveBag.FairPriorityProbabilityCurve(), true, new FractalSortedItemList()));
        //testBagSequence(new LevelBag(2, 2));
        testBagSequence(new GearBag(2,2));        
    }
    
    public static void testBagSequence(Bag b) {
        
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
        
        //if (b instanceof GearBag()) return;
        
        
        b.putIn(new NullConcept("b", 0.4f));
        
        
        assertEquals(2, b.size());
        assertEquals(0.2f, b.getMinPriority(),0.001f);
        assertEquals(0.4f, b.getMaxPriority(),0.001f);
        
        
        Item tb = b.take(new Term("b"));
        assertTrue(tb!=null);
        assertEquals(1, b.size());
        assertEquals(0.4f, tb.getPriority(), 0.001f);
        
        Item tc = b.takeNext();
        assertEquals(0, b.size());
        assertEquals(0.2f, tc.getPriority(), 0.001f);
        
        
        
        assertEquals(null, b.putIn(new NullConcept("a", 0.2f)));
        b.putIn(new NullConcept("b", 0.3f));
        //assertEquals(null, );
        
        if (b instanceof LevelBag) {
            assertEquals("a", b.putIn(new NullConcept("c", 0.1f)).name().toString()); //replaces item on level
        }
        else if (b instanceof CurveBag) {
            assertEquals("c", b.putIn(new NullConcept("c", 0.1f)).name().toString()); //could not insert, so got the object returned as result
            assertEquals(2, b.size());
        
            //same id, different priority (lower, so ignored)
            assertEquals(null, b.putIn(new NullConcept("b", 0.1f)));
            assertEquals(0.2f, b.getMinPriority(),0.001f); //unaffected, 0.2 still lowest

            //same id, higher priority
            assertEquals(0.3f, b.getMaxPriority(),0.001f); //affected, 0.4 highest        
            assertEquals(null, b.putIn(new NullConcept("b", 0.4f)));
            assertEquals(0.4f, b.getMaxPriority(),0.001f); //affected, 0.4 highest

        }
        
    }
}
