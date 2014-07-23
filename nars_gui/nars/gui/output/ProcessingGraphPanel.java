package nars.gui.output;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.model.mxGeometry;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.util.NARGraph;
import nars.util.NARGraph.DefaultGraphizer;
import nars.util.NARGraph.Filter;
import static nars.util.NARGraph.IncludeEverything;
import nars.gui.NSlider;
import nars.language.Term;
import nars.storage.Memory;
import org.jgrapht.ext.JGraphXAdapter;
import processing.core.PApplet;
import static processing.core.PConstants.DOWN;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;
import static processing.core.PConstants.UP;



class papplet extends PApplet implements ActionListener 
{
//(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
    
///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    ProcessingJs processingjs = new ProcessingJs();
//Hnav 2D navigation system   
    Hnav hnav = new Hnav();
//Object
    float selection_distance = 10;
    public float maxNodeSize = 200f;

    Hsim hsim = new Hsim();

    Hamlib hamlib = new Hamlib();


    public Button getBack;
    public Button conceptsView;
    public Button memoryView;
    public Button fetchMemory;
    Memory mem = null;

    public int mode = 0;
    
    boolean showBeliefs = false;
    
    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    private boolean updateNext;
    float nodeSize = 90;
    
    NARGraph graph;
    JGraphXAdapter layout;
    public boolean updating;

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
        hamlib.Update(128, 138, 128);
    }

    void hrend_DrawBegin() {
    }

    void hrend_DrawEnd() {
        //fill(0);
        //text("Hamlib simulation system demonstration", 0, -5);
        //stroke(255, 255, 255);
        //noStroke();

    }


    @Override
    public void setup() {  
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        background(0);
        
    }


    
    void drawArrowAngle(float cx, float cy, float len, float angle){
      pushMatrix();
      translate(cx, cy);
      rotate(radians(angle));
      line(0,0,len, 0);
      line(len, 0, len - 8, -8);
      line(len, 0, len - 8, 8);
      popMatrix();
    }

    void drawArrow(float x1, float y1, float x2, float y2) {
        float cx = (x1+x2)/2f;
        float cy = (y1+y2)/2f;
        float len = (float)Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
        float a = (float)(Math.atan2(y2-y1,x2-x1)*180.0/Math.PI);
        
        drawArrowAngle(x1, y1, len, a);
    }
    
    public static int getColor(Object o) {
        return getColor(o.getClass().getSimpleName());
    }
    

    public static int getColor(String s) {            
        double hue = (((double)s.hashCode()) / Integer.MAX_VALUE);
        return Color.getHSBColor((float)hue,0.7f,0.8f).getRGB();        
    }
    
    public void drawit() {
        
        if (updating)
            return;
        
        background(0, 0, 0);


        try {
            //  line(elem1.x, elem1.y, elem2.x, elem2.y);
            for (Object edge : graph.edgeSet()) {

                int rgb = getColor(edge.getClass().getSimpleName());
                stroke(rgb, 230f);            
                strokeWeight(linkWeight);                



                Object sourceVertex = graph.getEdgeSource(edge);
                mxGeometry sourcePoint = layout.getCellGeometry(layout.getVertexToCellMap().get(sourceVertex));

                Object targetVertex = graph.getEdgeTarget(edge);                          
                mxGeometry targetPoint = layout.getCellGeometry(layout.getVertexToCellMap().get(targetVertex));

                if ((sourcePoint == null) || (targetPoint == null))
                    continue;

                float x1 = (float)sourcePoint.getCenterX();
                float y1 = (float)sourcePoint.getCenterY();
                float x2 = (float)targetPoint.getCenterX();
                float y2 = (float)targetPoint.getCenterY();
                float cx = (x1 + x2) / 2.0f;
                float cy = (y1 + y2) / 2.0f;
                drawArrow(x1, y1, x2, y2);
                text(edge.toString(), cx, cy);            
            }

            strokeWeight(0);        
            for (Object vertex : graph.vertexSet()) {            
                Object cell = layout.getVertexToCellMap().get(vertex);
                mxGeometry b = layout.getCellGeometry(cell);            
                if (b == null) continue;

                int rgb = getColor(vertex.getClass().getSimpleName());
                float vertexAlpha = getVertexAlpha(vertex);
                fill(rgb, vertexAlpha*255/2);

                float x = (float)b.getCenterX();
                float y = (float)b.getCenterY();
                double w = b.getWidth();
                double h = b.getHeight();

                float size = getVertexSize(vertex, nodeSize);
                ellipse(x, y, size, size);            

                fill(255,255,255);        
                textSize(size/4.0f);
                text(vertex.toString(), x, y);
            }
        }
        catch (ConcurrentModificationException e) { }                    
    }

    
    public static float getVertexSize(Object o, float nodeSize) {
        if (o instanceof Sentence) {
            Sentence s = (Sentence)o;
            return (float)(nodeSize * (0.25 + 0.75 * s.getTruth().getConfidence()));
        }
        else if (o instanceof Term) {
            Term t = (Term)o;
            return (float)(Math.log(1+1 + t.getComplexity()) * nodeSize);
        }
        else if (o instanceof Concept) {
            Term t = ((Concept)o).getTerm();
            return (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
        }
        return nodeSize;
    }
    
    public static float getVertexAlpha(Object o) {
        if (o instanceof Sentence) {
            Sentence s = (Sentence)o;
            if (s.getTruth()!=null)
                return (float)((0.25 + 0.75 * s.getTruth().getConfidence()));            
        }
        return 1.0f;
    }    
    
    private static final float linkWeight = 6.0f;


    public void actionPerformed(ActionEvent e) {
        String command = ((Button) e.getSource()).getActionCommand();
        if (command.equals("Fetch")) {
            autofetch = true;
            return;
        }
    }

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
            difx = width / 2;
            dify = height / 2;
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
//            if (dragged && selected != null) {
//                selected.x = hnav.MouseToWorldCoordX(mouseX);
//                selected.y = hnav.MouseToWorldCoordY(mouseY);
//                hsim_ElemDragged(selected);
//            }
        }

        void checkSelect() {
            double selection_distanceSq = selection_distance*selection_distance;
            /*if (selected == null)*/ {
//                for (int i = 0; i < obj.size(); i++) {
//                    Obj oi = (Obj) obj.get(i);
//                    float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
//                    float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
//                    float distanceSq = (dx * dx + dy * dy);
//                    if (distanceSq < (selection_distanceSq)) {
//                        selected = oi;
//                        hsim_ElemClicked(oi);
//                        return;
//                    }
//                }
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
        }
    }

}

