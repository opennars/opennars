/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph.display;

import automenta.vivisect.TreeMLData;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import automenta.vivisect.timeline.LineChart;
import automenta.vivisect.timeline.StackedPercentageChart;
import automenta.vivisect.timeline.TimelineVis;
import java.awt.Color;
import java.util.Map;
import java.util.WeakHashMap;

/**
 *
 * @author me
 */
public class NeuralTimestream<V, E> implements GraphDisplay<V,E>  {
    
    public class NeuralStream {
        public final TimelineVis timeline;
        private final VertexVis<V, E> neuron;
        private final TreeMLData activation, signal;
        private final V vertex;

        Color a = new Color(0.35f, 1.0f, 0.35f, 0.85f);
        Color b = new Color(0.35f, 1.0f, 0.35f, 0.85f);
        
        public NeuralStream(VertexVis<V,E> neuron) {
            this.neuron = neuron;
            this.vertex = neuron.getVertex();
            this.activation = new TreeMLData("Act", a, 64);
            this.signal = new TreeMLData("Sig", b, 64);
            this.timeline = new TimelineVis(
                new LineChart(activation, signal).size(0.25f, 4f),
                new StackedPercentageChart(activation, signal).size(0.25f, 1f)
            );
            update();
        }
        
        public void update() {
//        
//        Set<String> keys = v.getPropertyKeys();
//        
          double signal = 0; //vertexProperty(vertex, "signal", 0.5d);
          double activity = 0; //property(vertex, "activity", 0d);
//        String layer = property(v, "layer", v.toString());
//
//        float total = (float)(signal/4f + activity);
//        
//        vv.radius = baseSize/2f + (float)(baseSize * total);
//        
//        vv.color = Video.getColor(layer.hashCode(), 0.75f, 0.75f, 0.75f).getRGB();
//        vv.label = layer;
//        vv.speed = 0.1f;
//        vv.strokeColor = strokeColor;                
//        if (signal != 0)
//            vv.stroke = Math.abs( signal.floatValue() ) * 5f;
//        else
//            vv.stroke = 0;

            this.activation.push(activity);
            this.signal.push(signal);
        }
    }
    
    Map<VertexVis<V,E>,NeuralStream> streams = new WeakHashMap();
    
//    public <T> T property(Element e, String id, T deefault) {
//        if (e.getPropertyKeys().contains(id)) 
//            return e.getProperty(id);
//        return deefault;
//    }    
    
    public boolean isNeuron(V v) {
        //return property(v, "layer", null) != null;
        return true;
    }
    
    @Override
    public void vertex(final AbstractGraphVis<V,E> g, final VertexVis<V,E> vv) {
        
        V v = vv.getVertex();
        if (!isNeuron(v))
            return;
        
        if (!streams.containsKey(vv)) {
            NeuralStream ns = new NeuralStream(vv);
            streams.put(vv, ns);
            vv.children.add(ns.timeline);
        }
        else {
            streams.get(vv).update();
        }



    }
    

    @Override
    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> ee) {
        
    }
    

    @Override
    public boolean preUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }

    @Override
    public boolean postUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }


}
