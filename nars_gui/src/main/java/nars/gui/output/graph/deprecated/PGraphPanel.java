//package nars.gui.output.graph.deprecated;
//
//import com.mxgraph.model.mxGeometry;
//import nars.Memory;
//import nars.gui.output.PPanel;
//import nars.nal.entity.Concept;
//import nars.nal.entity.Sentence;
//import nars.nal.entity.Term;
//import org.jgrapht.ext.JGraphXAdapter;
//import org.jgrapht.graph.DirectedMultigraph;
//
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//import java.util.Set;
//
//
//
//abstract public class PGraphPanel<V,E> extends PPanel implements ActionListener  {
////(^break,0_0)! //<0_0 --> deleted>>! (--,<0_0 --> deleted>>)!
//
/////////////////HAMLIB
////processingjs compatibility layer
//    int mouseScroll = 0;
//
//    ProcessingJs processingjs = new ProcessingJs();
////Hnav 2D navigation system
//    Hnav hnav = new Hnav();
////Object
//    float selection_distance = 10;
//    public float maxNodeSize = 200f;
//
//    Hsim hsim = new Hsim();
//
//    Hamlib hamlib = new Hamlib();
//
//
//
//    public Button getBack;
//    public Button conceptsView;
//    public Button memoryView;
//    public Button fetchMemory;
//    Memory mem = null;
//
//    public int mode = 0;
//
//    boolean showBeliefs = false;
//
//    long lasttime = -1;
//
//    boolean autofetch = true;
//    private final int MAX_UNSELECTED_LABEL_LENGTH = 32;
//    private boolean updateNext;
//    float nodeSize = 90;
//
//    DirectedMultigraph<V,E> graph;
//    JGraphXAdapter graphAdapter;
//    public boolean updating;
//    boolean drawn = false;
//
//    public PGraphPanel() {
//        super();
//    }
//
//
//    public void mouseScrolled() {
//        hamlib.mouseScrolled();
//    }
//
//    @Override
//    public void keyPressed() {
//        hamlib.keyPressed();
//    }
//
//    @Override
//    public void mouseMoved() {
//        hamlib.mouseMoved();
//    }
//
//    @Override
//    public void mouseReleased() {
//        hamlib.mouseReleased();
//    }
//
//    @Override
//    public void mouseDragged() {
//        hamlib.mouseDragged();
//    }
//
//    @Override
//    public void mousePressed() {
//        hamlib.mousePressed();
//    }
//
//    @Override
//    public void render() {
//        hamlib.Update(0, 0, 0);
//    }
//
//    void hrend_DrawBegin() {
//    }
//
//    void hrend_DrawEnd() {
//        //fill(0);
//        //text("Hamlib simulation system demonstration", 0, -5);
//        //stroke(255, 255, 255);
//        //noStroke();
//
//    }
//
//
//    @Override
//    public void setup() {
//
//        noLoop();
//        //frameRate(this.frameRateFPS);
//
//
//        //textFont(createFont("Arial",16,false));
//        textFont(createDefaultFont(14));
//        //textFont(new PFont(ChartsPanel.monofontSmall, false));
//
//    }
//
//
//
//    void drawArrowAngle(float cx, float cy, float len, float angle){
//      pushMatrix();
//      translate(cx, cy);
//      rotate(radians(angle));
//      line(0,0,len, 0);
//      line(len, 0, len - 8, -8);
//      line(len, 0, len - 8, 8);
//      popMatrix();
//    }
//
//    void drawArrow(float x1, float y1, float x2, float y2) {
//        float cx = (x1+x2)/2f;
//        float cy = (y1+y2)/2f;
//        float len = (float)Math.sqrt( (x2-x1)*(x2-x1) + (y2-y1)*(y2-y1) );
//        float a = (float)(Math.atan2(y2-y1,x2-x1)*180.0/Math.PI);
//
//        drawArrowAngle(x1, y1, len, a);
//    }
//
//    abstract public int edgeColor(E edge);
//    abstract public float edgeWeight(E edge);
//    abstract public int vertexColor(V vertex);
//        //return getColor(o.getClass().getSimpleName());
//
//
//
//    public void drawit() {
//
//        if (updating)
//            return;
//        if (graph == null) return;
//
//
//
//        Set edges = graph.edgeSet();
//        if (edges!=null) {
//            //  line(elem1.x, elem1.y, elem2.x, elem2.y);
//            for (E edge : graph.edgeSet()) {
//
//                try {
//                    if (edge == null) continue;
//
//                    int rgb = edgeColor(edge);
//                    float linkWeight = edgeWeight(edge);
//                    stroke(rgb,0.9f);//, 230f);
//                    strokeWeight(linkWeight);
//
//                    V sourceVertex = graph.getEdgeSource(edge);
//                    if (sourceVertex == null) continue;
//
//                    mxGeometry sourcePoint = graphAdapter.getCellGeometry(graphAdapter.getVertexToCellMap().get(sourceVertex));
//
//                    V targetVertex = graph.getEdgeTarget(edge);
//                    mxGeometry targetPoint = graphAdapter.getCellGeometry(graphAdapter.getVertexToCellMap().get(targetVertex));
//
//                    if ((sourcePoint == null) || (targetPoint == null))
//                        continue;
//
//                    float x1 = (float)sourcePoint.getCenterY();
//                    float y1 = (float)sourcePoint.getCenterX();
//                    float x2 = (float)targetPoint.getCenterY();
//                    float y2 = (float)targetPoint.getCenterX();
//                    float cx = (x1 + x2) / 2.0f;
//                    float cy = (y1 + y2) / 2.0f;
//                    drawArrow(x1, y1, x2, y2);
//                    text(t(edge.toString()), cx, cy);
//                }                catch (Exception e) {}
//            }
//        }
//
//        strokeWeight(0);
//        for (V vertex : graph.vertexSet()) {
//            Object cell = graphAdapter.getVertexToCellMap().get(vertex);
//            mxGeometry b = graphAdapter.getCellGeometry(cell);
//            if (b == null) continue;
//
//            int rgb = vertexColor(vertex);
//            float vertexAlpha = vertexAlpha(vertex);
//            fill(rgb, vertexAlpha*255/2);
//
//            float x = (float)b.getCenterY();
//            float y = (float)b.getCenterX();
//            double w = b.getWidth();
//            double h = b.getHeight();
//
//            float size = getVertexSize(vertex, nodeSize);
//            ellipse(x, y, size, size);
//
//
//            fill(255,255,255);
//            textSize(size/4.0f);
//            /*
//            pushMatrix();
//            translate(x, y);
//            rotate(radians(45));
//            */
//            text(t(vertex.toString()), x, y);
//            //popMatrix();
//        }
//
//
//
//    }
//
//    public String t(String s) {
//         int maxLen = 32;
//         if (s.length() > maxLen) {
//             return s.substring(0,maxLen-2) + "..";
//         }
//         return s;
//     }
//
//    public static float getVertexSize(Object o, float nodeSize) {
//        if (o instanceof Sentence) {
//            Sentence s = (Sentence)o;
//            if (s.truth!=null)
//                return (float)(nodeSize * (0.25 + 0.75 * s.truth.getConfidence()));
//            else
//                return (float)(nodeSize * (0.5));
//        }
//        else if (o instanceof Term) {
//            Term t = (Term)o;
//            return (float)(Math.log(1+1 + t.getComplexity()) * nodeSize);
//        }
//        else if (o instanceof Concept) {
//            Term t = ((Concept) o).term;
//            return (float)(Math.log(1+2 + t.getComplexity()) * nodeSize);
//        }
//        return nodeSize;
//    }
//
//    public static float vertexAlpha(Object o) {
//        if (o instanceof Sentence) {
//            Sentence s = (Sentence)o;
//            if (s.truth!=null)
//                return (float)((0.75 + 0.25 * s.truth.getConfidence()));
//        }
//        return 1.0f;
//    }
//
//
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        String command = ((Button) e.getSource()).getActionCommand();
//        if (command.equals("Fetch")) {
//            autofetch = true;
//            return;
//        }
//    }
//
//    void setUpdateNext() {
//        updateNext = true;
//    }
//
//    class ProcessingJs {
//
//        ProcessingJs() {
//            addMouseWheelListener(new java.awt.event.MouseWheelListener() {
//                @Override
//                public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
//                    mouseScroll = -evt.getWheelRotation();
//                    mouseScrolled();
//                }
//            }
//            );
//        }
//    }
//
//    class Hnav {
//
//        private float savepx = 0;
//        private float savepy = 0;
//        private final int selID = 0;
//        private float zoom = 1.0f;
//        private float difx = 0;
//        private float dify = 0;
//        private final int lastscr = 0;
//        private final boolean EnableZooming = true;
//        private final float scrollcamspeed = 1.1f;
//
//        float MouseToWorldCoordX(int x) {
//            return 1 / zoom * (x - difx - width / 2);
//        }
//
//        float MouseToWorldCoordY(int y) {
//            return 1 / zoom * (y - dify - height / 2);
//        }
//        private boolean md = false;
//
//        void mousePressed() {
//            md = true;
//            if (mouseButton == RIGHT) {
//                savepx = mouseX;
//                savepy = mouseY;
//            }
//            drawn = false;
//        }
//
//        void mouseReleased() {
//            md = false;
//        }
//
//        void mouseDragged() {
//            if (mouseButton == RIGHT) {
//                difx += (mouseX - savepx);
//                dify += (mouseY - savepy);
//                savepx = mouseX;
//                savepy = mouseY;
//            }
//            drawn = false;
//            redraw();
//        }
//        private final float camspeed = 20.0f;
//        private final float scrollcammult = 0.92f;
//        boolean keyToo = true;
//
//        void keyPressed() {
//            if ((keyToo && key == 'w') || keyCode == UP) {
//                dify += (camspeed);
//            }
//            if ((keyToo && key == 's') || keyCode == DOWN) {
//                dify += (-camspeed);
//            }
//            if ((keyToo && key == 'a') || keyCode == LEFT) {
//                difx += (camspeed);
//            }
//            if ((keyToo && key == 'd') || keyCode == RIGHT) {
//                difx += (-camspeed);
//            }
//            if (!EnableZooming) {
//                return;
//            }
//            if (key == '-' || key == '#') {
//                float zoomBefore = zoom;
//                zoom *= scrollcammult;
//                difx = (difx) * (zoom / zoomBefore);
//                dify = (dify) * (zoom / zoomBefore);
//            }
//            if (key == '+') {
//                float zoomBefore = zoom;
//                zoom /= scrollcammult;
//                difx = (difx) * (zoom / zoomBefore);
//                dify = (dify) * (zoom / zoomBefore);
//            }
//            drawn = false;
//        }
//
//        void Init() {
//            difx = width / 2;
//            dify = height / 2;
//        }
//
//        void mouseScrolled() {
//            if (!EnableZooming) {
//                return;
//            }
//            float zoomBefore = zoom;
//            if (mouseScroll > 0) {
//                zoom *= scrollcamspeed;
//            } else {
//                zoom /= scrollcamspeed;
//            }
//            difx = (difx) * (zoom / zoomBefore);
//            dify = (dify) * (zoom / zoomBefore);
//
//            drawn = false;
//            redraw();
//        }
//
//        void Transform() {
//            translate(difx + 0.5f * width, dify + 0.5f * height);
//            scale(zoom, zoom);
//        }
//    }
//
//////Object management - dragging etc.
//
//    class Hsim {
//
//        ArrayList obj = new ArrayList();
//
//        void Init() {
//            smooth();
//        }
//
//        void mousePressed() {
//            if (mouseButton == LEFT) {
//                checkSelect();
//            }
//        }
//        boolean dragged = false;
//
//        void mouseDragged() {
//            if (mouseButton == LEFT) {
//                dragged = true;
//                dragElems();
//            }
//        }
//
//        void mouseReleased() {
//            dragged = false;
//            //selected = null;
//        }
//
//        void dragElems() {
////            if (dragged && selected != null) {
////                selected.x = hnav.MouseToWorldCoordX(mouseX);
////                selected.y = hnav.MouseToWorldCoordY(mouseY);
////                hsim_ElemDragged(selected);
////            }
//        }
//
//        void checkSelect() {
//            double selection_distanceSq = selection_distance*selection_distance;
//            /*if (selected == null)*/ {
////                for (int i = 0; i < obj.size(); i++) {
////                    Obj oi = (Obj) obj.get(i);
////                    float dx = oi.x - hnav.MouseToWorldCoordX(mouseX);
////                    float dy = oi.y - hnav.MouseToWorldCoordY(mouseY);
////                    float distanceSq = (dx * dx + dy * dy);
////                    if (distanceSq < (selection_distanceSq)) {
////                        selected = oi;
////                        hsim_ElemClicked(oi);
////                        return;
////                    }
////                }
//            }
//        }
//    }
//
////Hamlib handlers
//    class Hamlib {
//
//        void Init() {
//            noStroke();
//            hnav.Init();
//            hsim.Init();
//        }
//
//        void mousePressed() {
//            hnav.mousePressed();
//            hsim.mousePressed();
//        }
//
//        void mouseDragged() {
//            hnav.mouseDragged();
//            hsim.mouseDragged();
//        }
//
//        void mouseReleased() {
//            hnav.mouseReleased();
//            hsim.mouseReleased();
//        }
//
//        public void mouseMoved() {
//        }
//
//        void keyPressed() {
//            hnav.keyPressed();
//        }
//
//        void mouseScrolled() {
//            hnav.mouseScrolled();
//        }
//
//        void Camera() {
//            hnav.Transform();
//        }
//
//        synchronized void Update(int r, int g, int b) {
//            if (!drawn) {
//
//                background(r, g, b);
//                //pushMatrix();
//                Camera();
//                hrend_DrawBegin();
//                //hsim.Simulate();
//                try {
//                    drawit();
//                }
//                catch (Exception e) {
//                    System.err.println(e);
//                }
//                hrend_DrawEnd();
//                //popMatrix();
//
//                drawn = true;
//            }
//        }
//    }
//
// }
