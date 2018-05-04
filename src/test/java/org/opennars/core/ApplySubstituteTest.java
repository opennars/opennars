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

import java.util.HashMap;
import java.util.Map;
import org.opennars.main.NAR;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Term;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.opennars.io.Narsese;


public class ApplySubstituteTest {
    
    NAR n = new NAR();
    Narsese np = new Narsese(n);
    
    @Test
    public void testApplySubstitute() throws Narsese.InvalidInputException {
            
        String abS ="<a --> b>";
        CompoundTerm ab = (CompoundTerm )np.parseTerm(abS);
        int originalComplexity = ab.getComplexity();
        
        String xyS ="<x --> y>";
        Term xy = np.parseTerm(xyS);
        
        Map<Term,Term> h = new HashMap();
        h.put(np.parseTerm("b"), xy);
        CompoundTerm c = ab.applySubstituteToCompound(h);
                
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertTrue(ab.name().toString().equals(abS)); //ab unmodified
        
        assertTrue(!c.name().equals(abS)); //c is actually different
        assertTrue(!c.equals(ab));
        
    }
    
    @Test
    public void test2() throws Narsese.InvalidInputException {
        //substituting:  <(*,$1) --> num>.  with $1 ==> 0
        NAR n = new NAR();
            
        Map<Term,Term> h = new HashMap();
        h.put(np.parseTerm("$1"), np.parseTerm("0"));        
        CompoundTerm c = ((CompoundTerm)np.parseTerm("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
