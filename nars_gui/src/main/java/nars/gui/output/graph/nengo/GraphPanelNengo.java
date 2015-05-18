package nars.gui.output.graph.nengo;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import automenta.vivisect.dimensionalize.HyperassociativeMap;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.AbstractMapNetwork;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.UINeoNode;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.viewer.NodeViewer;
import com.google.common.collect.Iterators;
import nars.NAR;
import nars.gui.WrapLayout;
import nars.model.impl.Default;
import nars.nal.Named;
import nars.util.graph.TermLinkGraph;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.jgrapht.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.util.*;

/** for displaying any generic Graph */
public class GraphPanelNengo<V extends Named, E> extends Nengrow {

    private final GraphNode graphNode;
    private NodeViewer networkUIViewer;
    private NodeViewer window;




    float time = 0;
    boolean printFPS = false;
    int frame = 0;

    UINetwork networkUI = null;



    public static class GraphNode extends AbstractMapNetwork implements UIBuilder {

        GraphPanelNengo vis;

        float simTimePerLayout = 0.25f;
        float simTimePerRefresh = 0.8f;

        long lasTLayout = System.currentTimeMillis();
        Rectangle2D layoutBounds = new Rectangle2D.Double(-500, -500, 1000, 1000);
        private int numVertices = 0;

        private final Graph graph;
        private DefaultUINetwork ui;

        final HyperassociativeMap hyperLayout = new HyperassociativeMap(2) {
            @Override
            public ArrayRealVector getPosition(UIVertex node) {
                updateCoordinates(Collections.singleton(node));
                return node.getCoordinates();
            }



            @Override
            public double getEdgeWeight(UIEdge e) {
                    /*if (e.e instanceof TermLink) {
                        return 4.0;
                    }*/
                //return 0.25 + 0.5 * e.get;
                return 1;
            }


            @Override
            public double getRadius(UIVertex narGraphVertex) {
                double r = 1 + narGraphVertex.getRadius();
                return r * 100;
            }

            @Override
            protected Iterator<UIVertex> getVertices() {
                return Iterators.filter(nodes().iterator(), UIVertex.class);
            }

        };

        final FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>> organicLayout =
                new FastOrganicIterativeLayout<UIVertex, UIEdge<UIVertex>>(layoutBounds) {


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
                        //return 0.25 + 0.5 * e.get;
                        return 1;
                    }


                    @Override
                    public double getRadius(UIVertex narGraphVertex) {
                        double r = 1 + narGraphVertex.getRadius();
                        return r * 100;
                    }

                    @Override
                    protected Iterator<UIVertex> getVertices() {
                        return Iterators.filter(nodes().iterator(), UIVertex.class);
                    }


                };

