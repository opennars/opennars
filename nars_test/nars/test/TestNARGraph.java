package nars.test;

import nars.core.NAR;
import nars.graph.NARGraph;
import nars.graph.NARGraph.DefaultGraphizer;
import static nars.graph.NARGraph.IncludeEverything;
import nars.io.TextInput;
import nars.io.TextOutput;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestNARGraph {
    
    @Test
    public void testGraph() {
    
        NAR n = new NAR();
        
        n.addInput("<a --> b>.");
        
        n.finish(2);
        
        //System.out.println(n);

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new DefaultGraphizer(true,true,true,true,true));
        
        //System.out.println(g);
        
        assert(g.vertexSet().size() > 0);
        assert(g.edgeSet().size() > 0);
    }
}
