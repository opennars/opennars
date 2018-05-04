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

import java.util.Arrays;
import static junit.framework.TestCase.assertTrue;
import static org.opennars.entity.Stamp.toSetArray;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestStamp {

    @Test 
    public void testStampToSetArray() {
        assertTrue(toSetArray(new long[] { 1, 2, 3 }).length == 3);        
        assertTrue(toSetArray(new long[] { 1, 1, 3 }).length == 2);
        assertTrue(toSetArray(new long[] { 1 }).length == 1);
        assertTrue(toSetArray(new long[] {  }).length == 0);
        assertTrue(
                Arrays.hashCode(toSetArray(new long[] { 3,2,1 }))
                ==
                Arrays.hashCode(toSetArray(new long[] { 2,3,1 }))
        );
        assertTrue(
                Arrays.hashCode(toSetArray(new long[] { 1,2,3 }))
                !=
                Arrays.hashCode(toSetArray(new long[] { 1,1,3 }))
        );    
    }
}
