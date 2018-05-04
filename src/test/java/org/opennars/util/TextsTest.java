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
package org.opennars.util;

import org.opennars.io.Texts;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class TextsTest {
 

    @Test
    public void testN2() {
        assertEquals("1.00", Texts.n2(1.00f).toString());
        assertEquals("0.50", Texts.n2(0.5f).toString());
        assertEquals("0.09", Texts.n2(0.09f).toString());
        assertEquals("0.10", Texts.n2(0.1f).toString());
        assertEquals("0.01", Texts.n2(0.009f).toString());
        assertEquals("0.00", Texts.n2(0.001f).toString());
        assertEquals("0.01", Texts.n2(0.01f).toString());
        assertEquals("0.00", Texts.n2(0f).toString());
    }
}