public class ProcessingGraphPanel extends JFrame {

    papplet app = null;
    private final NAR nar;
    private final Filter filter;
    float edgeDistance = 10;
    private boolean showSyntax;
    private final DefaultGraphizer graphizer;

    public ProcessingGraphPanel(NAR n) {
        this(n, new DefaultGraphizer(true,true,true,true,true), IncludeEverything);        
    }
    
    public ProcessingGraphPanel(NAR n, DefaultGraphizer graphizer, Filter filter) {
        super("NARS Graph");
        
        this.nar = n;
        this.filter = filter;
        this.graphizer = graphizer;

        app = new papplet();
        

        app.init();
        
        this.setSize(1000, 860);//initial size of the window
        this.setVisible(true);

        Container content = getContentPane();
        content.setLayout(new BorderLayout());

        JPanel menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
       
        
        final JCheckBox beliefsEnable = new JCheckBox("Syntax");
        beliefsEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showSyntax = beliefsEnable.isSelected();
                ProcessingGraphPanel.this.update();
            }
        });
        menu.add(beliefsEnable);
        
        NSlider nodeSize = new NSlider(app.nodeSize, 1, app.maxNodeSize) {
            @Override
            public void onChange(double v) {
                app.nodeSize = (float)v;
            }          
        };
        nodeSize.setPrefix("Node Size: ");
        nodeSize.setPreferredSize(new Dimension(125, 25));
        menu.add(nodeSize);

        NSlider edgeDist = new NSlider(edgeDistance, 1, 100) {
            @Override
            public void onChange(double v) {
                edgeDistance = (float)v;
                ProcessingGraphPanel.this.update();
            }          
        };
        edgeDist.setPrefix("Separation: ");
        edgeDist.setPreferredSize(new Dimension(125, 25));
        menu.add(edgeDist);        
        
        content.add(menu, BorderLayout.NORTH);
        content.add(app, BorderLayout.CENTER);

        update();
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {            
                app.stop();
                app = null;
            }
            
            //TODO stop when window becomes minimized, and restart when visible again
            
        });
        
    }
    
    


    public void update() {
        app.updating = true;
        
        graphizer.setShowSyntax(showSyntax);
        
        NARGraph g = new NARGraph();
        g.add(nar, filter, graphizer);                
        app.graph = g;
        

        // create a visualization using JGraph, via an adapter
        JGraphXAdapter layout = new JGraphXAdapter(g) {           
            
        };                
        app.layout = layout;
        
       
        /*
        mxCompactTreeLayout layout2 = 
                new mxCompactTreeLayout(jgxAdapter);                
        layout2.setUseBoundingBox(false);
        layout2.setResizeParent(true);
        layout2.setLevelDistance(30);
        layout2.setNodeDistance(50);
        layout2.execute(jgxAdapter.getDefaultParent());
        */
        
        
        mxFastOrganicLayout l = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxFastOrganicLayout(layout);
                //new mxCircleLayout(jgxAdapter);        
        l.setForceConstant(edgeDistance*10f);
        l.execute(layout.getDefaultParent());
        
        

        /*
        mxOrganicLayout layout = 
                //new mxCompactTreeLayout(jgxAdapter);
                new mxOrganicLayout(jgxAdapter);
                //new mxCircleLayout(jgxAdapter);        
        layout.setEdgeLengthCostFactor(0.001);*/

        app.updating = false;
                
        
    }
        
    
//    public static void main(String[] args) throws Exception {
//        NAR n = new NAR();
//        
//        /*
//        new TextInput(n, "<a --> b>.");
//        new TextInput(n, "<b --> c>.");
//        new TextInput(n, "<d <-> c>. %0.75;0.90%");
//        new TextInput(n, "<a --> c>?");      
//        new TextInput(n, "<a --> d>?");
//        n.run(12);
//        */
//        
//        n.addInput("<0 --> num>. %1.00;0.90% {0 : 1}");
//        n.addInput("<<$1 --> num> ==> <(*,$1) --> num>>. %1.00;0.90% {0 : 2}"); 
//        n.addInput("<(*,(*,(*,0))) --> num>?  {0 : 3}");
//       
//        n.run(500);
//        
//        new ProcessingGraphPanel(n);
//    }
}
