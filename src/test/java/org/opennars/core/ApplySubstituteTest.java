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
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class ApplySubstituteTest {
    
    final Nar n = new Nar();
    final Narsese np = new Narsese(n);

    public ApplySubstituteTest() throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
    }

    @Test
    public void testApplySubstitute() throws Narsese.InvalidInputException {
            
        final String abS ="<a --> b>";
        final CompoundTerm ab = (CompoundTerm )np.parseTerm(abS);
        final int originalComplexity = ab.getComplexity();
        
        final String xyS ="<x --> y>";
        final Term xy = np.parseTerm(xyS);
        
        final Map<Term,Term> h = new HashMap();
        h.put(np.parseTerm("b"), xy);
        final CompoundTerm c = ab.applySubstituteToCompound(h);
                
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertTrue(ab.name().toString().equals(abS)); //ab unmodified
        
        assertTrue(!c.name().equals(abS)); //c is actually different
        assertTrue(!c.equals(ab));
        
    }
    
    @Test
    public void test2() throws Narsese.InvalidInputException, IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
        //substituting:  <(*,$1) --> num>.  with $1 ==> 0
        final Nar n = new Nar();
            
        final Map<Term,Term> h = new HashMap();
        h.put(np.parseTerm("$1"), np.parseTerm("0"));        
        final CompoundTerm c = ((CompoundTerm)np.parseTerm("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
