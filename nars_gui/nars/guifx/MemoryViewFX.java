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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
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
public class MemoryViewFX extends Application {

    final int fps = 30;
    
    private NARSwing nar;
    private ArrayList<TermVertex> obj;

    final PerspectiveCamera camera = new PerspectiveCamera(true);

    private static final double CAMERA_INITIAL_DISTANCE = -1450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private PhongMaterial blueMaterial;
    private PhongMaterial sentenceMaterial;
    private PhongMaterial questionMaterial;
    private Group memoryDisplay;
    private HashSet<Edge> nextE;
    private Material termBeliefMaterial;
    private Material taskQuestionsMaterial;
    private Material termContainedMaterial;
    private Material termDerivedMaterial;

    @Override
    public void start(Stage stage) {
        nar = new NARSwing("nal/Examples/Example-MultiStep-edited.txt");


        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);

        Group root = new Group();

        Scene scene = new Scene(root, 1000, 800, true);
        scene.setFill(Color.color(0.2, 0.2, 0.2, 1.0));

        blueMaterial = new PhongMaterial(Color.color(0.5, 0.5, 1.0));
        sentenceMaterial = new PhongMaterial(Color.color(0.2, 0.5, 1.0));
        questionMaterial = new PhongMaterial(Color.color(1.0, 0.2, 0.5));
        
        termBeliefMaterial = new PhongMaterial(Color.ORANGE);
        taskQuestionsMaterial = new PhongMaterial(Color.PURPLE);
        termContainedMaterial = new PhongMaterial(Color.DARKGREEN);
        termDerivedMaterial = new PhongMaterial(Color.YELLOW);
        
        
        /*PhongMaterial phongMaterial = new PhongMaterial(Color.color(1.0, 0.7, 0.8));
         Cylinder cylinder1 = new Cylinder(100, 200);
         cylinder1.setMaterial(phongMaterial);
         Cylinder cylinder2 = new Cylinder(500, 500);
         cylinder2.setMaterial(phongMaterial);
         */
        memoryDisplay = new Group();
        

        /*
         Slider slider = new Slider(0, 360, 0);
         slider.setBlockIncrement(1);
         slider.setTranslateX(425);
         slider.setTranslateY(625);
         cylinder1.rotateProperty().bind(slider.valueProperty());
         cylinder2.rotateProperty().bind(slider.valueProperty());
         root.getChildren().addAll(slider);
         */
        root.getChildren().addAll( memoryDisplay);

        scene.setCamera(camera);

        stage.setScene(scene);
        stage.show();

        handleMouse(scene, root);
        setup();

