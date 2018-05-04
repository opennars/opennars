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

import org.opennars.storage.Bag;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author me
 */
public class BagPinTest {

    @Test
    public void test() {
        assertEquals(0, Bag.bin(0, 10));
        assertEquals(1, Bag.bin(0.1f, 10));
        assertEquals(9, Bag.bin(0.9f, 10));
        assertEquals(9, Bag.bin(0.925f, 10));
        assertEquals(10, Bag.bin(0.975f, 10));
        assertEquals(10, Bag.bin(1.0f, 10));
        
        
        assertEquals(0, Bag.bin(0f, 9));
        assertEquals(1, Bag.bin(0.1f, 9));
        assertEquals(8, Bag.bin(0.9f, 9));
        assertEquals(9, Bag.bin(1.0f, 9));
    }

}
