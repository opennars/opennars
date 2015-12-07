package nars.term;

import nars.Global;
import nars.NAR;
import nars.Narsese;
import nars.nar.Default;
import nars.term.compound.Compound;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;



public class ApplySubstituteTest {
    
    NAR n = new Default();

    @Test
    public void testApplySubstitute() throws Narsese.NarseseException {
            
        String abS ="<a --> b>";
        Compound ab = n.term(abS);
        int originalComplexity = ab.complexity();
        
        String xyS ="<x --> y>";
        Term xy = n.term(xyS);
        
        Map<Term,Term> h = Global.newHashMap();
        h.put(n.term("b"), xy);
        Compound c = ab.applySubstituteToCompound(h);
                
        assertTrue(c.complexity() > originalComplexity);
        
        assertEquals(abS, ab.toString()); //ab unmodified

        assertNotEquals(abS, c.toString()); //c is actually different
        assertNotEquals(ab, c);
        
    }
    
    @Test
    public void test2() throws Narsese.NarseseException {
        //substituting:  <(*,$1) --> num>.  with $1 ==> 0

        Map<Term,Term> h = new HashMap();
        h.put(n.term("$1"), n.term("0"));        
        Compound c = ((Compound)n.term("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
