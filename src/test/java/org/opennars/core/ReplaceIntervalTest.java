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
import org.opennars.io.Narsese;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

/**
 *
 * @author patha
 */
public class ReplaceIntervalTest {
   //<(*,{SELF},<{(*,fragmentC,fragmentD)} --> compare>,TRUE) =\> (*,{SELF},(&/,<{fragmentC} --> mutate>,+12),TRUE)>. %1.00;0.25% 
    @Test
    public void replaceIvalTest() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        Nar nar = new Nar();
        Narsese parser = new Narsese(nar);
        Term ret = parser.parseTerm("<(*,{SELF},<{(*,fragmentC,fragmentD)} --> compare>,TRUE) =\\> (*,{SELF},(&/,<{fragmentC} --> mutate>,+12),TRUE)>");
        CompoundTerm ct = (CompoundTerm) CompoundTerm.replaceIntervals(ret);
        assert(ct.toString().equals("<(*,{SELF},<{(*,fragmentC,fragmentD)} --> compare>,TRUE) =\\> (*,{SELF},(&/,<{fragmentC} --> mutate>,+1),TRUE)>"));
    }
}
