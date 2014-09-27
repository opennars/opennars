package nars.gui.output.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import nars.core.EventEmitter.Observer;
import nars.core.Events.FrameEnd;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NPanel;
import nars.gui.NSlider;
import nars.language.Term;
import nars.util.NARGraph;
import nars.util.sort.IndexedTreeSet;
import org.jgrapht.graph.DirectedMultigraph;
import processing.core.PApplet;

abstract class GraphCanvasProcessing<G extends DirectedMultigraph> extends PApplet {

    int mouseScroll = 0;

    Hnav hnav = new Hnav();
    Hsim hsim = new Hsim();

    float selection_distance = 10;
    public float maxNodeSize = 40f;
    float FrameRate = 30f;
    float boostMomentum = 0.98f;
    float boostScale = 6.0f;

    float vertexTargetThreshold = 4;

    boolean compressLevels = true;
    boolean drawn = false;

    int maxNodesWithLabels = 300;
    int maxNodes = 1000;
    int maxEdgesWithArrows = 500;
    int maxEdges = 1500;

    float minPriority = 0;

    public int mode = 0;

    boolean showBeliefs = false;

    float nodeSpeed = 0.1f;

    float sx = 800;
    float sy = 800;

    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    boolean updateNext;
    float nodeSize = 10;

    float lineAlpha = 0.75f;
    float lineWidth = 3.8f;

    Map<Object, VertexDisplay> vertices = new HashMap();
    Set<Object> deadVertices = new HashSet();
    Map<Class, Integer> edgeColors = new HashMap(16);

    G graph;
    boolean showSyntax;

    //bounds of last positioned vertices
    float minX = 0, minY = 0, maxX = 0, maxY = 0;
    float motionBlur = 0.0f;

    public GraphCanvasProcessing() {
        super();
        init();
    }

    class VertexDisplay {

        float x, y, tx, ty;
        int color;
        float radius;
        float alpha;
        String label;
        final Object object;
        float boost;
        float stroke;
        boolean visible;

        public VertexDisplay(Object o) {
            this.object = o;

            x = y = 0;
            tx = x;
            ty = y;
            stroke = 0;
            radius = nodeSize;
            visible = true;

            if (o instanceof Concept) {
                label = ((Concept) o).term.toString();
            } else {
                label = o.toString();
            }

            if (label.length() > MAX_UNSELECTED_LABEL_LENGTH) {
                label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH - 3) + "..";
            }

            update(o);

        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return object.equals(obj);
        }

        public void draw(boolean text) {
            update();

            if (!visible) {
                return;
            }

            /*if (stroke > 0) {
             stroke(Color.WHITE.getRGB());
             strokeWeight(stroke);
             }*/
            float r = (radius + boost * boostScale) * nodeSize / 2f;

            fill(color, alpha * 255 / 2);

            ellipse(x, y, r, r);

            if (text && (label != null)) {
                fill(255, 255, 255, alpha * 255 * 0.75f);
                textSize(r / 2);
                text(label, x, y);
            }

            /*if (stroke > 0) {                
             //reset stroke
             noStroke();
             }*/
        }

        protected void update() {
            x = (x * (1.0f - nodeSpeed) + tx * (nodeSpeed));
            y = (y * (1.0f - nodeSpeed) + ty * (nodeSpeed));

            if ((Math.abs(tx - x) + Math.abs(ty - y)) > vertexTargetThreshold) {
                //keep animating if any vertex hasnt reached its target
                drawn = false;
            }

            boost *= boostMomentum;
        }

