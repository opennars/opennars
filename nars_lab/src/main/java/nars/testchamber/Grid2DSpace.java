package nars.testchamber;

import automenta.vivisect.swing.NWindow;
import nars.NAR;
import nars.testchamber.Cell.Logic;
import nars.testchamber.Cell.Material;
import nars.testchamber.gui.EditorPanel;
import processing.core.PApplet;
import processing.core.PVector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.List;


public class Grid2DSpace extends PApplet {

    public static boolean world_used=false;

    public PVector target = new PVector(25, 25); //need to be init equal else feedback will
    public PVector current=new PVector(0, 0);
    
    ///////////////HAMLIB
    //processingjs compatibility layer
    int mouseScroll = 0;
    public final Hauto cells;
    public List<GridObject> objects = new ArrayList();
    //ProcessingJs processingjs = new ProcessingJs();
    //Hnav 2D navigation system
    Hnav hnav = new Hnav();
    //Object
    float selection_distance = 10;
    public float maxNodeSize = 40.0f;
    
    /** timing */
    float FrameRate = 50.0f;
    int automataPeriod = 1; //how many cycles between each automata update
    int agentPeriod = 1;  //how many cycles between each agent update
    
    boolean drawn = false;
    Hsim hsim = new Hsim();
    Hamlib hamlib = new Hamlib();
    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
//    int sx = 800;
//    int sy = 800;
    long lasttime = -1;
    double realtime;
    //public ParticleSystem particles;
    public NAR nar;
    
    public Grid2DSpace(Hauto cells, NAR nar) {

        this.cells = cells;
        world_used=true;
        this.nar=nar;

        
    }

    
    public void add(GridObject g) {
        objects.add(g);
        g.init(this);
    }
    
    public NWindow newWindow(int width, int height, boolean exitOnClose) {
        
        NWindow j = new NWindow("") {
            
            @Override
            protected void close() {
                stop();
                //destroy();

                getContentPane().removeAll();
                
                if (exitOnClose)
                    System.exit(0);                
            }            
        };
        
        Container content = j.getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        
        /*final JCheckBox syntaxEnable = new JCheckBox("Syntax");
        syntaxEnable.addActionListener(new ActionListener() {
        @Override public void actionPerformed(ActionEvent e) {
        }
        });
        menu.add(syntaxEnable);
         */
        
        EditorPanel editor = new EditorPanel(this);
        NWindow editorWindow = new NWindow("Edit", editor);
        editorWindow.setSize(200, 400);
        editorWindow.setVisible(true);
        
        
        
        content.add(menu, BorderLayout.NORTH);


        setSize(width, height);

        initSurface();
        startSurface();
        //showSurface();

        getSurface().setResizable(true);

        //System.out.println(getSurface().getClass() + " " + getSurface());
        Component canvas = (Component) (getSurface().getNative());
        content.add(canvas, BorderLayout.CENTER);
        
        
        
        j.setSize(width, height);//initial size of the window
        j.setVisible(true);

        j.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                frameResized(j.getWidth(), j.getHeight());
            }
        });
        canvas.addMouseWheelListener(evt -> {
            mouseScroll = -evt.getWheelRotation();
            mouseScrolled();
            drawn = false;
        });

        return j;
    }

    @Override
    public void frameResized(int w, int h) {
        super.frameResized(w, h); //To change body of generated methods, choose Tools | Templates.
        setSize(w, h);
        getSurface().setSize(w, h);
        //System.out.println("resize: " + w + " " + h);
        drawn = false;
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

    public void automataclicked(float x, float y) {
        if (x < 0 || y < 0) {
            return;
        }
        float realx = x / rendersize;
        float realy = y / rendersize;
        if (realx >= cells.w || realy >= cells.h) {
            return;
        }
        cells.clicked((int)realx,(int)realy,this);
    }

    public void updateAutomata() {
            cells.Exec();        
    }
    
    public void update(TestChamber s) {
        realtime = System.nanoTime() / 1.0e9;
        
        if (time % automataPeriod == 0 || TestChamber.executed) {
            updateAutomata();
        }
        if (time % agentPeriod == 0 || TestChamber.executed) {
            try
            {
                for (GridObject g : objects) {
                    Effect e = (g instanceof GridAgent) ? ((GridAgent)g).perceiveNext() : null;
                    g.update(e);

                    if (g instanceof GridAgent) {
                        GridAgent b = (GridAgent)g;
                        if (!b.actions.isEmpty()) {
                            Action a = b.actions.pop();
                            if (a!=null) {
                                process(b, a);
                            }
                        }

                    }
                }
            }
            catch(Exception ex)
            {}
        } 
        TestChamber.executed =false;
    }
    
    @Override
    public void draw() {
        background(0, 0, 0/*, 0.001f*/);
        pushMatrix();
         
        hnav.Transform();
        hrend_DrawBegin();
        
        drawGround();        
        drawObjects();
        drawParticles();
        
        hrend_DrawEnd();
        //popMatrix();
       
        
        
        popMatrix();
        hrend_DrawGUI();

    
    }

    
    public void process(GridAgent agent, Action action) {
        Effect e = action.process(this, agent);
        if (e!=null) {
            agent.perceive(e);
        }
    }
    
    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();
        /*if (lastclicked != null) {
        fill(255, 0, 0);
        ellipse(lastclicked.x, lastclicked.y, 10, 10);
        }*/
    }

    public void hrend_DrawGUI() {
        fill(255);
       // rect(10,10,10,10);
    }

    @Override
    public void setup() {
        if (FrameRate == 0) {
            noLoop();
        }
        else {
            loop();
            frameRate(FrameRate);
        }
        
        //particles = new ParticleSystem(this);
    }
    
    int time = 0;
    float rendersize = 1;

    public int getTime() {
        return time;
    }

    public double getRealtime() {
        return realtime;
    }
    

