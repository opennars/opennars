package nars.test.util;

import nars.core.build.DefaultNARBuilder;
import nars.core.NAR;
import nars.util.NARGraph;
import nars.util.NARGraph.DefaultGraphizer;
import static nars.util.NARGraph.IncludeEverything;
import org.junit.Test;

/**
 *
 * @author me
 */


public class TestNARGraph {
    
    @Test
    public void testGraph() {
    
        NAR n = new DefaultNARBuilder().build();
        
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
