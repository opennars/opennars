/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package automenta.vivisect.dimensionalize;

import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author me
 */
public class HyperassociativeLayout implements GraphDisplay {

    HyperassociativeMap h = null;
        float spcing = 100.0f;

    @Override
    public boolean preUpdate(AbstractGraphVis g) {
        
        if (h == null)
            h = new HyperassociativeMap(g.getGraph(), 2);        
        else
            h.setGraph(g.getGraph());
        
        h.align();
        return true;
    }
    
    
    @Override
    public void vertex(AbstractGraphVis g, VertexVis v) {
        if (h == null) return;
        if (v == null) return;
        if (v.vertex == null) return;
        if (h.getCoordinates()==null) return;
                
        
        ArrayRealVector c = (ArrayRealVector) h.getCoordinates().get(v.vertex); 
        if (c==null) return;
        
        
        double[] cc = c.getDataRef();
        v.tx = (float)cc[0] * spcing;
        v.ty = (float)cc[1] * spcing;
        
    }

    @Override
    public void edge(AbstractGraphVis g, EdgeVis e) {
    }
    
}
