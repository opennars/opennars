package nars.util.time;

import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/4/15.
 */
public class IntervalTreeTest {

        @Test
        public void testCreate(){
            new IntervalTree<Double, String>();
        }

        @Test
        public void testSinglePut(){
            IntervalTree<Double, String> t = new IntervalTree<>();
            t.put(30.0,50.0, "test");
        }

        @Test
        public void testSingleContainsValue(){
            IntervalTree<Double, String> t = new IntervalTree<>();
            t.put(30.0,50.0, "test");
            t.containsValue("test");
        }

        @Test
        public void testSingleKeyContains(){
            IntervalTree<Double, String> t = new IntervalTree<>();
            t.put(30.0,50.0, "test");
            assertFalse(t.searchContaining(30.0,45.0).isEmpty());
        }

        @Test
        public void testSingleKeyContainsNotOverlap(){
            IntervalTree<Double, String> t = new IntervalTree<>();
            t.put(30.0,50.0, "test");
            assertTrue(t.searchContaining(20.0,40.0).isEmpty());
        }

        @Test
        public void testSingleKeyOverlapping(){
            IntervalTree<Double, String> t = new IntervalTree<>();
            t.put(30.0,50.0, "test");
            assertFalse(t.searchOverlapping(20.0,40.0).isEmpty());
        }

        private IntervalTree<Integer, String> makeIntervalTree(){
            IntervalTree<Integer,String> t = new IntervalTree<>();
            t.put(0, 10, "0-10");
            t.put(5, 15, "5-15");
            t.put(10, 20, "10-20");
            t.put(15, 25, "15-25");
            t.put(20, 30, "20-30");
		/*Random r = new Random();
		for(int i=0;i<500;i++){
			t.put(r.nextInt(100000)+50,r.nextInt(100000) + 100050,"Desu");
			System.out.println("Size: " + t.size() + "\t Height: " + t.height() + "\t Average Height: " + t.averageHeight());
		}
		System.exit(-1);//*/
            return t;
        }

        @Test
        public void testClear(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            t.clear();
            assertTrue(t.isEmpty());
        }

        @Test
        public void testMultiMake(){
            makeIntervalTree();
        }

        @Test
        public void testMultiContainsValue(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            assertTrue(t.containsValue("15-25"));
            assertTrue(t.containsValue("5-15"));
            assertTrue(t.containsValue("20-30"));
        }

        @Test
        public void testMultiSearchContaining(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContaining(0, 6);
            assertTrue(res.contains("0-10"));
            assertTrue(res.size() == 1);
        }

        @Test
        public void testMultiSearchContaining2(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContaining(4, 16);
            assertFalse(res.contains("5-15"));
            assertTrue(res.isEmpty());
        }

        @Test
        public void testMultiSearchContaining3(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContaining(7, 31);
            assertTrue(res.isEmpty());
        }

        @Test
        public void testMultiSearchContainedBy(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContainedBy(0, 16);
            assertTrue(res.contains("0-10"));
            assertTrue(res.contains("5-15"));
            assertTrue(res.size() == 2);
        }

        @Test
        public void testMultiSearchContainedBy2(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContainedBy(6, 31);
            assertFalse(res.contains("5-15"));
            assertFalse(res.isEmpty());
        }

        @Test
        public void testMultiSearchContainedBy3(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            Collection<String> res = t.searchContainedBy(0, 31);
            assertTrue(res.size() == t.size());
        }

        @Test
        public void testRemove(){
            IntervalTree<Integer, String> t = makeIntervalTree();
            t.remove("0-10");
            t.remove("5-15");
            assertFalse(t.containsValue("0-10"));
            assertFalse(t.containsValue("5-15"));
        }


}