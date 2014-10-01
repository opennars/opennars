package nars.gui.output.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.language.Term;
import org.jgrapht.graph.DirectedMultigraph;
import processing.core.PApplet;
import static processing.core.PApplet.radians;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.PROJECT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.SQUARE;
import static processing.core.PConstants.UP;

/**
 *
 * @author me
 */


abstract public class ProcessingGraphCanvas<V, E> extends PApplet {

    int mouseScroll = 0;

    Hnav hnav = new Hnav();
    Hsim hsim = new Hsim();

    float selection_distance = 10;
    public float maxNodeSize = 100f;
    float FrameRate = 25f;
    static final float boostMomentum = 0.98f;

    static final float vertexTargetThreshold = 4;

    boolean compressLevels = true;
    boolean drawn = false;

    int maxNodesWithLabels = 300;
    int maxNodes = 1000;
    int maxEdgesWithArrows = 500;
    int maxEdges = 3500;

    float minPriority = 0;

    public int mode = 0;

    boolean showBeliefs = false;

    float nodeSpeed = 0.1f;

    float sx = 800;
    float sy = 800;

    long lasttime = -1;

    boolean autofetch = true;
    static final int MAX_UNSELECTED_LABEL_LENGTH = 32;
    boolean updateNext;

    float nodeSize = 7;
    static final float boostScale = 6.0f;

    float lineAlpha = 0.75f;
    float lineWidth = 3.8f;

    Map<V, VertexDisplay> vertices = new HashMap();
    Set<V> deadVertices = new HashSet();
    Map<Object, Integer> edgeColors = new HashMap(16);

    DirectedMultigraph<V,E> currentGraph;
    boolean showSyntax;

    //bounds of last positioned vertices
    float minX = 0, minY = 0, maxX = 0, maxY = 0;
    float motionBlur = 0.0f;

    public ProcessingGraphCanvas() {
        super();
        init();
    }
    
    public float getNodeSize(final V v) {
        //TODO normalize so it = 1
        return 10f;
    }
    public int getNodeColor(V o) {
        return PGraphPanel.getColor(o.getClass());
    }

    public float getEdgeThickness(E edge, VertexDisplay source, VertexDisplay target) {
        return (source.radius + target.radius) / 2.0f / lineWidth;
    }

    public static class VertexDisplay<V> {

        float x, y, tx, ty;
        int color;
        float radius;
        float alpha;
        String label;
        final V object;
        float boost;
        float stroke;
        boolean visible;

        public VertexDisplay(V o) {
            this.object = o;

            x = y = 0;
            tx = x;
            ty = y;
            stroke = 0;
            visible = true;

            if (o instanceof Concept) {
                label = ((Concept) o).term.toString();
            } else {
                label = o.toString();
            }

            if (label.length() > MAX_UNSELECTED_LABEL_LENGTH) {
                label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH - 3) + "..";
            }

            update(o, 1, 1);

        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return object.equals(obj);
        }

        public boolean draw(PApplet p, boolean text, float nodeSize, float nodeSpeed) {
            boolean needsUpdate = update(nodeSpeed);

            if (!visible) {
                return needsUpdate;
            }

            /*if (stroke > 0) {
             stroke(Color.WHITE.getRGB());
             strokeWeight(stroke);
             }*/
            float r = ((radius*nodeSize) + boost * boostScale) / 2f;
            if (r == 0)
                return false;

            p.fill(color, alpha * 255 / 2);

            p.ellipse(x, y, r, r);

            if (text && (label != null)) {
                p.fill(255, 255, 255, alpha * 255 * 0.75f);
                p.textSize(r / 2);
                p.text(label, x, y);
            }

            /*if (stroke > 0) {                
             //reset stroke
             noStroke();
             }*/
            return needsUpdate;
        }

        protected boolean update(final float nodeSpeed) {
            x = (x * (1.0f - nodeSpeed) + tx * (nodeSpeed));
            y = (y * (1.0f - nodeSpeed) + ty * (nodeSpeed));

            if ((Math.abs(tx - x) + Math.abs(ty - y)) > vertexTargetThreshold) {
                //keep animating if any vertex hasnt reached its target
                return false;
            }

            boost *= boostMomentum;
            return true;
        }

        private void update(V o, float radius, int color) {
            visible = true;

            if (o instanceof Sentence) {
                Sentence kb = (Sentence) o;
                TruthValue tr = kb.truth;
                float confidence = 0.5f;
                alpha = confidence * 0.75f + 0.25f;

                Term t = ((Sentence) o).content;
                //radius = (float) (Math.log(1 + 2 + confidence) * nodeSize);
            } else if (o instanceof Task) {
                Task ta = (Task) o;
                //radius = 2.0f + ta.getPriority() * 2.0f;
                alpha = ta.getDurability();                
            } else if (o instanceof Concept) {
                Concept co = (Concept) o;
                Term t = co.term;

                //radius = (float) (2 + 6 * co.budget.summary() * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);                
                stroke = 5;
            } else if (o instanceof Term) {
                Term t = (Term) o;
                //radius = (float) (Math.log(1 + 2 + t.getComplexity()) * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);                
            }
            this.radius = radius;
            this.color = color;
        }

