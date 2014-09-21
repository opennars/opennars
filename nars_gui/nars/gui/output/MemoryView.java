package nars.gui.output;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
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
import nars.core.Events.CycleStop;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.gui.NSlider;
import nars.gui.Window;
import nars.language.Term;
import nars.util.NARGraph;
import nars.util.sort.IndexedTreeSet;
import processing.core.PApplet;


class mvo_applet extends PApplet  //(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
{

///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    ProcessingJs processingjs = new ProcessingJs();
//Hnav 2D navigation system   
    Hnav hnav = new Hnav();
//Object
    float selection_distance = 10;
    public float maxNodeSize = 40f;
    float FrameRate = 30f;
    float boostMomentum = 0.98f;
    float boostScale = 6.0f;
    
    float vertexTargetThreshold = 4;
    
    private boolean compressLevels = true;
    boolean drawn = false;
    
    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();


    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    NAR nar;
    
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
    private boolean updateNext;
    float nodeSize = 10;
    
    float lineAlpha = 0.75f;
    float lineWidth = 3.8f;
    
    
    Map<Object,VertexDisplay> vertices = new HashMap();
    Set<Object> deadVertices = new HashSet();
    Map<Class,Integer> edgeColors = new HashMap(16);
    
    NARGraph graph;
    boolean showSyntax;

    //bounds of last positioned vertices
    float minX=0, minY=0, maxX=0, maxY=0;
    float motionBlur = 0.0f;

    public class VertexDisplay {
        float x, y, tx, ty;
        int color;
        float radius;
        float alpha;
        String label;
        private final Object object;
        private float boost;
        float stroke;
        private boolean visible;

        public VertexDisplay(Object o) {
            this.object = o;
            
            x = y = 0;
            tx = x;
            ty = y;
            stroke = 0;
            radius = nodeSize;
            visible = true;
            
            if (o instanceof Concept) {
                label = ((Concept)o).term.toString();
            }
            else {
                label = o.toString();
            }
            
            if (label.length() > MAX_UNSELECTED_LABEL_LENGTH)
                label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH-3) + "..";
            
           
            update(o);

        }
        
        public void position(float level, float index, float priority) {
            float LEVELRAD = maxNodeSize * 2.5f;
            
            if (mode == 2) {
                tx = ((float)Math.sin(index/10d) * LEVELRAD) * 5 * ((10+index)/20);
                //ty = -((((Bag<Concept>)nar.memory.concepts).levels - level) * maxNodeSize * 3.5f);
                ty = (1.0f - priority) * LEVELRAD * 150;
            }
            else if (mode == 1) {

                //double radius = ((((Bag<Concept>)nar.memory.concepts).levels - level)+8);
                double radius = (1.0 - priority) * LEVELRAD + 8;
                float angle = index; //TEMPORARY
                tx = (float)(Math.cos(angle/3.0) * radius) * LEVELRAD;
                ty = (float)(Math.sin(angle/3.0) * radius) * LEVELRAD;
            }
            else if (mode == 0) {
                //gridsort
                tx = index * LEVELRAD;
                ty = (1.0f - priority) * LEVELRAD * 100;
            }
            
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
            
            if (!visible) return;
             
            /*if (stroke > 0) {
              stroke(Color.WHITE.getRGB());
                strokeWeight(stroke);
            }*/
                        
            float r = (radius+boost*boostScale) * nodeSize / 2f;
            
            fill(color, alpha*255/2);
           
            ellipse(x, y, r, r);

            if (text && (label!=null)) {
                fill(255,255,255,alpha*255*0.75f);
                textSize(r/2);
                text(label, x, y);            
            }
            
            /*if (stroke > 0) {                
                //reset stroke
                noStroke();
            }*/
        }
               
        protected void update() {            
            x = ( x * (1.0f - nodeSpeed) + tx * (nodeSpeed) );
            y = ( y * (1.0f - nodeSpeed) + ty * (nodeSpeed) );            
            
            if ((Math.abs(tx-x) + Math.abs(ty-y)) > vertexTargetThreshold) {
                //keep animating if any vertex hasnt reached its target
                drawn = false;
            }
            
            boost *= boostMomentum;
        }

