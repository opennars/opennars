package nars.core;

import nars.build.Default;
import nars.io.narsese.InvalidInputException;
import nars.logic.entity.Compound;
import nars.logic.entity.Term;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        
        Map<Term,Term> h = new HashMap();
        h.put(n.term("b"), xy);
        Compound c = ab.applySubstituteToCompound(h);
                
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertTrue(ab.name().toString().equals(abS)); //ab unmodified
        
        assertTrue(!c.name().equals(abS)); //c is actually different
        assertTrue(!c.equals(ab));
        
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
