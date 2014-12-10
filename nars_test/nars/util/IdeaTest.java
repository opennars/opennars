/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.util.Idea.IdeaSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class IdeaTest {
    
    @Test public void testIdeaCreation() {
        Parameters.DEBUG = true;
        
        NAR n = new NAR(new Default());
        IdeaSet i = new IdeaSet(n);
        
        n.addInput("<a --> b>.");
        n.addInput("<a <-> b>.");
        n.addInput("<b --> a>.");
        
        n.finish(3);
        
        System.out.println(i);
        
        assertEquals(3, i.size());
        assertTrue(i.keySet().contains("[a, b]"));
        assertTrue(i.keySet().contains("a"));
        assertTrue(i.keySet().contains("b"));
        assertEquals(3, i.values().iterator().next().concepts.size());
        
        
    }
}