        private void update(Object o) {
            visible = true;
            
            if (o instanceof Sentence) {
                 Sentence kb = (Sentence)o;
                 TruthValue tr = kb.truth;
                 float confidence = 0.5f;
                 if (tr!=null) {
                     confidence = tr.getConfidence();
                     double hue = 0.25 + (0.25 * (kb.truth.getFrequency()-0.5));
                     color = Color.getHSBColor((float)hue, 0.9f, 0.9f).getRGB();
                 }
                 else {
                     color = Color.GRAY.getRGB();
                 }
                alpha = confidence*0.75f+0.25f;
                
                Term t = ((Sentence) o).content;
                radius = (float)(Math.log(1+2 + confidence) * nodeSize);
             }
             else if (o instanceof Task) {
                Task ta = (Task)o;
                radius = 2.0f + ta.getPriority()*2.0f;
                alpha = ta.getDurability();
                color = papplet.getColor(o);
             }            
             else if (o instanceof Concept) {
                Concept co = (Concept)o;
                Term t = co.term;
                
                radius = (float)(2 + 6 * co.budget.summary() * nodeSize);
                alpha = papplet.getVertexAlpha(o);                             
                color = papplet.getColor(t);
                stroke = 5;
             }
             else if (o instanceof Term) {
                Term t = (Term)o;                
                radius = (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
                alpha = papplet.getVertexAlpha(o);                             
                color = papplet.getColor(o);
             }
        }

    }

    @Override
    protected void resizeRenderer(int newWidth, int newHeight) {
        super.resizeRenderer(newWidth, newHeight); //To change body of generated methods, choose Tools | Templates.
        drawn = false;
    }
    
    
    public VertexDisplay updateVertex(Object o) {
        VertexDisplay v = vertices.get(o);
        if (v!=null) {
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
            i = papplet.getColor(e.getClass().getSimpleName());
            edgeColors.put(e.getClass(), i);
        }
        return i;
    }
    
    public void mouseScrolled() {
        hamlib.mouseScrolled();
    }

    @Override
    public void keyPressed() {
        hamlib.keyPressed();
    }

    @Override
    public void mouseMoved() {
        hamlib.mouseMoved();
    }

    @Override
    public void mouseReleased() {
        hamlib.mouseReleased();
    }

    @Override
    public void mouseDragged() {
        hamlib.mouseDragged();
    }

    @Override
    public void mousePressed() {
        hamlib.mousePressed();
    }