        final Duration oneFrameAmt = Duration.millis(1000 / fps);
        final KeyFrame oneFrame = new KeyFrame(oneFrameAmt,
                new EventHandler() {

                    @Override
                    public void handle(Event t) {
                        update(memoryDisplay);

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
    public float maxNodeSize = 80f;

    TermVertex lastclicked = null;

    /*public Button getBack;
     public Button conceptsView;
     public Button memoryView;
     public Button fetchMemory;*/
    Memory mem = null;

    public int mode = 0;

    boolean showBeliefs = true;

    float sx = 800;
    float sy = 800;

    HashSet<Edge> E ;
    HashSet<Edge> E2;
    //ArrayList<Edge> E3 = new ArrayList<Edge>(); //derivation chain
    HashMap<Sentence,Term> Sent_s; //derivation chain
    long lasttime = -1;

    boolean autofetch = true;
    private int MAX_UNSELECTED_LABEL_LENGTH = 32;
    private boolean updateNext;
    float nodeSize = 0.5f;
    float nodeSpeed = 0.05f;
    
    float lineDZ = 10f; //offset the line layers

    public void setup() {
        //size((int) sx, (int) sy);

        obj = new ArrayList<TermVertex>();
        E = new HashSet<Edge>();
        E2 = new HashSet<Edge>();
        nextE = new HashSet<Edge>();

        Sent_s = new HashMap(); //derivation chain

        mem = nar.memory;

    }

    public void update(Group g) {
        if ((mem.getTime() != lasttime) || (updateNext)) {
            updateNext = false;
            lasttime = mem.getTime();

            obj.clear();
            //E.clear();
            Sent_s.clear(); //derivation chain
            nextE.clear();

            int x = 0;
            int cnt = 0;
            ConceptBag bag = mem.concepts;
            for (int i = bag.levels; i >= 1; i--) {
                if (!bag.emptyLevel(i - 1)) {
                    if (!bag.emptyLevel(i - 1)) {
                        for (final Concept c : bag.getLevel(i - 1)) {

                            final Term name = c.getTerm();

                            obj.add(new TermVertex(x++, i, name, 0));
                            cnt++;

                            float xsave = x;

                            int bufcnt = cnt;

                            if (showBeliefs) {
                                for (int k = 0; k < c.beliefs.size(); k++) {
                                    Sentence kb = c.beliefs.get(k);
                                    Term name2 = kb.getContent();

                                    obj.add(new TermVertex(x++, i, name2, 0, kb.getStamp().creationTime));
                                    if (bufcnt!=cnt)
                                        nextE.add(new Edge(bufcnt, cnt, kb.truth.getConfidence(), termBeliefMaterial));
                                    Sent_s.put(kb, name2);
                                    cnt++;

                                }
                            }

                            for (Task q : c.getQuestions()) {
                                Term name2 = q.getContent();
                                obj.add(new TermVertex(x++, i, name2, 1));
                                if (bufcnt!=cnt)
                                   nextE.add(new Edge(bufcnt, cnt, q.getPriority(), taskQuestionsMaterial));
                                cnt++;

                            }

                        }
                    }
                }

                if (mode == 1) {
                    x = 0;
                }

            }
            for (int i = 0; i < obj.size(); i++) {
                final TermVertex ho = (TermVertex) obj.get(i);

                for (int j = 0; j < obj.size(); j++) {
                    if (i == j) continue;
                    
                    TermVertex target = (TermVertex) obj.get(j);
                    try {
                        if ((ho).term.containTerm((target.term))) {
                            int alpha = (ho.term.getComplexity() + target.term.getComplexity()) / 2;
                            alpha = alpha * 10;
                            alpha += 75;
                            if (alpha > 255) {
                                alpha = 255;
                            }
                            
                            nextE.add(new Edge(i, j, alpha, termContainedMaterial));
                        }
                    } catch (Exception ex) {
                    }
                }
            }
            //autofetch=false;
        }

        long currentTime = mem.getTime();

        final ArrayList<TermVertex> V = obj;

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


/*
        //stroke(127, 255, 255, 127);
        for (int i = 0; i < Sent_s.size(); i++) {
            final List<Term> deriv = Sent_s.get(i).getStamp().getChain();
            //final TermVertex elem1 = V.get(Sent_i.get(i));

            for (int j = 0; j < Sent_s.size(); j++) {

                //final TermVertex elem2 = V.get(Sent_i.get(j));

                for (int k = 0; k < deriv.size(); k++) {
                    if (i != j && deriv.get(k) == Sent_s.get(j).getContent()) {

                        //line(elem1.x, elem1.y, elem2.x, elem2.y);
                        nextE.add(new Edge(i, j, 0.5f, termDerivedMaterial));
                        break;
                    }
                }
            }
        }
*/
        
        //update vertices
        for (TermVertex elem : V) {
            String suffix = ".";

            float rad = elem.term.getComplexity() * nodeSize;
            float age = elem.creationTime != -1 ? currentTime - elem.creationTime : -1;

            double ageFactor = age == -1 ? 0.5f : 0.5 * (1f / (age / 1000.0f + 1.0f));
            ageFactor += 0.1f;
            
            Shape3D s = exists(elem.term, g);

            s.setScaleX(rad);
            s.setScaleY(rad);
            s.setScaleZ(rad);
            lerp(s, elem.x/10.0, elem.y/10.0, 0);

            if (elem.type == 0) {
                s.setMaterial(sentenceMaterial);
            } else {
                suffix = "?";
                s.setMaterial(questionMaterial);
            }
            //s.setOpacity(ageFactor);
            //s.setBlendMode(BlendMode.ADD);
            

            String label = elem.term.toString() + suffix;
            if (elem != lastclicked) {
                if (label.length() > MAX_UNSELECTED_LABEL_LENGTH) {
                    label = label.substring(0, MAX_UNSELECTED_LABEL_LENGTH - 3) + "...";
                }

            }
            //text(label, elem.x, elem.y);

        }
        
        
        //UPDATE EDGES
        for (Edge lin : E) {            
            if (!nextE.contains(lin)) {
                g.getChildren().remove(lin.shape);
            }
            else {
                exists(lin, g);
            }
        }
        
        E.retainAll(nextE);
        
        for (Edge lin : nextE) {        
            if (!E.contains(lin)) {
                exists(lin, g);
                E.add(lin);
            }
        }
        
        
 
    }

    Map<Term, Shape3D> objShapes = new HashMap();

    
    public Shape3D exists(Term e, Group g) {
        
        Shape3D existing = objShapes.get(e);
        if (existing != null) {
            return existing;
        }

        Cylinder s = new Cylinder(5, 5, 6);
        s.setRotationAxis(Rotate.X_AXIS);
        s.setRotate(90.0);

        g.getChildren().add(s);
        objShapes.put(e, s);

        return s;
    }
    
    public MeshView exists(Edge l, Group g) {
        MeshView existing = l.shape;
        if (existing != null) {
            updateLine(l, existing);
            return existing;
        }        
        
        final MeshView m = new MeshView();
        m.setMaterial(l.material);
        //m.setRotationAxis(Rotate.Y_AXIS);
// try commenting this line out to see what it's effect is . . .
        //m.setCullFace(CullFace.NONE);
        g.getChildren().add(m);        
        updateLine(l, m);
        
        l.shape = m;
        
        return m;
    }

    
    
    private static final float linkWeight = 4.0f;

    private void lerp(Shape3D s, double tx, double ty, double tz) {
        double speed = nodeSpeed;
        double ex = s.getTranslateX();
        double ey = s.getTranslateY();
        double ez = s.getTranslateZ();
        s.setTranslateX( ex * (1.0 - speed) + tx * (speed) );
        s.setTranslateY( ey * (1.0 - speed) + ty * (speed) );
        s.setTranslateZ( ez * (1.0 - speed) + tz * (speed) );
    }

    private void updateLine(Edge l, MeshView line) {
        
        Shape3D v1 = objShapes.get(obj.get(l.from).term);
        Shape3D v2 = objShapes.get(obj.get(l.to).term);
        if ((v1!=null) && (v2!=null)) {

            
            /*float[] points = {
            -Width/2,  Height/2, 0, // idx p0
            -Width/2, -Height/2, 0, // idx p1
            Width/2,  Height/2, 0, // idx p2
            Width/2, -Height/2, 0  // idx p3
            };
             */
            float r = l.alpha/16f; //nodeSize*8f;
            FlatPolygon p = new FlatPolygon();
            p.update3(new float[] {
                (float)v1.getTranslateX()-r, (float)v1.getTranslateY()-r, lineDZ,
                (float)v1.getTranslateX()+r, (float)v1.getTranslateY()+r, lineDZ,
                (float)v2.getTranslateX(), (float)v2.getTranslateY(), lineDZ,
   
            });
            line.setMesh(p);
            line.computeAreaInScreen();
            


        }
        
    }

    public class TermVertex {

        public float x;
        public float y;
        public final int type;
        public final Term term;
        private final long creationTime;

        public TermVertex(int index, int level, Term Name, int Mode) {
            this(index, level, Name, Mode, -1);
        }

        private int rowHeight = (int) (maxNodeSize);
        private int colWidth = (int) (maxNodeSize);

        public TermVertex(int index, int level, Term term, int type, long creationTime) {

            if (mode == 1) {
                this.y = -200 - (index * rowHeight);
                this.x = 2600 - (level * colWidth);
            } else if (mode == 0) {
                float LEVELRAD = maxNodeSize;

                double radius = ((mem.concepts.levels - level) + 1);
                float angle = index; //TEMPORARY
                this.x = (float) (Math.cos(angle / 3.0) * radius) * LEVELRAD;
                this.y = (float) (Math.sin(angle / 3.0) * radius) * LEVELRAD;
            }

            this.term = term;
            this.type = type;
            this.creationTime = creationTime;
        }



        @Override
        public String toString() {
            return "obj:" + term;
        }        
        
        
    }

    public class Edge {

        public final int from;
        public final int to;
        public final int alpha;
        public MeshView shape;
        private final Material material;

        public Edge(final int from, final int to, int alpha, Material m) {
            this.from = from;
            this.to = to;
            this.alpha = alpha;
            shape = null;
            this.material = m;
        }

        public Edge(final int from, final int to, float alpha, Material m) {
            this(from, to, (int) (255.0 * alpha), m);            
        }

        @Override
        public int hashCode() {
            return from + to * 9999 + material.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Edge) {
                Edge e = (Edge)obj;
                return e.from == from && e.to == to && material == e.material;
            }
            return false;
        }

        
        
    }

    /**
     * Render a graph to a particular Group
     *
     * @param graph
     * @param layout
     * @param viz
     */
    /*
     private void renderGraph(Graph graph, Layout layout, Group viz) {
     // draw the vertices in the graph
     for (String v : graph.getVertices()) {
     // Get the position of the vertex
     Point2D p = layout.transform(v);
            
     // draw the vertex as a circle
     Circle circle = CircleBuilder.create()
     .centerX(p.getX())
     .centerY(p.getY())
     .radius(CIRCLE_SIZE)
     .build();
            
     // add it to the group, so it is shown on screen
     viz.getChildren().add(circle);
     }

     // draw the edges
     for (Number n : graph.getEdges()) {
     // get the end points of the edge
     Pair endpoints = graph.getEndpoints(n);
            
     // Get the end points as Point2D objects so we can use them in the 
     // builder
     Point2D pStart = layout.transform(endpoints.getFirst());
     Point2D pEnd = layout.transform(endpoints.getSecond());
            
     // Draw the line
     Line line = LineBuilder.create()
     .startX(pStart.getX())
     .startY(pStart.getY())
     .endX(pEnd.getX())
     .endY(pEnd.getY())
     .build();
     // add the edges to the screen
     viz.getChildren().add(line);
     }
     }
     */
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

                    camera.setTranslateX(camera.getTranslateX()
                            + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                    camera.setTranslateY(camera.getTranslateY()
                            + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * MOUSE_SPEED * modifier;
                    camera.setTranslateZ(newZ);

                } else if (me.isMiddleButtonDown()) {

                }
            }
        }); // setOnMouseDragged
    } //handleMouse    

    public static void main(String[] args) {
        launch(args);
    }
}

