/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.core.bag;

import java.util.Iterator;
import org.opennars.perf.BagPerf.NullItem;
import org.opennars.storage.Bag;
import org.opennars.storage.LevelBag;
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
