package nars.gui.output.graph;

import nars.gui.output.graph.layout.FastOrganicLayout;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


public class AnimatedProcessingGraphCanvas<V,E> extends ProcessingGraphCanvas<V,E> {
    private final DirectedMultigraph<V, E> graph;
    //private final FastOrganicLayout layout;
    private final FastOrganicLayout layout;

    public AnimatedProcessingGraphCanvas(DirectedMultigraph<V,E> graph) {
        super();
        this.graph = graph;        
        setUpdateNext();
        layout = new FastOrganicLayout(graph);
    }

    
    @Override
    protected DirectedMultigraph<V,E> getGraph() {
        DirectedMultigraph<V,E> gg = (DirectedMultigraph)graph.clone();
        for (V v : gg.vertexSet()) {
            ProcessingGraphCanvas.VertexDisplay d = updateVertex(v);            
        }
        return gg;
    }

    @Override
    protected void updateVertices() {
        
        //layout.setMinDistanceLimit(25f);
        //layout.setMaxDistanceLimit(50f);
        layout.setMaxIterations(5);
        //layout.setForceConstant(100f);
        
        layout.execute(vertices);
    }
    
    

    @Override
    protected boolean hasUpdate() {
        //temporary:
        setUpdateNext();
        
        return true;
    }
    
}
