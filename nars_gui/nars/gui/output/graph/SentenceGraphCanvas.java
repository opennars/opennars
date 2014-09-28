package nars.gui.output.graph;

import java.awt.Color;
import nars.entity.Sentence;
import nars.inference.GraphExecutive;
import nars.language.Inheritance;
import nars.language.Similarity;
import nars.language.Term;
import nars.util.graph.SentenceGraph;

/**
 *
 * @author me
 */


public class SentenceGraphCanvas extends AnimatedProcessingGraphCanvas<Term,Sentence> {
    private final SentenceGraph sgraph;
    
    

    public SentenceGraphCanvas(SentenceGraph g) {
        super(g);
        this.sgraph = g;
    }

    @Override
    public void updateGraph() {
        super.updateGraph(); 
        
        
    }
    
    @Override
    public int getEdgeColor(final Sentence edge) {
        float freq = edge.truth.getFrequency();
        float conf = edge.truth.getConfidence();
        
        float hue = 0.5f;
        if (edge.content instanceof Similarity)
            hue = 0.1f + freq*0.4f;
        else if (edge.content instanceof Inheritance)
            hue = 0.4f + freq*0.4f;
        
        return Color.HSBtoRGB(hue, 0.5f + conf * 0.5f, 0.5f + conf * 0.5f);
    }
    

    @Override
    public float getEdgeThickness(final Sentence edge, final VertexDisplay source, final VertexDisplay target) {
        float c = edge.truth.getConfidence();
        return (0.1f + c) * 14f;
    }

    @Override
    public float getNodeSize(final Term v) {        
        float b = 10f * (0.2f + 2 * (float) GraphExecutive.getActualPriority(sgraph.memory, v));
        return b;
    }
    
    
    

    
}
