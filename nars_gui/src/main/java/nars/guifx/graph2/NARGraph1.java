package nars.guifx.graph2;

import automenta.vivisect.dimensionalize.HyperassociativeMap;
import automenta.vivisect.dimensionalize.IterativeLayout;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nars.Global;
import nars.NAR;
import nars.NARStream;
import nars.concept.Concept;
import nars.guifx.NARfx;
import nars.guifx.Spacegraph;
import nars.guifx.demo.Animate;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.apache.commons.math3.linear.ArrayRealVector;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import static javafx.application.Platform.runLater;

/**
 * Created by me on 8/6/15.
 */
public class NARGraph1<V,E> extends Spacegraph {

    private Animate updater;
    private GraphicsContext floorGraphics;
    private double scaleFactor;
    private Animate updaterSlow;


    private NAR nar;
    private Timeline time;

    final AtomicBoolean conceptsChanged = new AtomicBoolean(true);

    static final Random rng = new XORShiftRandom();


    final AtomicBoolean nodeDirty = new AtomicBoolean(true),
            edgeDirty = new AtomicBoolean(true);


    final static Font nodeFont = NARfx.mono(0.5);


    public static VisModel visModel = new VisModel() {

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
            if (c.hasBeliefs()) {
                double conf = c.getBeliefs().getConfidenceMax(0, 1);
                if (Double.isFinite(conf)) return conf;
            }
            return 0;
        }

        @Override
        public double getVertexScale(Concept c) {

            return (c!=null ? getVertexScaleByConf(c) : 0) * 0.75f + 0.25f;
            //return getVertexScaleByPri(c);
        }

        public Color getEdgeColor(double termMean, double taskMean) {
            // TODO color based on sub/super directionality of termlink(s) : e.getTermlinkDirectionality

            return Color.hsb(25.0 + 180.0 * (1.0 + (termMean - taskMean)),
                    0.95f,
                    Math.min(0.75f + 0.25f * (termMean + taskMean) / 2f, 1f)
                    //,0.5 * (termMean + taskMean)
                    );


//            return new Color(
//                    0.5f + 0.5f * termMean,
//                    0,
//                    0.5f + 0.5f * taskMean,
//                    0.5f + 0.5f * (termMean + taskMean)/2f
//            );
        }

    };

    float edgeThickness = 0.003f;


    final Map<Term, TermNode> terms = new LinkedHashMap();
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
                    return new TermNode(nar, k);
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
            Term target = t.getTerm();
            if (!source.equals(target.getTerm())) {
                TermNode tn = getTermNode(target, false);
                if (tn!=null) {
                    TermEdgeHalf e = getConceptEdgeHalf(sn, tn);
                    e.set(t, now);
                }
            }
        });

        c.getTermLinks().forEach(t -> {
            TermNode tn = getTermNode(t.getTerm(), false);
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

            nar.memory.getCycleProcess().forEachConcept(maxTerms, c -> {

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
        h.setRepulsiveWeakness(10.0);
        h.setAttractionStrength(10.0);
        h.setMaxRepulsionDistance(10);

        h.align(16);

        h.apply();




    }



    protected void updateNodes() {
        if (termList!=null)
            termList.forEach(n -> { if (n!=null) n.update(); } );
    }

    final List<TermEdge> removable = Global.newArrayList();

    final Color FADEOUT =
            Color.BLACK;
            //new Color(0,0,0,0.5);

    protected void renderEdges() {
        //if (edgeDirty.get()) {
        //edgeDirty.set(false);

        if (floorGraphics == null) floorGraphics = floorCanvas.getGraphicsContext2D();

        floorGraphics.setFill(
                FADEOUT
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
                this.updaterSlow = new Animate(200, a -> {
                    if (!termList.isEmpty()) {
                        layoutNodes();
                        renderEdges();
                    }
                });
                updater.start();
                updaterSlow.start();
            }
        }
    }

    protected void stop() {
        synchronized (nar) {
            if (this.updater != null) {
                updaterSlow.stop();
                updaterSlow = null;
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
