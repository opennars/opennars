package nars.core;

import nars.Global;
import nars.NAR;
import nars.nar.Default;
import nars.narsese.InvalidInputException;
import nars.term.Compound;
import nars.term.Term;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;



public class ApplySubstituteTest {
    
    NAR n = new NAR(new Default());

    @Test
    public void testApplySubstitute() throws InvalidInputException {
            
        String abS ="<a --> b>";
        Compound ab = (Compound)n.term(abS);
        int originalComplexity = ab.getComplexity();
        
        String xyS ="<x --> y>";
        Term xy = n.term(xyS);
        
        Map<Term,Term> h = Global.newHashMap();
        h.put(n.term("b"), xy);
        Compound c = ab.applySubstituteToCompound(h);
                
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertEquals(abS, ab.toString()); //ab unmodified

        assertNotEquals(abS, c.toString()); //c is actually different
        assertNotEquals(ab, c);
        
    }
    
    @Test
    public void test2() throws InvalidInputException {
        //substituting:  <(*,$1) --> num>.  with $1 ==> 0
        NAR n = new NAR(new Default());
            
        Map<Term,Term> h = new HashMap();
        h.put(n.term("$1"), n.term("0"));        
        Compound c = ((Compound)n.term("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
