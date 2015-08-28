package nars.util.graph;

import nars.NAR;
import nars.nar.Default;
import org.junit.Ignore;
import org.junit.Test;

import static nars.util.graph.NARGraph.IncludeEverything;


//NARGraph under construction

@Ignore
public class NARGraphTest {
    
    @Test
    public void testGraph() {

        NAR n = new NAR(new Default());
        
        n.input("<a --> b>.");
        
        n.runWhileInputting(2);
        
        //System.out.println(n);

        
        NARGraph g = new NARGraph();
        g.add(n, IncludeEverything, new DefaultGrapher(true,true,true,true,0,true,true));
        
        //System.out.println(g);
        
        assert(!g.vertexSet().isEmpty());
        assert(!g.edgeSet().isEmpty());
    }
}