//    enum MotionEffect {
//        Moved, PainfullyMoved, TooHigh, TooSolid /* collision, impenetrable, bump */, Stuck /* flypaper, quicksand */, TooFar
//    }
    
    public String whyNonTraversible(GridAgent agent, int x, int y, int tx, int ty) {
        int dx = Math.abs(tx-x);
        int dy = Math.abs(ty-y);
        
        if (!((dx <= 1) && (dy <= 1)))
            return "Too far";

        if ((tx < 0) || (ty < 0) || (tx >= cells.w) || (ty >= cells.h))
            return "Out of bounds: " + tx + ' ' + ty;
            
        Cell from = cells.at(x, y);
        Cell to = cells.at(tx, ty);
                
        //System.out.println(to + " " + to.material);
        if ((to.material == Material.StoneWall) || to.is_solid || to.material==Material.Water || to.logic==Logic.BRIDGE || to.logic==Logic.UNCERTAINBRIDGE)
            return "Too solid";
        
        float maxTraversableHeight = 8;
        float dHeight = to.height - from.height;
        //if (dHeight > maxTraversableHeight)
        //    return "Too high";
     
        return null;
    }

    
    public Effect getMotionEffect(GridAgent agent, Action a, int x, int y, int tx, int ty) {
        String reason = whyNonTraversible(agent, x, y, tx, ty);
        return reason == null ? new Effect(a, true, getTime(), "Moved") : new Effect(a, false, getTime(), reason);
        
        
    }
    
    public void drawObjects() {
        pushMatrix();
        
        //shift half a cell down and right so that when an object draws, it's centerd in the middle of a cell.
        translate(rendersize/ 4.0f, rendersize/ 4.0f);

        objects.forEach(GridObject::draw);
        popMatrix();
    }
    
    public void drawParticles() {
        //PImage b = particles.particleImage;
        //this.blend(b, 0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), PImage.ADD);
        
       /* particles.tick(); //crashes
        for (Particle p : particles.p) {
            fill(p.rgba);
            rect(p.xPos, p.yPos, 0.1f,0.1f);
        }*/
    }
    
    public void drawGround() {
        time++;
        
        //for speed:
        //strokeCap(SQUARE);
        //strokeJoin(PROJECT);
        
        noStroke();
        //Hauto h=cells;
        
        for (int i = 0; i < cells.w; i++) {
            for (int j = 0; j < cells.h; j++) {
                Cell c = cells.readCells[i][j];
                float x = i*rendersize; 
                float y = j*rendersize;

        
                pushMatrix();
                translate(x, y);
                c.draw(this,j==0 || i==0 || i==cells.w-1 || j==cells.w-1,hnav.MouseToWorldCoordX(width/2),hnav.MouseToWorldCoordY(height/2),x,y,hnav.zoom);
                popMatrix();

            }
        }
        
    }


    class Hnav {

        private float savepx = 0;
        private float savepy = 0;
        private final int selID = 0;
        private float zoom = 43.0f;
        private float difx = -750;
        private float dify = -1300;
        private final int lastscr = 0;
        private final boolean EnableZooming = true;
        private final float scrollcamspeed = 1.1f;

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
        private final float camspeed = 1.0f;
        private final float scrollcammult = 0.92f;
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
                float x = hnav.MouseToWorldCoordX(mouseX);
                float y = hnav.MouseToWorldCoordY(mouseY);
                automataclicked(x, y);
            }
        }
        boolean dragged = false;

        void mouseDragged() {
            if (mouseButton == LEFT) {
                dragged = true;
                dragElems();
            }
            mousePressed();
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

    static List<PVector> Reconstruct_Taken_Path(Map<PVector, PVector> parent, PVector start, PVector target) {
        List<PVector> path = new ArrayList<>();
        path.add( target );
        while (!path.get(path.size()-1).equals(start)) {
            //since bfs found a solution, start is included for sure and thus this loop terminates for sure
            path.add(parent.get(path.get(path.size()-1)));
        }
        Collections.reverse(path);
        return path;
    }

    static List<PVector> Shortest_Path(Grid2DSpace s, GridAgent a, PVector start, PVector target) {
        Set<PVector> avoid = new HashSet<>();
        Map<PVector, PVector> parent = new HashMap<>();
        ArrayDeque<PVector> queue = new ArrayDeque<>();
        queue.add(start);


        while (!queue.isEmpty()) {
            PVector active = queue.removeFirst();

            if (avoid.contains(active))
                continue;

            avoid.add(active);

            

            if (active.equals(target)) {
                return Reconstruct_Taken_Path(parent, start, target);
            }

            for (int i = 0; i < 4; i++) {
                PVector x = new PVector();
                switch (i) {
                    case 0: 
                        x.set(active.x+1, active.y);
                        break;
                    case 1: 
                        x.set(active.x-1, active.y);
                        break;
                    case 2: 
                        x.set(active.x, active.y+1);
                        break;
                    case 3: 
                        x.set(active.x, active.y-1);
                        break;
                }

                
                if (avoid.contains(x) || 
                        s.whyNonTraversible(a, (int)active.x, (int)active.y, (int)x.x, (int)x.y)!=null)
                    continue;
                
                parent.put(x, active);
                queue.add(x);
            }
        }        
        return null;        
    }

}