        public GraphNode(Graph g) {
            this.graph = g;
            addStepListener(new SubCycle() {
                @Override
                public double getTimePerCycle() {
                    return simTimePerRefresh;
                }
                @Override
                public void run(int numCycles, float endTime, long deltaMS) {

            /*
            hmap.setEquilibriumDistance(55);
            hmap.setMaxRepulsionDistance(5000);
            hmap.setAttractionStrength(16);
            hmap.setSpeedFactor(10);
            */

                    try {
                        for (Object e : g.edgeSet()) {

                            Object source = g.getEdgeSource(e);
                            Object target = g.getEdgeTarget(e);


                            Node v1 = add((Named) source);
                            Node v2 = add((Named) target);


                            //allow both source and target opportunity to combine with existing vertex
                            if ((v1 == null || v2 == null)) continue;
                            if (v2 == v1) continue; //loop
                            if (!(v1 instanceof UIVertex)) continue;
                            if (!(v2 instanceof UIVertex)) continue;

                            UIEdge ee = addEdge((UIVertex) v1, (UIVertex) v2);
                            if (ee != null) {

                                //System.out.println("edge: " + source + " to " + target);
                                //System.out.println(" " + v1 + " to " + v2);

                                ee.add(e);
                            }

                        }
                        for (Object n : nodes()) {
                            if (n instanceof UIVertex) {
                                UIVertex v = (UIVertex)n;
                                java.util.List<UIEdge> removals = new ArrayList();
                                for (Object oe : v.getEdgesOut()) {
                                    UIEdge e = (UIEdge)oe;
                                    if (!g.containsEdge(e.getSource().vertex, e.getTarget().vertex))
                                        removals.add(e);
                                }
                                for (UIEdge r : removals) {
                                    remove(r);
                                }
                            }
                        }
                    }
                    catch (ConcurrentModificationException e) {
                        //HACK we'll try again next time
                    }



                }

                @Override
                public String toString() {
                    return "refresh";
                }
            });

            addStepListener(new SubCycle() {
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

                    double layoutRad = Math.sqrt(g.vertexSet().size()) * 400;
                    layoutBounds.setRect(-layoutRad / 2, -layoutRad / 2, layoutRad, layoutRad);
                    //hmap.setInitialTemp(200, 0.5f);
                    //hmap.setForceConstant(100);

                    organicLayout.setForceConstant(300);
                    organicLayout.resetLearning();
                    organicLayout.run(1);


                    //hyperLayout.run(1);


                }

                @Override
                public String toString() {
                    return "layout";
                }
            });

        }

        @Override
        public Object name(Node node) {
            return node.name();
        }

        @Override
        public UINeoNode newUI(double width, double height) {
            return this.ui = new DefaultUINetwork(this);
        }

        public void start(GraphPanelNengo vis) {
            this.vis = vis;

            for (Object o :graph.vertexSet()) {
                if (o instanceof Named) {
                    try {
                        addNode(newVertex((Named) o));
                    } catch (StructuralException e) {
                        e.printStackTrace();
                    }
                }
                else
                    System.err.println("Unable to create vertex for non-named " + o);
            }
        }
        public void stop() {

        }


        void updateCoordinates(Collection<UIVertex> vertices) {
            long now = System.currentTimeMillis();
            long dt = (now - lasTLayout);
            if (dt > 0) {
                numVertices = vertices.size();
                for (UIVertex v : vertices) {
                    v.getActualCoordinates(dt);
                    v.update();
                }
                lasTLayout = now;
            }
        }

        /**
         * adds or gets existing edge between two vertices, directional
         */
        public UIEdge<UIVertex> addEdge(UIVertex<?> s, UIVertex<?> t) {
            String name = s.name() + ':' + t.name();

            //this may be inefficient
            for (UIEdge e : s.getEdgesOut()) {
                if (e.name().equals(name)) {
                    return e;
                }
            }

            UIEdge exist = new UIEdge(s, t) {

                @Override
                public void update() {
                    super.update();
                    setPaint(vis.getEdgeColor(this));
                }
            };
            if (!add(exist))
                return null;
            return exist;
        }

        protected boolean add(UIEdge<UIVertex> e) {
            //if (edges.putIfAbsent(e.name(), e)==null) {
            if (e.getSource().link(e, false)) {
                e.getTarget().link(e, true);
                return true;
            }
            //}
            return false;
        }
        protected synchronized Node add(Named o) {

            Node ui = getNode(o.toString());

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

        public void refresh(Object x) {


        }

        /**
         * should be called from within a synchronized(graph) { } block
         */
        public UIVertex newVertex(final Named x) {

            return new UIVertex(x) {

                public NodeIcon icon;
                public double radius = 1;

                @Override
                public float getPriority() {
                    return 1;
                }

                @Override
                public boolean isDependent() {
                    return false;
                }

                @Override
                public UIVertex add(Named v) {
                    return null;
                }

                @Override
                public UIVertex remove(Named v) {
                    return null;
                }

                @Override
                public double getRadius() {
                    return radius;
                }

                @Override
                public ModelIcon newIcon(ModelObject UI) {
                    return icon = new NodeIcon(UI);
                }


                @Override
                protected void paint(ca.nengo.ui.lib.world.PaintContext paintContext, double width, double height) {
                    super.paint(paintContext, width, height);

                    float alpha = 0.75f;
                    float scale = 1f;
                    float angle = 0;


                    float r = 0.20f;
                    float g = 0.50f;
                    float b = 0.50f;

                    float priority = 1;


                    Color color = Color.GRAY;
                    color = new Color(r, g, b, alpha);
                    alpha = 0.5f + (0.5f * color.getAlpha()) / 256f;

                    icon.getBody().setPaint(color);
                    icon.getBody().setTransparency(alpha);

                    if (priority < 0) priority = 0;

                    double[] d = getCoordinates().getDataRef();

                    double x = d[0];
                    double y = d[1];

                    //bounds
                    Rectangle2D bounds = layoutBounds;
                    if (x > bounds.getMaxX()) x= bounds.getMaxX();
                    if (x < bounds.getMinX()) x = bounds.getMinX();
                    if (y > bounds.getMaxY()) y= bounds.getMaxY();
                    if (y < bounds.getMinY()) y = bounds.getMinY();

                    //TODO combine these into one Transform update

                    float targetScale = scale * (0.75f + priority);

                    ui.scaleTo(targetScale, 0.05);

                    ui.dragTo(x, y, bounds.getWidth()*1 /* speed */, 0.01);
                    //ui.animateToPositionScaleRotation(x, y, targetScale, 0, 0);

                    ui.getIcon().getBody().setRotation(angle);

                    //System.out.println(x + " " + y);
                    layoutPeriod = -1;


                    //radius = ui.getFullBoundsReference().getWidth()/2.0; //hegith?
                    radius = ui.getWidth()*2;

                }
            };

        }


    }

    public Color getEdgeColor(UIEdge<? extends UIVertex> v) {
        return Color.GRAY;
    }

    public static GraphNode newGraph(Graph g) {
        GraphNode network = new GraphNode(g);
        return network;
    }



    public GraphPanelNengo(Graph g) {
        this(newGraph(g));
    }

    @Override
    protected void initialize() {
        super.initialize();
    }

    public GraphPanelNengo(GraphNode graph) {
        super();

        this.fps = 10;

        this.graphNode = graph;


        try {
            networkUI = (UINetwork) addNodeModel(graphNode);
            window = networkUI.openViewer(Window.WindowState.MAXIMIZED);
            window.getGridLayer().setBgColor(Color.BLACK);
        } catch (ContainerException e) {
            e.printStackTrace();
        }

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                networkUI.getViewerWindow().setWindowState(Window.WindowState.MAXIMIZED, true);
            }
        });


        JPanel controls = new JPanel(new WrapLayout(FlowLayout.LEFT));
        controls.setOpaque(false);
        setBackground(Color.BLACK);
