package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//
//import automenta.vivisect.graph.AnimatedProcessingGraphCanvas;
//import java.awt.Color;
//import nars.inference.GraphExecutive;
//import nars.language.Interval;
//import nars.language.Term;
//import nars.util.graph.ImplicationGraph.Cause;
//import org.jgrapht.Graph;
//
//
//
//public class ImplicationGraphCanvas extends AnimatedProcessingGraphCanvas<Term,Cause> {
//    private final GraphExecutive graphExec;
//    private double maxTermActivation = 0;
//    private double maxSentenceActivation = 0;
//    float minPriority = 0f;
//    
//    public ImplicationGraphCanvas(GraphExecutive g) {
//        super(g.implication, new NARGraphDisplay());
//        this.graphExec = g;        
//        
//    }
//
//    @Override
//    public Graph<Term,Cause> getGraph() {
//        return graphExec.implication;
//    }    
//    
//    @Override
//    public void updateGraph() {
//        maxSentenceActivation = graphExec.fadeAccumulatedSentences(0.95);        
//
//        super.updateGraph();        
//    }
//    
//
////    @Override
////    public int getEdgeColor(Cause c) {
////        float a = (float)graphExec.getCauseRelevancy(c);
////        if (a > 1.0f) a = 1.0f;
////        if (a < 0f) a = 0f;
////        
////        float activation = (float)c.getActivity();
////        
////        return Color.HSBtoRGB(0.4f * activation*0.1f, activation, 0.5f+ 0.5f * a);
////    }
////    
////    @Override
////    public float getEdgeThickness(Cause c, VertexDisplay source, VertexDisplay target) {
////        
////        float ww = (float)graphExec.getCauseRelevancy(c);
////        if (ww < 1.0f) ww = 1.0f;
////        
////        
////        if (maxSentenceActivation > 0) {            
////            double A = c.getActivity();
////            double a = A / maxSentenceActivation;
////            ww += (float)a;
////        }
////        return super.getEdgeThickness(c, source, target) * ww;
////    }
////    
////    @Override
////    public float getNodeSize(final Term v) {        
////        return super.getNodeSize(v) * (float)GraphExecutive.getEffectivePriority(graphExec.memory, v);
////    }
////    
////    @Override
////    public int getNodeColor(final Term o) {
////        if (o instanceof Interval) {
////            return color(0.8f, 0.25f);
////        }
////        return super.getNodeColor(o);
////    }
//            
//    
//    
//    
//
//    
//}
