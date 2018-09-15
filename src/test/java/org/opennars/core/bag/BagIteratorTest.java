/*
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
    
    public int numEmptyLevels(LevelBag bag) {
        int empty = 0;
        for (int i = 0; i < bag.level.length; i++) {
            if (bag.level[i].isEmpty()) {
                empty++;
            }
        }
        return empty;
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

        assert !(b instanceof LevelBag) || (numEmptyLevels((LevelBag) b) < L);
        
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
