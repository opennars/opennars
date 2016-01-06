package nars.gui.output.graph.deprecated;

//package nars.gui.output.graph;
//
//import automenta.vivisect.swing.NPanel;
//import automenta.vivisect.swing.Swing;
//import java.awt.BorderLayout;
//import java.awt.Color;
//import javax.swing.SwingUtilities;
//import nars.event.EventEmitter.Observer;
//import nars.core.Events;
//import nars.core.NAR;
//import nars.logic.entity.Sentence;
//import nars.logic.GraphExecutive;
//import nars.logic.entity.Term;
//import nars.other.graph.SentenceGraph;
//import nars.other.graph.SentenceGraph.GraphChange;
//import org.jgrapht.graph.DirectedMultigraph;
//
///**
// *
// * @author me
// */
//public class SentenceGraphPanel extends NPanel implements Observer {
//    
//    private final SentenceGraph sentence;
//    private ProcessingGraphPanel2 pgraph;
//    int minFrameTime = 1500; //ms
//    long lastFrame = 0;
//    private final NAR nar;
//    private DirectedMultigraph<Term,Sentence> sentenceCopy;
//    private boolean changed;
//
//    
//    public SentenceGraphPanel(NAR nar, SentenceGraph graph) {
//        super(new BorderLayout());
//        
//        this.nar = nar;
//        this.sentence = graph;        
//        this.sentenceCopy = (DirectedMultigraph) sentence.clone();
//
//        
//        pgraph = new ProcessingGraphPanel2<Term,Sentence>() {
//            @Override public DirectedMultigraph<Term,Sentence> getGraph() {
//                return sentenceCopy;
//            }            
//
//            @Override
//            public int edgeColor(final Sentence edge) {
//                float freq = edge.truth.getFrequency();
//                float conf = edge.truth.getConfidence();
//                //return PGraphPanel.getColor(edge.getClass().getSimpleName(), alpha);
//                return Color.HSBtoRGB(0.2f + freq*0.5f, 0.8f, 0.5f + conf * 0.5f);
//            }
//
//            @Override
//            public float edgeWeight(final Sentence edge) {
//                //edge.truth.getFrequency() * edge.truth.getConfidence() *14f;
//                float w = (float)graph.getEdgeWeight(edge);
//                if (w!=0) {                    
//                    return 1.0f / (w) * 14f;                    
//                }
//                else
//                    return 1;
//            }
//
//            @Override
//            public int vertexColor(final Term vertex) {
//                float cp = (float)GraphExecutive.getEffectivePriority(nar.memory, vertex);
//                //float alpha = 0.5f + 0.5f * cp;
//                //return PGraphPanel.getColor(vertex.getClass().getSimpleName(), alpha);                
//                //return Color.getHSBColor((float)hue,0.7f,0.8f).getRGB();        
//                return Swing.getColor(vertex.getClass().hashCode(), cp/2f + 0.5f, 0.95f).getRGB();
//                
//            }
//        };
//        
//        add(pgraph, BorderLayout.CENTER);
//        
//    }
//
//    final Runnable update = new Runnable() {
//
//        @Override
//        public void run() {
//            pgraph.update();
//            pgraph.redraw();
//            lastFrame = System.currentTimeMillis();
//        }
//            
//    };
//    
//    @Override
//    protected void onShowing(boolean showing) {
//        if (showing) {
//            sentence.event.on(GraphChange.class, this);
//            nar.on(Events.FrameEnd.class, this);
//            this.sentence.start();
//        }
//        else {
//            sentence.event.off(GraphChange.class, this);
//            nar.off(Events.FrameEnd.class, this);
//            this.sentence.stop();
//        }
//    }
//    
//    @Override
//    public void event(Class event, Object[] arguments) {
//        if ((event == Events.FrameEnd.class) && (changed) && (sentence!=null)) {
//            
//            changed = false;
//            
//            long now = System.currentTimeMillis();
//            if ((pgraph!=null) && (now - lastFrame > minFrameTime)) {
//                sentenceCopy = (DirectedMultigraph) sentence.clone();
//                SwingUtilities.invokeLater(update);
//            }            
//            
//        }
//        else {
//            //graph add/remove event
//            changed = true;
//        }
//    }
//    
// }
