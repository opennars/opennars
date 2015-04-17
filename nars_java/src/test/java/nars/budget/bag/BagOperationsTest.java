/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.budget.bag;

import nars.budget.Bag;
import nars.budget.Budget;
import nars.nal.concept.Concept;
import nars.nal.Item;
import nars.nal.term.Term;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class BagOperationsTest {

    public static class NullConcept extends Concept {

        public NullConcept(String id, float priority) {
            super(new Term(id), new Budget(priority, priority, priority), null, null, null);
        }    

        @Override
        public float getQuality() {
            return 0;
        }
        
    }

    @Test public void testLevelBag() {
        testBagSequence(new LevelBag(2, 2));
    }
    
    
    @Test public void testCurveBag() {
        testBagSequence(new CurveBag(2, new CurveBag.FairPriorityProbabilityCurve(), true));
    }

    /** test with a bag of capacity 2 */
    public static void testBagSequence(Bag b) {

        assertEquals(0, b.size());
        assertEquals(2, b.capacity());

        //different id, different priority
        b.put(new NullConcept("a", 0.1f));
        b.put(new NullConcept("b", 0.15f));
        assertEquals(2, b.size());
        b.clear();
        
        //same priority, different id
        b.put(new NullConcept("a", 0.1f));
        b.put(new NullConcept("b", 0.1f));
        assertEquals(2, b.size());
        
        b.put(new NullConcept("c", 0.2f));
        assertEquals(2, b.size());
        assertEquals(0.1f, b.getMinPriority(),0.001f);
        assertEquals(0.2f, b.getMaxPriority(),0.001f);
        

        //if (b instanceof GearBag()) return;
        
        b.put(new NullConcept("b", 0.4f));

        assertEquals(2, b.size());

        //results in 0.2, not 0.1 if merged and not simply replaced B's budget
        assertEquals("budget merge", 0.2f, b.getMinPriority(),0.001f);
        assertEquals(0.4f, b.getMaxPriority(),0.001f);
        
        
        Item tb = b.remove(new Term("b"));
        assertTrue(tb!=null);
        assertEquals(1, b.size());
        assertEquals(0.4f, tb.getPriority(), 0.001f);
        
        Item tc = b.pop();
        assertEquals(0, b.size());
        assertEquals(0.2f, tc.getPriority(), 0.001f);
        
        assertEquals(null, b.put(new NullConcept("a", 0.2f)));
        b.put(new NullConcept("b", 0.3f));

        if (b instanceof LevelBag) {
            assertEquals("a", b.put(new NullConcept("c", 0.1f)).name().toString()); //replaces item on level
        }
        else if (b instanceof CurveBag) {
            assertEquals("c", b.put(new NullConcept("c", 0.1f)).name().toString()); //could not insert, so got the object returned as result
            assertEquals(2, b.size());

            //same id, different priority (lower, so budget will not be affected)
            assertEquals(null, b.put(new NullConcept("b", 0.1f)));
            assertEquals(0.1f, b.getMinPriority(),0.001f); //affected, item budget replaced to new value, 0.1 new lowest
            assertEquals(0.2f, b.getMaxPriority(),0.001f); //affected, 0.4 highest

            //increasing b's priority should not cause 'a' to be removed
            Item zzz = b.put(new NullConcept("b", 0.4f));
            assertNull(null, zzz);

            assertEquals(0.4f, b.getMaxPriority(),0.001f); //affected, 0.4 highest
            assertNotNull(b.get(Term.get("a")));
        }
        
    }
}