        private void update(Object o) {
            visible = true;

            if (o instanceof Sentence) {
                Sentence kb = (Sentence) o;
                TruthValue tr = kb.truth;
                float confidence = 0.5f;
                if (tr != null) {
                    confidence = tr.getConfidence();
                    double hue = 0.25 + (0.25 * (kb.truth.getFrequency() - 0.5));
                    color = Color.getHSBColor((float) hue, 0.9f, 0.9f).getRGB();
                } else {
                    color = Color.GRAY.getRGB();
                }
                alpha = confidence * 0.75f + 0.25f;

                Term t = ((Sentence) o).content;
                radius = (float) (Math.log(1 + 2 + confidence) * nodeSize);
            } else if (o instanceof Task) {
                Task ta = (Task) o;
                radius = 2.0f + ta.getPriority() * 2.0f;
                alpha = ta.getDurability();
                color = PGraphPanel.getColor(o.getClass());
            } else if (o instanceof Concept) {
                Concept co = (Concept) o;
                Term t = co.term;

                radius = (float) (2 + 6 * co.budget.summary() * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);
                color = PGraphPanel.getColor(t.getClass());
                stroke = 5;
            } else if (o instanceof Term) {
                Term t = (Term) o;
                radius = (float) (Math.log(1 + 2 + t.getComplexity()) * nodeSize);
                alpha = PGraphPanel.vertexAlpha(o);
                color = PGraphPanel.getColor(o.getClass());
            }
        }

    }

    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        super.resizeRenderer(newWidth, newHeight);
        drawn = false;
    }

    public VertexDisplay updateVertex(Object o) {
        VertexDisplay v = vertices.get(o);
        if (v != null) {
            v.update(o);
            return v;
        }
        v = new VertexDisplay(o);

        vertices.put(o, v);

        return v;
    }

    public int getEdgeColor(Object e) {
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

    abstract protected G getGraph();
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
                    graph = getGraph();
                } catch (Exception e) {
                    System.err.println(e);
                }


                for (final Object v : deadVertices)
                    vertices.remove(v);
            }

            drawn = false;
        }
    }

    
    @Override
    public void draw() {

        if (drawn) {
            return;
        }

        drawn = true; //allow the vertices to invalidate again in drawit() callee

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

        if (graph == null) {
            return;
        }

        synchronized (vertices) {
            //for speed:
            strokeCap(SQUARE);
            strokeJoin(PROJECT);

            int numEdges = graph.edgeSet().size();
            if (numEdges < maxEdges) {
                for (final Object edge : graph.edgeSet()) {

                    final VertexDisplay elem1 = vertices.get(graph.getEdgeSource(edge));
                    final VertexDisplay elem2 = vertices.get(graph.getEdgeTarget(edge));
                    if ((elem1 == null) || (elem2 == null)) {
                        continue;
                    }

                    stroke(getEdgeColor(edge), (elem1.alpha + elem2.alpha) / 2f * 255f * lineAlpha);
                    strokeWeight((elem1.radius + elem2.radius) / 2.0f / lineWidth);

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
            if (numNodes < maxNodes) {
                for (final VertexDisplay d : vertices.values()) {
                    d.draw(text);
                }
            }

        }
    }

    void drawArrowAngle(final float cx, final float cy, final float len, final float angle) {
        pushMatrix();
        translate(cx, cy);
        rotate(radians(angle));
        line(0, 0, len, 0);
        line(len, 0, len - 8 * 2, -8);
        line(len, 0, len - 8 * 2, 8);
        popMatrix();
    }

    void drawArrow(final float x1, final float y1, final float x2, final float y2) {
        float cx = (x1 + x2) / 2f;
        float cy = (y1 + y2) / 2f;
        float len = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float a = (float) (Math.atan2(y2 - y1, x2 - x1) * 180.0 / Math.PI);

        drawArrowAngle(x1, y1, len, a);
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

public class MemoryView extends NPanel implements Observer {

    GraphCanvasProcessing app = null;
    private final NAR nar;

    public MemoryView(NAR n) {
        super(new BorderLayout());

        this.nar = n;
    }

    protected void init() {
        app = new GraphCanvasProcessing<NARGraph>() {

            public void position(GraphCanvasProcessing.VertexDisplay v, float level, float index, float priority) {
                float LEVELRAD = maxNodeSize * 2.5f;

                if (mode == 2) {
                    v.tx = ((float) Math.sin(index / 10d) * LEVELRAD) * 5 * ((10 + index) / 20);
                    //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
                    v.ty = (1.0f - priority) * LEVELRAD * 150;
                } else if (mode == 1) {

                    //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
                    double radius = (1.0 - priority) * LEVELRAD + 8;
                    float angle = index; //TEMPORARY
                    v.tx = (float) (Math.cos(angle / 3.0) * radius) * LEVELRAD;
                    v.ty = (float) (Math.sin(angle / 3.0) * radius) * LEVELRAD;
                } else if (mode == 0) {
                    //gridsort
                    v.tx = index * LEVELRAD;
                    v.ty = (1.0f - priority) * LEVELRAD * 100;
                }

            }

            final IndexedTreeSet<Concept> sortedConcepts = new IndexedTreeSet(new Comparator<Concept>() {
                @Override
                public int compare(Concept o1, Concept o2) {
                    return o1.getKey().toString().compareTo(o2.getKey().toString());
                }
            });

            @Override
            protected boolean hasUpdate() {
                if (nar.getTime() != lasttime) {
                    lasttime = nar.getTime();   
                    return true;
                }
                return false;
            }

            //TODO genrealize to DirectedMultigraph
            public NARGraph getGraph() {                
                
                final Sentence currentBelief = nar.memory.getCurrentBelief();
                final Concept currentConcept = nar.memory.getCurrentConcept();
                final Task currentTask = nar.memory.getCurrentTask();
                
                if (mode == 0) {
                    sortedConcepts.clear();
                    sortedConcepts.addAll(nar.memory.getConcepts());
                }
                
                return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority),
                        new NARGraph.DefaultGraphizer(showBeliefs, true, showBeliefs, true, false) {

                            float level;
                            float index = 0;
                            int levelContents = 0;
                            private float priority;
                            Term lastTerm = null;
                            GraphCanvasProcessing.VertexDisplay lastTermVertex = null;

                            public void preLevel(NARGraph g, int l) {
                                if (!compressLevels) {
                                    level = l;
                                }

                                levelContents = 0;

                                if (mode == 1) {
                                    index = 0;
                                }
                            }

                            public void postLevel(NARGraph g, int l) {
                                if (compressLevels) {
                                    if (levelContents > 0) {
                                        level--;
                                    }
                                }
                            }

                            @Override
                            public void onConcept(NARGraph g, Concept c) {
                                super.onConcept(g, c);

                                priority = c.getPriority();
                                level = (float) (priority * 100.0);

                                if (mode == 0) {
                                    index = sortedConcepts.entryIndex(c);
                                } else {
                                    if ((lastTerm != null) && (c.term.equals(lastTerm))) {
                                                    //terms equal to concept, ordinarily displayed as subsequent nodes
                                        //should just appear at the same position as the concept
                                        //lastTermVertex.visible = false;
                                        position(lastTermVertex, level, index, priority);
                                        lastTermVertex.visible = false;
                                    } else {
                                        index++;
                                    }
                                }

                                GraphCanvasProcessing.VertexDisplay d = updateVertex(c);
                                position(d, level, index, priority);
                                deadVertices.remove(c);

                                if (currentConcept != null) {
                                    if (c.equals(currentConcept)) {
                                        d.boost = 1.0f;

                                    }
                                }

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }

                            @Override
                            public void onTerm(Term t) {

                                index++;

                                GraphCanvasProcessing.VertexDisplay d = updateVertex(t);
                                position(d, level, index, priority);
                                deadVertices.remove(d);

                                lastTerm = t;
                                lastTermVertex = d;

                                levelContents++;

                            }

                            @Override
                            public void onBelief(Sentence kb) {
                                index += 0.25f;

                                GraphCanvasProcessing.VertexDisplay d = updateVertex(kb);
                                position(d, level, index, priority);
                                deadVertices.remove(kb);

                                if (currentBelief != null) {
                                    if (kb.equals(currentBelief)) {
                                        d.boost = 1.0f;
                                    }
                                }

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;

                            }

                            @Override
                            public void onQuestion(Task t) {
                                index += 0.25f;

                                GraphCanvasProcessing.VertexDisplay d = updateVertex(t);
                                position(d, level, index, priority);
                                deadVertices.remove(t);

                                if (currentTask != null) {
                                    if (t.equals(currentTask)) {
                                        d.boost = 1.0f;
                                    }
                                }

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }

                        });

            }


        };

        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));

        final JComboBox modeSelect = new JComboBox();
        modeSelect.addItem("GridSort");
        modeSelect.addItem("Circle");
        modeSelect.addItem("Grid");
        modeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.mode = modeSelect.getSelectedIndex();
                app.setUpdateNext();
            }
        });
        menu.add(modeSelect);

        final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
        beliefsEnable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.showBeliefs = (beliefsEnable.isSelected());
                app.setUpdateNext();
            }
        });
        menu.add(beliefsEnable);

        /*
         final JCheckBox syntaxEnable = new JCheckBox("Syntax");
         syntaxEnable.addActionListener(new ActionListener() {
         @Override public void actionPerformed(ActionEvent e) {
         app.showSyntax = (syntaxEnable.isSelected());        
         app.setUpdateNext();
         }
         });
         menu.add(syntaxEnable);        
         */
        NSlider nodeSize = new NSlider(app.nodeSize, 1, app.maxNodeSize) {
            @Override
            public void onChange(float v) {
                app.nodeSize = (float) v;
                app.drawn = false;
            }
        };
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSize);

        //final int numLevels = ((Bag<Concept>)n.memory.concepts).levels;
        NSlider maxLevels = new NSlider(1, 0, 1) {
            @Override
            public void onChange(float v) {
                app.minPriority = (float) (1.0 - v);
                app.setUpdateNext();
            }
        };
        maxLevels.setPrefix("Min Level: ");
        maxLevels.setPreferredSize(new Dimension(125, 25));
        menu.add(maxLevels);

        NSlider nodeSpeed = new NSlider(app.nodeSpeed, 0.001f, 0.99f) {
            @Override
            public void onChange(float v) {
                app.nodeSpeed = (float) v;
                app.drawn = false;
            }
        };
        nodeSpeed.setPrefix("Node Speed: ");
        nodeSpeed.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSpeed);

        NSlider blur = new NSlider(0, 0, 1.0f) {
            @Override
            public void onChange(float v) {
                app.motionBlur = (float) v;
                app.drawn = false;
            }
        };
        blur.setPrefix("Blur: ");
        blur.setPreferredSize(new Dimension(85, 25));
        menu.add(blur);

        add(menu, BorderLayout.NORTH);
        add(app, BorderLayout.CENTER);

    }

    @Override
    protected void onShowing(boolean showing) {
        if (showing) {
            init();
            nar.memory.event.on(FrameEnd.class, this);
        } else {
            nar.memory.event.off(FrameEnd.class, this);

            app.stop();
            app.destroy();
            removeAll();
            app = null;
        }
    }

    @Override
    public void event(Class event, Object[] arguments) {
        if (app != null) {
            app.updateGraph();
        }
    }

}
