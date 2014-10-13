package nars.gui.output.graph;


import java.awt.Color;
import nars.entity.Sentence;
import nars.inference.GraphExecutive;
import nars.language.Interval;
import nars.language.Term;
import nars.util.graph.ImplicationGraph;
import org.jgrapht.graph.DirectedMultigraph;



public class ImplicationGraphCanvas extends AnimatedProcessingGraphCanvas<Term,Sentence> {
    private final GraphExecutive graphExec;
    private double maxTermActivation = 0;
    private double maxSentenceActivation = 0;

    public ImplicationGraphCanvas(GraphExecutive g) {
        super(g.implication);
        this.graphExec = g;        
        
    }

    @Override
    protected DirectedMultigraph<Term,Sentence> getGraph() {
        if (graph!=null)
            return new ImplicationGraph(graphExec.implication, false, minPriority);
        
        //otherwise, should override in subclasses
        return null;
    }    
    @Override
    public void updateGraph() {
        maxSentenceActivation = graphExec.fadeAccumulatedSentences(0.95);
        maxTermActivation = graphExec.fadeAccumulatedTerms(0.95);

        super.updateGraph();        
    }
    

    @Override
    public int getEdgeColor(Sentence e) {
        float a = (float)graphExec.getSentenceRelevancy(e);
        if (a > 1.0f) a = 1.0f;
        if (a < 0f) a = 0f;

        float activation = 0;
        Double A = graphExec.accumulatedSentence.get(e);
        if (A!=null)
            activation = A.floatValue();
        
        return Color.HSBtoRGB(0.4f * activation*0.1f, activation, 0.5f+ 0.5f * a);
    }
    
    @Override
    public float getEdgeThickness(Sentence edge, VertexDisplay source, VertexDisplay target) {
        
        float ww = (float)graphExec.getSentenceRelevancy(edge);
        if (ww < 1.0f) ww = 1.0f;
        
        
        if (maxSentenceActivation > 0) {
            Double A = graphExec.accumulatedSentence.get(edge);
            if (A!=null) {
                double a = A / maxSentenceActivation;
                ww += (float)a;
            }
        }
        return super.getEdgeThickness(edge, source, target) * ww;
    }
    
    @Override
    public float getNodeSize(final Term v) {        
        return super.getNodeSize(v) * (float)GraphExecutive.getEffectivePriority(graphExec.memory, v);
    }
    
    @Override
    public int getNodeColor(final Term o) {
        if (o instanceof Interval) {
            return color(0.8f, 0.25f);
        }
        return super.getNodeColor(o);
    }
            
    
    
    

    
}