class FlatPolygon extends TriangleMesh {

        public FlatPolygon() {
            super();
            
        }
        public void update3(float[] points) {
            
            float[] texCoords = {
                    1, 1, // idx t0
                    1, 0, // idx t1
                    0, 1 // idx t2
            };
            /**
             * points:
             * 1      3
             *  -------   texture:
             *  |\    |  1,1    1,0
             *  | \   |    -------
             *  |  \  |    |     |
             *  |   \ |    |     |
             *  |    \|    -------
             *  -------  0,1    0,0
             * 0      2
             *
             * texture[3] 0,0 maps to vertex 2
             * texture[2] 0,1 maps to vertex 0
             * texture[0] 1,1 maps to vertex 1
             * texture[1] 1,0 maps to vertex 3
             *
             * Two triangles define rectangular faces:
             * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
             * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
             */

// if you use the co-ordinates as defined in the above comment, it will be all messed up
//            int[] faces = {
//                    0, 0, 1, 1, 2, 2,
//                    0, 0, 2, 2, 3, 3
//            };

// try defining faces in a counter-clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 2, 1, 1, 0, 0,
//                    2, 2, 3, 3, 1, 1
//            };

// try defining faces in a clockwise order to see what the difference is.
            int[] faces = {
                    2, 1, 0, 2, 1, 0
            };

            this.getPoints().setAll(points);
            this.getTexCoords().setAll(texCoords);
            this.getFaces().setAll(faces);
            
        }

