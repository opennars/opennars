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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.opennars.entity.Concept;
import org.opennars.io.Narsese;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

/**
 *
 * @author patha
 */
public class SaveLoadMemoryTest {
    @Test
    public void testloadSaveMem() throws IOException, InstantiationException, NoSuchMethodException, SAXException, ParseException, 
            IllegalAccessException, InvocationTargetException, ParserConfigurationException, ClassNotFoundException, Narsese.InvalidInputException, Exception {
        Nar nar = new Nar();
        nar.addInput("<a --> b>.");
        nar.cycles(1);
        String fname = "test1.nars";
        nar.SaveToFile(fname);
        Nar nar2 = Nar.LoadFromFile(fname);
        Concept c2 = nar2.concept("<a --> b>");
        assert(c2 != null);
    }
}
