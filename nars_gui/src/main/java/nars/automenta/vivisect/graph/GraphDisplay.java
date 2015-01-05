/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph;

/**
 *
 * @author me
 */
public interface GraphDisplay<V,E> {

    default public boolean preUpdate(AbstractGraphVis<V,E> g) {
        return true;
    }

    public void vertex(AbstractGraphVis<V,E> g, VertexVis<V,E> v);
    public void edge(AbstractGraphVis<V,E> g, EdgeVis<V,E> e);

    default public boolean postUpdate(AbstractGraphVis<V,E> g) {
        return true;
    }
    
    public static enum Shape { Rectangle, Ellipse };

//    
//    public Shape getVertexShape(V v);
//    public String getVertexLabel(final V v);
//    
//    /** return 0 to hide vertex */
//    public float getVertexSize(final V v);
//    
//    public int getVertexColor(V o);
//    public float getEdgeThickness(E edge, VertexVis source, VertexVis target);
//    public int getEdgeColor(E e);
//    public int getTextColor(V v);
//
//    public int getVertexStrokeColor(V v);
//    public float getVertexStroke(V v);
}
