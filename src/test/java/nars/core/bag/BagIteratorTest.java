package nars.core.bag;

import java.util.Iterator;
import nars.perf.BagPerf.NullItem;
import nars.storage.Bag;
import nars.storage.LevelBag;
import static org.junit.Assert.assertTrue;
import org.junit.Test;



public class BagIteratorTest {
    
    int L = 4;


    public void testIterator(Bag<NullItem,CharSequence> b) {
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
        
        assertTrue(count==b.size());
    }
    
    public void testBagIterator(Bag<NullItem,CharSequence> b) {
        
        b.putIn(new NullItem(0.1f));
        b.putIn(new NullItem(0.2f));
        b.putIn(new NullItem(0.3f));
        b.putIn(new NullItem(0.4f));
        b.putIn(new NullItem(0.5f));
        b.putIn(new NullItem(0.6f));
        b.putIn(new NullItem(0.7f));
        b.putIn(new NullItem(0.8f));
                
        if (b instanceof LevelBag)
            assert(((LevelBag)b).numEmptyLevels() < L);
        
        testIterator(b);
        
        b.clear();
        
        testIterator(b);        
        
        b.putIn(new NullItem(0.6f));
        
        testIterator(b);
        
    }
    
    @Test
    public void testBags() {
        testBagIterator(new LevelBag(L, L*2));
        
    }
    
}
