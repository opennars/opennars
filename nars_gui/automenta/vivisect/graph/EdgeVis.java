/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph;

import processing.core.PGraphics;

/**
 *
 * @author me
 */
public class EdgeVis<V, E> {
    public final E edge;
    public VertexVis elem1 = null;
    public VertexVis elem2 = null;
    public int color;
    public float thickness;
    
    public EdgeVis(E edge) {
        this.edge = edge;
        color = 0xffffff;
        thickness = 1f;
    }

    public void draw(AbstractGraphVis c, PGraphics g) {

        if (elem1 == null) {
            elem1 = c.getVertexDisplay(c.currentGraph.getEdgeSource(edge));
            elem2 = c.getVertexDisplay(c.currentGraph.getEdgeTarget(edge));
        }
        float scale = elem1.scale;
        assert(elem2.scale == scale);
        
        if ((elem1 == null) || (elem2 == null) || (elem1 == elem2)) {
            return;
        }

        //TODO create EdgeDisplay class to cacahe these properties
        g.stroke(color);
        g.strokeWeight(thickness);

        float x1 = elem1.x*scale;
        float y1 = elem1.y*scale;
        float x2 = elem2.x*scale;
        float y2 = elem2.y*scale;

        c.drawArrow(g, x1, y1, x2, y2);

        //float cx = (x1 + x2) / 2.0f;
        //float cy = (y1 + y2) / 2.0f;
        //text(edge.toString(), cx, cy);

    }
    
    
    
}
