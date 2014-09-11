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
        CompoundTerm c = ab.applySubstitute(h);
        
        assertTrue(c.getComplexity() > originalComplexity);
        
        assertTrue(ab.name().toString().equals(abS)); //ab unmodified
        
        assertTrue(!c.name().equals(abS)); //c is actually different
        assertTrue(!c.equals(ab));
        
    }
}
