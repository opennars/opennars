package automenta.vivisect.javafx.demo;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.javafx.Spacegraph;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.concept.Concept;
import nars.guifx.NARfx;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.apache.commons.math3.util.FastMath;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph1 extends Spacegraph {

    public interface VisModel {

        Color getEdgeColor(double termPrio, double taskMean);

        Paint getVertexColor(double priority, float conf);

        double getVertexScale(Concept c);
    }


    private NAR nar;
    private Timeline time;

    static final Random rng = new XORShiftRandom();


    final AtomicBoolean nodeDirty = new AtomicBoolean(true),
            edgeDirty = new AtomicBoolean(true);

    public static Polygon newPoly(int sides, double d) {

        Polygon polygon = new Polygon();
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

    final static Font baseFont = new Font(1);

    public class TermNode extends StackPane {


        private final Term term;
        private final Text titleBar;
        private final Polygon base;
        Concept c = null;

        private double priorityDisplayed = -1;

        /**
         * granularity for discretizing displayed scales to reduces # of updates
         */
        final static double priorityDisplayedResolution = 100;


        double minSize = 32;
        double maxSize = 128;

        /**
         * cached from last set
         */
        private double scaled;
        private double tx;
        private double ty;

        private boolean hover = false;
        private Color stroke;

        public TermNode(Term t) {
            super();

            this.titleBar = new Text(t.toStringCompact());
            base = newPoly(6, 1.0);




            this.term = t;
            this.c = nar.concept(t);

            randomPosition(30, 30);

            titleBar.setFill(Color.WHITE);
            titleBar.setScaleX(0.25);
            titleBar.setScaleY(0.25);
            titleBar.setPickOnBounds(false);
            titleBar.setMouseTransparent(true);
            titleBar.setFont(baseFont);
            titleBar.setCache(true);
            titleBar.setCacheHint(CacheHint.SPEED);


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
                    base.setStrokeType(StrokeType.INSIDE);
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

            getChildren().setAll(base, titleBar);
            //getChildren().add(new Rectangle(1,1))

            update();

        }


        public void randomPosition(double bx, double by) {

            move(rng.nextDouble() * bx, rng.nextDouble() * by);
        }

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
            v[0] = getTranslateX();
            v[1] = getTranslateY();
        }

        final public TermNode move(final double x, final double y) {
            setTranslateX(this.tx = x);
            setTranslateY(this.ty = y);
            return this;
        }

        final public boolean move(final double[] v, final double threshold) {
            final double x = getTranslateX();
            final double y = getTranslateY();
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

        public double x() {
            return tx; //getTranslateX();
        }

        public double y() {
            return ty; //getTranslateY();
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
            //return getVertexScaleByConf(c);
            return getVertexScaleByPri(c);
        }

        public Color getEdgeColor(double termMean, double taskMean) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            return Color.hsb(25.0 + 100.0 * (1.0 + (termMean - taskMean)),
                    0.95f,
                    0.15f + 0.85f * (termMean + taskMean) / 2f);


//        return new Color(
//                0.5f + 0.5f * termMean,
//                0,
//                0.5f + 0.5f * taskMean,
//                0.5f + 0.5f * (termMean + taskMean)/2f
//        );
        }

    };


    public class TermEdge extends Polygon implements ChangeListener {


        TermLink termLink = null;
        final Set<TaskLink> taskLinks = new LinkedHashSet();


        //final double minPriVisiblityThresh = 0.1;
        final double minThickVisibility = 1;

        private final TermNode from;
        private final TermNode to;

        final private AtomicBoolean dirty = new AtomicBoolean(true);


        private final Translate translate;
        private final Rotate rotate;
        private final Scale scale;

        private double termlinkPriority;
        private double taskPrioSum;
        private double thicks;
        private Color color;

        final protected void dirty(boolean newValue) {
            dirty.set(newValue);
            if (newValue)
                edgeDirty.set(true);
        }


        public void delete() {
            from.localToSceneTransformProperty().removeListener(this);
            to.localToSceneTransformProperty().removeListener(this);
            taskLinks.clear();
        }

        public TermEdge(TermNode from, TermNode to) {
            super();
            this.from = from;
            this.to = to;

            //setCache(true);

            //setManaged(false);


            from.localToSceneTransformProperty().addListener(this);
            to.localToSceneTransformProperty().addListener(this);


            //getPoints().setAll(0.5d, 0d, -0.5d, -0.5d, -0.5d, +0.5d); //isoceles triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)
            getPoints().setAll(0.5d, 0d, -0.5d, 0d, -0.5d, -0.5d); //right triangle

            getTransforms().setAll(
                    translate = Transform.translate(0, 0),
                    rotate = Transform.rotate(0, 0, 0),
                    scale = Transform.scale(0, 0)
            );
        }


        @Override
        public final void changed(ObservableValue observable, Object oldValue, Object newValue) {


            int numTasks = taskLinks.size();
            final double taskSum, taskMean;
            if (numTasks > 0) {
                this.taskPrioSum = taskSum = taskLinks.stream()
                        .mapToDouble(t -> t.getPriority()).sum();//.orElse(0);
                taskMean = taskSum / numTasks;
            } else {
                taskSum = taskMean = 0;
            }

            final double termPrio = termLink != null ? termLink.getPriority() : 0;
            this.thicks = (taskSum + termPrio);
            this.color = visModel.getEdgeColor(termPrio, taskMean);


            dirty(true);
        }

        public void update() {


            setFill(color);


            if (!from.isVisible() || !to.isVisible()) {
                setVisible(false);
                return;
            }


            double fw = from.width();
            //double fh = from.height();
            double tw = to.width();
            //double th = to.height();
            double thicks = this.thicks * Math.max(tw, fw) / 4;

            if (thicks < minThickVisibility) {
                setVisible(false);
                return;
            }

            setVisible(true);

            double x1 = from.x();// + fw / 2d;
            double y1 = from.y();// + fh / 2d;
            double x2 = to.x();// + tw / 2d;
            double y2 = to.y();// + th / 2d;
            double dx = (x1 - x2);
            double dy = (y1 - y2);
            double len = Math.sqrt(dx * dx + dy * dy);
            //double rot = Math.atan2(dy, dx);
            double rot = FastMath.atan2(dy, dx);
            double cx = 0.5f * (x1 + x2);
            double cy = 0.5f * (y1 + y2);


            translate.setX(cx);
            translate.setY(cy);
            rotate.setAngle(FastMath.toDegrees(rot));
            scale.setX(len);
            scale.setY(thicks);


            dirty(false);
        }

        public final TermNode otherNode(final TermNode x) {
            if (from == x) return to;
            return from;
        }

    }

    final Map<Term, TermNode> terms = new LinkedHashMap();
    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();

    int maxTerms = 64;

    public TermNode getTermNode(final Term t) {
        TermNode tn = terms.get(t);
        if (tn == null) {
            tn = termToAdd.get(t);
            if (tn == null) {
                tn = new TermNode(t);
                termToAdd.put(t, tn);
            }
        }
        return tn;
    }

    public TermEdge getConceptEdge(final TermNode s, final TermNode t) {
        TermEdge e = edges.get(s.term, t.term);

        if (e == null) {
            e = edgeToAdd.get(s.term, t.term);
            if (e == null) {
                e = new TermEdge(s, t);
                edgeToAdd.put(s.term, t.term, e);
            }
        }
        return e;
    }

    final Set<Term> toRemove = Global.newHashSet(1);

    public synchronized void updateGraph() {
        int n = 0;

        toRemove.addAll(terms.keySet());

        for (Concept c : nar.memory.getControl()) {

            final Term source = c.getTerm();
            TermNode sn = getTermNode(source);

            for (TaskLink t : c.getTaskLinks()) {
                Term target = t.getTarget();
                TermNode tn = getTermNode(target);
                TermEdge e = getConceptEdge(sn, tn);
                e.taskLinks.add(t);
            }

            for (TermLink t : c.getTermLinks()) {
                Term target = t.getTarget();
                TermNode tn = getTermNode(target);
                TermEdge e = getConceptEdge(sn, tn);
                e.termLink = (t);
            }

            toRemove.remove(source);

            if (n++ > maxTerms)
                break;
        }

        if (!toRemove.isEmpty()) {
            final Term[] tr = toRemove.toArray(new Term[toRemove.size()]);
            toRemove.clear();
            runLater(() -> {
                for (Term r : tr) {
                    TermNode c = terms.remove(r);
                    if (c != null)
                        removeNodes(c);

                    Map<Term, TermEdge> er = edges.rowMap().remove(r);
                    if (er != null)
                        removeEdges((Collection) er.values());

                    Map<Term, TermEdge> ec = edges.columnMap().remove(r);
                    if (ec != null)
                        removeEdges((Collection) ec.values());
                }
            });
        }

        if (!termToAdd.isEmpty()) {
            TermNode[] x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
            termToAdd.clear();
            runLater(() -> {
                for (TermNode tn : x)
                    terms.put(tn.term, tn);
                addNodes(x);
            });
        }

        if (!edgeToAdd.isEmpty()) {
            TermEdge[] x = edgeToAdd.values().toArray(new TermEdge[edgeToAdd.size()]);
            edgeToAdd.clear();
            runLater(() -> {
                for (TermEdge te : x)
                    edges.put(te.from.term, te.to.term, te);
                addEdges(x);
            });
        }


    }

    HyperassociativeMap<TermNode, TermEdge> h = null;

    protected void layoutNodes() {

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
                    return false;
                }


                @Override
                public double getRadius(TermNode termNode) {
                    //return termNode.width() / 2 / scaleFactor / 2;
                    return termRadius;
                }

                @Override
                public double getSpeedFactor(TermNode termNode) {
                    return 3 / termNode.width(); //heavier is slower, forcing smaller ones to move faster around it
                }

                @Override
                public void apply(TermNode node, double[] dataRef) {
                    node.move(dataRef, 0.01);
                }

                @Override
                protected Iterator<TermNode> getVertices() {
                    double scaleFactor = 100 + 100 * Math.sqrt(1 + terms.size());
                    setScale(scaleFactor);

                    termRadius = (float) (1.0f / Math.sqrt(terms.size() + 1));

                    setEquilibriumDistance(0f); //termRadius * 1.5f);

                    return terms.values().iterator();
                }

                @Override
                protected void edges(final TermNode nodeToQuery, Consumer<TermNode> updateFunc, boolean ins, boolean outs) {
                    for (TermEdge te : edges.values()) {
                        if (te.isVisible())
                            updateFunc.accept(te.otherNode(nodeToQuery));
                    }
                }

            };


            h.setRepulsiveWeakness(4.0);
            h.setAttractionStrength(4.0);
            h.setMaxRepulsionDistance(10.5);
        }

        h.align();

        h.apply();

        h.resetLearning();


    }

    protected void updateNodes() {

        for (TermNode n : terms.values()) {
            n.update();
        }

    }

    protected void updateEdges() {
        //if (edgeDirty.get()) {
        //edgeDirty.set(false);

        for (TermEdge e : edges.values()) {
            if (e.dirty.get())
                e.update();
        }

        //}
    }


    public NARGraph1(NAR n) {

        super();



        new NARStream(this.nar = n)
                //.stdout()
                //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")
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

        new Animate(60, a -> {
            layoutNodes();
            updateNodes();
            updateEdges();
        }).start();


    }


    public static class Animate extends AnimationTimer {

        private final Consumer<Animate> run;
        private long periodMS;
        private long last;

        public Animate(long periodMS, Consumer<Animate> r) {
            super();
            this.periodMS = periodMS;
            this.run = r;
        }

        @Override
        public void handle(final long now) {
            if (now - last > periodMS) {
                run.accept(this);
                last = now;
            }
        }
    }



}
