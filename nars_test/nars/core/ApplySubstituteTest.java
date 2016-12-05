package nars.core;

import java.util.HashMap;
import java.util.Map;
import nars.NAR;
import nars.config.Plugins;
import nars.language.CompoundTerm;
import nars.language.Term;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import nars.io.Narsese;


public class ApplySubstituteTest {
    
    NAR n = new NAR(new Plugins());
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
        NAR n = new NAR(new Plugins());
            
        Map<Term,Term> h = new HashMap();
        h.put(np.parseTerm("$1"), np.parseTerm("0"));        
        CompoundTerm c = ((CompoundTerm)np.parseTerm("<(*,$1) --> num>")).applySubstituteToCompound(h);
        
        assertTrue(c!=null);
    }
}
