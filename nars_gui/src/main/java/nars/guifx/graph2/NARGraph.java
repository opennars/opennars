package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.IterativeLayout;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.guifx.Spacegraph;
import nars.guifx.demo.Animate;
import nars.link.TLink;
import nars.nar.Default;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.infinispan.commons.util.WeakValueHashMap;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph<V> extends Spacegraph {

    final Map<Term, TermNode> terms = new WeakValueHashMap<>();



    public final SimpleObjectProperty<EdgeRenderer<TermEdge>> edgeRenderer = new SimpleObjectProperty<>();


    public final SimpleObjectProperty<IterativeLayout<TermNode, TermEdge>> layout = new SimpleObjectProperty<>();

    final AtomicBoolean conceptsChanged = new AtomicBoolean(true);


    private Animate animator;


    public final NAR nar;


    static final Random rng = new XORShiftRandom();
    private final SimpleIntegerProperty maxNodes;


    int layoutPeriodMS = 30 /* slightly less than 2 * 17, approx sooner than 30fps */;


    public final SimpleObjectProperty<VisModel> vis = new SimpleObjectProperty<>();
    public TermNode[] displayed = new TermNode[0];
    private Set<TermNode> prevActive;


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



    public final TermNode getOrCreateTermNode(final Term t/*, boolean createIfMissing*/) {
        TermNode tn = getTermNode(t);
        if (tn == null) {
            final VisModel gv = vis.get();
            tn = terms.compute(t,
                    (k, prev) -> {
                        TermNode n = gv.newNode(k);
                        layout.get().init(n);

                        if (prev != null) {
                            getVertices().remove(prev);
                        }

                        return n;
                    });
        }

        return tn;
    }


//    /**
//     * synchronizes an active graph with the scenegraph nodes
//     */
//    public void commit(final Collection<TermNode> active /* should probably be a set for fast .contains() */,
//                       ) {
//
//        final List<TermNode> toDetach = Global.newArrayList();
//
//        termList.clear();
//
//
//        runLater(() -> {
//
//
//            for (final TermNode tn : active) {
//                termList.add(tn);
//            }
//
//
//            //List<TermEdge> toDetachEdge = new ArrayList();
//            addNodes(x);
//
//            getVertices().forEach(nn -> {
//                if (!(nn instanceof TermNode)) return;
//
//                TermNode r = (TermNode) nn;
//                if (!active.contains(r.term)) {
//                    TermNode c = terms.remove(r.term);
//
//                    if (c != null) {
//                        c.setVisible(false);
//                        toDetach.add(c);
//                        //Map<Term, TermEdge> edges = c.edge;
//                        /*if (edges != null && edges.size() > 0) {
//                            //iterate the map, because the array snapshot may differ until its next update
//                            toDetachEdge.addAll(edges.values());
//                        }*/
//                    }
//                }
//
//            });
//
//            removeNodes((Collection) toDetach);
//
//            //removeEdges((Collection) toDetachEdge);
//
//            termList.clear();
//            termList.addAll(terms.values());
//
//            //print();
//            toDetach.clear();
//
//        });
//    }


    public final void updateGraph(NAR n) {

        if (!isVisible())
            return;

        if (conceptsChanged.compareAndSet(true, false)) {

            //synchronized (conceptsChanged)
            {
                Set<TermNode> active = Global.newHashSet(maxNodes.get());

                ((Default) nar).core.concepts().forEach(maxNodes.get(), c -> {
                    TermNode tn = getOrCreateTermNode(c.getTerm());
                    if (tn != null) {

                        active.add(tn);
                        refresh(tn, c);
                    }
                });

                if (prevActive != null && !prevActive.equals(active)) {

                    runLater(() -> {

                        ObservableList<Node> v = getVertices();
                        //                    for (Node d : v) {
                        //                        if (!active.contains(d))
                        //                            v.remove(d);
                        //                    }
                        v.clear();
                        v.addAll(active);

                        displayed = v.toArray(displayed);

                        System.out.println("cached: " + terms.size() + ", displayed: " + displayed.length + " , shown=" + v.size());
                    });
                }

                prevActive = active;
            }
        }

    }

    public void refresh(TermNode tn, Concept cc/*, long now*/) {

        //final Term source = c.getTerm();

        tn.c = cc;
        //conPri.accept(cc.getPriority());

        final Term t = tn.term;
        final DoubleSummaryReusableStatistics ta = tn.taskLinkStat;
        final DoubleSummaryReusableStatistics te = tn.termLinkStat;

        tn.termLinkStat.clear();
        cc.getTermLinks().forEach(l ->
            updateConceptEdges(tn, l, te)
        );


        tn.taskLinkStat.clear();
        cc.getTaskLinks().forEach(l -> {
            if (!l.getTerm().equals(t)) {
                updateConceptEdges(tn, l, ta);
            }
        });

//        System.out.println("refresh " + Thread.currentThread() + " " + termLinkMean.getResult() + " #" + termLinkMean.getN() );


//        Consumer<TLink> tLinkConsumer = t -> {
//            Term target = t.getTerm();
//            if (!source.equals(target.getTerm())) {
//                TermNode tn = getTermNode(graph, target);
//                //TermEdge edge = getConceptEdge(graph, sn, tn);
//
//            }
//        };
//
//        c.getTaskLinks().forEach(tLinkConsumer);
//        c.getTermLinks().forEach(tLinkConsumer);


    }

    public void updateConceptEdges(TermNode s, TLink link, DoubleSummaryReusableStatistics accumulator) {


        Term t = link.getTerm();
        TermNode target = getTermNode(t);
        if ((target == null) || (s == target)) return;

        TermEdge ee = getConceptEdge(s, target);
        if (ee!=null) {
            ee.linkFrom(s, link);
            accumulator.accept(link.getPriority());
        }
    }

    public TermEdge getConceptEdge(TermNode s, TermNode t) {
        //re-order
        if (!NARGraph.order(s.term, t.term)) {
            TermNode x = s;
            s = t;
            t = x;
        }

        TermEdge e = getConceptEdgeOrdered(s, t);
        if (e == null) {
            e = new TermEdge(s, t);
        }
        s.putEdge(t.term, e);

        return e;
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

        /** apply vis properties */
        VisModel v = vis.get();
        for (TermNode t: displayed)
            v.accept(t);


        /** apply layout */
        IterativeLayout<TermNode, TermEdge> l;
        if ((l = layout.get()) != null) {
            l.run(this, 1);
        } else {
            System.err.println(this + " has no layout");
        }


        edgeRenderer.get().reset(this);


        //Collections.addAll(removable, edges.getChildren());
        final EdgeRenderer<TermEdge> er = edgeRenderer.get();

        for (TermNode n: displayed) {

        //termList.forEach((Consumer<TermNode>) n -> {

//        for (int i = 0, termListSize = termList.size(); i < termListSize; i++) {
//            final TermNode n = termList.get(i);
//            for (final TermEdge e : n.getEdges()) {
//                removable.remove(e);
//            }
//        });
//
//
//
//        termList.forEach((Consumer<TermNode>)n -> {
//        for (int i = 0, termListSize = termList.size(); i < termListSize; i++) {
//            final TermNode n = termList.get(i);
            if (n!=null) {
                for (final TermEdge e : n.getEdges())
                    if (e!=null) er.accept(e);
            }
        }

//        removable.forEach(x -> {
//            edges.getChildren().remove(x);
//        });
//        edges.getChildren().removeAll(removable);

//        removable.clear();


    }


    public NARGraph(NAR n, int size) {
        super();

        this.maxNodes = new SimpleIntegerProperty(size);

        this.nar = n
                //.stdout()
                //.stdoutTrace()
//                .input("<a --> b>. %1.00;0.7%", //$0.9;0.75;0.2$
//                        "<b --> c>. %1.00;0.7%")
                .onConceptActive((c) -> conceptsChanged.set(true))
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
                    if (displayed.length!=0) {
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
