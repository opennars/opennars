package ca.nengo.test.nargraph;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import automenta.vivisect.dimensionalize.UIEdge;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StepListener;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;
import com.google.common.collect.Iterators;
import javolution.util.FastSet;
import nars.build.Default;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.ConceptReaction;
import nars.gui.NARSwing;
import nars.logic.Terms;
import nars.logic.entity.Concept;
import nars.logic.entity.Named;
import nars.logic.entity.Task;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.piccolo2d.util.PPaintContext;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.*;


public class TestNARGraph extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    float time = 0;

    public static Node newGraph(NAR n) {
        NARGraphNode network = new NARGraphNode(n);
        return network;
    }

    public static void main(String[] args) {
        new TestNARGraph().window(800, 600);
    }

    @Override
    public void init() throws Exception {


        Default d = new Default(128, 1, 1);
        d.setSubconceptBagSize(0);
        NAR nar = new NAR(d);
//        nar.input("<a --> {b}>.");
//        nar.input("<b --> c>.");
//        nar.input("<[c] --> a>. :/:");
////        nar.input("<{(*,key1,value1),(*,key2,value2)} --> table>.");
//
//        //      nar.input("<a --> b>.");
//        nar.run(200);

        Parameters.DEBUG = true;
        NARSwing.themeInvert();
        NARSwing s = new NARSwing(nar);
        nar.input(new File("/tmp/h.nal"));


        s.controls.setSpeed(0.1f);


        UINetwork networkUI = (UINetwork) addNodeModel(newGraph(nar));
        NodeViewer window = networkUI.openViewer(Window.WindowState.MAXIMIZED);


        getUniverse().setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setAnimatingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);
        getUniverse().setInteractingRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);


        float fps = 50f;

        java.util.Timer timer = new java.util.Timer("", false);
        timer.scheduleAtFixedRate(new TimerTask() {

            int frame = 0;

            @Override
            public void run() {

                long start = 0;

                float dt = 0.25f;
                try {

                    if (frame%100==0) {
                        start = System.nanoTime();
                    }

                    networkUI.node().run(time, time + dt, 1);

                    if (frame%100==0) {
                        long end = System.nanoTime();
                        double time = (end - start)/1e6;

                        System.out.println(time +  " ms");
                    }


                } catch (SimulationException e) {
                    e.printStackTrace();
                }

                time += dt;
                frame++;
            }
        }, 0, (int) (1000 / fps));


    }

    public static class NARGraphNode extends AbstractMapNetwork<String, UIVertex> implements UIBuilder {

        private final NAR nar;
        private final ConceptReaction conceptReaction;
        float simTimePerCycle = 1f;
        float simTimePerLayout = 0.25f;
        long lasTLayout = System.currentTimeMillis();
        Rectangle2D layoutBounds = new Rectangle2D.Double(-500, -500, 1000, 1000);
        final FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>> hmap =
                new FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>>(layoutBounds) {
        /*final HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>> hmap =
            new HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>>(graph, 2) {*/


                    @Override
                    public ArrayRealVector getPosition(UIVertex node) {
                        return node.getCoordinates();
                    }

                    @Override
                    public void pre(Collection<UIVertex> vertices) {
                        updateCoordinates(vertices);
                    }


                    @Override
                    public double getEdgeWeight(UIEdge e) {
                        /*if (e.e instanceof TermLink) {
                            return 4.0;
                        }*/
                        return 1.0;
                    }


                    @Override
                    public double getRadius(UIVertex narGraphVertex) {
                        double r = 1 + narGraphVertex.ui.getFullBoundsReference().getWidth();
                        return r * 0.5f;
                    }

                    @Override
                    protected Iterator<UIVertex> getVertices() {
                        return Iterators.filter(nodes().iterator(), UIVertex.class);
                    }


                };
        SubCycle narCycle = new SubCycle() {
            @Override
            public double getTimePerCycle() {
                return simTimePerCycle;
            }

            @Override
            public void run(int numCycles, float endTime, long deltaMS) {

                if (!nar.isRunning())
                    nar.step(numCycles);
            }

            @Override
            public String toString() {
                return "nar";
            }
        };
        SubCycle layoutCycle = new SubCycle() {
            @Override
            public double getTimePerCycle() {
                return simTimePerLayout;
            }

            @Override
            public void run(int numCycles, float endTime, long deltaMS) {

                /*
                hmap.setEquilibriumDistance(55);
                hmap.setMaxRepulsionDistance(5000);
                hmap.setAttractionStrength(16);
                hmap.setSpeedFactor(10);
                */


                double layoutRad = Math.sqrt( numVertices )* 500;
                layoutBounds.setRect(-layoutRad / 2, -layoutRad / 2, layoutRad, layoutRad);
                hmap.resetLearning();
                hmap.run(2);


            }

            @Override
            public String toString() {
                return "layout";
            }
        };
        private UIEdge[] nextEdges;
        private Set<UIEdge> edges = new FastSet<UIEdge>().atomic();
        private UINARGraph ui;
        private boolean edgesChanged = false;
        private int numVertices = 0;


        public NARGraphNode(NAR n) {
            super();

            this.nar = n;

            this.conceptReaction = new ConceptReaction(nar) {

                @Override
                public void onFiredConcept(Concept c) {

                    refresh(c);
                }

                @Override
                public void onNewConcept(Concept c) {
                    refresh(c);
                    //add(c);
                    //if (add(c) != null)
                    //  refresh(c);
                }

                @Override
                public void onForgetConcept(Concept c) {

                    remove(c);


                    //int x = ui.getViewer().getGround().printTree(System.out);

                    //int y = ui.getViewer().getGround().printTree(System.out);
//
//
//                    System.out.println(" forget: " +
//                        nar.memory.concepts.size() + " concepts, "
//                    + graph.vertexSet().size() + " vertices, " +
//                            synchronized(graph) {
//                    Iterators.size(Iterators.filter(graph.vertexSet().iterator(), new Predicate<UIVertex>() {
//
//                        @Override
//                        public boolean apply(UIVertex input) {
//                            if (input instanceof TermNode) {
//                                return ((TermNode) input).concept!=null;
//                            }
//                            return false;
//                        }
//                    })) + " concept ui nodes" );
//                            }

                }
            };

            addStepListener(layoutCycle);
            //addStepListener(narCycle);

        }


        void updateCoordinates(Collection<UIVertex> vertices) {
            long now = System.currentTimeMillis();
            long dt = (now - lasTLayout);
            if (dt > 0) {
                numVertices = vertices.size();
                for (UIVertex v : vertices) {
                    v.getActualCoordinates(dt);
                    if (v.isDependent() && v.degree()==0) {
                        System.err.println("late removing " + v);
                        remove(v);
                    }

                }
                lasTLayout = now;
            }
        }


        /**
         * returns a list of edges which can be used by drawing thread for non-synchronized fast drawing
         */
        public Iterable<UIEdge> getEdges() {
//            if (edgesChanged) {
//                synchronized (edges) {
//                    if (edgesChanged) { //test again in case a previously blocked thread goes in after it has been updated already
//                        int numEdges = edges.size();
//                        nextEdges = edges.toArray(new UIEdge[numEdges]); //use atomicRef?
//                        edgesChanged = false;
//                    }
//                }
//            }
            return edges;
        }

        @Override
        public String name(UIVertex node) {
            return node.name().toString();
        }


        /**
         * should be called from within a synchronized(graph) { } block
         */
        public UIVertex newVertex(final Named x) {

            TermNode v = null;

            if (x instanceof Concept) {
                v = new TermNode(this, (Concept) x);
            } else if (x instanceof Task) {
                v = new TermNode(this, (Task) x);
            }

                /*
                else if (x instanceof Term) {
                    v = new TermNode(this, (Term)x);
                }
                */


            return v;
        }


        public void refresh(Object x) {
            new MyGrapher().on(null, x, false).finish();
        }


        @Override
        public UINARGraph newUI(double width, double height) {
            return this.ui = new UINARGraph(this);
        }

        private boolean add(UIEdge<UIVertex> e) {
            if (edges.add(e)) {
                e.getSource().link(e, true);
                e.getTarget().link(e, false);
                edgesChanged();
                return true;
            }
            return false;
        }

        private void remove(UIEdge<UIVertex> e) {
            if (edges.remove(e)) {
                if (e.getSource() != null)
                    e.getSource().unlink(e, true);
                if (e.getTarget() != null) {
                    e.getTarget().unlink(e, false);
                }
                ensureValid(e.getSource());
                ensureValid(e.getTarget());
                edgesChanged();
            }
        }

        private void ensureValid(UIVertex v) {
            if (v.isDependent() && v.degree() == 0)
                remove(v);
        }

        /**
         * if the UI node doesnt exist, it will add it to the graph and attempt creating one;
         * returns the existing one if already existed
         */
        protected UIVertex add(Named o) {

            UIVertex ui = getNode(o);

            if (ui == null) {

                ui = newVertex(o);
                if (ui == null) //should not be materialized
                    return null;


                try {
                    addNode(ui);
                } catch (StructuralException e) {
                    throw new RuntimeException(e);
                }

            } else {
                //allow upgrading from Term to Concept; not downgrading
                /*if ((existing.vertex.getClass() == Term.class) && (o instanceof Concept)) {
                    remove(existing.vertex);
                    return add(o);
                }*/

            }

            return ui;
        }

        public UIVertex getNode(Named v) {
            final Named input = v;
            if ((v instanceof Concept)||(v instanceof Task))
                v = ((Terms.Termable)v).getTerm();
            UIVertex u = getNode(v.name().toString());
            if (u!=null)
                u = u.add(input);
            return u;
        }


        protected UIVertex remove(Named v) {

            String id = v.name().toString();
            UIVertex existing = removeNode(id);
            if (existing == null) return null;

            existing = existing.remove(v);
            if (existing == null) return null;



            existing.destroy();

            List<UIEdge> toRemove = new ArrayList(existing.incoming.size() + existing.outgoing.size());
            toRemove.addAll(existing.incoming);
            toRemove.addAll(existing.outgoing);
            for (UIEdge e : toRemove) {
                remove(e);
            }

            SwingUtilities.invokeLater(existing.ui::destroy);


            return existing;
        }

        protected void edgesChanged() {
            edgesChanged = true;
        }

        @Override
        public String toPostScript(HashMap scriptData) {
            return null;
        }

        public Rectangle2D getLayoutBounds() {
            return layoutBounds;
        }

        abstract public class SubCycle implements StepListener {

            float lastStep = 0;
            long lastStepReal = System.currentTimeMillis();

            @Override
            public void stepStarted(float time) {
                double interval = getTimePerCycle();
                float dt = time - lastStep;
                int numCycles = (int) (Math.floor(dt / interval));

                if (numCycles > 0) {

                    long now = System.currentTimeMillis();
                    run(numCycles, time, now - lastStepReal);

                    lastStep = time;
                    lastStepReal = now;
                }

                //System.out.println(this + " run: " + time + " waiting since " + lastStep);
            }

            abstract public double getTimePerCycle();

            abstract public void run(int count, float endTime, long deltaMS);
        }

        private class MyGrapher extends DefaultGrapher {


            public MyGrapher() {
                super(false, false, false, false, 0, true, true);
            }

            @Override
            public Object addVertex(Named o) {
                //only non-dependent objects can be added outside of an edge
                return add(o);
            }

            @Override
            public Object addEdge(NARGraph g, Named source, Named target, Object edge) {

                //TODO defer creation of v1 and v2 until sure that edge can be created

                UIVertex v1 = add(source);
                if (v1 == null) return null;
                UIVertex v2 = add(target);
                if (v2 == null) return null;


                UIEdge e = new UIEdge(v1, v2, edge);
                if (add(e)) {
                    return e;
                }

                ensureValid(v1);
                ensureValid(v2);

                return null;
            }


        }


    }


}
