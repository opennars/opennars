///*
// * Here comes the text of your license
// * Each line should be prefixed with  *
// */
//package nars.bag;
//
//import nars.bag.impl.CurveBag;
//import nars.bag.impl.LevelBag;
//import nars.budget.Item;
//import nars.budget.UnitBudget;
//import nars.util.data.random.XORShiftRandom;
//import org.junit.Test;
//
//import java.util.Random;
//
//import static junit.framework.TestCase.assertNotNull;
//import static junit.framework.TestCase.assertNull;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// *
// * @author me
// */
//public class BagOperationsTest {
//
//    final Random rng = new XORShiftRandom();
//
//    public static class NullConcept extends Item<CharSequence> {
//
//        private final String id;
//
//        public NullConcept(String id, float priority) {
//            super(new UnitBudget(priority, priority, priority));
//            this.id= id;
//        }
//
//
//        @Override
//        public CharSequence name() {
//            return id;
//        }
//
//        @Override
//        public String toString() {
//            return getBudgetString() + ' ' +  id;
//        }
//    }
//
//    @Test public void testLevelBag() {
//        testBagSequence(new LevelBag(2, 2));
//    }
//
//
//    @Test public void testCurveBag() {
//        testBagSequence(new CurveBag(2, rng));
//    }
//
//    /** test with a bag of capacity 2 */
//    public static void testBagSequence(Bag<CharSequence,Item<CharSequence>> b) {
//
//        b.mergePlus();
//
//        assertEquals(0, b.size());
//        assertEquals(2, b.capacity());
//
//        //different id, different priority
//        b.put(new NullConcept("a", 0.1f));
//        b.put(new NullConcept("b", 0.15f));
//        assertEquals(2, b.size());
//        b.clear();
//        assertEquals(0, b.size());
//        assertTrue(b.isEmpty());
//
//        //different id
//        b.put(new NullConcept("a", 0.05f));
//        b.put(new NullConcept("b", 0.1f));
//        assertEquals(2, b.size());
//
//        b.put(new NullConcept("c", 0.2f));
//        assertEquals(2, b.size());
//        assertEquals(0.1f, b.getPriorityMin(),0.001f);
//        assertEquals(0.2f, b.getPriorityMax(),0.001f);
//
//
//        //if (b instanceof GearBag()) return;
//
//        NullConcept B;
//        b.put(B = new NullConcept("b", 0.4f));
//
//        assertEquals(2, b.size());
//
//        //b.printAll();
//
//        //results in 0.2, 0.5 because of the '.mergePlus' specified above
//        assertEquals(0.2f, b.getPriorityMin(),0.001f);
//        assertEquals(0.5f, b.getPriorityMax(),0.001f);
//
//
//        Item tb = b.remove(B.name());
//        assertEquals(1, b.size());
//        assertTrue(tb!=null);
//        assertEquals(0.5f, tb.getPriority(), 0.001f);
//
//        Item tc = b.pop();
//        assertEquals(0, b.size());
//        assertEquals(0.2f, tc.getPriority(), 0.001f);
//
//        assertEquals(null, b.put(new NullConcept("a", 0.2f)));
//        b.put(new NullConcept("b", 0.3f));
//
//        if (b instanceof LevelBag) {
//            assertEquals("a", b.put(new NullConcept("c", 0.1f)).name().toString()); //replaces item on level
//        }
//        else if (b instanceof CurveBag) {
//            assertEquals("c", b.put(new NullConcept("c", 0.1f)).name().toString()); //could not insert, so got the object returned as result
//            assertEquals(2, b.size());
//
//            //same id, different priority (lower, so budget will not be affected)
//            assertEquals(null, b.put(new NullConcept("b", 0.1f)));
//            assertEquals(0.2f, b.getPriorityMin(),0.001f); //affected, item budget merged to new value, 0.1 new lowest
//            assertEquals(0.4f, b.getPriorityMax(),0.001f); //affected, 0.4 highest
//            assertTrue(b.getPriorityMax() > b.getPriorityMin());
//
//            //increasing b's priority should not cause 'a' to be removed
//            Item zzz = b.put(new NullConcept("b", 0.4f));
//            assertNull(null, zzz);
//
//            assertEquals(0.8f, b.getPriorityMax(),0.001f); //affected, 0.4 highest
//            assertNotNull(b.get("a"));
//        }
//
//    }
//}
