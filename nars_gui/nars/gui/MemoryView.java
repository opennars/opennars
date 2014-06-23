package nars.gui;

import java.util.ArrayList;
import nars.language.*;
import processing.core.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.storage.Memory;
import nars.storage.ConceptBag;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;

class applet extends PApplet implements ActionListener //(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
{

///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    ProcessingJs processingjs = new ProcessingJs();
//Hnav 2D navigation system   
    Hnav hnav = new Hnav();
//Object
    float selection_distance = 10;

    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();

    Obj lastclicked = null;

    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    Memory mem = null;

    boolean showBeliefs = true;
    
    float sx = 800;
    float sy = 800;

    ArrayList<link> E = new ArrayList<link>();
    ArrayList<link> E2 = new ArrayList<link>();
    ArrayList<link> E3 = new ArrayList<link>(); //derivation chain
    ArrayList<Sentence> Sent_s = new ArrayList<Sentence>(); //derivation chain
    ArrayList<Integer> Sent_i = new ArrayList<Integer>(); //derivation chain
    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 16;

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
        take_from_mem();
        hamlib.Update(128, 138, 128);
    }

    void hsim_ElemClicked(Obj i) {
        lastclicked = i;
    }

    void hsim_ElemDragged(Obj i) {
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
        //size((int) sx, (int) sy);

        
        hsim.obj = new ArrayList<Obj>();
        E = new ArrayList<link>();
        E2 = new ArrayList<link>();
        Sent_s = new ArrayList<Sentence>(); //derivation chain
        Sent_i = new ArrayList<Integer>(); //derivation chain
    
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
        }
        background(0);
        
    }

    public void hsim_Draw(Obj oi) {
        text(oi.name.toString(), oi.x, oi.y);
    }

    public void drawit() {
        long currentTime = mem.getTime();
        
        final ArrayList<Obj> V = hsim.obj;
        
        background(0, 0, 0);
        
        stroke(13, 13, 4);
        strokeWeight(linkWeight);
        
        for (int i = 0; i < E2.size(); i++) {
            float sizzfloat = (float) E2.size();
            float sizzi = (float) i;

            final link lin = E2.get(i);
            final Obj elem1 = V.get(lin.from);
            final Obj elem2 = V.get(lin.to);
            fill(225, 225, 225, 50); //transparent

            float mul = 0f;
            try {
                if (mem.currentBelief != null && (elem1.name.containTerm(mem.currentBelief.getContent()) || mem.currentBelief.getContent().containTerm(elem1.name))) {
                    ellipse(elem2.x, elem2.y, 100, 100);
                    mul = 1.0f;
                }
                if (mem.currentBelief != null && (elem1.name.equals(mem.currentBelief.getContent()) || mem.currentBelief.getContent().equals(elem1.name))) {
                    ellipse(elem2.x, elem2.y, 200, 200);
                    mul = 1.0f;
                }
                if (mem.currentTask != null && (elem2.name.containTerm(mem.currentTask.getContent()) || mem.currentTask.getContent().containTerm(elem2.name))) {
                    ellipse(elem2.x, elem2.y, 100, 100);
                    mul = 1.0f;
                }
                if (mem.currentTask != null && (elem2.name.equals(mem.currentTask.getContent()) || mem.currentTask.getContent().equals(elem2.name))) {
                    ellipse(elem2.x, elem2.y, 200, 200);
                    mul = 1.0f;
                }
            } catch (Exception ex) {
            }
            float addi = (128.0f + 64.0f) * mul;
            stroke(255.0f - sizzi / sizzfloat * 255.0f, 64 + addi - sizzi / sizzfloat * 255.0f, 255.0f - sizzi / sizzfloat * 255.0f, lin.alpha);

            //ellipse(elem1.x,elem1.y,10,10);
            line(elem1.x, elem1.y, elem2.x, elem2.y);
        }
        
        fill(255);
        

        for (int i = 0; i < E.size(); i++) {
            link lin = E.get(i);
            final Obj elem1 = V.get(lin.from);
            final Obj elem2 = V.get(lin.to);
            //ellipse(elem1.x,elem1.y,10,10);
            stroke(255, 255, 255, lin.alpha);
            line(elem1.x, elem1.y, elem2.x, elem2.y);
        }
        
        stroke(255, 255, 255, 127);

        for (int i = 0; i < Sent_s.size(); i++) {
            final ArrayList<Term> deriv = Sent_s.get(i).getStamp().getChain();
            final Obj elem1 = V.get(Sent_i.get(i));
            
            for (int j = 0; j < Sent_s.size(); j++) {
                
                final Obj elem2 = V.get(Sent_i.get(j));
                
                for (int k = 0; k < deriv.size(); k++) {
                    if (i != j && deriv.get(k) == Sent_s.get(j).getContent()) {
                        
                        line(elem1.x, elem1.y, elem2.x, elem2.y);
                        break;
                    } 
               }
            }
        }
        
        strokeWeight(0);
        textSize(16);
        
        for (int i = 0; i < V.size(); i++) {
            Obj elem = V.get(i);
            String suffix = ".";
            
            float rad = elem.name.getComplexity() * 20;
            float age = elem.creationTime!=-1 ? currentTime - elem.creationTime : -1;
            
                    
            float ageFactor = age == -1? 0 : (1f/(age/100.0f+1.0f));
            int ai = (int)(100.0 * ageFactor);
            
            if (elem.mode == 0) {
                fill( 155 + ai, 155 + ai, 155 + ai, 155+ai );
            } else {
                suffix = "?";
                fill(255, 255, 0, 155+ai);
            }
            
            if (suffix.equals("?")) {
                ellipse(elem.x, elem.y, rad, rad);
            } else {
                ellipse(elem.x, elem.y, rad, rad);
            }
            
            fill(255, 255, 255);
            
            String label = elem.name.toString() + suffix;
            if (elem != lastclicked) {
                if (label.length() > MAX_UNSELECTED_LABEL_LENGTH)
                    label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH-3) + "...";
                
            }
            text(label, elem.x, elem.y);
            
        }
    }
    private static final float linkWeight = 4.0f;


    public void take_from_mem() {
        if (mem.getTime() != lasttime) {
            lasttime = mem.getTime();
            
            hsim.obj.clear();
            E.clear();
            E2.clear();
            Sent_s.clear(); //derivation chain
            Sent_i.clear(); //derivation chain
            
            float y = 0;
            float x = 0;
            int cnt = 0;
            ConceptBag bag = mem.concepts;
            for (int i = bag.TOTAL_LEVEL; i >= 1; i--) {
                if (!bag.emptyLevel(i - 1)) {
                    for (int j = 0; j < bag.itemTable[i - 1].size(); j++) {
                        final Concept c = bag.itemTable[i - 1].get(j);
                                                
                        final Term name = c.getTerm();
                        hsim.obj.add(new Obj(x, y, name, 0));
                        cnt++;
                        
                        float xsave = x;

                        int bufcnt = cnt;
                        
                        if (showBeliefs) {
                            for (int k = 0; k < c.beliefs.size(); k++) {
                                Sentence kb = c.beliefs.get(k);
                                Term name2 = kb.getContent();
                                x += sx / 2.0;
                                hsim.obj.add(new Obj(x, y, name2, 0, kb.getStamp().creationTime));
                                E.add(new link(bufcnt, cnt, kb.truth.getConfidence()));
                                Sent_s.add(kb);
                                Sent_i.add(cnt);
                                cnt++;
                                y += sy / (float) 10.0 * k; //new concept
                            }
                        }
                        
                        float xneu = x;
                        x = xsave;
                        for (int k = 0; k < c.questions.size(); k++) {
                            Task q = c.questions.get(k);
                            Term name2 = q.getContent();                            
                            x -= sx / 2.0;
                            hsim.obj.add(new Obj(x, y, name2, 1));
                            E.add(new link(bufcnt, cnt, q.getPriority()));
                            cnt++;
                            y += sy / (float) 10.0 * k; //new concept
                        }
                        x = xneu;
                        x += sx / 2.0;
                        //y += sy / (float) 10.0; //new concept
                    }
                }
                x = 0;
                y += sy / (float) 20.0; //new layer

            }
            for (int i = 0; i < hsim.obj.size(); i++) {
                final Object ho = hsim.obj.get(i);
                for (int j = 0; j < hsim.obj.size(); j++) {
                    try {
                        if (((Obj) ho).name.containTerm(((Obj) hsim.obj.get(j)).name)) {
                            E2.add(new link(i, j, 150));
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            //autofetch=false;
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = ((Button) e.getSource()).getActionCommand();
        if (command.equals("Fetch")) {
            autofetch = true;
            return;
        }
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

    public class Obj {

        public float x;
        public float y;
        public final int mode;
        public final Term name;
        private final long creationTime;

        public Obj(float X, float Y, Term Name, int Mode) {
            this(X, Y, Name, Mode, -1);
        }

        public Obj(float x, float y, Term term, int mode, long creationTime) {
            this.x = x;
            this.y = y;
            this.name = term;
            this.mode = mode;
            this.creationTime = creationTime;
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
            selected = null;
        }
        Obj selected = null;

        void dragElems() {
            if (dragged && selected != null) {
                selected.x = hnav.MouseToWorldCoordX(mouseX);
                selected.y = hnav.MouseToWorldCoordY(mouseY);
                hsim_ElemDragged(selected);
            }
        }

        void checkSelect() {
            double selection_distanceSq = selection_distance*selection_distance;
            if (selected == null) {
                for (int i = 0; i < obj.size(); i++) {
                    Obj oi = (Obj) obj.get(i);
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
            hnav.Transform();
        }

        void Update(int r, int g, int b) {
            background(r, g, b);
            pushMatrix();
            Camera();
            hrend_DrawBegin();
            //hsim.Simulate();
            drawit();
            hrend_DrawEnd();
            popMatrix();
            hrend_DrawGUI();
        }
    }

    public class link {

        public final int from;
        public final int to;
        public final int alpha;

        public link(final int from, final int to, int alpha) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
        }
        public link(final int from, final int to, float alpha) {
            this(from, to, (int)(255.0 * alpha));
        }        
    }
}

public class MemoryView extends JFrame {

    applet app = null;
    static boolean had = false; //init already

    public MemoryView(NAR n) {
        this(n.memory);
    }

    public MemoryView(Memory mem) {
        super("NARS Concept Graph");
        if (had) {
            return;
        }
        had = true;

        app = new applet();
        app.init();
        app.mem = mem;
        app.getBack = new Button("Back");
        app.conceptsView = new Button("Concepts - 1 node per concept, link between if common subterm in a belief");
        app.memoryView = new Button("Entire Graph - All beliefs, link between if common subterm");
        app.fetchMemory = new Button("Fetch");

        //app.getBack.addActionListener(app);
        app.memoryView.addActionListener(app);
        app.conceptsView.addActionListener(app);
        app.fetchMemory.addActionListener(app);

        this.setSize(1000, 860);//add the size of the window
        this.setVisible(true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout());

        menu.add(app.getBack);
        menu.add(app.conceptsView);
        menu.add(app.memoryView);
        menu.add(app.fetchMemory);

        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);
    }
}
