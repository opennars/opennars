package automenta.vivisect.javafx.graph2;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.dimensionalize.IterativeLayout;
import automenta.vivisect.javafx.Spacegraph;
import automenta.vivisect.javafx.demo.Animate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Group;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.concept.Concept;
import nars.guifx.NARfx;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph1 extends Spacegraph {

    private Animate updater;
    private GraphicsContext floorGraphics;
    private double scaleFactor;

    public interface VisModel {

        Color getEdgeColor(double termPrio, double taskMean);

        Paint getVertexColor(double priority, float conf);

        double getVertexScale(Concept c);
    }


    private NAR nar;
    private Timeline time;

    final AtomicBoolean conceptsChanged = new AtomicBoolean(true);

    static final Random rng = new XORShiftRandom();


    final AtomicBoolean nodeDirty = new AtomicBoolean(true),
            edgeDirty = new AtomicBoolean(true);

    public static Polygon newPoly(int sides, double d) {

        Polygon polygon = new Polygon();
        polygon.setStrokeWidth(0);
        polygon.setStroke(null);
        double da = (2 * Math.PI) / sides, a = 0;
        double r = d / 2;
        for (int i = 0; i < sides; i++) {
            polygon.getPoints().addAll(
                    r * Math.cos(a),
                    r * Math.sin(a)
            );
            a += da;
        }
        return polygon;
    }


    final static Font nodeFont = NARfx.mono(0.5);

    final static private TermEdge[] empty = new TermEdge[0];

    public class TermNode extends Group {


        final public Map<Term,TermEdge> edge = new LinkedHashMap(8);

        /** copy of termedge values for fast iteration during rendering */
        TermEdge[] edges = null;

        private final Term term;
        private final Text titleBar;
        private final Polygon base;
        Concept c = null;

        private double priorityDisplayed = -1;

        /**
         * granularity for discretizing displayed scales to reduces # of updates
         */
        final static double priorityDisplayedResolution = 100;


        double minSize = 16;
        double maxSize = 64;

        /**
         * cached from last set
         */
        private double scaled;
        private double tx;
        private double ty;

        private boolean hover = false;
        private Color stroke;
        public Concept concept;


        public TermNode(Term t) {
            super();

            this.titleBar = new Text(t.toStringCompact());
            base = newPoly(6, 2.0);


            this.term = t;
            this.c = nar.concept(t);

            randomPosition(150, 150);

            titleBar.setFill(Color.WHITE);
            titleBar.setBoundsType(TextBoundsType.VISUAL);

            titleBar.setPickOnBounds(false);
            titleBar.setMouseTransparent(true);
            titleBar.setFont(nodeFont);
            titleBar.setTextAlignment(TextAlignment.CENTER);
            titleBar.setSmooth(false);
            titleBar.setManaged(false);

            base.setStrokeType(StrokeType.INSIDE);

            base.setOnMouseClicked(e -> {
                //System.out.println("click " + e.getClickCount());
                if (e.getClickCount() == 2) {
                    if (c != null)
                        NARfx.window(nar, c);
                }
            });

            EventHandler<MouseEvent> mouseActivity = e -> {
                if (!hover) {
                    base.setStroke(Color.ORANGE);
                    base.setStrokeWidth(0.05);
                    hover = true;
                }
            };
            //base.setOnMouseMoved(mouseActivity);
            base.setOnMouseEntered(mouseActivity);
            base.setOnMouseExited(e -> {
                if (hover) {
                    base.setStroke(null);
                    base.setStrokeWidth(0);
                    hover = false;
                }
            });

            setPickOnBounds(false);


            getChildren().setAll(base, titleBar);//, titleBar);


            update();

            base.setLayoutX(-0.5f);
            base.setLayoutY(-0.5f);

            /*titleBar.setScaleX(0.25f);
            titleBar.setScaleY(0.25f);*/
            titleBar.setLayoutX(-getLayoutBounds().getWidth() / (2) + 0.25);
            //titleBar.setY(-getLayoutBounds().getHeight()/2);
//            System.out.println(titleBar);
//            System.out.println(titleBar.getLayoutBounds());
//            System.out.println(titleBar.getLocalToParentTransform());
//            System.out.println(titleBar.getLocalToSceneTransform());
//            System.out.println(titleBar.getBoundsInLocal());


            //setCache(true);
            //setCacheHint(CacheHint.SPEED);
            base.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            base.setCache(true);

            titleBar.setCacheHint(CacheHint.SCALE_AND_ROTATE);
            titleBar.setCache(true);


            setCacheShape(true);

            /*double s = 1.0 / titleBar.getBoundsInLocal().getWidth();

            titleBar.setScaleX(s);
            titleBar.setScaleY(s);*/

            //getChildren().add(new Rectangle(1,1))

        }


        public void randomPosition(double bx, double by) {

            move(rng.nextDouble() * bx, rng.nextDouble() * by);
        }

        /** NAR update thread */
        public void update() {

            double vertexScaling = visModel.getVertexScale(c);

            if ((int) (priorityDisplayedResolution * priorityDisplayed) !=
                    (int) (priorityDisplayedResolution * vertexScaling)) {

                double scale = minSize + (maxSize - minSize) * vertexScaling;
                this.scaled = scale;

                setScaleX(scale);
                setScaleY(scale);

                float conf = c != null ? c.getBeliefs().getConfidenceMax(0, 1) : 0;
                base.setFill(visModel.getVertexColor(vertexScaling, conf));

                //setOpacity(0.75f + 0.25f * vertexScaling);

                this.priorityDisplayed = vertexScaling;
                //System.out.println(scale + " " + vertexScaling + " " + (int)(priorityDisplayedResolution * vertexScaling));
            }

        }


        public void getPosition(final double[] v) {
            v[0] = tx;
            v[1] = ty;
        }

        Point2D sceneCoord;// = new Point2D(0,0);

        final public TermNode move(final double x, final double y) {
            setTranslateX(this.tx = x);
            setTranslateY(this.ty = y);

            sceneCoord = null;
            return this;
        }


        final public void move(final double[] v, final double speed, final double threshold) {
            final double px = tx;
            final double py = ty;
            final double momentum = 1f - speed;
            final double nx = v[0] * speed + px * momentum;
            final double ny = v[1] * speed + py * momentum;
            final double dx = Math.abs(px - nx);
            final double dy = Math.abs(py - ny);
            if ((dx > threshold) || (dy > threshold)) {
                move(nx, ny);
            }
        }

        final public boolean move(final double[] v, final double threshold) {
            final double x = tx;
            final double y = ty;
            final double nx = v[0];
            final double ny = v[1];
            if (!((Math.abs(x - nx) < threshold) && (Math.abs(y - ny) < threshold))) {
                move(nx, ny);
                return true;
            }
            return false;
        }

        public double width() {
            return scaled; //getScaleX();
        }

        public double height() {
            return scaled; //getScaleY();
        }

        public double sx() {
            if (sceneCoord == null) sceneCoord = localToScene(0,0);
            return sceneCoord.getX();
        }
        public double sy() {

            if (sceneCoord == null) sceneCoord = localToScene(0,0);
            return sceneCoord.getY();
        }

        public double x() { return tx; }

        public double y() {
            return ty;
        }

        public TermEdge putEdge(Term b, TermEdge e) {
            TermEdge r = edge.put(b, e);
            if (e!=r)
                edges = null;
            return r;
        }

        public TermEdge[] updateEdges() {
            final int s = edge.size();
            return edges = edge.values().toArray(new TermEdge[s]);
        }

        /** untested */
        public void removeEdge(TermEdge e) {
            if (edge.remove(e.bSrc)!=e) {
                throw new RuntimeException("wtf");
            }
            edges = null;
        }

        public TermEdge[] getEdges() {
            if (edges == null) {
                if (edge.size() > 0)
                    updateEdges();
                else
                    edges = empty;
            }

            return edges;
        }

    }

    public VisModel visModel = new VisModel() {

        public Color getVertexColor(double priority, float conf) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            if (!Double.isFinite(conf)) {
                conf = 0;
            }

            return Color.hsb(250.0 + 75.0 * (conf),
                    0.10f + 0.85f * priority,
                    0.10f + 0.5f * priority);


        }


        public double getVertexScaleByPri(Concept c) {
            return (c != null ? c.getPriority() : 0);
        }

        public double getVertexScaleByConf(Concept c) {
            double conf = c.getBeliefs().getConfidenceMax(0, 1);
            if (Double.isFinite(conf)) return conf;
            return 0;
        }

        @Override
        public double getVertexScale(Concept c) {
            return getVertexScaleByConf(c) * 0.75f + 0.25f;
            //return getVertexScaleByPri(c);
        }

        public Color getEdgeColor(double termMean, double taskMean) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
                    0.95f,
                    Math.min(0.25f + 0.75f * (termMean + taskMean) / 2f, 1f),
                    0.5 * (termMean + taskMean)
                    );


