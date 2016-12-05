/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.bag;

import nars.NAR;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.language.Term;
import nars.storage.Bag;
import nars.storage.LevelBag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class BagOperationsTest {

    static NAR nar = new NAR();
    static Concept makeConcept(String name, float priority) {
        BudgetValue budg = new BudgetValue(priority,priority,priority);
        Concept s = new Concept(budg,new Term(name),nar.memory);
        return s;
    }  
    
    @Test
    public void testConcept() {
        testBagSequence(new LevelBag(2, 2));    
    }
    
    public static void testBagSequence(Bag b) {

        //different id, different priority
        b.putIn(makeConcept("a", 0.1f));
        b.putIn(makeConcept("b", 0.15f));
        assertEquals(2, b.size());
        b.clear();
        
        //same priority, different id
        b.putIn(makeConcept("a", 0.1f));
        b.putIn(makeConcept("b", 0.1f));
        assertEquals(2, b.size());
        
        b.putIn(makeConcept("c", 0.2f));
        assertEquals(2, b.size());
        assertEquals(0.1f, b.getMinPriority(),0.001f);
        assertEquals(0.2f, b.getMaxPriority(),0.001f);
        
        //if (b instanceof GearBag()) return;
        
        
        b.putIn(makeConcept("b", 0.4f));
        
        
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
        
        
        
        assertEquals(null, b.putIn(makeConcept("a", 0.2f)));
        assertEquals(null, b.putIn(makeConcept("b", 0.3f)));
        
        if (b instanceof LevelBag) {
            assertEquals("a", b.putIn(makeConcept("c", 0.1f)).name().toString()); //replaces item on level
        }
        
    }
}
