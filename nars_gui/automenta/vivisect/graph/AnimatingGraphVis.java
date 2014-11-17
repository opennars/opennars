package automenta.vivisect.graph;

import org.jgrapht.Graph;
import processing.core.PGraphics;

/**
 *
 * @author me
 */


public class AnimatingGraphVis<V,E> extends AbstractGraphVis<V,E> {
    Graph<V, E> graph;    

    public AnimatingGraphVis(Graph<V,E> graph, GraphDisplay<V,E>... displays) {
        this(graph, new GraphDisplays<V,E>(displays));
    }
    
    public AnimatingGraphVis(Graph<V,E> graph, GraphDisplay<V,E> display) {
        super(display);
        
        this.graph = graph;
                
        updateGraph();
        setUpdateNext();
    }

    
    @Override
    public Graph<V,E> getGraph() {
        return this.graph;
    }


    @Override
    public boolean draw(PGraphics g) {
        updateGraph();
        
        return super.draw(g);
    }
    

    
    @Override
    protected boolean hasUpdate() {
        return false;
    }

    
    
}
