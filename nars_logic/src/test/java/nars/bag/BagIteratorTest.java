package nars.bag;

import nars.analyze.experimental.BagPerf.NullItem;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
import nars.util.data.random.XORShiftRandom;
import org.junit.Test;

import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



public class BagIteratorTest {

    final static Random rng = new XORShiftRandom();

    int L = 4;


    public void testIterator(Bag<CharSequence, NullItem> b, int expectedCount) {
        int count = 0;
        NullItem first = null, current = null;
        Iterator<NullItem> i = b.iterator();
        while (i.hasNext()) {
            NullItem n = i.next();
            if (first == null)
                first = n;
            current =n;
            count++;
        }

        assertEquals(expectedCount, count);
        
        if (b.size() > 1) {
            assertTrue(first.getPriority() >= current.getPriority());
        }
        
        assertEquals(b.size(), count);
    }
    
    public void testBagIterator(Bag<CharSequence, NullItem> b) {

        b.clear();

        b.put(new NullItem(0.1f));
        b.put(new NullItem(0.2f));
        b.put(new NullItem(0.3f));
        b.put(new NullItem(0.4f));
        b.put(new NullItem(0.5f));
        b.put(new NullItem(0.6f));
        b.put(new NullItem(0.7f));
        b.put(new NullItem(0.8f));

        assertEquals(8, b.size());

        if (b instanceof LevelBag)
            assert(((LevelBag)b).numEmptyLevels() < L);
        
        testIterator(b, 8);
        
        b.clear();
        
        testIterator(b, 0);
        
        b.put(new NullItem(0.6f));
        
        testIterator(b, 1);
        
    }
    
    @Test
    public void testBags() {
        testBagIterator(new LevelBag(L, L * 2));
    }
    @Test
    public void testCurveBagSequenceIterator() {
        testBagIterator(new CurveBag(rng, L * 2, false));
    }
    @Test
    public void testCurveBagRandomIterator() {
        testBagIterator(new CurveBag(rng, L*2, true));
        
    }
    
}
