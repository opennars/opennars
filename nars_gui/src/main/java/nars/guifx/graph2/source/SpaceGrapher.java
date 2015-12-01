package nars.guifx.graph2.source;


import com.gs.collections.impl.map.mutable.UnifiedMap;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import nars.Global;
import nars.guifx.Spacegraph;
import nars.guifx.demo.Animate;
import nars.guifx.graph2.GraphSource;
import nars.guifx.graph2.TermEdge;
import nars.guifx.graph2.TermNode;
import nars.guifx.graph2.VisModel;
import nars.guifx.graph2.impl.CanvasEdgeRenderer;
import nars.guifx.graph2.layout.IterativeLayout;
import nars.guifx.graph2.layout.None;
import nars.term.Term;
import nars.term.Termed;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class SpaceGrapher<K extends Termed, V extends TermNode<K>> extends Spacegraph {

    final Map<Term, V> terms = new UnifiedMap();
    //new WeakValueHashMap<>();


    public final SimpleObjectProperty<EdgeRenderer<TermEdge>> edgeRenderer = new SimpleObjectProperty<>();

    public final SimpleObjectProperty<IterativeLayout<V>> layout = new SimpleObjectProperty<>();
    public static final IterativeLayout nullLayout = new None();


    public final SimpleIntegerProperty maxNodes;
    public final SimpleObjectProperty<GraphSource<K>> source = new SimpleObjectProperty<>();
    private int animatinPeriodMS = -1;


    private Animate animator; //TODO atomic reference


    //static final Random rng = new XORShiftRandom();


    public final SimpleObjectProperty<VisModel<K,V>> vis = new SimpleObjectProperty<>();
    public TermNode[] displayed = new TermNode[0];
    private Set<TermNode> prevActive;

    /**
     * produces a spacegraph instance for a given collection of items
     * and a method of rendering them
     * TODO does not yet support collections which change but this is feasible
     */
    static public <X extends Object, K extends Termed> SpaceGrapher<K, TermNode<K>>
    forCollection(
            final Collection<X> c,
            final Function<X, K> termize,
            final BiConsumer<X, TermNode> builder /* decorator actually */,
            final IterativeLayout layout /* the initial one, it can be changed */
    ) {

        final Map<K, X> termObject = new HashMap();

        int defaultCapacity = 128;

        return new SpaceGrapher(

                new GraphSource<K>() {

                    Set<TermNode> nodes = Global.newHashSet(16);


                    @Override
                    public void start(SpaceGrapher<K, ? extends TermNode<K>> spaceGrapher) {
//
//                    }
//
//                    @Override
//                    public void start(SpaceGrapher<K,TermNode<K>> spaceGrapher) {

                        c.forEach(o -> {
                            if (o != null) {

                                //TODO generalize this so term isnt needed here
                                K term = termize.apply(o);
                                termObject.put(term, o);

                                TermNode tn = spaceGrapher.getOrNewTermNode(term);
                                if (tn != null) {
                                    nodes.add(tn);
                                }
                            }
                        });

                        runLater(() -> {
                            accept(spaceGrapher);
                            spaceGrapher.layout.set(layout);
                            spaceGrapher.rerender();
                        });


                    }

                    /*{
                        return Atom.the(o.toString(),true);
                    }*/

//                    @Override
//                    public void accept(SpaceGrapher<K,?> g) {
//                        //a cache would need to be invalidated here if this will support mutable collections
//                        g.setVertices(nodes);
//                    }

                    @Override
                    public void accept(SpaceGrapher g) {
                        g.setVertices(nodes.toArray(new TermNode[nodes.size()]));
                    }
                },
                new VisModel<K, TermNode<K>>() {

                    @Override
                    public void accept(TermNode<K> termNode) {


                        //t.update();

                        //t.scale(minSize + (maxSize - minSize) * t.priNorm);
                    }

                    @Override
                    public TermNode<K> newNode(K t) {
                        TermNode<K> tn = new TermNode<>(t);
                        builder.accept(
                                termObject.get(t),
                                tn
                        );
                        return tn;
                    }
                },
                new CanvasEdgeRenderer(),
                defaultCapacity);
    }


    /**
     * assumes that 's' and 't' are already ordered
     *
     * @param s
     * @param t
     * @param edgeBuilder
     */
    public final TermEdge getConceptEdgeOrdered(TermNode<K> s, TermNode<K> t, BiFunction<TermNode, TermNode, TermEdge> edgeBuilder) {
        return getEdge(s.term, t.term, edgeBuilder);
    }

    public final TermEdge getEdge(K a, K b, BiFunction<TermNode, TermNode, TermEdge> builder) {
        TermNode<K> A = getTermNode(a);
        TermEdge newEdge = null;
        if (A != null) {
            newEdge = A.getEdge(b);
        }

        if (newEdge == null) {
            newEdge = builder.apply(A, getTermNode(b));
            addEdge(a, b, newEdge);
        }

        return newEdge;
    }

    public final boolean addEdge(K a, K b, TermEdge e) {
        TermNode n = getTermNode(a);
        if (n != null) {
            return n.putEdge(b, e) == null;
        }
        return false;
    }

    public final V getTermNode(final Term t) {
        return terms.get(t);
    }
    public final V getTermNode(final K t) {
        return getTermNode(t.getTerm());
    }

    public final V getOrNewTermNode(final K t/*, boolean createIfMissing*/) {
        return terms.computeIfAbsent(t.getTerm(), k -> {
            return newNode(t);
        });
    }


    protected final V newNode(K k) {
        V v = vis.get().newNode(k);
        if (v!=null) {
            IterativeLayout<V> l = layout.get();
            if (l != null)
                l.init(v);
        }
        return v;
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


    public final AtomicBoolean ready = new AtomicBoolean(true);

    protected final Runnable clear = () -> {
        this.displayed = TermNode.empty;
        getVertices().clear();
        edgeRenderer.get().reset(this);
        ready.set(true);
    };

    /**
     * commit what is to be displayed
     */
    public final void setVertices(TermNode[] active) { //final Set<? extends V> active) {

        ready.set(false);

        Runnable next;

        if (active.length == 0) {
            next = clear;
        } else {
            final TermNode[] toDisplay = active; //active.toArray(displayed);
            if (toDisplay == null) {
                throw new RuntimeException("null toDisplay");
            }

            if (toDisplay.length == 0) {
                next = clear; //necessary?
            } else {
                next = (() -> {
                    this.displayed = toDisplay;
                    getVertices().setAll(
                        active
                        //toDisplay
                    );
                    ready.set(true);
                    //System.out.println("cached: " + terms.size() + ", displayed: " + displayed.length + " , shown=" + v.size());
                });
            }
        }


        runLater(next);
    }


    public void setVertices(Iterable<K> v) {

        final GraphSource<K> ss = this.source.get();
        final VisModel vv = vis.get();

        Iterator<K> cc = v.iterator();


        int n = this.maxNodes.get();

        final Set<TermNode> active = new LinkedHashSet(n); //Global.newHashSet(maxNodes);

        while (cc.hasNext() && ((n--) > 0)) {

            final K k = cc.next();
            TermNode t = getOrNewTermNode(k);
            if (t != null) {
                active.add(t);

                ss.refresh(this, k, t);
                vv.updateNode(t);
            }
            else {
                cc.remove();
            }
        }

        if (!Objects.equals(prevActive, active)) {
            setVertices(active.toArray(new TermNode[active.size()]));
        } else {
            prevActive = active;
        }

    }


//    @FunctionalInterface
//    public interface PreallocatedResultFunction<X, Y> {
//        public void apply(X x, Y setResultHereAndReturnIt);
//    }

//    @FunctionalInterface
//    public interface PairConsumer<A, B> {
//        public void accept(A a, B b);
//    }


//    protected void updateNodes() {
//        if (termList != null)
//            termList.forEach(n -> {
//                if (n != null) n.update();
//            });
//    }


    public interface EdgeRenderer<E> extends Consumer<E> {
        /**
         * called before any update begins
         */
        public void reset(SpaceGrapher g);
    }


    /**
     * called in JavaFX thread
     */
    public void rerender() {

        /** apply layout */
        IterativeLayout<V> l;
        if ((l = layout.get()) != null) {
            l.run(this, 1);
        } else {
            System.err.println(this + " has no layout");
        }

        final EdgeRenderer<TermEdge> er = edgeRenderer.get();
        er.reset(this);

        /** apply vis properties */
        VisModel v = vis.get();
        for (TermNode n : displayed) {
            v.accept(n);

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
            if (n != null) {
                for (final TermEdge e : n.getEdges())
                    if (e != null) er.accept(e);
            }
        }

//        removable.forEach(x -> {
//            edges.getChildren().remove(x);
//        });
//        edges.getChildren().removeAll(removable);

//        removable.clear();


    }


    public SpaceGrapher(GraphSource<K> g, VisModel vv, CanvasEdgeRenderer edgeRenderer, int size) {
        super();


        this.maxNodes = new SimpleIntegerProperty(size);

        source.addListener((e, c, v) -> {

            if (c != null) {
                v.stop(this);
            }

            if (v != null) {
                v.start(this);
            } else {
                System.out.println("no signal");
            }
        });
        vis.addListener((l, p, n) -> {
            SpaceGrapher<K, V> gg = SpaceGrapher.this;
            if (p != null)
                p.stop(gg);
            if (n != null) {
                n.start(gg);
            }
        });


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

        //TODO add enable override boolean switch
        sceneProperty().addListener(v -> checkVisibility());
        parentProperty().addListener(v -> checkVisibility());
        visibleProperty().addListener(v -> checkVisibility());

        runLater(() -> checkVisibility());


        this.edgeRenderer.set(edgeRenderer);

        this.vis.set(vv); //set vis before source

        this.source.set(g);

    }

    /**
     * called before next layout changes
     */
    synchronized void layoutUpdated() {
        int animationPeriod = this.animatinPeriodMS;
//        if (this.animator!=null) {
//            this.animator.stop();
//            this.animator = null;
//        }
        stop();

        //reset visiblity state to true for all, in case previous layout had hidden then
        getVertices().forEach(t -> t.setVisible(true));

        source.getValue().refresh();

        rerender();

        if (animationPeriod != -1)
            start(animationPeriod);
    }


    final static int defaultFramePeriodMS = 30; //~60hz/2

    protected synchronized void checkVisibility() {
        if (isVisible() && getParent() != null && getScene() != null) {
            start(defaultFramePeriodMS);
        } else
            stop();
    }

    public synchronized void start(int layoutPeriodMS) {

        stop();

        if (this.animator == null) {
            this.animator = new Animate(layoutPeriodMS, a -> {
                if (displayed.length != 0) {
                    rerender();
                }
            });

            System.out.println(this + " started");


                /*this.updaterSlow = new Animate(updatePeriodMS, a -> {
                    if (!termList.isEmpty()) {
                        layoutNodes();
                        renderEdges();
                    }
                });*/
            animatinPeriodMS = layoutPeriodMS;
            animator.start();
            //updaterSlow.start();
        }

    }

    public synchronized void stop() {
        if (this.animator != null) {
            animator.stop();
            animator = null;
            animatinPeriodMS = -1;

            System.out.println(this + " stopped");
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
