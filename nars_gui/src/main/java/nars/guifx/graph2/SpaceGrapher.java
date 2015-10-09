package nars.guifx.graph2;


import com.gs.collections.impl.map.mutable.UnifiedMap;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import nars.Global;
import nars.guifx.Spacegraph;
import nars.guifx.demo.Animate;
import nars.guifx.graph2.layout.CanvasEdgeRenderer;
import nars.guifx.graph2.layout.IterativeLayout;
import nars.guifx.graph2.layout.None;
import nars.term.Termed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class SpaceGrapher<K extends Termed, V extends TermNode<K>> extends Spacegraph {

    final Map<K, V> terms = new UnifiedMap();
    //new WeakValueHashMap<>();


    public final SimpleObjectProperty<EdgeRenderer<TermEdge>> edgeRenderer = new SimpleObjectProperty<>();

    public final SimpleObjectProperty<IterativeLayout<V>> layout = new SimpleObjectProperty<>();
    public static final IterativeLayout nullLayout = new None();


    public final SimpleIntegerProperty maxNodes;
    public final SimpleObjectProperty<GraphSource<K>> source = new SimpleObjectProperty<>();


    private Animate animator; //TODO atomic reference


    //static final Random rng = new XORShiftRandom();




    public final SimpleObjectProperty<VisModel> vis = new SimpleObjectProperty<>();
    public TermNode[] displayed = new TermNode[0];

    /** produces a spacegraph instance for a given collection of items
     *  and a method of rendering them
     *  TODO does not yet support collections which change but this is feasible
     *  */
    static public <X extends Object, K extends Termed> SpaceGrapher<K,TermNode<K>>
    forCollection(
            final Collection<X> c,
            final Function<X,K> termize,
            final BiConsumer<X,TermNode> builder /* decorator actually */,
            final IterativeLayout layout /* the initial one, it can be changed */
    ) {

        final Map<K,X> termObject = new HashMap();

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
                            if (o!=null) {

                                //TODO generalize this so term isnt needed here
                                K term = termize.apply(o);
                                termObject.put(term, o);

                                TermNode tn = spaceGrapher.getOrCreateTermNode(term);
                                if (tn!=null) {
                                    nodes.add(tn);
                                }
                            }
                        });

                        runLater(() -> {
                            accept(spaceGrapher);
                            spaceGrapher.layout.set(layout);
                            spaceGrapher.rerender();
                        } );


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
                        g.setVertices(nodes);
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
     */
    public final TermEdge getConceptEdgeOrdered(V s, V t) {
        return getEdge(s.term, t.term);
    }

    public final TermEdge getEdge(K a, K b) {
        TermNode<K> n = getTermNode(a);
        if (n != null) {
            return n.edge.get(b);
        }
        return null;
    }

    public final boolean addEdge(K a, K b, TermEdge e) {
        TermNode n = getTermNode(a);
        if (n != null) {
            return n.putEdge(b, e) == null;
        }
        return false;
    }

    public final V getTermNode(final K t) {
        return terms.get(t);
    }


    public final V getOrCreateTermNode(final K t/*, boolean createIfMissing*/) {
        V tn = getTermNode(t);
        if (tn == null) {
            final VisModel<K,V> gv = vis.get();
            tn = terms.compute(t,
                    (k, prev) -> {
                        V n = gv.newNode(k);

                        IterativeLayout<V> l = layout.get();
                        if (l != null)
                            l.init(n);

                        if (prev != null) {
                            Parent p = prev.getParent();
                            if (p!=null)
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


    protected final Runnable clear = () -> {
        this.displayed = TermNode.empty;
        getVertices().clear();
        edgeRenderer.get().reset(this);
    };

    /** commit what is to be displayed */
    public final void setVertices(final Set<? extends V> active) {

        Runnable next;

        if (active.isEmpty()) {
            next = clear;
        } else {
            final TermNode[] toDisplay = active.toArray(displayed);
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
                    );
                    //System.out.println("cached: " + terms.size() + ", displayed: " + displayed.length + " , shown=" + v.size());
                });
            }
        }


        runLater(next);
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
            SpaceGrapher<K,V> gg = SpaceGrapher.this;
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

    /** called before next layout changes */
    protected void layoutUpdated() {
        //reset visiblity state to true for all, in case previous layout had hidden then
        getVertices().forEach(t -> t.setVisible(true));

        source.getValue().refresh();

        rerender();
    }


    final static int defaultFramePeriodMS = 30; //~60hz/2

    protected synchronized void checkVisibility() {
        if (isVisible() && getParent() != null && getScene()!=null) {
            start(defaultFramePeriodMS);
        } else
            stop();
    }

    public synchronized void start(int layoutPeriodMS) {

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
            animator.start();
            //updaterSlow.start();
        }

    }

    public synchronized void stop() {
        if (this.animator != null) {
            animator.stop();
            animator = null;

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
