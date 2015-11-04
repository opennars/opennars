package nars.util.sort;

import nars.util.ArraySortedIndex;
import nars.util.meter.bag.NullItem;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/10/15.
 */
public class ArraySortedIndexTest {

    @Test public void test01() { test(2, 1); }
    @Test public void test02() { test(3, 2); }
    @Test public void test03() { test(4, 3); }
    @Test public void test111111() { test(5, 5); }
    @Test public void test11111() { test(10, 5); }
    @Test public void test1111() { test(15, 5); }
    @Test public void test111() { test(20, 5); }

    @Test public void test1() { test(100, 50); }
    @Test public void test2() { test(100, 100); }
    @Test public void test3() { test(100, 200); }

    void test(int insertions, int capacity) {
        ArraySortedIndex<NullItem> x = new ArraySortedIndex(capacity);
        for (int i = 0; i < insertions; i++) {
            //ensureSorted(x, i);
            x.insert(new NullItem());
            ensureSorted(x, i);
        }

        //x.forEach(y -> System.out.println(y));

    }

    private void ensureSorted(ArraySortedIndex<NullItem> x, int i) {
        boolean s = x.isSorted();
        if (!s) {
            System.err.println("improper sorting detected on cycle " + i);
            x.print(System.err);
        }
        assertTrue(x.isSorted());
        assertTrue(x.size() <= x.capacity());
        //assertTrue(x.size() <= i);
    }
}