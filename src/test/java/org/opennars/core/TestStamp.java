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
package org.opennars.core;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import org.opennars.entity.Stamp.BaseEntry;
import static org.opennars.entity.Stamp.toSetArray;

/**
 *
 * @author me
 */


public class TestStamp {
    private long narid = 0;
    BaseEntry entry(long inputId) {
        return new BaseEntry(narid, inputId);
    }
    @Test 
    public void testStampToSetArray() {
        
        assertTrue(toSetArray(new BaseEntry[] { entry(1), entry(2), entry(3) }).length == 3);        
        assertTrue(toSetArray(new BaseEntry[] { entry(1), entry(1), entry(3) }).length == 2);
        assertTrue(toSetArray(new BaseEntry[] { entry(1) }).length == 1);
        assertTrue(toSetArray(new BaseEntry[] {  }).length == 0);
        assertTrue(
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(3),entry(2),entry(1) }))
                ==
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(2),entry(3),entry(1) }))
        );
        assertTrue(
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(1),entry(2),entry(3) }))
                !=
                Arrays.hashCode(toSetArray(new BaseEntry[] { entry(1),entry(1),entry(3) }))
        );    
    }
}
