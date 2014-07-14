/*
 * Copyright (C) 2014 me
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.guifx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.language.Term;
import nars.storage.ConceptBag;
import nars.storage.Memory;

/**
 *
 * //http://blog.jeffreyguenther.com/post/61616273121/exploring-the-possibility-of-graph-layouts-in-javafx
 * //http://docs.oracle.com/javase/8/javafx/graphics-tutorial/sampleapp3d-code.htm#CJAGGIFG
 *
 * @author me
 */
public class MemoryViewFX2D extends Application {

    final int fps = 30;
    public int mode = 0;
    boolean showBeliefs = true;
    boolean derivations = false;
    
    int minBagLevel = 10;
    
    private NARSwing nar;
    private HashSet<TermVertex> obj;


    private Paint sentenceMaterial;
    private Paint questionMaterial;
    private HashSet<Edge> nextE;
    private Paint termBeliefMaterial;
    private Paint taskQuestionsMaterial;
    private Paint termContainedMaterial;
    private Paint termDerivedMaterial;
    private Color textMaterial;
    private HashSet<TermVertex> nextObj;
    Map<Term, Node> objShapes = new HashMap();
    Map<Edge, Polygon> edgePolygons = new HashMap();

    Scale scaling = new Scale();
    Translate translation = new Translate(0, 0);
    private Group vertexDisplay;
    private Group edgeDisplay;
        
    @Override
    public void start(Stage stage) {
        nar = new NARSwing("nal/Examples/Example-MultiStep-edited.txt");
        //nar = new NARSwing("nal/Examples-1.3.3/Example_Recursion.txt");

       vertexDisplay = new Group();
        edgeDisplay = new Group();
        
        Group root = new Group(edgeDisplay, vertexDisplay);

        Scene scene = new Scene(root, 1000, 800);
        scene.setFill(Color.color(0.1, 0.1, 0.1, 1.0));
        
        

        sentenceMaterial = (Color.color(0.2, 0.5, 1.0));
        questionMaterial = (Color.color(1.0, 0.2, 0.5));
        
        termBeliefMaterial = (Color.ORANGE);
        taskQuestionsMaterial = (Color.PURPLE);
        termContainedMaterial = (Color.GRAY);
        termDerivedMaterial = (Color.YELLOW);
        
        textMaterial = (Color.WHITE);

        
 
        
        root.getTransforms().addAll(scaling, translation);
        

        stage.setScene(scene);
        stage.show();

        handleMouse(scene, root);
        setup();

        final Duration oneFrameAmt = Duration.millis(1000 / fps);
        final KeyFrame oneFrame = new KeyFrame(oneFrameAmt,
                new EventHandler() {

                    @Override
                    public void handle(Event t) {
                        update();

                    }
                }); // oneFrame

        TimelineBuilder.create()
                .cycleCount(Animation.INDEFINITE)
                .keyFrames(oneFrame)
                .build()
                .play();

    }

///////////////HAMLIB
//processingjs compatibility layer
    int mouseScroll = 0;

    float selection_distance = 10;
    public float maxNodeSize = 170f;

    TermVertex lastclicked = null;

    /*public Button getBack;
     public Button conceptsView;
     public Button memoryView;
     public Button fetchMemory;*/
    Memory mem = null;


    HashSet<Edge> E;
    HashSet<Edge> E2;
    //ArrayList<Edge> E3 = new ArrayList<Edge>(); //derivation chain
    HashMap<Sentence,TermVertex> Sent_s; //derivation chain
    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    private boolean updateNext;
    float nodeSize = 25f;
    float nodeSpeed = 0.05f;
    

    public void setup() {
        //size((int) sx, (int) sy);

        obj = new HashSet<TermVertex>();
        E = new HashSet<Edge>();
        E2 = new HashSet<Edge>();
        nextE = new HashSet<Edge>();
        nextObj = new HashSet<TermVertex>();

        Sent_s = new HashMap(); //derivation chain

        mem = nar.memory;

    }

    ArrayList<Concept> cb = new ArrayList();
    
