/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph.display;

import automenta.vivisect.Video;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.VertexVis;

/**
 *
 * @author me
 */
abstract public class NeuralDisplay<V, E> extends DefaultDisplay<V,E>  {

    float baseSize = 32;
    final int strokeColor = Video.color(255, 255, 255, 70);
    final int defaultEdgeColor = Video.color(127,127,127,255);

    abstract public <T> T vertexProperty(V e, String id, T deefault);
    abstract public <T> T edgeProperty(E e, String id, T deefault);
    /*{
        if (e.getPropertyKeys().contains(id)) 
            return e.getProperty(id);
        return deefault;
    } */   
    
    
    @Override
    public void vertex(final AbstractGraphVis<V,E> g, final VertexVis<V,E> vv) {
        super.vertex(g, vv);

        V v = vv.getVertex();
        
        Double signal = vertexProperty(v, "signal", 0.5d);
        Double activity = vertexProperty(v, "activity", 0d);
        String layer = vertexProperty(v, "layer", v.toString());

        float total = (float)(signal/4f + activity);
        
        vv.radius = baseSize/2f + (float)(baseSize * Math.abs(total));
        
        vv.color = Video.getColor(layer.hashCode(), 0.75f, 0.75f, 0.75f);
        vv.label = layer;
        vv.speed = 0.1f;
        vv.strokeColor = strokeColor;                
        if (signal != 0)
            vv.stroke = Math.abs( signal.floatValue() ) * 5f;
        else
            vv.stroke = 0;


    }
    
    

    @Override
    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> ee) {
        super.edge(g, ee); //To change body of generated methods, choose Tools | Templates.

        E e = ee.edge;
        
        Double x = edgeProperty(e, "signal", null);
        
        if (x == null)
            ee.color = defaultEdgeColor;
        else {
            float ax = (float)Math.abs(x);
            float intensity = 255f * (ax/2f + 0.5f);
            if (x < 0) {
                ee.color = Video.color(0, 0, intensity, intensity);
            }
            else {            
                ee.color = Video.color(0f, intensity, 0f, intensity);            
            }     
        }

        Double w = edgeProperty(e, "weight", 0.2d);
        float ax = (float)Math.abs(w);
        ee.thickness = 10f*ax + 20f;
        
    }
    
    
    
}
