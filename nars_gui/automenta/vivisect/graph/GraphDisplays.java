/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.graph;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author me
 */
public class GraphDisplays<V,E> implements GraphDisplay<V,E> {
    
    final public List<GraphDisplay<V,E>> sequence;

    public GraphDisplays(GraphDisplay<V,E>... d) {
        sequence = new CopyOnWriteArrayList( Lists.newArrayList(d) );
    }

    @Override
    public boolean preUpdate(final AbstractGraphVis<V, E> g) {
        int n = sequence.size();
        boolean allTrue = true;
        for (int i = 0; i < n; i++) {
            allTrue &= sequence.get(i).preUpdate(g);
        }
        return allTrue;
    }

    @Override
    public void vertex(final AbstractGraphVis<V, E> g, final VertexVis<V, E> v) {
        int n = sequence.size();       
        for (int i = 0; i < n; i++) {
            sequence.get(i).vertex(g, v);
        }        
    }

    @Override
    public void edge(final AbstractGraphVis<V, E> g, final EdgeVis<V, E> e) {
        int n = sequence.size();
        for (int i = 0; i < n; i++) {
            sequence.get(i).edge(g, e);
        }
    }

    @Override
    public boolean postUpdate(final AbstractGraphVis<V, E> g) {
        int n = sequence.size();
        boolean allTrue = true;
        for (int i = 0; i < n; i++) {
            allTrue &= sequence.get(i).postUpdate(g);
        }
        return allTrue;
    }
    
    
}
