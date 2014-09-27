package nars.gui.output.graph;

import nars.entity.Sentence;
import nars.inference.GraphExecutive;
import nars.language.Term;

/**
 *
 * @author me
 */


public class ImplicationGraphCanvas extends AnimatedProcessingGraphCanvas<Term,Sentence> {
    private final GraphExecutive graphExec;

    public ImplicationGraphCanvas(GraphExecutive g) {
        super(g.implication);
        this.graphExec = g;        
        
    }

    @Override
    public int getEdgeColor(Sentence e) {
        float a = (float)(1.0f / graphExec.implication.getEdgeWeight(e));
        return PGraphPanel.getColor(e.getClass().getSimpleName().toString(), a);        
    }
    
    @Override
    public float getEdgeThickness(Sentence edge, VertexDisplay source, VertexDisplay target) {
        float ar = (source.radius + target.radius)/2f;
        return (float)(ar/graphExec.implication.getEdgeWeight(edge));
    }
    
    @Override
    public float getNodeSize(final Term v) {
        return 10f * (0.2f + (float) GraphExecutive.getEffectivePriority(graphExec.memory, v));
    }
    
    
    

    
}
