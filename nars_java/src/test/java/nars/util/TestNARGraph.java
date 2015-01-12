package nars.util;

import nars.core.NAR;
import nars.core.build.Default;
import org.junit.Test;

import static nars.util.NARGraph.IncludeEverything;



public class TestNARGraph {
    
    @Test
    public void testGraph() {

        NAR n = new NAR(new Default());
        
        n.addInput("<a --> b>.");
        
        n.run(2);
        
        //System.out.println(n);

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new DefaultGraphizer(true,true,true,true,0,true,true));
        
        //System.out.println(g);
        
        assert(g.vertexSet().size() > 0);
        assert(g.edgeSet().size() > 0);
    }
}
