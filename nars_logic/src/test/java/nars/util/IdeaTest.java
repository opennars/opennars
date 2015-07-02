/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.Global;
import nars.NAR;
import nars.nar.Default;
import nars.util.graph.experimental.Idea.IdeaSet;
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
        Global.DEBUG = true;
        
        n = new NAR(new Default());
        i = new IdeaSet(n);        
    }
    
    @Test public void testIdeaCreation() {
        
        n.input("<a <=> b>.");
        n.input("<a <-> b>.");
        n.input("<b --> a>.");
        
        n.runWhileNewInput(4);

        //System.out.println(i.keySet());
                        
        assertEquals(5 /* used to be 4 */, i.size());
        assertTrue(i.keySet().contains("[a, b]"));
        assertTrue(i.keySet().contains("(b, a]"));
        assertTrue(i.keySet().contains("a"));
        assertTrue(i.keySet().contains("b"));
        assertEquals(2, i.values().iterator().next().concepts.size());
                
    }
    
    @Test public void testOpPuncAggregation() {



        //n.test(2);
        //assertEquals(i.get("[a, b]").getSentenceTypes().toString(), 1, i.get("[a, b]").getSentenceTypes().size());

        n.input("<a <-> b>.");
        n.input("<a <=> b>.");
        n.input("<a <=> b>!");
        n.input("<b <=> a>?");
        n.input("<a <-> b>.");
        n.input("<a <-> b>!");
        n.input("<b <-> a>?");
        
        n.runWhileNewInput(12);

        /** [<-> ?, <-> ., <-> !, <=> !, <=> ., <=> ?]  */
        assertEquals(i.get("[a, b]").getSentenceTypes().toString(), 6, i.get("[a, b]").getSentenceTypes().size());

    }    
}
