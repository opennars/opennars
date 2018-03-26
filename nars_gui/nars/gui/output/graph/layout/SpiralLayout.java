/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph.layout;

import automenta.vivisect.Video;
import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import nars.main.NAR;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Task;

/**
 * Item Hash = theta, Priority = radius
 */
public class SpiralLayout implements GraphDisplay<Item, Object> {
    float spacing;
    NAR nar;

    public SpiralLayout(NAR nar, float spacing) {
        this.spacing = spacing;
        this.nar = nar;
    }

    @Override
    public void vertex(AbstractGraphVis<Item, Object> g, VertexVis<Item, Object> v) {
        Item vertex = v.getVertex();

        float priority = vertex.getPriority();

        boolean task = false;
        Concept x = null;
        if(vertex instanceof Concept) {
            x = (Concept) vertex;
        }
        else
        if(vertex instanceof Task) {
            task = true;
            x = nar.memory.concept(((Task) vertex).getTerm());
        }

        int i = 0;
        try{
            for(Concept c: nar.memory.concepts) {
                if(x == c) { //not elegant and fast but k at least no term equals
                    break;
                }
                i++;
            }
        }catch(Exception ex){}
        float count_elems = nar.memory.concepts.size();
        // float ratio = 30.0f*(0.10f + (((float)priority) / (1.0f)));
        float ratio = 30.0f*(0.10f + (((float)i) / (count_elems)));
        v.tx = (float) (ratio*Math.cos(ratio)) * spacing;
        v.ty = (float) (ratio*Math.sin(ratio)) * spacing;
        
        if(task) {
            v.ty += spacing*Video.hashFloat(vertex.hashCode());
        }
        
    }

    @Override
    public void edge(AbstractGraphVis<Item, Object> g, EdgeVis<Item, Object> e) {

    }

}