        public void update4(float[] points) {
            
            float[] texCoords = {
                    1, 1, // idx t0
                    1, 0, // idx t1
                    0, 1, // idx t2
                    0, 0  // idx t3
            };
            /**
             * points:
             * 1      3
             *  -------   texture:
             *  |\    |  1,1    1,0
             *  | \   |    -------
             *  |  \  |    |     |
             *  |   \ |    |     |
             *  |    \|    -------
             *  -------  0,1    0,0
             * 0      2
             *
             * texture[3] 0,0 maps to vertex 2
             * texture[2] 0,1 maps to vertex 0
             * texture[0] 1,1 maps to vertex 1
             * texture[1] 1,0 maps to vertex 3
             *
             * Two triangles define rectangular faces:
             * p0, t0, p1, t1, p2, t2 // First triangle of a textured rectangle
             * p0, t0, p2, t2, p3, t3 // Second triangle of a textured rectangle
             */

// if you use the co-ordinates as defined in the above comment, it will be all messed up
//            int[] faces = {
//                    0, 0, 1, 1, 2, 2,
//                    0, 0, 2, 2, 3, 3
//            };

// try defining faces in a counter-clockwise order to see what the difference is.
//            int[] faces = {
//                    2, 2, 1, 1, 0, 0,
//                    2, 2, 3, 3, 1, 1
//            };

// try defining faces in a clockwise order to see what the difference is.
            int[] faces = {
                    2, 3, 0, 2, 1, 0,
                    2, 3, 1, 0, 3, 1
            };

            this.getPoints().setAll(points);
            this.getTexCoords().setAll(texCoords);
            this.getFaces().setAll(faces);
            
        }
    }