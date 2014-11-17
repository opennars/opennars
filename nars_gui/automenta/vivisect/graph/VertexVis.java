/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph;

import automenta.vivisect.Vis;
import automenta.vivisect.graph.GraphDisplay.Shape;
import static automenta.vivisect.graph.GraphDisplay.Shape.Ellipse;
import automenta.vivisect.timeline.TimelineVis;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import processing.core.PGraphics;

/**
 *
 * @author me
 */
public class VertexVis<V, E> {
    public final V vertex;
    public float x;
    public float y;
    public float tx;
    public float ty;
    public float radius;
    public float stroke;
    public float scale;
    public String label;
    public int color;
    public int textColor;
    public float textScale;
    public int strokeColor;
    public Set<E> edges;
    public Shape shape;
    public float speed;
    public List<Vis> children = null;

    public VertexVis(V o) {
        this.vertex = o;
        x = y = 0;
        tx = x;
        ty = y;
        stroke = 0;
        strokeColor = 0;
        speed = 0.9f;
        scale = 1f;
        textScale = 1f;
    }

    @Override
    public String toString() {
        return "vis[" + vertex.toString() + "]";
    }

    
            
    @Override
    public int hashCode() {
        return vertex.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return vertex.equals(obj);
    }

    public boolean draw(final AbstractGraphVis c, final PGraphics p) {
        
        boolean needsUpdate = update(c);
        //System.out.println(radius + " " + color + " " + label + " " + x + " " + y);
        
        float r = radius * scale;
        if (r == 0) {
            return needsUpdate;
        }
        
        if (stroke > 0) {
            p.stroke(strokeColor);
            p.strokeWeight(stroke * scale);
        }
        p.fill(color); //, alpha * 255 / 2);
        if (shape == null) shape = Ellipse;
        switch (shape) {
            case Rectangle:
                p.rect(x * scale - r / 2f, y * scale - r / 2f, r, r);
                break;
            case Ellipse:
            default:
                p.ellipse(x * scale, y * scale, r, r);
                break;
        }
        
        if ((label != null) && (textScale > 0)) {
            p.fill(textColor); //, alpha * 255 * 0.75f);
            p.textSize(r / 2f * textScale);
            p.text(label, x * scale, y * scale);
        }
        
        if (stroke > 0) {
            //reset stroke
            p.noStroke();
        }
        
        
        if ((children!=null) && (!children.isEmpty())) {
            p.pushMatrix();
            p.translate(x*scale, y*scale);
            p.scale(radius/32f, radius/32f);
            
            for (int i = 0; i < children.size(); i++) {
                final Vis child = children.get(i);
                child.draw(p);
            }
            p.popMatrix();
        }
        
        return needsUpdate;
    }

    protected boolean update(AbstractGraphVis c) {
        if (this.edges == null) {
            if (c.currentGraph != null) {
                this.edges = c.currentGraph.edgesOf(vertex);
            }
        }
        x = (x * (1.0f - speed) + tx * (speed));
        y = (y * (1.0f - speed) + ty * (speed));
        if ((Math.abs(tx - x) + Math.abs(ty - y)) > AbstractGraphVis.vertexTargetThreshold) {
            //keep animating if any vertex hasnt reached its target
            return false;
        }
        return true;
    }

    public Set<E> getEdges() {
        return edges;
    }

    public V getVertex() {
        return vertex;
    }

//    void update(final V o) {
//        GraphDisplay<V, E> d = canvas.display;
//        this.edges = null;
//        this.textColor = d.getTextColor(o);
//        this.radius = d.getVertexSize(o);
//        this.color = d.getVertexColor(o);
//        this.label = d.getVertexLabel(o);
//        this.stroke = d.getVertexStroke(o);
//        this.strokeColor = d.getVertexStrokeColor(o);
//    }

    public void setPosition(final float x, final float y) {
        this.tx = x;
        this.ty = y;
    }

    public void movePosition(final float dx, final float dy) {
        this.tx += dx;
        this.ty += dy;
    }

    public float getX() {
        return tx;
    }

    public float getY() {
        return ty;
    }

    public float getRadius() {
        return radius;
    }

    public void addChild(TimelineVis timeline) {
        if (children == null)
            children = new ArrayList();
        children.add(timeline);
    }
    
}