//        controls.add(new NSlider(new AtomicDouble(0.5f), "Concept Pri >", 0f, 1.0f));
//        controls.add(new NSlider(new AtomicDouble(0.5f), "TermLink Pri >", 0f, 1.0f));
//        controls.add(new NSlider(new AtomicDouble(0.5f), "TaskLink Pri >", 0f, 1.0f));
//        controls.add(new NSlider(new AtomicDouble(0.5f), "Task Pri >", 0f, 1.0f));

        add(controls, BorderLayout.SOUTH);


    }

    @Override
    protected void start() {
        super.start();
        graphNode.start(this);
    }

    @Override
    protected void stop() {
        super.stop();
        graphNode.stop();
    }

    //
//    @Override
//    protected NodeContainer getRoot() {
//        //return super.getRoot();
//        return networkUIViewer;
//    }





    @Override
    public void run() {

        long start = 0;

        float dt = 0.25f;
        try {


            if (printFPS) {
                if (frame % 100 == 0) {
                    start = System.nanoTime();
                }
            }

            if (networkUI!=null)
                networkUI.node().run(time, time + dt, 1);


            if (printFPS) {
                if (frame % 100 == 0) {
                    long end = System.nanoTime();
                    double time = (end - start) / 1e6;

                    System.out.println(this + " " + time + " ms");
                }
            }


        } catch (SimulationException e) {
            e.printStackTrace();
        }

        time += dt;
        frame++;
    }


    @Override
    public void init() throws Exception {

    }


    //TEST
    public static void main(String[] arg) {
        NAR n = new NAR(new Default());

        n.input("<(&&,<$x --> flyer>,<$x --> [chirping]>) ==> <$x --> bird>>.");
        n.input("<<$y --> [withwings]> ==> <$y --> flyer>>.");

        n.run(5);

        TermLinkGraph g = new TermLinkGraph();
        g.add(n.concept("<(&&,<$x --> flyer>,<$x --> [chirping]>) ==> <$x --> bird>>"), true);
        g.add(n.concept("<<$y --> [withwings]> ==> <$y --> flyer>>"), true);

        new GraphPanelNengo<>(g).newWindow(600,500);
    }
}
