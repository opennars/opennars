package nars.test.util;

import nars.core.NAR;
import nars.core.build.Default;
import nars.util.DefaultGraphizer;
import nars.util.NARGraph;
import static nars.util.NARGraph.IncludeEverything;
import org.junit.Test;



public class TestNARGraph {
    
    @Test
    public void testGraph() {
    
        NAR n = new Default().build();
        
        n.addInput("<a --> b>.");
        
        n.finish(2);
        
        //System.out.println(n);

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new DefaultGraphizer(true,true,true,true,0,true,true));
        
        //System.out.println(g);
        
        assert(g.vertexSet().size() > 0);
        assert(g.edgeSet().size() > 0);
    }
}
