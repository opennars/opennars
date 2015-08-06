package automenta.vivisect.javafx.demo;

import automenta.vivisect.javafx.Spacegraph;
import automenta.vivisect.javafx.Windget;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import nars.Global;
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

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph1 extends Application {

    final Spacegraph space = new Spacegraph();
    private NAR nar;
    private Timeline time;

    static final Random rng = new XORShiftRandom();

    public static class TermNode extends Windget {


        private final Term term;
        Concept c = null;

        public TermNode(Term t) {
            super(t.toStringCompact(), new Button(" "));

            this.term = t;

            randomPosition(10, 10);
        }

        public void randomPosition(double bx, double by) {

            move(rng.nextDouble() * bx,
                    rng.nextDouble() * by);
        }

        protected void update() {

        }
    }

    public static class TermEdge extends Polygon {

        final Set<TermLink> termLinks = new LinkedHashSet(); //should not exceed size two
        final Set<TaskLink> taskLinks = new LinkedHashSet();

        private final TermNode from;
        private final TermNode to;

        AtomicBoolean dirty = new AtomicBoolean(true);

        private final ChangeListener<? super Transform> onNodeTransformed = new ChangeListener() {

            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                dirty.set(true);
            }
        };


        public TermEdge(TermNode from, TermNode to) {
            super();
            this.from = from;
            this.to = to;


            /*from.layoutXProperty().addListener(onNodeMoved);
            from.layoutYProperty().addListener(onNodeMoved);
            to.layoutXProperty().addListener(onNodeMoved);
            to.layoutYProperty().addListener(onNodeMoved);*/
            from.localToSceneTransformProperty().addListener(onNodeTransformed);
            to.localToSceneTransformProperty().addListener(onNodeTransformed);

            setFill(Color.ORANGE);
            setOpacity(0.5);

            getPoints().setAll(0d, 0.5d, -0.5d, -0.5d, +0.5d, -0.5d); //isoceles triangle within -0.5,-0.5...0.5,0.5 (len/wid = 1)

        }

        public void update() {


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

            getTransforms().setAll(
                    Transform.translate(cx, cy),
                    Transform.rotate(FastMath.toDegrees(rot), 0, 0),
                    Transform.scale(len, 2)
            );

            dirty.set(false);
        }

    }

    final Map<Term, TermNode> terms = new HashMap();
    final Table<Term, Term, TermEdge> edges = HashBasedTable.create();
    final List<TermNode> termToAdd = Global.newArrayList();
    final List<TermEdge> edgeToAdd = Global.newArrayList();

    public TermNode getTermNode(final Term t) {
        TermNode tn = terms.get(t);
        if (tn == null) {
            terms.put(t, tn = new TermNode(t));
            termToAdd.add(tn);
        }
        return tn;
    }

    public TermEdge getConceptEdge(final TermNode s, final TermNode t) {
        TermEdge e = edges.get(s.term, t.term);

        if (e == null) {
            edges.put(s.term, t.term, e = new TermEdge(s, t));
            edgeToAdd.add(e);
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
                e.termLinks.add(t);
            }

        }

        if (!termToAdd.isEmpty()) {
            TermNode[] x = termToAdd.toArray(new TermNode[termToAdd.size()]);
            runLater(() -> space.addNodes(x));
            termToAdd.clear();
        }
        if (!edgeToAdd.isEmpty()) {
            TermEdge[] x = edgeToAdd.toArray(new TermEdge[edgeToAdd.size()]);
            runLater(() -> space.addEdges(x));
            edgeToAdd.clear();
        }


    }


    protected void updateEdges() {
        for (TermEdge e : edges.values()) {
            if (e.dirty.get())
                e.update();
        }
    }

    @Override
    public void start(Stage primaryStage) {

        Scene scene = space.newScene(1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();


        NAR nar = this.nar = new NAR(new Default());
        nar.input("<a --> b>.");
        nar.input("<b --> c>.");

        time = new Timeline( 10 );
        time.setAutoReverse(true);
        time.cycleCountProperty().addListener((observable, oldValue, newValue) -> {
            updateEdges();
        });
        time.play();

        new CycleReaction(nar) {

            @Override
            public void onCycle() {
                updateGraph();
            }
        };

        new Thread(() -> nar.runAtRate(500)).start();
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