    public void update() {
        if ((mem.getTime() != lasttime) || (updateNext)) {
            updateNext = false;
            lasttime = mem.getTime();

            Sent_s.clear(); //derivation chain
            nextE = new HashSet();
            nextObj = new HashSet();

            float x = 0;
            int cnt = 0;
            ConceptBag bag = mem.concepts;
            for (int i = bag.levels; i >= minBagLevel; i--) {
                {
                    if (!bag.emptyLevel(i - 1)) {
                        
                        //copy the array to avoid concurrentmodification exception
                        cb.clear();
                        cb.addAll(bag.getLevel(i - 1));
                        
                        for (final Concept c : cb) {

                            final Term name = c.getTerm();

                            x = x + 1;
                            TermVertex a = new TermVertex(x, i, name, 0);
                            nextObj.add(a);
                            cnt++;

                            float xsave = x;

                            int bufcnt = cnt;

                            if (showBeliefs) {
                                for (int k = 0; k < c.beliefs.size(); k++) {
                                    Sentence kb = c.beliefs.get(k);
                                    Term name2 = new Term(kb.toKey()); //kb.getContent();

                                    x = x + 0.25f;
                                    TermVertex b = new TermVertex(x, i, name2, 0, kb.getStamp().creationTime);
                                    nextObj.add(b);
                                    
                                    
                                    double hue = 0.25 + (0.25 * (kb.truth.getFrequency()-0.5));
                                    
                                    nextE.add(new Edge(a, b, kb.truth.getConfidence(),
                                            Color.hsb(255*hue, 1.0, 0.9)));
                                    Sent_s.put(kb, b);
                                    cnt++;

                                }
                            }

                            for (Task q : c.getQuestions()) {
                                //Term name2 = q.getContent();
                                Term name2 = new Term(q.getKey());
                                
                                x = x + 0.25f;
                                TermVertex b = new TermVertex(x, i, name2, 1);
                                nextObj.add(b);
                                
                                nextE.add(new Edge(a, b, q.getPriority(), taskQuestionsMaterial));
                                cnt++;

                            }

                        }
                    }
                }

                if (mode == 1) {
                    x = 0;
                }

            }
            for (final TermVertex ho : obj) {

                for (final TermVertex target : obj) {
                    if (ho == target) continue;
                    
                    if (ho.term.containTerm((target.term))) {
                        nextE.add(new Edge(ho, target, 0.25f, termContainedMaterial));
                    }
                }
            }
            //autofetch=false;
        }

        long currentTime = mem.getTime();


        //stroke(13, 13, 4);
        //strokeWeight(linkWeight);
        //final float sizzfloat = (float) E2.size();

        /*
        for (int i = 0; i < E2.size(); i++) {
            float sizzi = (float) i;

            final Edge lin = E2.get(i);
            final TermVertex elem1 = V.get(lin.from);
            final TermVertex elem2 = V.get(lin.to);

            //fill(225, 225, 225, 50); //transparent
            float mul = 0f;
            try {
                if (mem.currentBelief != null && (elem1.term.containTerm(mem.currentBelief.getContent()) || mem.currentBelief.getContent().containTerm(elem1.term))) {
                    //ellipse(elem2.x, elem2.y, 100, 100);

                    mul = 1.0f;
                }
                if (mem.currentBelief != null && (elem1.term.equals(mem.currentBelief.getContent()) || mem.currentBelief.getContent().equals(elem1.term))) {
                    //ellipse(elem2.x, elem2.y, 200, 200);
                    mul = 1.0f;
                }
                if (mem.currentTask != null && (elem2.term.containTerm(mem.currentTask.getContent()) || mem.currentTask.getContent().containTerm(elem2.term))) {
                    //ellipse(elem2.x, elem2.y, 100, 100);
                    mul = 1.0f;
                }
                if (mem.currentTask != null && (elem2.term.equals(mem.currentTask.getContent()) || mem.currentTask.getContent().equals(elem2.term))) {
                    //ellipse(elem2.x, elem2.y, 200, 200);
                    mul = 1.0f;
                }
            } catch (Exception ex) {
            }
            ///float addi = (128.0f + 64.0f) * mul;
             //stroke(255.0f - sizzi / sizzfloat * 255.0f, 64 + addi - sizzi / sizzfloat * 255.0f, 255.0f - sizzi / sizzfloat * 255.0f, lin.alpha);
            //stroke(200, 200, 200, lin.alpha);

            ////ellipse(elem1.x,elem1.y,10,10);
            //line(elem1.x, elem1.y, elem2.x, elem2.y);
        }
        */



        
        if (derivations) {
            for (Entry<Sentence,TermVertex> s : Sent_s.entrySet()) {
                final List<Term> deriv = s.getKey().getStamp().getChain();
                final TermVertex elem1 = s.getValue();

                for (Entry<Sentence,TermVertex> t : Sent_s.entrySet()) {
                    if (s == t) continue;

                    final Sentence ts = t.getKey();
                    final TermVertex elem2 = t.getValue();

                    if (deriv.contains(ts.getContent())) {
                        nextE.add(new Edge(elem1, elem2, 0.5f, termDerivedMaterial)); 
                    }

                }
            }
        }
        

        
        //System.out.println("obj " + obj.size() + " " + objShapes.size() + " " + edgePolygons.size());
        
        //update vertices
        for (TermVertex elem : obj) {
            if (!nextObj.contains(elem)) {
                vertexDisplay.getChildren().remove(objShapes.get(elem.term));
                objShapes.remove(elem.term);
            }
            else {
                exists(elem, vertexDisplay);
            }            
        }
        for (TermVertex elem : nextObj) {        
            if (!obj.contains(elem)) {
                exists(elem, vertexDisplay);
            }
        }        
        obj = nextObj;
        

        
        
                        
        //UPDATE EDGES
        for (Edge lin : E) {            
            if (!nextE.contains(lin)) {                
                edgeDisplay.getChildren().remove(edgePolygons.get(lin));
                edgePolygons.remove(lin);
            }
            else {
                exists(lin, edgeDisplay);
            }
        }
        
        for (Edge lin : nextE) {        
            if (!E.contains(lin)) {
                exists(lin, edgeDisplay);
            }
        }        
        E = nextE;
        

        
        
 
    }



