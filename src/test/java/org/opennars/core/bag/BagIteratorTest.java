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

import org.junit.Test;
import org.opennars.perf.BagPerf.NullItem;
import org.opennars.storage.Bag;
import org.opennars.storage.LevelBag;
import static org.junit.Assert.assertTrue;
import org.opennars.main.Nar;


public class BagIteratorTest {
    
    final int L = 4;


    public void testIterator(final Bag<NullItem,CharSequence> b) {
        int count = 0;
        NullItem first = null, current = null;
        for (final NullItem n : b) {
            if (first == null)
                first = n;
            current = n;
            //System.out.println(current);
            count++;
        }               
        
        if (b.size() > 1) {
            //check correct order
            assertTrue(first.getPriority() > current.getPriority());
        }
        
        assertTrue(count==b.size());
    }
    
    public void testBagIterator(final Bag<NullItem,CharSequence> b) {
        
        b.putIn(new NullItem(0.1f));
        b.putIn(new NullItem(0.2f));
        b.putIn(new NullItem(0.3f));
        b.putIn(new NullItem(0.4f));
        b.putIn(new NullItem(0.5f));
        b.putIn(new NullItem(0.6f));
        b.putIn(new NullItem(0.7f));
        b.putIn(new NullItem(0.8f));

        assert !(b instanceof LevelBag) || (((LevelBag) b).numEmptyLevels() < L);
        
        testIterator(b);
        
        b.clear();
        
        testIterator(b);        
        
        b.putIn(new NullItem(0.6f));
        
        testIterator(b);
        
    }
    
    @Test
    public void testBags() throws Exception {
        Nar nar = new Nar();
        testBagIterator(new LevelBag(L, L*2, nar.narParameters));
        assert(true);
    }
    
}
