package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//import automenta.vivisect.graph.AnimatedProcessingGraphCanvas;
//import nars.entity.Sentence;
//import nars.language.Term;
//import nars.util.graph.SentenceGraph;
//
///**
// *
// * @author me
// */
//
//
//public class SentenceGraphCanvas extends AnimatedProcessingGraphCanvas<Term,Sentence> {
//    private final SentenceGraph sgraph;
//    
//    
//
//    public SentenceGraphCanvas(SentenceGraph g) {
//        super(g, new NARGraphDisplay());
//        this.sgraph = g;
//    }
//
//
////    @Override
////    public int getEdgeColor(final Sentence edge) {
////        float freq = edge.truth.getFrequency();
////        float conf = edge.truth.getConfidence();
////        
////        float hue = 0.5f;
////        if (edge.content instanceof Similarity)
////            hue = 0.1f + freq*0.4f;
////        else if (edge.content instanceof Inheritance)
////            hue = 0.4f + freq*0.4f;
////        
////        return Color.HSBtoRGB(hue, 0.5f + conf * 0.5f, 0.5f + conf * 0.5f);
////    }
////    
////
////    @Override
////    public float getEdgeThickness(final Sentence edge, final VertexDisplay source, final VertexDisplay target) {
////        float c = edge.truth.getConfidence();
////        return c * super.getEdgeThickness(edge, source, target);
////    }
////
////    @Override
////    public float getNodeSize(final Term v) {        
////        return (float) GraphExecutive.getEffectivePriority(sgraph.memory, v) * super.getNodeSize(v);
////    }
////    
//    
//    
//
//    
//}
