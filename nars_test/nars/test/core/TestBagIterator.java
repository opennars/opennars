package nars.test.core;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import nars.storage.AbstractBag;
import nars.storage.Bag;
import nars.test.core.BagPerf.NullItem;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestBagIterator {
    

    public void testIterator(AbstractBag<NullItem> b) {
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
        
        if (b.size() > 1) {
            //check correct order
            assertTrue(first.getPriority() > current.getPriority());
        }
        
        assertTrue(count==b.size());
    }
    
    @Test
    public void testBagIterator() {
        int L = 4;
        Bag<NullItem> b = new Bag(L, L*2, new AtomicInteger(10));
        
        b.putIn(new NullItem(0.1f));
        b.putIn(new NullItem(0.2f));
        b.putIn(new NullItem(0.3f));
        b.putIn(new NullItem(0.4f));
        b.putIn(new NullItem(0.5f));
        b.putIn(new NullItem(0.6f));
        b.putIn(new NullItem(0.7f));
        b.putIn(new NullItem(0.8f));
                
        assert(b.numEmptyLevels() < L);
        
        testIterator(b);
        
        b.clear();
        
        testIterator(b);        
        
        b.putIn(new NullItem(0.6f));
        
        testIterator(b);
        
    }
    
}