    /** should be called from NAR update thread, not swing thread  */
    public void updateGraph() {
        final Sentence currentBelief = nar.memory.getCurrentBelief();
        final Concept currentConcept = nar.memory.getCurrentConcept();
        final Task currentTask = nar.memory.getCurrentTask();
        final IndexedTreeSet<Concept> concepts = new IndexedTreeSet(new Comparator<Concept>() {
            @Override public int compare(Concept o1, Concept o2) {
                return o1.getKey().toString().compareTo(o2.getKey().toString());
            }            
        });

        if ((nar.getTime() != lasttime) || (updateNext)) {
            updateNext = false;
            lasttime = nar.getTime();
            


            synchronized (vertices) {
                deadVertices.clear();
                
                if (mode == 0) {
                    //index concepts
                    concepts.addAll(nar.memory.getConcepts());              
                }
                deadVertices.addAll(vertices.keySet());

                    try {
                        graph = new NARGraph();
                        graph.add(nar, new NARGraph.ExcludeBelowPriority(minPriority), 
                                new NARGraph.DefaultGraphizer(showBeliefs, true, showBeliefs, true, false) {

                            float level;
                            float index = 0;
                            int levelContents = 0;
                            private float priority;
                            Term lastTerm = null;
                            VertexDisplay lastTermVertex = null;

                            public void preLevel(NARGraph g, int l) {
                                if (!compressLevels)
                                    level = l;

                                levelContents = 0;

                                if (mode == 1)
                                    index = 0;
                            }


                            public void postLevel(NARGraph g, int l) {                        
                                if (compressLevels) {
                                    if (levelContents > 0)
                                        level--;
                                }                        
                            }

                            @Override
                            public void onConcept(NARGraph g, Concept c) {
                                super.onConcept(g, c);

                                priority = c.getPriority();
                                level = (float)(priority*100.0);

                                if (mode == 0) {
                                    index = concepts.entryIndex(c);                                    
                                }
                                else {
                                    if ((lastTerm!=null) && (c.term.equals(lastTerm))) {
                                        //terms equal to concept, ordinarily displayed as subsequent nodes
                                        //should just appear at the same position as the concept
                                        //lastTermVertex.visible = false;
                                        lastTermVertex.position(level, index, priority);
                                        lastTermVertex.visible = false;
                                    }
                                    else
                                        index++;
                                }

                                VertexDisplay d = updateVertex(c);
                                d.position(level, index, priority);
                                deadVertices.remove(c);

                                if (currentConcept!=null)
                                    if (c.equals(currentConcept))
                                        d.boost = 1.0f;                        


                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }

                            @Override
                            public void onTerm(Term t) {

                                index++;                                               

                                VertexDisplay d = updateVertex(t);
                                d.position(level, index, priority);
                                deadVertices.remove(d);

                                lastTerm = t;
                                lastTermVertex = d;

                                levelContents++;

                            }

                            @Override
                            public void onBelief(Sentence kb) {
                                index+= 0.25f;

                                VertexDisplay d = updateVertex(kb);
                                d.position(level, index, priority);                    
                                deadVertices.remove(kb);

                                if (currentBelief!=null)
                                    if (kb.equals(currentBelief))
                                        d.boost = 1.0f;                        

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;

                            }

                            @Override
                            public void onQuestion(Task t) {
                                index+= 0.25f;

                                VertexDisplay d = updateVertex(t);
                                d.position(level, index, priority);                    
                                deadVertices.remove(t);

                                if (currentTask!=null)
                                    if (t.equals(currentTask))
                                        d.boost = 1.0f;                        

                                levelContents++;

                                lastTerm = null;
                                lastTermVertex = null;
                            }


                        });
                    } catch (Exception e)  { System.err.println(e); } 


                for (Object v : deadVertices)
                    vertices.remove(v);
            }
            
            drawn = false;
        }
    }
    
    @Override
    public void draw() {
        
                
        
        if (drawn)
            return;
        
        drawn = true; //allow the vertices to invalidate again in drawit() callee

        
        if (motionBlur > 0) {
            fill(0,0,0,255f*(1.0f - motionBlur));
            rect(0,0,getWidth(),getHeight());
        }
        else {        
            background(0,0,0, 0.001f);
        }
        
        //pushMatrix();
        hnav.Transform();
        drawGraph();
        //popMatrix();        
        
    }



    @Override
    public void setup() {  
        frameRate(FrameRate);
        
        /*
        size(500,500,"P3D");
        if (isGL()) {
            System.out.println("Processing.org enabled OpenGL");
        }
        */
        
    }


    public void drawGraph() {
                        
        if (graph== null) return;

        
        synchronized (vertices) {
            //for speed:
            strokeCap(SQUARE);
            strokeJoin(PROJECT);

            int numEdges = graph.edgeSet().size();
            if (numEdges < maxEdges) {
                for (final Object edge : graph.edgeSet()) {

                    final VertexDisplay elem1 = vertices.get(graph.getEdgeSource(edge));
                    final VertexDisplay elem2 = vertices.get(graph.getEdgeTarget(edge));                
                    if ((elem1 == null) || (elem2 == null))
                        continue;

                    stroke(getEdgeColor(edge), (elem1.alpha + elem2.alpha)/2f * 255f*lineAlpha );
                    strokeWeight( (elem1.radius + elem2.radius)/2.0f / lineWidth );

                    float x1 = elem1.x;
                    float y1 = elem1.y;
                    float x2 = elem2.x;
                    float y2 = elem2.y;
                    
                    if (numEdges < maxEdgesWithArrows)
                        drawArrow(x1, y1, x2, y2);     
                    else
                        drawLine(x1, y1, x2, y2);

                    //float cx = (x1 + x2) / 2.0f;
                    //float cy = (y1 + y2) / 2.0f;
                    //text(edge.toString(), cx, cy);
                }
            }

            noStroke();

            int numNodes = vertices.size();
            boolean text = numNodes < maxNodesWithLabels;
            if (numNodes < maxNodes)
                for (final VertexDisplay d : vertices.values())
                    d.draw(text);

        }   
    }

    
    void drawArrowAngle(final float cx, final float cy, final float len, final float angle){
      pushMatrix();
      translate(cx, cy);
      rotate(radians(angle));
      line(0,0,len, 0);
      line(len, 0, len - 8*2, -8);
      line(len, 0, len - 8*2, 8);
      popMatrix();
    }