    static double[] hexagonPoints = new double[2*6];
    static double[] trianglePoints = new double[2*3];
    static {        
        double a = 0;
        for (int i = 0; i < 12; i+=2) {
            hexagonPoints[i] = Math.cos(a);
            hexagonPoints[i+1] = Math.sin(a);
            a += Math.PI*2.0/6.0;
        }        
        a = Math.PI*2.0/12.0;        
        for (int i = 0; i < 6; i+=2) {
            trianglePoints[i] = Math.cos(a);
            trianglePoints[i+1] = Math.sin(a);
            a += Math.PI*2.0/3.0;
        } 
        
    }
    
    public Node updateTermVertex(final TermVertex e, final Group existing) {
        /*
        boolean active = false;
        if (mem.currentBelief != null) {
            if (e.term.containTerm(mem.currentBelief.getContent()) || mem.currentBelief.getContent().containTerm(e.term)) {
                e.active+=1.0;
            }
        }        
        e.active *= 0.99;
        
        for (Node n : existing.getChildren()) {
            if (n instanceof Polygon) {
                Polygon p = (Polygon)n;                
                p.setStrokeWidth(e.active*1.5);
            }
        } 
        */
        
        return existing;
    }
    
    public Node exists(final TermVertex e, final Group g) {        
        double size = e.getSize();
        //float age = e.creationTime != -1 ? currentTime - elem.creationTime : -1;
        //double ageFactor = age == -1 ? 0.5f : 0.5 * (1f / (age / 1000.0f + 1.0f));
        //ageFactor += 0.1f;

        
        Node existing = objShapes.get(e.term);
        if (existing != null) {
            lerp(existing, e.x, e.y);            
            return updateTermVertex(e, (Group)existing);            
        }

        Group rs = new Group();
        

        Polygon s = new Polygon(e.type == 0 ? hexagonPoints : trianglePoints);
        //s.setStroke(Color.rgb(255, 255,255,0.5f));
        s.setScaleX(size);
        s.setScaleY(size);
               
        String suffix = "";
        if (e.type == 0) {
        } else {
            suffix = "?";
        }
        
        String termtype = e.term.getClass().getSimpleName();
        double hue = (((double)termtype.hashCode()) / Integer.MAX_VALUE);
        s.setFill(Color.hsb(255*hue,0.5,0.5));
        
        String label = e.term.toString() + suffix;
        if (label.length() > MAX_UNSELECTED_LABEL_LENGTH) {
            label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH - 3) + "...";
        }

