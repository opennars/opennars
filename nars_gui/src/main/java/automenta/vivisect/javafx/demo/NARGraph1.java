package automenta.vivisect.javafx.demo;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.javafx.Spacegraph;
import automenta.vivisect.javafx.Windget;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import nars.NAR;
import nars.concept.Concept;
import nars.event.CycleReaction;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nar.Default;
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
public class NARGraph1 extends Application {

    final Spacegraph space = new Spacegraph();
    private NAR nar;
    private Timeline time;

    static final Random rng = new XORShiftRandom();


    final AtomicBoolean edgeDirty = new AtomicBoolean(true);

    public static class TermNode extends Windget {


        private final Term term;
        Concept c = null;



        public TermNode(Term t) {
            super(t.toStringCompact());

            this.term = t;

            randomPosition(30, 30);

            titleBar.setMouseTransparent(true);
        }

        public void randomPosition(double bx, double by) {

            move(rng.nextDouble() * bx,
                    rng.nextDouble() * by);
        }

        protected void update() {

        }


        public void getPosition(final double[] v) {
            v[0] = getLayoutX();
            v[1] = getLayoutY();
        }
    }

    public static Color getEdgeColor(double termMean, double taskMean) {
        // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

        return Color.hsb(1.0 + (termMean - taskMean), 0.5f,
                0.85f,
                0.5f + 0.5f * (termMean + taskMean)/2f );

//        return new Color(
//                0.5f + 0.5f * termMean,
//                0,
//                0.5f + 0.5f * taskMean,
//                0.5f + 0.5f * (termMean + taskMean)/2f
//        );
    }

    public class TermEdge extends Polygon implements ChangeListener {

        TermLink termLink = null;
        final Set<TaskLink> taskLinks = new LinkedHashSet();

        private final TermNode from;
        private final TermNode to;

        final private AtomicBoolean dirty = new AtomicBoolean(true);


        private final Translate translate;
        private final Rotate rotate;
        private final Scale scale;

        private double termlinkPriority;
        private double taskPrioSum;

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

            setManaged(false);

            /*from.layoutXProperty().addListener(onNodeMoved);
            from.layoutYProperty().addListener(onNodeMoved);
            to.layoutXProperty().addListener(onNodeMoved);
            to.layoutYProperty().addListener(onNodeMoved);*/
            from.localToSceneTransformProperty().addListener(this);
            to.localToSceneTransformProperty().addListener(this);

            setFill(Color.ORANGE);
            setOpacity(0.5);

            getPoints().setAll(0.5d, 0d, -0.5d, -0.5d, -0.5d, +0.5d); //isoceles triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)

            getTransforms().setAll(
                    translate = Transform.translate(0,0),
                    rotate = Transform.rotate(0,0,0),
                    scale = Transform.scale(0,0)
            );
        }



        public void update() {

            //TODO move this to a TermNode specific subclass


            int numTasks = taskLinks.size();
            final double taskSum, taskMean;
            if (numTasks > 0) {
                this.taskPrioSum = taskSum = taskLinks.stream()
                        .mapToDouble(t -> t.getPriority()).sum();//.orElse(0);
                taskMean = taskSum/numTasks;
            }
            else {
                taskSum = taskMean = 0;
            }



            final double termPrio = termLink!=null ? termLink.getPriority() : 0;


            setFill(getEdgeColor(termPrio, taskMean));



            if (!from.isVisible() || !to.isVisible()) {
                setVisible(false);
                return;
            } else {
                setVisible(true);
            }

            double fw = from.getWidth();
            double fh = from.getHeight();
            double tw = to.getWidth();
            double th = to.getHeight();

            double x1 = from.getLayoutX() + fw / 2d;
            double y1 = from.getLayoutY() + fh / 2d;
            double x2 = to.getLayoutX() + tw / 2d;
            double y2 = to.getLayoutY() + th / 2d;
            double dx = (x1 - x2);
            double dy = (y1 - y2);
            double len = Math.sqrt(dx * dx + dy * dy);
            //double rot = Math.atan2(dy, dx);
            double rot = FastMath.atan2(dy, dx);
            double cx = 0.5f * (x1 + x2);
            double cy = 0.5f * (y1 + y2);

            double thicks = 10 + 20 * ((0.5 * taskSum) + termPrio);
            if (thicks > tw/2f) thicks = tw/2f;
            if (thicks > th/2f) thicks = th/2f;

            translate.setX(cx); translate.setY(cy);
            rotate.setAngle(FastMath.toDegrees(rot));
            scale.setX(len);
            scale.setY(thicks);


            dirty(false);
        }

        public final TermNode otherNode(final TermNode x) {
            if (from == x) return to;
            return from;
        }

        @Override
        public final void changed(ObservableValue observable, Object oldValue, Object newValue) {
            dirty(true);
        }
    }

    final Map<Term, TermNode> terms = new LinkedHashMap();
    final Map<Term, TermNode> termToAdd = new LinkedHashMap();
    final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final Table<Term, Term, TermEdge> edgeToAdd = HashBasedTable.create();

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

    public void updateGraph() {
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

        }

        if (!termToAdd.isEmpty()) {
            TermNode[] x = termToAdd.values().toArray(new TermNode[termToAdd.size()]);
            termToAdd.clear();
            runLater(() -> {
                for (TermNode tn : x)
                    terms.put(tn.term, tn);
                space.addNodes(x);
            });
        }

        if (!edgeToAdd.isEmpty()) {
            TermEdge[] x = edgeToAdd.values().toArray(new TermEdge[edgeToAdd.size()]);
            edgeToAdd.clear();
            runLater(() -> {
                for (TermEdge te: x)
                    edges.put(te.from.term, te.to.term, te);
                space.addEdges(x);
            });
        }


    }

    HyperassociativeMap<TermNode,TermEdge> h = null;

    protected void layoutNodes() {

        if (h == null) {
            h = new HyperassociativeMap<TermNode,TermEdge>(2) {

                @Override
                public void getPosition(final TermNode node, final double[] v) {
                    node.getPosition(v);
                }

                @Override
                public void apply(TermNode node, double[] dataRef) {
                    node.move(dataRef, 0.02);
                }

                @Override
                protected Iterator<TermNode> getVertices() {
                    return terms.values().iterator();
                }

                @Override
                protected void edges(final TermNode nodeToQuery, Consumer<TermNode> updateFunc, boolean ins, boolean outs) {
                    for (TermEdge te : edges.values()) {
                        updateFunc.accept(te.otherNode(nodeToQuery));
                    }
                }

            };

            h.setScale(250);
            h.setEquilibriumDistance(3);
        }

        h.align();

        h.apply();



    }

    protected void updateEdges() {
        if (edgeDirty.get()) {
            edgeDirty.set(false);
            for (TermEdge e : edges.values()) {
                if (e.dirty.get())
                    e.update();
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {

        Scene scene = space.newScene(1200, 800);
        //scene.getStylesheets().addAll("dark.css" );

        primaryStage.setScene(scene);
        primaryStage.show();


        NAR nar = this.nar = new NAR(new Default());
        nar.input("<a --> b>.");
        nar.input("<b --> c>.");


        new Animate(50, a -> { layoutNodes(); } ).start();
        new Animate(75, a -> { updateEdges(); } ).start();

        new CycleReaction(nar) {

            @Override
            public void onCycle() {
                updateGraph();
            }
        };

        new Thread(() -> nar.frameEvery(100)).start();

        primaryStage.setOnCloseRequest((e) -> System.exit(1));
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


    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);

    }

}