    void drawArrow(final float x1, final float y1, final float x2, final float y2) {
        float cx = (x1+x2)/2f;
        float cy = (y1+y2)/2f;
        float len = (float)Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
        float a = (float)(Math.atan2(y2-y1,x2-x1)*180.0/Math.PI);
        
        drawArrowAngle(x1, y1, len, a);
    }    
    void drawLine(final float x1, final float y1, final float x2, final float y2) {
        line(x1,y1,x2,y2);
    }
    
    void setUpdateNext() {
        updateNext = true;
        drawn = false;        
    }

    class ProcessingJs {

        ProcessingJs() {
            addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                @Override
                public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                    mouseScroll = -evt.getWheelRotation();
                    mouseScrolled();
                }
            }
            );
        }
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

//Hamlib handlers
    class Hamlib {

        void Init() {
            noStroke();
            hnav.Init();
            hsim.Init();
        }

        void mousePressed() {
            hnav.mousePressed();
            hsim.mousePressed();
        }

        void mouseDragged() {
            hnav.mouseDragged();
            hsim.mouseDragged();
        }

        void mouseReleased() {
            hnav.mouseReleased();
            hsim.mouseReleased();
        }

        public void mouseMoved() {
        }

        void keyPressed() {
            hnav.keyPressed();
        }

        void mouseScrolled() {
            hnav.mouseScrolled();
        }

        void Camera() {

        }


    }

    public class Edge {

        public final int from;
        public final int to;
        public final int alpha;

        public Edge(final int from, final int to, int alpha) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
        }
        public Edge(final int from, final int to, float alpha) {
            this(from, to, (int)(255.0 * alpha));
        }
    }
}

public class MemoryView extends Window {

    mvo_applet app = null;

    public MemoryView(NAR n) {
        super("Memory View");


        app = new mvo_applet();
        app.init();
        app.nar = n;
        
        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

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
            @Override public void actionPerformed(ActionEvent e) {
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
                app.nodeSize = (float)v;
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
                app.minPriority = (float)(1.0 - v);
                app.setUpdateNext();
            }          
        };        
        maxLevels.setPrefix("Min Level: ");
        maxLevels.setPreferredSize(new Dimension(125, 25));
        menu.add(maxLevels);

        NSlider nodeSpeed = new NSlider(app.nodeSpeed, 0.001f, 0.99f) {
            @Override
            public void onChange(float v) {
                app.nodeSpeed = (float)v;
                app.drawn = false;
            }          
        };        
        nodeSpeed.setPrefix("Node Speed: ");
        nodeSpeed.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSpeed);        
        
        NSlider blur = new NSlider(0, 0, 1.0f) {
            @Override
            public void onChange(float v) {
                app.motionBlur = (float)v;
                app.drawn = false;
            }          
        };        
        blur.setPrefix("Blur: ");
        blur.setPreferredSize(new Dimension(85, 25));
        menu.add(blur);        
        
        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
        

        n.memory.event.on(CycleStop.class, new Observer() {
            @Override
            public void event(Class event, Object... arguments) {
                if (app!=null)
                    app.updateGraph();
                else
                    n.memory.event.off(CycleStop.class, this);
            }  
        });
    
    }

    @Override
    protected void close() {
        app.stop();
        app.destroy();
        getContentPane().removeAll();
        app = null;
    }

    
    
    
}