        double fontSize = size * 0.5;
        Text t = new Text(label);        
        t.setX(-size/2);
        t.setY(fontSize-size/2);
        t.setFont(new Font(fontSize));
        t.setFill(textMaterial);        
        
        
        rs.getChildren().addAll(s, t);                        
        rs.setCache(true);
        rs.setCacheHint(CacheHint.DEFAULT);
        
        g.getChildren().add(rs);
        
        objShapes.put(e.term, rs);

        return updateTermVertex(e, rs);
    }
    
    public Polygon exists(Edge l, Group g) {
        
        
        Polygon existing = edgePolygons.get(l);
        if (existing != null) {
            updateLine(l, existing);
            return existing;
        }        
        
        Polygon m = new Polygon();
        m.setOpacity(0.5);
        m.setFill(l.material);
        updateLine(l, m);
        
        edgePolygons.put(l, m);
        g.getChildren().add(m);      
        
        return m;
    }

    
    
    private static final float linkWeight = 4.0f;

    private void lerp(Node s, double tx, double ty) {
        double speed = nodeSpeed;
        double ex = s.getTranslateX();
        double ey = s.getTranslateY();
        s.setTranslateX( ex * (1.0 - speed) + tx * (speed) );
        s.setTranslateY( ey * (1.0 - speed) + ty * (speed) );
    }

    private void updateLine(final Edge l, final Polygon line) {
        
        Node v1 = objShapes.get(l.from.term);
        Node v2 = objShapes.get(l.to.term);
        if ((v1!=null) && (v2!=null)) {

            final double v1x = v1.getTranslateX();
            final double v1y = v1.getTranslateY();
            final double v2x = v2.getTranslateX();
            final double v2y = v2.getTranslateY();
            
            line.setOpacity(l.alpha);
            
            double r = (l.from.getSize()+l.to.getSize())/2.0/2.0;
            double dx = v2x - v1x;
            double dy = v2y - v1y;
            double normAngle = Math.atan2(dy,dx)+Math.PI/2;
            double ox1 = Math.cos(normAngle)*r;
            double oy1 = Math.sin(normAngle)*r;
                        
            line.getPoints().clear();
            line.getPoints().addAll(
                    v1x+ox1,
                    v1y+oy1,
                    v1x-ox1,
                    v1y-oy1,
                    v2x,
                    v2y );
            
        }
        
    }

    public class TermVertex {

        public float x;
        public float y;
        public final int type;
        public final Term term;
        private final long creationTime;
        public double active = 0;

        public TermVertex(float index, int level, Term Name, int Mode) {
            this(index, level, Name, Mode, -1);
        }



        public TermVertex(float index, int level, Term term, int type, long creationTime) {
            
            if (mode == 1) {
                final float rowHeight = maxNodeSize;
                final float colWidth = maxNodeSize;                
                this.y = (index * rowHeight);
                this.x = (level * colWidth);
            } else if (mode == 0) {
                final double radius = ((mem.concepts.levels - level) + 1)*maxNodeSize + (maxNodeSize*8);
                final float angle = index; 
                this.x = (float) (Math.cos(angle / 3.0) * radius);
                this.y = (float) (Math.sin(angle / 3.0) * radius);
            }

            this.term = term;
            this.type = type;
            this.creationTime = creationTime;
        }

        @Override
        public int hashCode() {
            return (17+term.hashCode())* 31 + type;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TermVertex) {
                TermVertex tv = (TermVertex)obj;
                return term.equals(tv.term) && tv.type==type;
                
            }
            return false;
        }

        private double getSize() {
            return (1+term.getComplexity()) * nodeSize;
        }
        

        
    }

    public class Edge {

        public final TermVertex from;
        public final TermVertex to;
        public final float alpha;
        private final Paint material;

        public Edge(final TermVertex from, final TermVertex to, float alpha, Paint m) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
            this.material = m;
        }


        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + from.hashCode();
            hash = hash * 31 + to.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Edge) {
                Edge e = (Edge)obj;
                return (e.from.equals(from)) && (e.to.equals(to));
            }
            return false;
        }

        
        
    }

    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 1.0;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;

    private void handleMouse(Scene scene, final Node root) {

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;

                if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                }
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }

                
                if (me.isPrimaryButtonDown()) {

                    translation.setX(translation.getX()
                            + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED / scaling.getX());
                    translation.setY(translation.getY()
                            + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED / scaling.getX());
                    
                } else if (me.isSecondaryButtonDown()) {
                    double z = scaling.getX();
                    double newZ = z * 1.0 + (mouseDeltaX * MOUSE_SPEED * modifier / 2500.0);
                    newZ = Math.max(0.01, newZ);
                    newZ = Math.min(3.5, newZ);
                    
                    scaling.setX(newZ);
                    scaling.setY(newZ);
                    

                } else if (me.isMiddleButtonDown()) {

                }
             
                
            }
        }); // setOnMouseDragged
    } //handleMouse    

    public static void main(String[] args) {
        launch(args);
    }
}

