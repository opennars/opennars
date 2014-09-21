package nars.gui.output.graph;

import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import nars.core.EventEmitter.Observer;
import nars.gui.NPanel;
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


    
    public SentenceGraphPanel(SentenceGraph graph) {
        super(new BorderLayout());
        
        this.sentence = graph;

        pgraph = new ProcessingGraphPanel() {
            @Override public DirectedMultigraph getGraph() {
                synchronized (update) {
                    return (DirectedMultigraph) sentence.clone();
                }
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
            this.sentence.start();
        }
        else {
            sentence.event.off(GraphChange.class, this);
            this.sentence.stop();
        }
    }
    
    @Override
    public void event(Class event, Object[] arguments) {
        long now = System.currentTimeMillis();
        if ((pgraph!=null) && (now - lastFrame > minFrameTime)) {
            SwingUtilities.invokeLater(update);
        }
    }
    
}
