package nars.gui.output;

import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.gui.NPanel;
import nars.util.graph.ImplicationGraph;
import org.jgrapht.graph.DirectedMultigraph;

/**
 *
 * @author me
 */


public class ImplicationGraphPanel extends NPanel {
    private final NAR nar;
    private ImplicationGraph implication;
    private ProcessingGraphPanel pgraph;
    int minFrameTime = 1500; //ms
    long lastFrame = 0;

    public ImplicationGraphPanel(NAR n) {
        super(new BorderLayout());
        
        this.nar = n;
        
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
            this.implication = new ImplicationGraph(nar) {

                @Override
                public void add(Sentence s) {
                    synchronized (update) {
                        super.add(s);
                        long now = System.currentTimeMillis();
                        if ((pgraph!=null) && (now - lastFrame > minFrameTime)) {
                            SwingUtilities.invokeLater(update);
                        }
                    }
                }

                @Override
                public void remove(Sentence s) {
                    synchronized (update) {
                        super.remove(s);
                        long now = System.currentTimeMillis();
                        if ((pgraph!=null) && (now - lastFrame > minFrameTime)) {
                            SwingUtilities.invokeLater(update);
                        }
                    }
                }
                
                
            };

            pgraph = new ProcessingGraphPanel() {
                @Override public DirectedMultigraph getGraph() {
                    synchronized (update) {
                        return (DirectedMultigraph) implication.clone();
                    }
                }            
            };
            add(pgraph, BorderLayout.CENTER);
            
        }
        else {
            this.implication.stop();
        }
    }
    
    
}