//class FlatPolygon extends TriangleMesh {
//
//        public FlatPolygon() {
//            super();
//            
//        }
//        public void update3(float[] points) {
//            
//            float[] texCoords = {
//                    1, 1, // idx t0
//                    1, 0, // idx t1
//                    0, 1 // idx t2
//            };
//            /**
//             * points:
//             * 1      3
//             *  -------   texture:
//             *  |\    |  1,1    1,0
//             *  | \   |    -------
//             *  |  \  |    |     |
//             *  |   \ |    |     |
//             *  |    \|    -------
//             *  -------  0,1    0,0
//             * 0      2
//             *
//             * texture[3] 0,0 maps to vertex 2
//             * texture[2] 0,1 maps to vertex 0
//             * texture[0] 1,1 maps to vertex 1
//             * texture[1] 1,0 maps to vertex 3
//             *
//             * Two triangles define rectangular faces:
//             * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
//             * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
//             */
//
//// if you use the co-ordinates as defined in the above comment, it will be all messed up
////            int[] faces = {
////                    0, 0, 1, 1, 2, 2,
////                    0, 0, 2, 2, 3, 3
////            };
//
//// try defining faces in a counter-clockwise order to see what the difference is.
////            int[] faces = {
////                    2, 2, 1, 1, 0, 0,
////                    2, 2, 3, 3, 1, 1
////            };
//
//// try defining faces in a clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 1, 0, 2, 1, 0
//            };
//
//            this.getPoints().setAll(points);
//            this.getTexCoords().setAll(texCoords);
//            this.getFaces().setAll(faces);
//            
//        }
//
//        public void update4(float[] points) {
//            
//            float[] texCoords = {
//                    1, 1, // idx t0
//                    1, 0, // idx t1
//                    0, 1, // idx t2
//                    0, 0  // idx t3
//            };
//            /**
//             * points:
//             * 1      3
//             *  -------   texture:
//             *  |\    |  1,1    1,0
//             *  | \   |    -------
//             *  |  \  |    |     |
//             *  |   \ |    |     |
//             *  |    \|    -------
//             *  -------  0,1    0,0
//             * 0      2
//             *
//             * texture[3] 0,0 maps to vertex 2
//             * texture[2] 0,1 maps to vertex 0
//             * texture[0] 1,1 maps to vertex 1
//             * texture[1] 1,0 maps to vertex 3
//             *
//             * Two triangles define rectangular faces:
//             * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
//             * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
//             */
//
//// if you use the co-ordinates as defined in the above comment, it will be all messed up
////            int[] faces = {
////                    0, 0, 1, 1, 2, 2,
////                    0, 0, 2, 2, 3, 3
////            };
//
//// try defining faces in a counter-clockwise order to see what the difference is.
////            int[] faces = {
////                    2, 2, 1, 1, 0, 0,
////                    2, 2, 3, 3, 1, 1
////            };
//
//// try defining faces in a clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 3, 0, 2, 1, 0,
//                    2, 3, 1, 0, 3, 1
//            };
//
//            this.getPoints().setAll(points);
//            this.getTexCoords().setAll(texCoords);
//            this.getFaces().setAll(faces);
//            
//        }
//    }