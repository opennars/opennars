package nars.util.sort;

import nars.meter.bag.NullItem;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/10/15.
 */
public class ArraySortedIndexTest {

    @Test
    public void test1() {
        test(100, 200);
        test(100, 100);
        test(100, 50);
    }

    protected void test(int insertions, int capacity) {
        ArraySortedIndex<NullItem> x = new ArraySortedIndex(capacity);
        for (int i = 0; i < insertions; i++) {
            x.insert(new NullItem());
            assertTrue(x.isSorted());
        }

        //x.forEach(y -> System.out.println(y));

        assertTrue(x.size() <= capacity);
    }
}