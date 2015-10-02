package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.IterativeLayout;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nars.Global;
import nars.NAR;
import nars.guifx.NARfx;
import nars.guifx.Spacegraph;
import nars.guifx.demo.Animate;
import nars.term.Term;
import nars.util.data.list.FasterList;
import nars.util.data.random.XORShiftRandom;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph<V> extends Spacegraph {

    final Map<Term, TermNode> terms = new LinkedHashMap();


    protected Consumer<NARGraph> source;

    public final AtomicReference<EdgeRenderer<TermEdge>> edgeRenderer = new AtomicReference<>();


    public final AtomicReference<IterativeLayout<TermNode, TermEdge>> layout = new AtomicReference<>();

    public final AtomicBoolean conceptsChanged = new AtomicBoolean(true);



    private Animate animator;


    public final NAR nar;


    static final Random rng = new XORShiftRandom();
    final FasterList<TermNode> termList = new FasterList<>();

    int layoutPeriodMS = 30 /* slightly less than 2 * 17, approx sooner than 30fps */;


    final static Font nodeFont = NARfx.mono(0.5);

    public final AtomicReference<VisModel> vis = new AtomicReference<>();


    /**
     * assumes that 's' and 't' are already ordered
     */
    public final TermEdge getConceptEdgeOrdered(TermNode s, TermNode t) {
        return getEdge(s.term, t.term);
    }

    static boolean order(final Term x, final Term y) {
        final int i = x.compareTo(y);
        if (i == 0) throw new RuntimeException("order=0 but must be non-equal");
        return i < 0;
    }

    public final TermEdge getEdge(Term a, Term b) {
        TermNode n = getTermNode(a);
        if (n != null) {
            return n.edge.get(b);
        }
        return null;
    }

    public final boolean addEdge(Term a, Term b, TermEdge e) {
        TermNode n = getTermNode(a);
        if (n != null) {
            return n.putEdge(b, e) == null;
        }
        return false;
    }

    public final TermNode getTermNode(final Term t) {
        return terms.get(t);
    }



    /** sets the input source that this will display from, next */
    public final void input(Consumer<NARGraph> source) {
        this.source = source;
    }


    /** synchronizes an active graph with the scenegraph nodes */
    public void commit(final Collection<Term> active /* should probably be a set for fast .contains() */,
                       final TermNode[] x,
                       final TermEdge[] y) {



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
                addEdges(y);
            }

            List<TermNode> toDetach = Global.newArrayList();
            List<TermEdge> toDetachEdge = new ArrayList();

            getVertices().forEach(nn -> {
                if (!(nn instanceof TermNode)) return;

                TermNode r = (TermNode) nn;
                if (!active.contains(r)) {
                    TermNode c = terms.remove(r.term);


                    c.setVisible(false);
                    toDetach.add(c);
                    Map<Term, TermEdge> edges = c.edge;
                    if (edges != null && edges.size() > 0) {
                        //iterate the map, because the array snapshot may differ until its next update
                        toDetachEdge.addAll(edges.values());
                    }
                }

            });

            removeNodes((Collection) toDetach);
            removeEdges((Collection) toDetachEdge);

            termList.clear();
            termList.addAll(terms.values());

            //print();

        });
    }

    public final void updateGraph(NAR n) {

        System.out.println("update " + Thread.currentThread());

        if (!isVisible())
            return;

        /** update */
        if (source != null)
            source.accept(this);
        else {
            System.err.println(this + "disconnected"); //no updater
        }

        /** apply vis properties */
        termList.forEach(vis.get());


        /** apply layout */
        IterativeLayout<TermNode, TermEdge> l;
        if ((l = layout.get()) != null) {
            l.run(this, 1);
        } else {
            System.err.println(this + " has no layout");
        }


    }


//    @FunctionalInterface
//    public interface PreallocatedResultFunction<X, Y> {
//        public void apply(X x, Y setResultHereAndReturnIt);
//    }

    @FunctionalInterface
    public interface PairConsumer<A, B> {
        public void accept(A a, B b);
    }





//    protected void updateNodes() {
//        if (termList != null)
//            termList.forEach(n -> {
//                if (n != null) n.update();
//            });
//    }

    final List<Object /*TermEdge*/> removable = Global.newArrayList();

    final Color FADEOUT = Color.BLACK;
    //new Color(0,0,0,0.5);

    public interface EdgeRenderer<E> extends Consumer<E> {
        /**
         * called before any update begins
         */
        public void reset(NARGraph g);
    }



    /**
     * called in JavaFX thread
     */
    protected void rerender() {



        EdgeRenderer<TermEdge> er = edgeRenderer.get();
        er.reset(this);


        Collections.addAll(removable, edges.getChildren());

        termList.forEach((Consumer<TermNode>) n -> {

//        for (int i = 0, termListSize = termList.size(); i < termListSize; i++) {
//            final TermNode n = termList.get(i);
            for (final TermEdge e : n.getEdges()) {
                removable.remove(e);
            }
//        });
//
//
//
//        termList.forEach((Consumer<TermNode>)n -> {
//        for (int i = 0, termListSize = termList.size(); i < termListSize; i++) {
//            final TermNode n = termList.get(i);
            for (final TermEdge e : n.getEdges())
                er.accept(e);
        });

        removable.forEach(x -> {
            edges.getChildren().remove(x);
        });

        removable.clear();


    }


    public NARGraph(NAR n) {
        super();


        this.nar = n
                //.stdout()
                //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")
                .onConceptActive( (c) -> conceptsChanged.set(true) )
                .onEachFrame(this::updateGraph)

                //.onEachNthFrame(this::updateGraph, 1);

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
        ;

        visibleProperty().addListener(v -> {
            checkVisibility();
        });

        runLater(() -> {
            checkVisibility();
        });

    }

    protected void checkVisibility() {
        if (isVisible())
            start();
        else
            stop();
    }

    protected void start() {
        synchronized (nar) {
            if (this.animator == null) {

                this.animator = new Animate(layoutPeriodMS, a -> {
                    if (!termList.isEmpty()) {
                        rerender();
                    }
                });

                /*this.updaterSlow = new Animate(updatePeriodMS, a -> {
                    if (!termList.isEmpty()) {
                        layoutNodes();
                        renderEdges();
                    }
                });*/
                animator.start();
                //updaterSlow.start();
            }
        }
    }

    protected void stop() {
        synchronized (nar) {
            if (this.animator != null) {
                animator.stop();
                animator = null;
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
