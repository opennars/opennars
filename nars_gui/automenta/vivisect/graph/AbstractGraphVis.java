package automenta.vivisect.graph;



import automenta.vivisect.Vis;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import static processing.core.PApplet.radians;
import static processing.core.PConstants.PROJECT;
import static processing.core.PConstants.SQUARE;
import processing.core.PGraphics;

/**
 *
 * @author me
 */
abstract public class AbstractGraphVis<V, E> implements Vis {

    

    Map<V, VertexVis<V,E>> vertices = new HashMap();
    Map<E, EdgeVis<V,E>> edges = new HashMap();
    Set<V> deadVertices = new HashSet();
    Set<E> deadEdges = new HashSet();
    Map<Object, Integer> edgeColors = new HashMap(16);

    Graph<V,E> currentGraph;
    boolean showSyntax;

    boolean updateNext = true;
    
    //bounds of last positioned vertex
    float minX = 0f, minY = 0f, maxX = 0f, maxY = 0f;
    
    private GraphDisplay<V,E> display;


    int maxNodesWithLabels = 5000;
    int maxNodes = 5000;
    int maxEdgesWithArrows = 10000;
    int maxEdges = 10000;

    float nodeSpeed = 0.1f;

    float sx = 800f;
    float sy = 800f;

    float arrowHeadScale = 1f/16f;
    static final float vertexTargetThreshold = 4f;

    
    public AbstractGraphVis(GraphDisplay display) {
        super();
        this.display = display;       
    }

    public void setUpdateNext() {
        updateNext = true;
    }
    
    public void setDisplay(GraphDisplay display) {
        this.display = display;
    }

    public GraphDisplay getDisplay() {
        return display;
    }
    

    public VertexVis getVertexDisplay(V v) {
        return vertices.get(v);
    }
    


    public VertexVis updateVertex(final V o) {        
        
        deadVertices.remove(o);
        
        VertexVis v = vertices.get(o);      
        if (v != null) {
            display.vertex(this, v);
            return v;
        }
        
        v = new VertexVis(o);
        vertices.put(o, v);

        return v;
    }

    public EdgeVis updateEdge(final E o) {        
        
        deadEdges.remove(o);
        
        EdgeVis v = edges.get(o);      
        if (v != null) {
            display.edge(this, v);
            return v;
        }
        
        v = new EdgeVis(o);
        edges.put(o, v);

        return v;
    }
    

    public abstract Graph<V,E> getGraph();
    
    abstract protected boolean hasUpdate();
    
    /**
     * called from NAR update thread, not swing thread
     */
    public void updateGraph() {
        
            
        if (hasUpdate() || (updateNext) || display.preUpdate(this)) {

            updateNext = false;

            synchronized (vertices) {
                deadVertices.clear();
                
                

                currentGraph = getGraph();
                if (currentGraph == null) {
                    vertices.clear();
                    edges.clear();
                    return;
                }
                
                deadVertices.addAll(vertices.keySet());
                for (final V v : currentGraph.vertexSet())
                   updateVertex(v);                  
                deadEdges.addAll(edges.keySet());
                for (final V v : deadVertices)
                    vertices.remove(v);
                
                
                for (final E e : currentGraph.edgeSet())
                    updateEdge(e);                
                for (final E e : deadEdges)
                    edges.remove(e);
            }

        }
    }

    @Override
    public boolean draw(PGraphics g) {

        //long start = System.nanoTime();
        
        if (currentGraph == null) {
            return true;
        }

        synchronized (vertices) {
            //for speed:
            g.strokeCap(SQUARE);
            g.strokeJoin(PROJECT);

            /*boolean changed = false;*/
            
            int numEdges = currentGraph.edgeSet().size();
            if (numEdges < maxEdges) {
                
                for (final EdgeVis d : edges.values()) {
                    /*changed |= */d.draw(this, g);
                }
                
            }
            
            g.noStroke();

            int numNodes = vertices.size();
            boolean text = numNodes < maxNodesWithLabels;
            if (numNodes < maxNodes) {
                for (final VertexVis d : vertices.values()) {
                    /*changed |= */d.draw(this, g);
                }
            }
            //drawn = !changed;

        }
 
        /*long end = System.nanoTime();
        float time = end - start;
        if (currentGraph!=null)
            System.out.println(time + "ns for " + currentGraph.vertexSet().size() + "|" + currentGraph.edgeSet().size());
        */
     
        display.postUpdate(this);

        return true;
    }
    
    
    public void resurrectVertex(V v) {
        deadVertices.remove(v);        
    }
    

 
    void drawArrowAngle(final PGraphics g, final float cx, final float cy, final float len, final float angle, float arrowHeadRadius) {        
        g.pushMatrix();
        g.translate(cx, cy);
        g.rotate(radians(angle));
        g.line(0, 0, len, 0);
        g.line(len, 0, len - arrowHeadRadius, -arrowHeadRadius/2f);
        g.line(len, 0, len - arrowHeadRadius, arrowHeadRadius/2f);
        g.popMatrix();
    }

    
    void drawArrow(final PGraphics g, final float x1, final float y1, final float x2, final float y2) {
        float cx = (x1 + x2) / 2f;
        float cy = (y1 + y2) / 2f;
        float len = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float a = (float) (Math.atan2(y2 - y1, x2 - x1) * 180.0f / Math.PI);

        drawArrowAngle(g, x1, y1, len, a, len * arrowHeadScale /* nodeSize/16f*/);
    }

//    void drawLine(final float x1, final float y1, final float x2, final float y2) {
//        line(x1, y1, x2, y2);
//    }

}