//        return new Color(
//                0.5f + 0.5f * termMean,
//                0,
//                0.5f + 0.5f * taskMean,
//                0.5f + 0.5f * (termMean + taskMean)/2f
//        );
        }

    };

    float edgeThickness = 0.003f;

    public class TermEdge /*xtends Group implements ChangeListener*/ {

        public final TermNode bSrc;
        public final TermNode aSrc;
        final TermEdgeHalf a, b;

//        private final Translate translate;
//        private final Rotate rotate;
//        private final Scale scale;
        //private AtomicBoolean changed = new AtomicBoolean(true);
        public double len;
        public boolean visible = false;

        public TermEdge(TermNode aSrc, TermNode bSrc) {

            this.aSrc = aSrc;
            this.bSrc = bSrc;

            a = new TermEdgeHalf(aSrc, bSrc, this);
            //a.setVisible(false);
            b = new TermEdgeHalf(bSrc, aSrc, this);
            //b.setVisible(false);

            if (aSrc.term.compareTo(bSrc.term) > 0) {
                throw new RuntimeException("invalid term order for TermEdge: " + aSrc + " " + bSrc);
            }

            //getChildren().setAll(a, b);

            //aSrc.layoutXProperty().addListener(this);
            //aSrc.layoutXProperty().addListener(this);
//            aSrc.localToSceneTransformProperty().addListener(this);
//            bSrc.localToSceneTransformProperty().addListener(this);


//            getTransforms().setAll(
//                    translate = Transform.translate(0, 0),
//                    rotate = Transform.rotate(0, 0, 0),
//                    scale = Transform.scale(0, 0)
//            );
//
//            setNeedsLayout(false);
//            setCacheShape(true);
            //setCache(true);
            //setCacheHint(CacheHint.DEFAULT);
        }

        public void delete() {
//            aSrc.localToSceneTransformProperty().removeListener(this);
//            bSrc.localToSceneTransformProperty().removeListener(this);
        }

//        @Override
//        public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//
//            changed.set(true);
//
//        }

        //        private void setA(TermNode aSrc) {
//            this.aSrc = aSrc;
//            a.setVisible(aSrc!=null);
//        }
//
//        private void setB(TermNode bSrc) {
//            this.bSrc = bSrc;
//            b.setVisible(bSrc!=null);
//        }

//        /** fx thread */
//        public boolean OLDrender() {
//
//            //changed.set(false);
//
//
//            if (!aSrc.isVisible() || !bSrc.isVisible()) {
//                setVisible(false);
//                return false;
//            }
//
//            double x1 = aSrc.x();// + fw / 2d;
//            double y1 = aSrc.y();// + fh / 2d;
//            double x2 = bSrc.x();// + tw / 2d;
//            double y2 = bSrc.y();// + th / 2d;
//            double dx = (x1 - x2);
//            double dy = (y1 - y2);
//            this.len = Math.sqrt(dx * dx + dy * dy);
//            //len-=fw/2;
//
//            //double rot = Math.atan2(dy, dx);
//            double rot = FastMath.atan2(dy, dx);
//            double cx = 0.5f * (x1 + x2);
//            double cy = 0.5f * (y1 + y2);
//
//
//            translate.setX(cx);
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);
//
//
//            return a.update() || b.update();
//        }

        public final TermNode otherNode(final TermNode x) {
            if (aSrc == x) return bSrc;
            return aSrc;
        }

        double[] xp = new double[3];
        double[] yp = new double[3];
        double[] xr = new double[3];
        double[] yr = new double[3];

        public boolean render(final GraphicsContext g) {

            if (!aSrc.isVisible() || !bSrc.isVisible()) {
                return false;
            }

            boolean aVis = a.update(),bVis = b.update();
            visible = (aVis || bVis);
            if (!visible) return false;

            double x1 = aSrc.sx();// + fw / 2d;
            double y1 = aSrc.sy();// + fh / 2d;
            double x2 = bSrc.sx();// + tw / 2d;
            double y2 = bSrc.sy();// + th / 2d;
            double dx = (x1 - x2);
            double dy = (y1 - y2);
            //this.len = Math.sqrt(dx * dx + dy * dy);
            //len-=fw/2;

            //double rot = Math.atan2(dy, dx);
            double rot = /*Fast*/Math.atan2(dy, dx);

            //double cx = 0.5f * (x1 + x2);
            //double cy = 0.5f * (y1 + y2);

            //norm vector
            double nx = Math.sin(rot);
            double ny = Math.cos(rot);

            //Affine.translate(cx,cy).rotate(rot, 0,0).scale(len,len)
//            translate.setY(cy);
//            rotate.setAngle(FastMath.toDegrees(rot));
//            scale.setX(len);
//            scale.setY(len);


            if (aVis) {
                render(g, a, x1, y1, x2, y2, nx, ny, xp, yp);
            }
            if (bVis) {
                render(g, b, x2, y2, x1, y1, -nx, -ny, xr, yr);
            }

            return true;
        }

        protected void render(GraphicsContext g, TermEdgeHalf e, double x1, double y1, double x2, double y2, double nx, double ny, double[] X, double[] Y ) {
            final double t = e.thickness;
            X[0] = x1;
            Y[0] = y1;
            X[1] = x1 + nx * t;
            Y[1] = y1 + ny * t;
            X[2] = x2;
            Y[2] = y2;

            g.setFill(e.fill);
            g.fillPolygon(X, Y, 3);
        }
    }

    public class TermEdgeHalf  {

        private final TermEdge edge;
        long lastUpdate = -1;

        final double minThickVisibility = 0.25;

        private final TermNode from;
        private final TermNode to;

        float taskPri = 0, termPri = 0;
        private int tasks;

        //SimpleDoubleProperty thickness = new SimpleDoubleProperty();
        public Color fill;
        private double thickness;

        public void set(TaskLink t, long when) {
            if (lastUpdate != when) {
                reset(when);
            }

            taskPri += t.getPriority();
            tasks++;
        }

        protected void reset(long when) {
            //edge.changed.set(true);
            taskPri = termPri = 0;
            tasks = 0;
            lastUpdate = when;
        }

        public void set(TermLink t, long when) {
            if (lastUpdate != when) {
                reset(when);
            }

            termPri += t.getPriority();
        }

//        final protected void dirty(boolean newValue) {
//            dirty.set(newValue);
//            if (newValue)
//                edgeDirty.set(true);
//        }
//


        public TermEdgeHalf(TermNode from, TermNode to, TermEdge termEdge) {
            super();
            this.from = from;
            this.to = to;
            this.edge = termEdge;


            //setManaged(false);

            //getPoints().setAll(0.5d, 0d, -0.5d, -0.5d, -0.5d, +0.5d); //isoceles triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)

//            double q = 0.25f;
//            if (!order(from.term, to.term)) {
//                getPoints().setAll(0.5d, 0d, -0.5d, q, -0.5d, -q); //right triangle
//            } else {
//                //180deg rotate
//                getPoints().setAll(-0.5d, 0d, 0.5d, -q, 0.5d, q); //right triangle
//            }



        }


        public boolean update() {
            if (termPri > 1) termPri = 1;
            float taskPriMean = tasks > 0 ? taskPri / tasks : 0;
            if (taskPriMean > 1) taskPriMean = 1f;

            double t = 0.5f * (taskPriMean + termPri);

            boolean vis = (t > minThickVisibility);
            if (!vis)
                return false;

            double fw = from.width();
            //double fh = from.height();
            double tw = to.width();
            //double th = to.height();
            double thickness = t * Math.min(fw, tw);
            this.thickness = thickness;

            this.fill = visModel.getEdgeColor(termPri, taskPri / tasks);

            return true;
        }

//        public final void updateIfVisible() {
//
//
////            int numTasks = taskLinks.size();
////            final double taskSum, taskMean;
////            if (numTasks > 0) {
////                this.taskPrioSum = taskSum = taskLinks.stream()
////                        .mapToDouble(t -> t.getPriority()).sum();//.orElse(0);
////                taskMean = taskSum / numTasks;
////            } else {
////                taskSum = taskMean = 0;
////            }
//
//            //temporary
//            //float taskMean = termPri + taskPri;
//
//            //final double termPrio = termLink != null ? termLink.getPriority() : 0;
//            //this.thickness = (taskMean + termPrio);
//
//            //dirty(false);
//        }


    }


    final Map<Term, TermNode> terms =
            new LinkedHashMap();
            //Collections.synchronizedMap(new LinkedHashMap());
    final List<TermNode> termList = Global.newArrayList();

    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    //final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();

    int maxTerms = 64;

    public TermNode getTermNode(final Term t, boolean createIfMissing) {
        TermNode tn = terms.get(t);
        if (tn == null) {
            if (createIfMissing) {
                tn = termToAdd.computeIfAbsent(t, (k) -> {
                    return new TermNode(k);
                });
            }
        }
        return tn;
    }

    public TermEdgeHalf getConceptEdgeHalf(final TermNode s, final TermNode t) {
        TermEdge parent = getConceptEdge(s, t);
        if (order(s.term, t.term)) {
            return parent.a;
        } else {
            return parent.b;
        }
    }

    static boolean order(final Term x, final Term y) {
        final int i = x.compareTo(y);
        if (i == 0) throw new RuntimeException("order=0 but must be non-equal");
        return i < 0;
    }

    public TermEdge getEdge(Term a, Term b){
        TermNode n = getTermNode(a, false);
        if (n != null) {
            return n.edge.get(b);
        }
        return null;
    }
    public boolean addEdge(Term a, Term b, TermEdge e){
        TermNode n = getTermNode(a, false);
        if (n != null) {
            return n.putEdge(b, e)==null;
        }
        return false;
    }

    public TermEdge getConceptEdge(TermNode s, TermNode t) {

        if (!order(s.term, t.term)) {
            TermNode x = s;
            s = t;
            t = x;
        }

        TermEdge e = getEdge(s.term, t.term);

        if (e == null) {
            e = edgeToAdd.get(s.term, t.term);
            if (e == null) {
                e = new TermEdge(s, t);
                edgeToAdd.put(s.term, t.term, e);
            }
        }
        return e;
    }


    final Set<TermNode> active = Global.newHashSet(1);


    protected void refresh(TermNode sn, Concept c, long now) {
        final Term source = c.getTerm();

        sn.concept = c;

        c.getTaskLinks().forEach(t -> {
            Term target = t.getTarget();
            if (!source.equals(target.getTerm())) {
                TermNode tn = getTermNode(target, false);
                if (tn!=null) {
                    TermEdgeHalf e = getConceptEdgeHalf(sn, tn);
                    e.set(t, now);
                }
            }
        });

        c.getTermLinks().forEach(t -> {
            TermNode tn = getTermNode(t.getTarget(), false);
            if (tn !=null) {
                TermEdgeHalf e = getConceptEdgeHalf(sn, tn);
                e.set(t, now);
            }
        });
    }

    public void updateGraph() {
        int n = 0;

        if (!isVisible()) return;


        final long now = nar.time();

        if (conceptsChanged.getAndSet(false)) {
            active.clear();

            nar.memory.getControl().forEach(maxTerms, c -> {

                final Term source = c.getTerm();

                TermNode sn = getTermNode(source, true);

                active.add(sn);

                refresh(sn, c, now);
            });
        }
        else {
            active.forEach(sn -> {
               refresh(sn, sn.concept, now);
            });
        }


        final TermNode[] x;
        if (!termToAdd.isEmpty()) {
            x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
            termToAdd.clear();
        } else x = null;

        final TermEdge[] y;
        if (!edgeToAdd.isEmpty()) {
            y = edgeToAdd.values().toArray(new TermEdge[edgeToAdd.size()]);
            edgeToAdd.clear();
        } else y = null;

        if (x != null || y != null) {

            runLater(() -> {

                if (x != null) {
                    for (final TermNode tn : x)
                        terms.put(tn.term, tn);

                    addNodes(x);
                }

                if (y != null) {
                    for (final TermEdge te : y) {
                        addEdge(te.aSrc.term, te.bSrc.term, te);
                    }
                    //addEdges(y);
                }

                List<TermNode> toDetach = new ArrayList();
                //List<TermEdge> toDetachEdge = new ArrayList();

                getVertices().forEach(nn -> {
                    if (!(nn instanceof TermNode)) return;

                    TermNode r = (TermNode)nn;
                    if (!active.contains(r)) {
                        TermNode c = terms.remove(r.term);

                        if (c!=null) {
                            c.setVisible(false);
                            toDetach.add(c);
                        }


//                        Map<Term, TermEdge> er = edges.rowMap().remove(r.term);
//                        /*if (er != null)
//                            toDetachEdge.addAll((Collection) er.values());*/
//
//                        Map<Term, TermEdge> ec = edges.columnMap().remove(r.term);
//                        /*if (ec != null)
//                            toDetachEdge.addAll((Collection) ec.values());*/

                    }
                });

                removeNodes((Collection)toDetach);
                //removeEdges((Collection)toDetachEdge);

                termList.clear();
                termList.addAll(terms.values());
                //print();

            });

        }

        updateNodes();


    }

    @FunctionalInterface
    public interface PreallocatedResultFunction<X, Y> {
        public void apply(X x, Y setResultHereAndReturnIt);
    }

    @FunctionalInterface
    public interface PairConsumer<A, B> {
        public void accept(A a, B b);
    }

    public static class CircleLayout<N, E> implements IterativeLayout<N, E> {


        public void run(Collection<N> verts,
                        //PreallocatedResultFunction<N,double[]> getPosition,
                        ToDoubleFunction<N> radiusFraction,
                        ToDoubleFunction<N> angle,
                        PairConsumer<N, double[]> setPosition) {


            double d[] = new double[2];

            verts.forEach(v -> {
                final double r = radiusFraction.applyAsDouble(v);
                final double a = angle.applyAsDouble(v);
                d[0] = Math.cos(a) * r;
                d[1] = Math.sin(a) * r;
                setPosition.accept(v, d);
            });

        }

        @Override
        public ArrayRealVector getPosition(N vertex) {
            return null;
        }

        @Override
        public void run(int iterations) {

        }

        @Override
        public void resetLearning() {

        }

        @Override
        public double getRadius(N vertex) {
            return 0;
        }

    }

    ;

    IterativeLayout<TermNode, TermEdge> layout = null;

    HyperassociativeMap<TermNode, TermEdge> h = null;

    protected void layoutNodes() {
        //layoutNodesCircle();
        layoutNodesHyper();
    }

    protected void layoutNodesCircle() {
        if (layout == null) {
            layout = new CircleLayout<TermNode, TermEdge>();
        }

        double[] i = new double[1];
        double numFraction = Math.PI * 2.0 * 1.0 / termList.size();
        double radiusMin = (termList.size() + 1) * 10;
        double radiusMax = 3f * radiusMin;

        ((CircleLayout<TermNode, TermEdge>) layout).
                run(termList,
                        (v) -> {
                            double r = 1f - (v.c != null ? v.c.getPriority() : 0);
                            double min = radiusMin;
                            double max = radiusMax;
                            return r * (max - min) + min;
                        },
                        (v) -> {
                            //return Math.PI * 2 * (v.term.hashCode() % 8192) / 8192.0;

                            i[0] += numFraction;
                            return i[0];
                        },
                        (v, d) -> {
                            v.move(d[0], d[1]);//, 0.5f, 1f);
                        });


    }

    protected void layoutNodesHyper() {


        if (h == null) {


            h = new HyperassociativeMap<TermNode, TermEdge>(2) {
                float termRadius = 1;

                @Override
                public void getPosition(final TermNode node, final double[] v) {
                    node.getPosition(v);
                }

                /*
                @Override
                public double getEdgeWeight(TermEdge termEdge) {
                    ///doesnt do anything in this anymore
                }
                */

                /*@Override
                public double getRadius(TermNode termNode) {
                    return super.getRadius(termNode);
                }*/

                @Override
                public boolean normalize() {
                    return true;
                }


                @Override
                public double getRadius(TermNode termNode) {

                    //return termNode.width() / 4f;
                    return 0;
                }

                @Override
                public double getSpeedFactor(TermNode termNode) {
                    //return 120 + 120 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
                    return scaleFactor*2f;
                }

                @Override
                public void apply(final TermNode node, final double[] dataRef) {

                    node.move(dataRef[0], dataRef[1]);//, 1.0, 0);
                }

                @Override
                protected Collection<TermNode> getVertices() {
                    scaleFactor = 150 + 70 * Math.sqrt(1 + termList.size());
                    setScale(scaleFactor);

                    //termRadius = (float) (1.0f / Math.sqrt(terms.size() + 1));

                    setEquilibriumDistance(0.1f); //termRadius * 1.5f);

                    return termList;
                }

                @Override
                protected void edges(final TermNode nodeToQuery, Consumer<TermNode> updateFunc, boolean ins, boolean outs) {
//                    for (final TermEdge e : edges.row(nodeToQuery.term).values()) {
//                        updateFunc.accept(e.otherNode(nodeToQuery));
//                    }

                    for (final TermEdge e : nodeToQuery.getEdges()) {
                        if (e.visible)
                            updateFunc.accept(e.otherNode(nodeToQuery));
                    }

                    /*edges.values().forEach(e ->
                            updateFunc.accept(e.otherNode(nodeToQuery)));*/

                }

            };



        }

        h.resetLearning();
        h.setLearningRate(0.4f);
        h.setRepulsiveWeakness(14.0);
        h.setAttractionStrength(7.0);
        h.setMaxRepulsionDistance(10);

        h.align(1);

        h.apply();




    }



    protected void updateNodes() {
        termList.forEach(n -> n.update());
    }

    final List<TermEdge> removable = Global.newArrayList();

    final Color FADEOUT = new Color(0,0,0,0.5);

    protected void renderEdges() {
        //if (edgeDirty.get()) {
        //edgeDirty.set(false);

        if (floorGraphics == null) floorGraphics = floorCanvas.getGraphicsContext2D();

        floorGraphics.setFill(
                //FADEOUT
                Color.BLACK
        );

        floorGraphics.fillRect(0,0, floorGraphics.getCanvas().getWidth(), floorGraphics.getCanvas().getHeight());

        floorGraphics.setStroke(null);
        floorGraphics.setLineWidth(0);

        for (int i = 0, termListSize = termList.size(); i < termListSize; i++) {
            final TermNode n = termList.get(i);
            for (final TermEdge e : n.getEdges()) {
                if (!e.render(floorGraphics)) {

                }
            }
        }

//        removable.forEach(x -> {
//            edges.remove(x.aSrc.term, x.bSrc.term);
//        });

        removable.clear();

    }


    final Consumer<Concept> ifConceptsChanged = c -> {
        this.conceptsChanged.set(true);
    };

    public NARGraph1(NAR n) {

        super();


        new NARStream(this.nar = n)
                //.stdout()
                //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")
                .onConceptActive(ifConceptsChanged)
                .onConceptForget(ifConceptsChanged)
                .forEachNthFrame(this::updateGraph, 1);
                /*.forEachCycle(() -> {
                    double[] dd = new double[4];
                    nar.memory.getControl().conceptPriorityHistogram(dd);
                    System.out.println( Arrays.toString(dd) );

                    System.out.println(
                            nar.memory.getActivePrioritySum(true, false, false) +
                            " " +
                            nar.memory.getActivePrioritySum(false, true, false) +
                            " " +
                            nar.memory.getActivePrioritySum(false, false, true)  );

                })*/



        visibleProperty().addListener(v -> {
            checkVisibility();
        });

        runLater(() -> { checkVisibility(); } );

    }

    protected void checkVisibility() {
        if (isVisible())
            start();
        else
            stop();
    }

    protected void start() {
        synchronized (nar) {
            if (this.updater == null) {
                this.updater = new Animate(75, a -> {
                    if (!termList.isEmpty()) {
                        layoutNodes();
                        renderEdges();
                    }
                });
                updater.start();
            }
        }
    }

    protected void stop() {
        synchronized (nar) {
            if (this.updater != null) {
                updater.stop();
                updater = null;
            }
        }
    }

//    private class TermEdgeConsumer implements Consumer<TermEdge> {
//        private final Consumer<TermNode> updateFunc;
//        private final TermNode nodeToQuery;
//
//        public TermEdgeConsumer(Consumer<TermNode> updateFunc, TermNode nodeToQuery) {
//            this.updateFunc = updateFunc;
//            this.nodeToQuery = nodeToQuery;
//        }
//
//        @Override
//        public void accept(TermEdge te) {
//            if (te.isVisible())
//                updateFunc.accept(te.otherNode(nodeToQuery));
//        }
//    }
}
