package nars.test.util;

import nars.test.core.BagPerf.NullItem;
import nars.util.FastBag;
import org.junit.Test;

/**
 *
 * @author me
 */


public class FastBagTest {
 
    @Test public void testFastBag() {
        FastBag<NullItem> f = new FastBag(4, 10);
        
        f.putIn(new NullItem(.25f));
        assert(f.size() == 1);
        assert(f.getMass() > 0);
        
        f.putIn(new NullItem(.9f));
        f.putIn(new NullItem(.75f));
        
        System.out.println(f);
        
        //sorted
        assert(f.items.get(0).getPriority() < f.items.get(1).getPriority());

        assert(f.size() == 3);
        f.takeOut();
        
        assert(f.size() == 2);
        f.takeOut();
        assert(f.size() == 1);
        f.takeOut();
        assert(f.size() == 0);
        
        assert(f.getMass() == 0);
    }

    @Test public void testFastBagCapacityLimit() {
        FastBag<NullItem> f = new FastBag(4, 10);
        f.putIn(new NullItem());
        f.putIn(new NullItem());
        f.putIn(new NullItem());
        boolean a = f.putIn(new NullItem());
        assert(a);
        f.putIn(new NullItem()); //limit
        assert(f.size() == 4);
        f.putIn(new NullItem()); //limit
        assert(f.size() == 4);

    }
    
    @Test public void testRemovalDistribution() {
        int N = 4;
        int samples = 4096 * N;
        
        int count[] = new int[N];
        
        FastBag<NullItem> f = new FastBag(N, 10);
        
        //fill
        for (int i= 0; i < N; i++) {
            f.putIn(new NullItem());
        }
        
        for (int i= 0; i < samples; i++) {
            count[f.nextRemovalIndex()]++;
        }
        
        assert(count[0] < count[1]);
        assert(count[1] < count[2]);
        assert(count[2] < count[3]);
        
        //System.out.println(count[0] + " " + count[1] + " " + count[2] + " " + count[3]);
        
    }
    
}
