package nars.gui.output.graph;

import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


public class AnimatedProcessingGraphCanvas<V,E> extends ProcessingGraphCanvas<DirectedMultigraph<V,E>> {
    private final DirectedMultigraph<V, E> graph;

    public AnimatedProcessingGraphCanvas(DirectedMultigraph<V,E> graph) {
        super();
        this.graph = graph;        
    }

    
    @Override
    protected DirectedMultigraph<V,E> getGraph() {
        return graph;
    }

    @Override
    protected boolean hasUpdate() {
        //temporary:
        updateNext = true;
        drawn = false;
        
        return true;
    }
    
}
