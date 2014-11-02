package nars.test.core;

import java.util.HashMap;
import java.util.Map;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.language.CompoundTerm;
import nars.language.Term;
import static org.junit.Assert.assertTrue;
import org.junit.Test;



public class ApplySubstituteTest {

    @Test
    public void testApplySubstitute() {
        NAR n = new DefaultNARBuilder().build();
            
        String abS ="<a --> b>";
        CompoundTerm ab = (CompoundTerm )n.term(abS);
        int originalComplexity = ab.getComplexity();
        
        String xyS ="<x --> y>";
        Term xy = n.term(xyS);
        
        Map<Term,Term> h = new HashMap();
        h.put(n.term("b"), xy);
        CompoundTerm c = ab.applySubstituteToCompound(h);
        
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertTrue(ab.name().toString().equals(abS)); //ab unmodified
        
        assertTrue(!c.name().equals(abS)); //c is actually different
        assertTrue(!c.equals(ab));
        
    }
    
    @Test
    public void test2() {
        //substituting:  <(*,$1) --> num>.  with $1 ==> 0
        NAR n = new DefaultNARBuilder().build();
            
        Map<Term,Term> h = new HashMap();
        h.put(n.term("$1"), n.term("0"));        
        CompoundTerm c = ((CompoundTerm)n.term("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
