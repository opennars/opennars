package nars.gui.output.graph;

import nars.core.NAR;
import nars.util.NARGraph;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 */
public class ConceptGraphCanvas2 extends AnimatedProcessingGraphCanvas<Object,Object> {
    private final NAR nar;
    
    public ConceptGraphCanvas2(NAR n) {
        super();
        this.nar = n;
    }

    @Override
    protected DirectedMultigraph<Object, Object> getGraph() {
        return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new NARGraph.DefaultGraphizer(false, true, false, false, false, true));
    }

    
    
    
}
