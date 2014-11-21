/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automenta.vivisect.dimensionalize;

import automenta.vivisect.graph.AbstractGraphVis;
import automenta.vivisect.graph.EdgeVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.VertexVis;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.abego.treelayout.NodeExtentProvider;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 *
 * Also able to handle forests (multi-roots)
 */
public class AbegoTreeLayout<V,E> implements GraphDisplay<V,E> {

    boolean finished = false;
    
    @Override
    public boolean preUpdate(AbstractGraphVis<V, E> g) {
        return true;
    }

    @Override
    public void vertex(AbstractGraphVis<V, E> g, VertexVis<V, E> v) {
    }

    @Override
    public void edge(AbstractGraphVis<V, E> g, EdgeVis<V, E> e) {
    }

    @Override
    public boolean postUpdate(AbstractGraphVis<V, E> g) {
        
        if (finished)
            return true;
        
        float px = 0, py = 0;
        float horizontalMargin = 16f;
        float levelGap = 32f;
        float nodeGap = 8f;
        
        DirectedGraph<V, E> graph = (DirectedGraph<V,E>)g.getGraph();
        
        ArrayList<V> roots = new ArrayList<V>();
        for (V v: graph.vertexSet())
            if (graph.inDegreeOf(v) == 0)
                roots.add(v);

        KruskalMinimumSpanningTree<V,E> st = new KruskalMinimumSpanningTree<V,E>(graph);
        
        //Set<V> processed = new HashSet();
        
        for (V r : roots) {
            
            DefaultTreeForTreeLayout<VertexVis<V,E>> tree = new DefaultTreeForTreeLayout(g.getVertexDisplay(r));
            
            DirectedMultigraph<V,E> subgraph = new DirectedMultigraph(DefaultEdge.class);
            subgraph.addVertex(r);
            //add all other vertices which are not roots
            for (V v: graph.vertexSet()) {
                if (!roots.contains(v))
                    subgraph.addVertex(v);
            }
            for (E e : st.getMinimumSpanningTreeEdgeSet()) {
                V from = graph.getEdgeSource(e);
                V to = graph.getEdgeTarget(e);
                //if (processed.contains(from) || processed.contains(to))
                  //  continue;
                if (!subgraph.containsVertex(from) || !subgraph.containsVertex(to))
                    continue;
                //processed.add(from);
                //processed.add(to);
                subgraph.addEdge(from, to, e);
            }
            
            BreadthFirstIterator<V, E> i = new BreadthFirstIterator<V,E>(subgraph, r) {
                
                @Override
                protected void encounterVertex(V parent, E edge) {
                    super.encounterVertex(parent, edge);
                    
                    VertexVis p = g.getVertexDisplay(parent);
                    
                    Set<E> children = graph.outgoingEdgesOf(parent);
                    VertexVis[] cv = new VertexVis[children.size()];
                    int j = 0;
                    for (E ec : children) {
                        V vc = graph.getEdgeTarget(ec);
                        cv[j++] = g.getVertexDisplay(vc);
                    }

                    //System.out.println("vertex -> " + parent + " " + Arrays.toString(cv));
                    
                    try {
                        tree.addChildren(p, cv);
                    }
                    catch (Exception e) { 
                        System.err.println(e);
                        System.err.println("vertex -> " + parent + " " + Arrays.toString(cv));
                    }
                    
                }
            };
            while (i.hasNext()) i.next();                                  

            TreeLayout<VertexVis<V,E>> treeLayout = new TreeLayout(tree, new NodeExtentProvider<VertexVis<V,E>>() {
                
                @Override
                public double getWidth(VertexVis<V, E> tn) {
                    return tn.getRadius();
                }

                @Override
                public double getHeight(VertexVis<V, E> tn) {
                    return tn.getRadius();
                }

                

            }, new DefaultConfiguration(levelGap,nodeGap));
            
            Map<VertexVis<V, E>, Rectangle2D.Double> bounds = treeLayout.getNodeBounds();
            for (Map.Entry<VertexVis<V, E>, Rectangle2D.Double> vv : bounds.entrySet()) {
                VertexVis<V, E> v = vv.getKey();
                Rectangle2D.Double b = vv.getValue();
                v.setPosition(px + (float)b.getCenterX(), py + (float)b.getCenterY());
            }
            //System.out.println(bounds);
            
            px += treeLayout.getBounds().getWidth() + horizontalMargin;
            
            
        }
    
        finished = true;
        
        return true;
        
    }
    
}
