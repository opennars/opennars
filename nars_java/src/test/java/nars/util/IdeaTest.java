/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.util.Idea.IdeaSet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class IdeaTest {
    private IdeaSet i;
    private NAR n;
    
    @Before public void setup() {
        Parameters.DEBUG = true;
        
        n = new NAR(new Default());
        i = new IdeaSet(n);        
    }
    
    @Test public void testIdeaCreation() {
        
        n.addInput("<a <=> b>.");
        n.addInput("<a <-> b>.");
        n.addInput("<b --> a>.");
        
        n.run(4);
                        
        assertEquals(2+2, i.size());
        assertTrue(i.keySet().contains("[a, b]"));
        assertTrue(i.keySet().contains("(b, a]"));
        assertTrue(i.keySet().contains("a"));
        assertTrue(i.keySet().contains("b"));
        assertEquals(2, i.values().iterator().next().concepts.size());
                
    }
    
    @Test public void testOpPuncAggregation() {



        //n.test(2);
        //assertEquals(i.get("[a, b]").getSentenceTypes().toString(), 1, i.get("[a, b]").getSentenceTypes().size());

        n.addInput("<a <-> b>.");
        n.addInput("<a <=> b>.");
        n.addInput("<a <=> b>!");
        n.addInput("<b <=> a>?");
        n.addInput("<a <-> b>.");
        n.addInput("<a <-> b>!");
        n.addInput("<b <-> a>?");
        
        n.run(12);

        /** [<-> ?, <-> ., <-> !, <=> !, <=> ., <=> ?]  */
        assertEquals(i.get("[a, b]").getSentenceTypes().toString(), 6, i.get("[a, b]").getSentenceTypes().size());

    }    
}
