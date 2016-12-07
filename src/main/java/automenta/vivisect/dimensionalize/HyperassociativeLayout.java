/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package automenta.vivisect.dimensionalize;

import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import nars.entity.Concept;
import nars.entity.TaskLink;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author me
 */
public class HyperassociativeLayout implements GraphDisplay {

    HyperassociativeMap h = null;
    float spcing = 200.0f;
    
    private AtomicBoolean newNode = new AtomicBoolean(false);
    
    @Override
    public boolean preUpdate(AbstractGraphVis g) {
        
        
        if (h == null)
            h = new HyperassociativeMap(g.getGraph(), HyperassociativeMap.Euclidean, 2) {
                @Override
                protected ArrayRealVector newNodeCoordinates(Object node) {
                    newNode.set(true);
                    return super.newNodeCoordinates(node);
                }

            @Override
            public double getEdgeWeight(Object e) {
                if (e instanceof TaskLink) {                   
                    return 1.0 + ((TaskLink)e).getBudget().getPriority() * 1.0;                }
                return 1;
            }

                
            
                
            @Override
            public double getRadius(Object n) {
                if (n instanceof Concept) {
                    return 1.0 + ((Concept)n).getBudget().getPriority() * 1.0;                }
                return 1;
            }

                
            };    
        else {
            if (newNode.get()) {
                h.resetLearning();
                newNode.set(false);
            }
            
            h.setGraph(g.getGraph());
        }
        
        h.align();
        return true;
    }
    
    
    @Override
    public void vertex(AbstractGraphVis g, VertexVis v) {
        if (h == null) return;
        if (v == null) return;
        if (v.vertex == null) return;                
        
        ArrayRealVector c = h.getPosition(v.vertex); 
        if (c==null) return;
        
        
        double[] cc = c.getDataRef();
        v.tx = (float)cc[0] * spcing;
        v.ty = (float)cc[1] * spcing;
        
    }

    @Override
    public void edge(AbstractGraphVis g, EdgeVis e) {
    }
    
}
