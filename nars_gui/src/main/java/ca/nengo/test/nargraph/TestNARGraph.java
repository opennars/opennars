package ca.nengo.test.nargraph;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import automenta.vivisect.dimensionalize.UIEdge;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;
import nars.build.Default;
import nars.core.NAR;
import nars.core.Parameters;
import nars.event.ConceptReaction;
import nars.gui.NARSwing;
import nars.logic.entity.Concept;
import nars.logic.entity.Named;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;

import javax.swing.*;
import java.io.File;
import java.util.*;

import static automenta.vivisect.dimensionalize.HyperassociativeMap.randomCoordinatesArray;


public class TestNARGraph extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    public static Node newGraph(NAR n) {
        NARGraphNode network = new NARGraphNode(n);
        return network;
    }

    public static class NARGraphNode extends AbstractMapNetwork<String,UIVertex> implements UIBuilder {

        private final NARGraph<UIVertex,UIEdge<UIVertex>> graph = new NARGraph<UIVertex,UIEdge<UIVertex>>() {

//            @Override
//            public boolean removeVertex(UIVertex o) {
//                if (super.removeVertex(o)) {
//                    remove(o); //remove widget
//                    return true;
//                }
//                return false;
//            }

        };

        private final NAR nar;
        private final ConceptReaction conceptReaction;
        private UINARGraph ui;


        float simTimePerCycle = 0.5f;
        float simTimePerLayout = 0.5f;


        float radius = 2500;
        final FastOrganicIterativeLayout<UIVertex,UIEdge<UIVertex>> hmap =
                new FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>>(graph, radius) {
        /*final HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>> hmap =
            new HyperassociativeMap<NARGraphVertex,UIEdge<NARGraphVertex>>(graph, 2) {*/

            @Override public ArrayRealVector newPosition(UIVertex node) {
                ArrayRealVector a = node.getCoordinates();
                randomCoordinatesArray(a.getDataRef());
                a.mapMultiplyToSelf(250);
                return a;
            }



            @Override
            public void pre(Collection<UIVertex> vertices) {
                updateCoordinates(vertices);
            }


            @Override
            public double getEdgeWeight(UIEdge<UIVertex> e) {
                /*if (e.e instanceof TermLink) {
                    return 4.0;
                }*/
                return 1.0;
            }


            @Override
            public double getRadius(UIVertex narGraphVertex) {
                double r = 1+narGraphVertex.ui.getFullBoundsReference().getWidth();
                return r * 0.5f;
            }


        };//.scale(10, 200, 20);





        long lasTLayout = System.currentTimeMillis();
        public List<UIEdge<UIVertex>> nextEdges = new ArrayList();

        void updateCoordinates(Collection<UIVertex> vertices) {
            long now = System.currentTimeMillis();
            long dt = (now - lasTLayout);
            if (dt > 0) {
                for (UIVertex v : vertices) {
                    v.getActualCoordinates(dt);
                }
                lasTLayout = now;
            }
        }

        /** returns a list of edges which can be used by drawing thread for non-synchronized fast drawing */
        public List<UIEdge<UIVertex>> getEdges() {
            synchronized(graph) {
                if (edgesChanged) {
                    edgesChanged = false;
                    nextEdges = new ArrayList(graph.edgeSet()); //use atomicRef?
                }
            }
            return nextEdges;
        }




        abstract public class SubCycle {

            float lastCycle = 0;
            long lastCycleReal = System.currentTimeMillis();

            public void update(float endTime) {
                double interval = getTimePerCycle();
                    float dt = endTime - lastCycle;
                    int numCycles = (int)(Math.floor( dt / interval));

                    if (numCycles > 0) {

                        long now = System.currentTimeMillis();
                        run(numCycles, endTime, now - lastCycleReal);

                        lastCycle = endTime;
                        lastCycleReal = now;
                    }
                //System.out.println("run: " + endTime);
                //System.out.println("waiting since " + lastCycle + " at " + endTime);
            }

            abstract public double getTimePerCycle();
            abstract public void run(int count, float endTime, long deltaMS);
        }

        SubCycle narCycle = new SubCycle() {
            @Override public double getTimePerCycle() {
                return simTimePerCycle;
            }
            @Override public void run(int numCycles, float endTime, long deltaMS) {

                nar.step(numCycles);
            }
        };
        SubCycle layoutCycle = new SubCycle() {
            @Override public double getTimePerCycle() {
                return simTimePerLayout;
            }
            @Override public void run(int numCycles, float endTime, long deltaMS) {

                /*
                hmap.setEquilibriumDistance(55);
                hmap.setMaxRepulsionDistance(5000);
                hmap.setAttractionStrength(16);
                hmap.setSpeedFactor(10);
                */

                hmap.resetLearning();
                hmap.run(1);
            }
        };


        @Override
        public String name(UIVertex node) {
            return node.vertex.name().toString();
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            super.run(startTime, endTime);

            narCycle.update(endTime);
            layoutCycle.update(endTime);

        }


        /** vertex can just call their .getCoordinate() to get the same value without needing to do a map lookup */
        public ArrayRealVector getPosition(UIVertex v) {
            ArrayRealVector a = hmap.getPosition(v);
            return a;
        }

        /** should be called from within a synchronized(graph) { } block */
        public UIVertex newVertex(final Named x) {

            TermNode v = null;

                if (x instanceof Concept) {
                    v = new TermNode(this, (Concept)x);
                }
                else if (x instanceof Task) {
                    v = new TermNode(this, (Task)x);
                }
                else if (x instanceof Term) {
                    v = new TermNode(this, (Term)x);
                }



            return v;
        }

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
                    if (add(c)!=null)
                        refresh(c);
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

            updateItems();
        }


        public void updateItems() {

        }


        public void refresh(Object x) {
            new MyGrapher().on(null, x, true).finish();
        }


        @Override
        public UINARGraph newUI(double width, double height) {
            return this.ui = new UINARGraph(this);
        }

        private class MyGrapher extends DefaultGrapher {


            public MyGrapher() {
                super(false, false, false, false, 0, true, true);
            }

            @Override
            public Object addVertex(Named o) {
                return add(o);
            }

            @Override
            public Object addEdge(NARGraph g, Named source, Named target, Object edge) {

                UIEdge e;
                //TODO use getVertex to see if it already exists to avoid reconstrucing
                UIVertex v1 = add(source);
                if (v1 == null) return null;
                UIVertex v2 = add(target);
                if (v2 == null) return null;


                    e = new UIEdge(v1, v2, edge);
                    boolean added;
                    synchronized (graph) {
                         added = graph.addEdge(v1, v2, e);
                    }
                    if (added) {
                        updateEdges();
                            return e;
                        }

                return null;
            }


        }

        /** if the UI node doesnt exist, it will add it to the graph and attempt creating one;
         *  returns the existing one if already existed
         * */
        protected UIVertex add(Named o) {

            UIVertex existing = getNode(o);

            if (existing==null) {
                synchronized (graph) {

                    existing = newVertex(o);

                    if (!graph.addVertex(existing))
                        throw new RuntimeException("graph / index inconsistency; vertex already existed");

                    try {
                        addNode(existing);
                    } catch (StructuralException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
            else {
                //allow upgrading from Term to Concept; not downgrading
                if ((existing.vertex.getClass() == Term.class) && (o instanceof Concept)) {
                    remove(existing.vertex);
                    return add(o);
                }


            }



            return existing;
        }

        public UIVertex getNode(Named v) {
            return getNode(v.name().toString());
        }

        protected UIVertex remove(Named v) {

            String id = v.name().toString();
            UIVertex existing = getNode(id);
            if (existing == null) return null;

            SwingUtilities.invokeLater(existing.ui::destroy);

            synchronized (graph) {

                int edgesBefore = graph.edgeSet().size();

                if (!graph.removeVertex(existing))
                    throw new RuntimeException("graph / index inconsistency; vertex already existed");

                int edgesAfter = graph.edgeSet().size();


                try {
                    removeNode(id);
                } catch (StructuralException e) {
                    throw new RuntimeException(e);
                }


                edgesChanged = edgesAfter!=edgesBefore;
            }



            return existing;
        }

        private boolean edgesChanged = false;

        protected void updateEdges() {
            if (!edgesChanged) {
                edgesChanged = true;
            }
        }

        @Override
        public String toPostScript(HashMap scriptData) {
            return null;
        }

    }


    @Override
    public void init() throws Exception {


        Default d = new Default(256, 1, 1);
        d.setSubconceptBagSize(0);
        NAR nar = new NAR(d);
        /*nar.input("<a --> {b}>.");
        nar.input("<b --> c>.");
        nar.input("<[c] --> a>.");*/

//        nar.input("<{(*,key1,value1),(*,key2,value2)} --> table>.");

  //      nar.input("<a --> b>.");

        //nar.run(200);
        Parameters.DEBUG = true;
        NARSwing.themeInvert();
        new NARSwing(nar);
        nar.input(new File("/tmp/h.nal"));


        UINetwork networkUI = (UINetwork) addNodeModel(newGraph(nar));
        NodeViewer window = networkUI.openViewer(Window.WindowState.MAXIMIZED);

        float fps = 50f;

        java.util.Timer timer = new java.util.Timer("", false);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float dt = 0.25f;
                try {
                    networkUI.node().run(time, time + dt);
                } catch (SimulationException e) {
                    e.printStackTrace();
                }
                time += dt;
            }
        }, 0, (int)(1000/fps));


//        Timer t = new Timer((int)(1000/fps), new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    float dt = 0.5f;
//                    networkUI.node().run(time, time + dt);
//                    time += dt;
//                } catch (SimulationException e1) {
//                    e1.printStackTrace();
//                }
//                //cycle();
//            }
//        });
//        t.setCoalesce(true);
//         t.start();

    }

    float time = 0;



    public static void main(String[] args) {
        new TestNARGraph().window(800, 600);
    }


}
