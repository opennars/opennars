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
public class CircleLayout implements GraphDisplay<Item, Object> {

    //# of radians to cover
    float arcStart, arcStop;
    float spacing;
    NAR nar;

    public CircleLayout(NAR nar, float arcStart, float arcStop, float spacing) {
        this.arcStart = arcStart;
        this.arcStop = arcStop;
        this.spacing = spacing;
        this.nar = nar;
    }

    @Override
    public void vertex(AbstractGraphVis<Item, Object> g, VertexVis<Item, Object> v) {
        Item vertex = v.getVertex();

        float priority = vertex.getPriority();
        double radius = (1.0 - priority) * spacing + 8;

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

        float angle = ((arcStop - arcStart) * Video.hashFloat(x.hashCode()) + arcStart) * ((float) Math.PI * 2f);
        v.tx = (float) (Math.cos(angle) * radius) * spacing;
        v.ty = (float) (Math.sin(angle) * radius) * spacing;

        if(task) {
            v.ty += spacing*Video.hashFloat(vertex.hashCode());
        }
    }

    @Override
    public void edge(AbstractGraphVis<Item, Object> g, EdgeVis<Item, Object> e) {

    }

}
