package nars.gui.output.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.core.Events;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.gui.NPanel;
import nars.language.Term;
import nars.util.graph.SentenceGraph;
import nars.util.graph.SentenceGraph.GraphChange;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


public class SentenceGraphPanel extends NPanel implements Observer {
    
    private final SentenceGraph sentence;
    private ProcessingGraphPanel pgraph;
    int minFrameTime = 1500; //ms
    long lastFrame = 0;
    private final NAR nar;
    private DirectedMultigraph<Term,Sentence> sentenceCopy;
    private boolean changed;

    
    public SentenceGraphPanel(NAR nar, SentenceGraph graph) {
        super(new BorderLayout());
        
        this.nar = nar;
        this.sentence = graph;        
        this.sentenceCopy = (DirectedMultigraph) sentence.clone();

        
        pgraph = new ProcessingGraphPanel<Term,Sentence>() {
            @Override public DirectedMultigraph<Term,Sentence> getGraph() {
                return sentenceCopy;
            }            

            @Override
            public int edgeColor(final Sentence edge) {
                float freq = edge.truth.getFrequency();
                float conf = edge.truth.getConfidence();
                //return PGraphPanel.getColor(edge.getClass().getSimpleName(), alpha);
                return Color.HSBtoRGB(0.2f + freq*0.5f, 0.8f, 0.3f + conf * 0.7f);
            }

            @Override
            public float edgeWeight(final Sentence edge) {
                //edge.truth.getFrequency() * edge.truth.getConfidence() *14f;
                float w = (float)graph.getEdgeWeight(edge);
                if (w!=0)
                    return 1.0f / (w) * 14f;
                else
                    return 1;
            }

            @Override
            public int vertexColor(final Term vertex) {
                return PGraphPanel.getColor(vertex.getClass());
                //return Color.getHSBColor((float)hue,0.7f,0.8f).getRGB();        
            }
        };
        
        add(pgraph, BorderLayout.CENTER);
        
    }

    final Runnable update = new Runnable() {

        @Override
        public void run() {
            pgraph.update();
            pgraph.redraw();
            lastFrame = System.currentTimeMillis();
        }
            
    };
    
    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            sentence.event.on(GraphChange.class, this);
            nar.on(Events.FrameEnd.class, this);
            this.sentence.start();
        }
        else {
            sentence.event.off(GraphChange.class, this);
            nar.off(Events.FrameEnd.class, this);
            this.sentence.stop();
        }
    }
    
    @Override
    public void event(Class event, Object[] arguments) {
        if ((event == Events.FrameEnd.class) && (changed) && (sentence!=null)) {
            changed = false;
            
            long now = System.currentTimeMillis();
            if ((pgraph!=null) && (now - lastFrame > minFrameTime)) {
                sentenceCopy = (DirectedMultigraph) sentence.clone();
                SwingUtilities.invokeLater(update);
            }            
            
        }
        else {
            //graph add/remove event
            changed = true;
        }
    }
    
}
