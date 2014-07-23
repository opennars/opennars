package nars.gui.output;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.util.NARGraph;
import nars.util.NARGraph.ExcludeLevelsBelow;
import nars.gui.NSlider;
import nars.gui.Window;
import nars.gui.output.mvoapplet.Vertex;
import nars.language.Term;
import nars.storage.Memory;
import processing.core.*;


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
    
    private boolean compressLevels = true;
    
    
    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();

    Vertex lastclicked = null;

    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    NAR nar;
    
    
    int minLevel = 0;
            
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
    private static final float linkWeight = 4.0f;
    
    
    WeakHashMap<Object,VertexDisplay> vertices = new WeakHashMap();
    Set<Object> deadVertices = new HashSet();
    Map<Class,Integer> edgeColors = new HashMap(16);
    
    NARGraph graph;
    boolean showSyntax;


    public class VertexDisplay {
        float x, y, tx, ty;
        int color;
        float radius;
        float alpha;
        String label;
        private final Object object;
        private float boost;

        public VertexDisplay(Object o) {
            this.object = o;
            x = y = 0;
            tx = x;
            ty = y;
            radius = nodeSize;
            
            label = o.toString();
            if (label.length() > MAX_UNSELECTED_LABEL_LENGTH)
                label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH-3) + "..";
            
            if (o instanceof Sentence) {
                 Sentence kb = (Sentence)o;
                 TruthValue tr = kb.getTruth();
                 if (tr!=null) {
                     alpha = tr.getConfidence()*0.75f+0.25f;
                     double hue = 0.25 + (0.25 * (kb.truth.getFrequency()-0.5));
                     color = Color.getHSBColor((float)hue, 0.9f, 0.9f).getRGB();
                 }
                 else {
                     alpha = 0.5f;
                     color = Color.GRAY.getRGB();
                 }
                Term t = ((Sentence)o).getContent();
                radius = (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
             }
             else if (o instanceof Task) {
                Task ta = (Task)o;
                radius = 2.0f + ta.getPriority()*2.0f;
                alpha = ta.getDurability();
                color = papplet.getColor(o);
             }            
             else if (o instanceof Concept) {
                Term t = ((Concept)o).getTerm();
                radius = (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
                alpha = papplet.getVertexAlpha(o);                             
                color = papplet.getColor(o);                
             }
             else if (o instanceof Term) {
                Term t = (Term)o;
                radius = (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
                alpha = papplet.getVertexAlpha(o);                             
                color = papplet.getColor(o);
             }
        }
        
        public void position(float level, float index) {
            if (mode == 1) {
                ty = (index * maxNodeSize * 3.5f);
                tx = -4500+(level * maxNodeSize * 3.5f);
            }
            else if (mode == 0) {
                float LEVELRAD = maxNodeSize * 2.5f;

                double radius = ((nar.memory.concepts.levels - level)+8);
                float angle = index; //TEMPORARY
                tx = (float)(Math.cos(angle/3.0) * radius) * LEVELRAD;
                ty = (float)(Math.sin(angle/3.0) * radius) * LEVELRAD;
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
        
        public void draw() {
            update();
            
            float r = (radius+boost*boostScale) * nodeSize / 2f;
            
            fill(color, alpha*255/2);
           
            ellipse(x, y, r, r);
             
            if (label!=null) {
                fill(255,255,255,alpha*255*0.75f);
                textSize(r/2);
                text(label, x, y);            
            }
        }
        
        protected void update() {            
            x = ( x * (1.0f - nodeSpeed) + tx * (nodeSpeed) );
            y = ( y * (1.0f - nodeSpeed) + ty * (nodeSpeed) );            
            boost *= boostMomentum;
        }

 
    }
    
    public VertexDisplay displayVertex(Object o) {
        VertexDisplay v = vertices.get(o);
        if (v!=null) {
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

    public void keyPressed() {
        hamlib.keyPressed();
    }

    public void mouseMoved() {
        hamlib.mouseMoved();
    }

    public void mouseReleased() {
        hamlib.mouseReleased();
    }

    public void mouseDragged() {
        hamlib.mouseDragged();
    }

    public void mousePressed() {
        hamlib.mousePressed();
    }

    @Override
    public void draw() {
        
        final Memory mem = nar.memory;
        final Sentence currentBelief = mem.currentBelief;
        final Concept currentConcept = mem.currentConcept;
        final Task currentTask = mem.currentTask;
        
        if ((nar.getTime() != lasttime) || (updateNext)) {
            updateNext = false;
            lasttime = nar.getTime();
            


            deadVertices.clear();
            deadVertices.addAll(vertices.keySet());
            
            try {
                
                graph = new NARGraph();
                graph.add(nar, new ExcludeLevelsBelow(minLevel), 
                        new NARGraph.DefaultGraphizer(showBeliefs, showBeliefs, showBeliefs, true, showSyntax) {

                    float level = nar.memory.concepts.levels;
                    float index = 0;
                    int levelContents = 0;

                    @Override
                    public void preLevel(NARGraph g, int l) {
                        super.preLevel(g, l);

                        if (!compressLevels)
                            level = l;
                        
                        levelContents = 0;

                        if (mode == 1)
                            index = 0;
                        
                                                
                    }

                    @Override
                    public void postLevel(NARGraph g, int l) {                        
                        if (compressLevels) {
                            if (levelContents > 0)
                                level--;
                        }                        
                        
                    }

                    

                    
                    
                    @Override
                    public void onConcept(NARGraph g, Concept c) {
                        super.onConcept(g, c);

                        index++;
                        VertexDisplay d = displayVertex(c);
                        d.position(level, index);
                        deadVertices.remove(c);

                        if (currentConcept!=null)
                            if (c.equals(currentConcept))
                                d.boost = 1.0f;                        

                        levelContents++;
                                 
                    }

                    @Override
                    public void onTerm(Term t) {
                        index++;

                        VertexDisplay d = displayVertex(t);
                        d.position(level, index);
                        deadVertices.remove(d);

                        levelContents++;
                        
                    }

                    @Override
                    public void onBelief(Sentence kb) {
                        index+= 0.25f;

                        VertexDisplay d = displayVertex(kb);
                        d.position(level, index);                    
                        deadVertices.remove(kb);
                        
                        if (currentBelief!=null)
                            if (kb.equals(currentBelief))
                                d.boost = 1.0f;                        
                    
                        levelContents++;
                    
                    }

                    @Override
                    public void onQuestion(Task t) {
                        index+= 0.25f;

                        VertexDisplay d = displayVertex(t);
                        d.position(level, index);                    
                        deadVertices.remove(t);

                        if (currentTask!=null)
                            if (t.equals(currentTask))
                                d.boost = 1.0f;                        

                        levelContents++;
                    
                    }
                
                
                });
            } catch (Exception e)  { System.err.println(e); } 
                        
            for (Object v : deadVertices)
                vertices.remove(v);
        }
        
        
        //pushMatrix();
        hnav.Transform();
        hrend_DrawBegin();
        drawit();
        hrend_DrawEnd();
        //popMatrix();
        
        hrend_DrawGUI();         
    }


    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();
        if (lastclicked != null) {
            fill(255, 0, 0);
            ellipse(lastclicked.x, lastclicked.y, 10, 10);
        }
    }

    public void hrend_DrawGUI() {
    }

    @Override
    public void setup() {  
        frameRate(FrameRate);
       
    }


    public void drawit() {
        
        background(0,0,0);
                        
        //for speed:
        strokeCap(SQUARE);
        strokeJoin(PROJECT);
        
            
        
        
        for (final Object edge : graph.edgeSet()) {

            

            final VertexDisplay elem1 = vertices.get(graph.getEdgeSource(edge));
            final VertexDisplay elem2 = vertices.get(graph.getEdgeTarget(edge));                
            if ((elem1 == null) || (elem2 == null))
                continue;

            
            stroke(getEdgeColor(edge), (elem1.alpha + elem2.alpha)/2f * 255f/2f );
            strokeWeight( (elem1.radius + elem2.radius)/2.0f / 3.8f );

            float x1 = elem1.x;
            float y1 = elem1.y;
            float x2 = elem2.x;
            float y2 = elem2.y;
            float cx = (x1 + x2) / 2.0f;
            float cy = (y1 + y2) / 2.0f;
            drawArrow(x1, y1, x2, y2);     
            //text(edge.toString(), cx, cy);
        }

        
        noStroke();
        for (final VertexDisplay d : vertices.values())
            d.draw();
        
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
    

//    public void take_from_mem() {
//        if ((mem.getTime() != lasttime) || (updateNext)) {
//            updateNext = false;
//            lasttime = mem.getTime();
//            
//            hsim.obj.clear();
//            E.clear();
//            E2.clear();
//            Sent_s.clear(); //derivation chain
//            Sent_i.clear(); //derivation chain
//            
//            int x = 0;
//            int cnt = 0;
//            ConceptBag bag = mem.concepts;
//            for (int i = bag.levels; i >= 1; i--) {
//                if (!bag.emptyLevel(i - 1)) {
//                    if (!bag.emptyLevel(i-1)) {
//                        try {
//                            for (final Concept c : Collections.unmodifiableCollection(bag.getLevel(i-1))) {
//
//                                final Term name = c.getTerm();
//
//
//                                hsim.obj.add(new Vertex(x++, i, name, 0));
//                                cnt++;
//
//                                float xsave = x;
//
//                                int bufcnt = cnt;
//
//                                if (showBeliefs) {
//                                    for (int k = 0; k < c.beliefs.size(); k++) {
//                                        Sentence kb = c.beliefs.get(k);
//                                        Term name2 = kb.getContent();
//
//                                        hsim.obj.add(new Vertex(x++, i, name2, 0, kb.getStamp().creationTime));
//                                        E.add(new Edge(bufcnt, cnt, kb.truth.getConfidence()));
//                                        Sent_s.add(kb);
//                                        Sent_i.add(cnt);
//                                        cnt++;
//
//                                    }
//                                }
//
//                                for (Task q : c.getQuestions()) {
//                                    Term name2 = q.getContent();                            
//                                    hsim.obj.add(new Vertex(x++, i, name2, 1));
//                                    E.add(new Edge(bufcnt, cnt, q.getPriority()));
//                                    cnt++;
//
//                                }
//                            }
//                        }
//                        catch (ConcurrentModificationException e) { }
//
//                    }
//                }
//                
//                if (mode == 1)
//                    x = 0;
//
//            }
//            for (int i = 0; i < hsim.obj.size(); i++) {
//                final Vertex ho = (Vertex)hsim.obj.get(i);
//                
//                for (int j = 0; j < hsim.obj.size(); j++) {
//                    Vertex target = (Vertex) hsim.obj.get(j);
//                    try {
//                        if ((ho).name.containTerm((target.name))) {
//                            int alpha = (ho.name.getComplexity() + target.name.getComplexity())/2;
//                            alpha = alpha * 10;
//                            alpha += 75;
//                            if (alpha > 255) alpha = 255;
//                            E2.add(new Edge(i, j, alpha));
//                        }
//                    } catch (Exception ex) {
//                    }
//                }
//            }
//            //autofetch=false;
//        }
//    }



    void setUpdateNext() {
        updateNext = true;
    }

    class ProcessingJs {

        ProcessingJs() {
            addMouseWheelListener(new java.awt.event.MouseWheelListener() {
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
        modeSelect.addItem("Circle");
        modeSelect.addItem("Grid");
        modeSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modeSelect.getSelectedIndex() == 0) {
                    app.mode = 0;
                }
                else {
                    app.mode = 1;
                }
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
            public void onChange(double v) {
                app.nodeSize = (float)v;
            }          
        };        
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSize);

        
        final int numLevels = n.memory.concepts.levels;
        NSlider maxLevels = new NSlider(numLevels, 1, numLevels) {
            @Override
            public void onChange(double v) {
                app.minLevel = numLevels - (int)v;
                app.setUpdateNext();
            }          
        };        
        maxLevels.setPrefix("Min Level: ");
        maxLevels.setPreferredSize(new Dimension(125, 25));
        menu.add(maxLevels);

        NSlider nodeSpeed = new NSlider(app.nodeSpeed, 0.001, 0.99) {
            @Override
            public void onChange(double v) {
                app.nodeSpeed = (float)v;
            }          
        };        
        nodeSpeed.setPrefix("Node Speed: ");
        nodeSpeed.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSpeed);        
        
        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
        

    
    }

    @Override
    protected void close() {
        app.stop();
        app.destroy();
        getContentPane().removeAll();
        app = null;
    }

    
    
    
}
