/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome;

import com.google.common.collect.SetMultimap;
import objenome.solution.dependency.Builder;
import objenome.util.Packatainer;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class PackatainerTest {
    
    @Test 
    public void test1() {

        
    
        Packatainer rg = new Packatainer(
                new String[] { "objenome" },
                Multitainer.class, Container.class, Builder.class);
                
        SetMultimap<Class, Class> anc = rg.includeAncestorImplementations();

        assertEquals(7, anc.keySet().size());
        assertTrue(anc.size() > anc.keySet().size());
        
        for (Class c : anc.keySet()) {
            System.out.println(c + "=" + anc.get(c));
        }
        
        Set<Class> c = rg.getImplementable();
        assertTrue(c.size() > 1);
        
        //System.out.println(c);
        
        for (Class ci : rg.getImplementable()) {
            Multitainer g = new Multitainer(rg);
            try {
                System.out.println(ci + ": " + g.random(ci).getSolutions());
            }
            catch (Exception e) {
                System.out.println("  unable: " + ci + ": " + e);
            }
        }
            
    }
    
    
}
