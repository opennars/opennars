package nars.bag;

import nars.analyze.experimental.BagPerf.NullItem;
import nars.bag.impl.CurveBag;
import nars.bag.impl.LevelBag;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



public class BagIteratorTest {
    
    int L = 4;


    public void testIterator(Bag<CharSequence, NullItem> b) {
        int count = 0;
        NullItem first = null, current = null;
        Iterator<NullItem> i = b.iterator();
        while (i.hasNext()) {
            NullItem n = i.next();
            if (first == null)
                first = n;
            current =n;
            //System.out.println(current);
            count++;
        }               
        
        if (b.size() > 1) {
            //check correct order
            assertTrue(first.getPriority() > current.getPriority());
        }
        
        assertEquals(b.size(), count);
    }
    
    public void testBagIterator(Bag<CharSequence, NullItem> b) {
        
        b.put(new NullItem(0.1f));
        b.put(new NullItem(0.2f));
        b.put(new NullItem(0.3f));
        b.put(new NullItem(0.4f));
        b.put(new NullItem(0.5f));
        b.put(new NullItem(0.6f));
        b.put(new NullItem(0.7f));
        b.put(new NullItem(0.8f));
                
        if (b instanceof LevelBag)
            assert(((LevelBag)b).numEmptyLevels() < L);
        
        testIterator(b);
        
        b.clear();
        
        testIterator(b);        
        
        b.put(new NullItem(0.6f));
        
        testIterator(b);
        
    }
    
    @Test
    public void testBags() {
        testBagIterator(new LevelBag(L, L*2));
        testBagIterator(new CurveBag(L*2, false));
        testBagIterator(new CurveBag(L*2, true));
        
    }
    
}
