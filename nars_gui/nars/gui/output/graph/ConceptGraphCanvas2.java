package nars.gui.output.graph;

import nars.core.NAR;
import nars.util.DefaultGraphizer;
import nars.util.NARGraph;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 */
public class ConceptGraphCanvas2 extends AnimatedProcessingGraphCanvas<Object,Object> {
    private final NAR nar;
    
    boolean taskLinks = true;
            
    public ConceptGraphCanvas2(NAR n) {
        super();
        this.nar = n;
    }

    @Override
    protected DirectedMultigraph<Object, Object> getGraph() {
        return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new DefaultGraphizer(false, true, false, false, 0, true, taskLinks));
    }

    public void setTaskLinks(boolean taskLinks) {
        this.taskLinks = taskLinks;
    }

    
    
    
    
}