        public void setPosition(final float x, final float y) {
            this.tx = x; this.ty = y;
        }

        public void movePosition(final float dx, final float dy) {
            this.tx += dx; this.ty += dy;
        }

        public float getX() { return tx; }
        public float getY() { return ty; }

        public float getRadius() {
            final float radScale = 4.0f;
            return radius * radScale;
        }

    }

    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        super.resizeRenderer(newWidth, newHeight);
        drawn = false;
    }

    public VertexDisplay updateVertex(final V o) {
        deadVertices.remove(o);
        
        VertexDisplay v = vertices.get(o);
        boolean changed = false;
        if (v != null) {
            v.update(o, getNodeSize(o), getNodeColor(o));
            return v;
        }
        v = new VertexDisplay(o);

        
        
        vertices.put(o, v);

        return v;
    }

    public int getEdgeColor(E e) {
        Integer i = edgeColors.get(e.getClass());
        if (i == null) {
            i = PGraphPanel.getColor(e.getClass().getSimpleName());
            edgeColors.put(e.getClass(), i);
        }
        return i;
    }

    public void mouseScrolled() {
        hnav.mouseScrolled();
    }

    @Override
    public void keyPressed() {
        hnav.keyPressed();
    }

    @Override
    public void mouseMoved() {
    }

    @Override
    public void mouseReleased() {
        hnav.mouseReleased();
        hsim.mouseReleased();
    }

    @Override
    public void mouseDragged() {
        hnav.mouseDragged();
        hsim.mouseDragged();
    }

    @Override
    public void mousePressed() {
        hnav.mousePressed();
        hsim.mousePressed();
    }

    abstract protected DirectedMultigraph<V,E> getGraph();
    abstract protected boolean hasUpdate();
    
    /**
     * called from NAR update thread, not swing thread
     */
    public void updateGraph() {

        if (hasUpdate() || (updateNext)) {

            updateNext = false;

            synchronized (vertices) {
                deadVertices.clear();
                deadVertices.addAll(vertices.keySet());

                try {
                    currentGraph = getGraph();
                } catch (Exception e) {
                    System.err.println(e);
                }

                for (V v : currentGraph.vertexSet()) {
                   ProcessingGraphCanvas.VertexDisplay d = updateVertex(v);            
                }

                for (final V v : deadVertices)
                    vertices.remove(v);
            }

            updateVertices();
            drawn = false;
        }
    }

    /** called after the graph has updated the current set of vertices; override to layout */
    protected void updateVertices() {
        
    }
    
    @Override
    public void draw() {

        if (drawn) {
            return;
        }

        if (motionBlur > 0) {
            fill(0, 0, 0, 255f * (1.0f - motionBlur));
            rect(0, 0, getWidth(), getHeight());
        } else {
            background(0, 0, 0, 0.001f);
        }

        //pushMatrix();
        hnav.Transform();
        drawGraph();
        //popMatrix();        

    }

    @Override
    public void setup() {
        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                mouseScroll = -evt.getWheelRotation();
                mouseScrolled();
            }
        });

        frameRate(FrameRate);

        if (isGL()) {
            System.out.println("Processing.org enabled OpenGL");
        }

        /*
         size(500,500,"P3D");
         */
    }

    public void drawGraph() {        
        
        if (currentGraph == null) {
            return;
        }

        synchronized (vertices) {
            //for speed:
            strokeCap(SQUARE);
            strokeJoin(PROJECT);

            int numEdges = currentGraph.edgeSet().size();
            if (numEdges < maxEdges) {
                for (final E edge : currentGraph.edgeSet()) {

                    final VertexDisplay elem1 = vertices.get(currentGraph.getEdgeSource(edge));
                    final VertexDisplay elem2 = vertices.get(currentGraph.getEdgeTarget(edge));
                    if ((elem1 == null) || (elem2 == null)) {
                        continue;
                    }

                    stroke(getEdgeColor(edge), (elem1.alpha + elem2.alpha) / 2f * 255f * lineAlpha);
                    strokeWeight(getEdgeThickness(edge, elem1, elem2));

                    float x1 = elem1.x;
                    float y1 = elem1.y;
                    float x2 = elem2.x;
                    float y2 = elem2.y;

                    if (numEdges < maxEdgesWithArrows) {
                        drawArrow(x1, y1, x2, y2);
                    } else {
                        drawLine(x1, y1, x2, y2);
                    }

                    //float cx = (x1 + x2) / 2.0f;
                    //float cy = (y1 + y2) / 2.0f;
                    //text(edge.toString(), cx, cy);
                }
            }

            noStroke();

            int numNodes = vertices.size();
            boolean text = numNodes < maxNodesWithLabels;
            boolean changed = false;
            if (numNodes < maxNodes) {
                for (final VertexDisplay d : vertices.values()) {
                    changed |= d.draw(this, text, nodeSize, nodeSpeed);
                }
            }
            //drawn = !changed;

        }
    }

    void drawArrowAngle(final float cx, final float cy, final float len, final float angle, float arrowHeadRadius) {
        pushMatrix();
        translate(cx, cy);
        rotate(radians(angle));
        line(0, 0, len, 0);
        line(len, 0, len - arrowHeadRadius * 2, -arrowHeadRadius);
        line(len, 0, len - arrowHeadRadius * 2, arrowHeadRadius);
        popMatrix();
    }

    void drawArrow(final float x1, final float y1, final float x2, final float y2) {
        float cx = (x1 + x2) / 2f;
        float cy = (y1 + y2) / 2f;
        float len = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float a = (float) (Math.atan2(y2 - y1, x2 - x1) * 180.0 / Math.PI);

        drawArrowAngle(x1, y1, len, a, nodeSize);
    }

    void drawLine(final float x1, final float y1, final float x2, final float y2) {
        line(x1, y1, x2, y2);
    }

    void setUpdateNext() {
        updateNext = true;
        drawn = false;
    }

    class Hnav {

        private float savepx = 0;
        private float savepy = 0;
        private int selID = 0;
        private float zoom = 1.0f;
        private float difx = 0;
        private float dify = 0;
        private int lastscr = 0;
        private boolean EnableZooming = true;
        private float scrollcamspeed = 1.1f;

        float MouseToWorldCoordX(int x) {
            return 1 / zoom * (x - difx - width / 2);
        }

        float MouseToWorldCoordY(int y) {
            return 1 / zoom * (y - dify - height / 2);
        }
        private boolean md = false;

        void mousePressed() {
            md = true;
            if (mouseButton == RIGHT) {
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }

        void mouseReleased() {
            md = false;
        }

        void mouseDragged() {
            if (mouseButton == RIGHT) {
                difx += (mouseX - savepx);
                dify += (mouseY - savepy);
                savepx = mouseX;
                savepy = mouseY;
            }
            drawn = false;
        }
        private float camspeed = 20.0f;
        private float scrollcammult = 0.92f;
        boolean keyToo = true;

        void keyPressed() {
            if ((keyToo && key == 'w') || keyCode == UP) {
                dify += (camspeed);
            }
            if ((keyToo && key == 's') || keyCode == DOWN) {
                dify += (-camspeed);
            }
            if ((keyToo && key == 'a') || keyCode == LEFT) {
                difx += (camspeed);
            }
            if ((keyToo && key == 'd') || keyCode == RIGHT) {
                difx += (-camspeed);
            }
            if (!EnableZooming) {
                return;
            }
            if (key == '-' || key == '#') {
                float zoomBefore = zoom;
                zoom *= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            if (key == '+') {
                float zoomBefore = zoom;
                zoom /= scrollcammult;
                difx = (difx) * (zoom / zoomBefore);
                dify = (dify) * (zoom / zoomBefore);
            }
            drawn = false;
        }

        void Init() {
            difx = -width / 2;
            dify = -height / 2;
        }

        void mouseScrolled() {
            if (!EnableZooming) {
                return;
            }
            float zoomBefore = zoom;
            if (mouseScroll > 0) {
                zoom *= scrollcamspeed;
            } else {
                zoom /= scrollcamspeed;
            }
            difx = (difx) * (zoom / zoomBefore);
            dify = (dify) * (zoom / zoomBefore);
            drawn = false;
        }

        void Transform() {
            translate(difx + 0.5f * width, dify + 0.5f * height);
            scale(zoom, zoom);
        }
    }

    ////Object management - dragging etc.
    class Hsim {

        ArrayList obj = new ArrayList();

        void Init() {
            smooth();
        }

        void mousePressed() {
            if (mouseButton == LEFT) {
                checkSelect();
            }
        }
        boolean dragged = false;

        void mouseDragged() {
            if (mouseButton == LEFT) {
                dragged = true;
                dragElems();
            }
        }

        void mouseReleased() {
            dragged = false;
            //selected = null;
        }

        void dragElems() {
            /*
             if (dragged && selected != null) {
             selected.x = hnav.MouseToWorldCoordX(mouseX);
             selected.y = hnav.MouseToWorldCoordY(mouseY);
             hsim_ElemDragged(selected);
             }
             */
        }

        void checkSelect() {
            /*
             double selection_distanceSq = selection_distance*selection_distance;
             if (selected == null) {
             for (int i = 0; i < obj.size(); i++) {
             Vertex oi = (Vertex) obj.get(i);
             float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
             float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
             float distanceSq = (dx * dx + dy * dy);
             if (distanceSq < (selection_distanceSq)) {
             selected = oi;
             hsim_ElemClicked(oi);
             return;
             }
             }
             }
             */
        }
    }

    static class Edge {

        public final int from;
        public final int to;
        public final int alpha;

        public Edge(final int from, final int to, int alpha) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
        }

        public Edge(final int from, final int to, float alpha) {
            this(from, to, (int) (255.0 * alpha));
        }
    }
}
