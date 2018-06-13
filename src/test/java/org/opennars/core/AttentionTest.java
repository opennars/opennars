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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.core;


import com.google.common.collect.Iterables;
import org.junit.Test;
import org.opennars.entity.Concept;
import org.opennars.main.Nar;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO run this for each different kind of attention/bag etc
 */
public class AttentionTest {
    
    @Test public void testSampleNextConcept() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        
        final int numConcepts = 32;
        final Nar n = new Nar();
        for (int i = 0; i < numConcepts; i++)
            n.addInput("<x" + i + " <-> x" + (i+1) + ">.");
        
        n.cycles(100);
        
        final int c = Iterables.size(n.memory.concepts);
        assertTrue(c > 32);
        
        final Set<Concept> uniqueconcepts = new HashSet();
        
        for (int i = 0; i < numConcepts; i++) {
            final Concept s = n.memory.concepts.peekNext();
            uniqueconcepts.add(s);
        }

        assertTrue(uniqueconcepts.size() > 1);
        
        final int c2 = Iterables.size(n.memory.concepts);
        assertEquals("does not affect # of concepts", c, c2);
    }
    
}
