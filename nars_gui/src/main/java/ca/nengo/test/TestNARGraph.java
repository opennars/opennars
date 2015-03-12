package ca.nengo.test;

import automenta.vivisect.dimensionalize.FastOrganicIterativeLayout;
import automenta.vivisect.dimensionalize.IterativeLayout;
import automenta.vivisect.dimensionalize.UIEdge;
import automenta.vivisect.swing.ColorArray;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.elastic.ElasticGround;
import ca.nengo.ui.lib.world.piccolo.object.Window;
import ca.nengo.ui.model.UIBuilder;
import ca.nengo.ui.model.icon.ModelIcon;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.viewer.NetworkViewer;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.ScriptGenException;
import nars.build.Default;
import nars.core.NAR;
import nars.event.ConceptReaction;
import nars.logic.Terms;
import nars.logic.entity.Concept;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.util.FastMath;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static automenta.vivisect.dimensionalize.HyperassociativeMap.randomCoordinatesArray;


public class TestNARGraph extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    public static Node newGraph(NAR n) {
        NARGraphNode network = new NARGraphNode(n);


        return network;
    }

    public static class NARGraphNode extends NetworkImpl implements UIBuilder {

        private final NARGraph<NARGraphVertex,UIEdge<NARGraphVertex>> graph = new NARGraph();
        private final NARGraph<NARGraphVertex,UIEdge<NARGraphVertex>> prev = new NARGraph();
        private final NAR nar;
        private final ConceptReaction conceptReaction;
        private UINARGraph ui;


        float simTimePerCycle = 1.0f;
        float simTimePerLayout = 2.0f;


        final IterativeLayout<NARGraphVertex,UIEdge<NARGraphVertex>> hmap = new FastOrganicIterativeLayout<NARGraphVertex, UIEdge<NARGraphVertex>>(graph, 500) {
            @Override public ArrayRealVector newPosition(NARGraphVertex node) {
                ArrayRealVector a = node.getCoordinates();
                randomCoordinatesArray(a.getDataRef());
                a.mapMultiplyToSelf(525);
                return a;
            }


            @Override
            public void pre(Collection<NARGraphVertex> vertices) {
                updateCoordinates(vertices);
            }


            @Override
            public double getEdgeWeight(UIEdge<NARGraphVertex> e) {
                if (e.e instanceof TermLink) {
                    return 4.0;
                }
                return 0.5;
            }

            @Override
            public double getRadius(NARGraphVertex narGraphVertex) {
                double r = 1+narGraphVertex.ui.getFullBoundsReference().getWidth();
                return r/4.0;
                //return 150;
            }
        };


        long lasTLayout = System.currentTimeMillis();
        public List<UIEdge<NARGraphVertex>> nextEdges = new ArrayList();

        void updateCoordinates(Collection<NARGraphVertex> vertices) {
            long now = System.currentTimeMillis();
            long dt = (now - lasTLayout);
            if (dt > 0) {
                for (NARGraphVertex v : vertices) {
                    v.getActualCoordinates(dt);
                }
                lasTLayout = now;
            }
        }

        /** returns a list of edges which can be used by drawing thread for non-synchronized fast drawing */
        public List<UIEdge<NARGraphVertex>> getEdges() {
            return nextEdges;
        }

        abstract public class SubCycle {

            float lastCycle = 0;
            long lastCycleReal = System.currentTimeMillis();

            public void update(float endTime) {
                if (endTime - lastCycle >= getTimePerCycle()) {
                    float dt = endTime - lastCycle;
                    int numCycles = (int)Math.floor( dt );


                    long now = System.currentTimeMillis();
                    run(numCycles, endTime, now - lastCycleReal);

                    lastCycle = endTime;
                    lastCycleReal = now;
                    //System.out.println("run: " + endTime);
                }
                else {
                    //System.out.println("waiting since " + lastCycle + " at " + endTime);
                }
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
                hmap.run(2);
            }
        };

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            super.run(startTime, endTime);

            narCycle.update(endTime);
            layoutCycle.update(endTime);

        }


        /** vertex can just call their .getCoordinate() to get the same value without needing to do a map lookup */
        public ArrayRealVector getPosition(NARGraphVertex v) {
            ArrayRealVector a = hmap.getPosition(v);
            return a;
        }

        /** should be called from within a synchronized(graph) { } block */
        public NARGraphVertex vertex(final Object x, boolean createIfNotExist) {
            NARGraphVertex v = (NARGraphVertex) getNode(x.toString());
            if (v!=null) {
                //already exists
                if (x instanceof Concept) {
                    ((TermNode)v).setConcept((Concept) x);
                }
            }
            else if (v == null && createIfNotExist) {
                TermNode vertex = null;

                if (x instanceof Concept) {
                    vertex = new TermNode(this, (Concept)x);
                }
                else if (x instanceof Terms.Termable) {
                    vertex = new TermNode(this, (Terms.Termable)x);
                }

                if (vertex!=null) {
                    if (graph.addVertex(vertex)) {


                        try {
                            super.addNode(vertex);
                        } catch (StructuralException e) {
                            e.printStackTrace();
                        }

                        new MyGrapher().on(graph, x).finish();

                    }
                }
            }
            return v;
        }

        public NARGraphNode(NAR n) {
            super();

            this.nar = n;

            this.conceptReaction = new ConceptReaction(nar) {

                @Override
                public void onNewConcept(Concept c) {
                    vertex(c, true);
                }

                @Override
                public void onForgetConcept(Concept c) {
                    NARGraphVertex node;
                    node = vertex(c, false);
                    if (node!=null /*&& node instanceof TermNode*/) {
                        //another option is to set the node to pre-concept and leave it
                        try {
                            removeNode(node);
                        } catch (StructuralException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            updateItems();
        }


        public void updateItems() {

        }

        protected void removeNode(NARGraphVertex node) throws StructuralException {
            boolean removed;
            removed = graph.removeVertex(node);
            if (removed)
                super.removeNode(node.vertex.toString());
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
            public Object addVertex(Object o) {
                Object x;
                x = vertex(o, true);
                return x;
            }

            @Override
            public Object addEdge(NARGraph g, Object source, Object target, Object edge) {
                UIEdge e;
                final NARGraphVertex v1 = vertex(source, true);
                if (v1 == null) return null;
                final NARGraphVertex v2 = vertex(target, true);
                if (v2 == null) return null;

                graph.addEdge(v1, v2, e = new UIEdge(v1, v2, edge));

                updateEdges();

                return e;

            }
        }

        private boolean edgesChanged = false;

        protected void updateEdges() {
            if (!edgesChanged) {
                edgesChanged = true;
                SwingUtilities.invokeLater(this::updateCopyOfEdges);
            }
        }

        private void updateCopyOfEdges() {
            nextEdges = new ArrayList(graph.edgeSet()); //use atomicRef?
            edgesChanged = false;
        }

    }

    public static class UINARGraph extends UINetwork {

        float arrowHeadScale = 1f / 16f;

        private final NARGraphNode nargraph;

        public UINARGraph(NARGraphNode n) {
            super(n);
            this.nargraph = n;
        }

        @Override
        public ModelIcon getIcon() {
            return (ModelIcon) super.getIcon();
        }

        @Override
        public NodeViewer createViewerInstance() {
            return new UINARGraphViewer(this);
        }

        public Polygon drawArrow(final Graphics2D g, Polygon p, final Color color, final float thick, final int x1, final int y1, final int x2, final int y2, float destinationRadius) {
            final float arrowHeadRadius = /*len **/ arrowHeadScale * (thick);
            if (arrowHeadRadius > 0) {

                float dx = x2 - x1;
                float dy = y2 - y1;

                float angle = (float) (FastMath.atan2(dy, dx));
                final float arrowAngle = (float) FastMath.PI / 12f + thick / 200f;

                float len = (float) FastMath.sqrt(dx * dx + dy * dy) - destinationRadius;
                if (len <= 0) return null;

                final int ix2 = (int) (FastMath.cos(angle) * len + x1);
                final int iy2 = (int) (FastMath.sin(angle) * len + y1);

                final double aMin = angle - Math.PI - arrowAngle;
                final double aMax = angle - Math.PI + arrowAngle;

                int plx = (int) (FastMath.cos(aMin) * arrowHeadRadius);
                int ply = (int) (FastMath.sin(aMin) * arrowHeadRadius);
                int prx = (int) (FastMath.cos(aMax) * arrowHeadRadius);
                int pry = (int) (FastMath.sin(aMax) * arrowHeadRadius);



                //Triangle
                //g.triangle(x2, y2, x2 + prx, y2 + pry, x2 + plx, y2 + ply);

                //Quad
                //(x2, y2, x2 + prx, y2 + pry, x1, y1, x2 + plx, y2 + ply);


                if (p!=null)
                    p.reset();
                else
                    p = new Polygon(); //TODO recycle this .reset()
                p.addPoint(ix2, iy2);
                p.addPoint( ix2 + prx, iy2 + pry);
                p.addPoint( x1, y1);
                p.addPoint(  x2 + plx, y2 + ply );

                g.setPaint(color);
                g.fillPolygon(p);
                return p;
            }

            return null;
        }




        protected void drawEdges(PaintContext paintContext) {

            Graphics2D g = paintContext.getGraphics();


                for (final UIEdge e : nargraph.getEdges()) {

                    NARGraphVertex source = (NARGraphVertex) e.getSource();
                    if (source == null) continue;
                    NARGraphVertex target = (NARGraphVertex) e.getTarget();
                    if (target == null) continue;

                    if ((!source.ui.getVisible() || !target.ui.getVisible())) {
                        continue;
                    }

                    double sx = source.ui.getCenterX();
                    double sy = source.ui.getCenterY();
                    double tx = target.ui.getCenterX();
                    double ty = target.ui.getCenterY();

                    final float targetRadius = (float) target.ui.getWidth() / 2f;
                    //g.drawLine((int)sx, (int)sy, (int)tx, (int)ty);
                    e.shape = drawArrow(g, (Polygon) e.shape, getEdgeColor(e), 256f, (int) sx, (int) sy, (int) tx, (int) ty, targetRadius);

            }
        }

        final ColorArray red = new ColorArray(64, new Color(0.4f, 0.2f, 0.2f, 0.5f), new Color(1f, 0.7f, 0.3f, 1.0f));
        final ColorArray blue = new ColorArray(64, new Color(0.2f, 0.2f, 0.4f, 0.5f), new Color(0.3f, 0.7f, 1f, 1.0f));


        public Color getEdgeColor(UIEdge e) {

            final Object x = e.e;
            if (x instanceof TermLink) {
                float p = ((TermLink)x).budget.getPriority();
                return red.get(p);
            }
            else if (e.e instanceof TaskLink) {
                float p = ((TaskLink)x).budget.getPriority();
                return blue.get(p);
            }
            return Color.WHITE;
        }

        class UINARGraphGround extends ElasticGround {

            @Override
            public void paint(PaintContext paintContext) {
                super.paint(paintContext);
                drawEdges(paintContext);
            }
        }

        class UINARGraphViewer extends NetworkViewer {

            public UINARGraphViewer(UINARGraph g) {
                super(g, new UINARGraphGround());
            }


        }
    }

    abstract public static class NARGraphVertex<V> extends AbstractWidget {

        private final V vertex;
        private final ArrayRealVector coords;
        protected long layoutPeriod = -1;


        public NARGraphVertex(V vertex) {
            super(vertex.toString());
            this.vertex = vertex;
            this.coords = new ArrayRealVector(2);

            //initial random position, to seed layout
            ui.setOffset(Math.random()-0.5, Math.random()-0.5);
            getActualCoordinates(-1);
        }

        @Override
        public String toString() {
            return "NARGraphVertex[" + vertex.toString() + ']';
        }

        @Override
        public int hashCode() {
            return vertex.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NARGraphVertex)
                return vertex.equals(((NARGraphVertex)obj).vertex);
            return false;
        }

        @Override
        protected void paint(PaintContext paintContext, double width, double height) {

        }

        public V vertex() {
            return this.vertex;
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return vertex.toString();
        }

        public ArrayRealVector getCoordinates() {
            return coords;
        }

        /** loads the actual geometric coordinates to the coords array prior to next layout iteration */
        public void getActualCoordinates(long layoutPeriod /* in realtime msec */) {
            double x = getX();
            double y = getY();
            if (!Double.isFinite(x) || !Double.isFinite(y)) {
                this.layoutPeriod = -1;
                return;
            }
            coords.setEntry(0, x);
            coords.setEntry(1, y);
            this.layoutPeriod = layoutPeriod;
        }

        public double getX() {
            return ui.getPNode().getTransform().getTranslateX();
        }
        public double getY() {
            return ui.getPNode().getTransform().getTranslateY();
        }
    }

    /** a node which can represent a pre-Term, a Term, or a Concept */
    public static class TermNode extends NARGraphVertex {

        private final NARGraphNode graphnode;
        String text;
        Terms.Termable term = null;
        Concept concept = null;


        private NodeIcon icon;
        private float priority = -1;
        private float lastUIUpdate;
        private float minUpdateTime = 1;

        public TermNode(NARGraphNode graphnode) {
            this(graphnode, "");
        }

        public TermNode(NARGraphNode graphnode, String text) {
            super(UUID.randomUUID().toString());
            this.graphnode = graphnode;
            this.text = text;
            this.term = null;
            setConcept(null);
        }

        public TermNode(NARGraphNode graphnode, Concept c) {
            super(c.getTerm());
            this.graphnode = graphnode;
            setConcept(c);
        }

        public TermNode(NARGraphNode graphnode, Terms.Termable term) {
            super(term);
            this.graphnode = graphnode;
            this.term = term;
            setConcept(null);
        }

        public boolean setConcept(Concept c) {
            if (this.concept == c) return false;
            this.priority = -1; //will be updated
            this.concept = c;
            if (c == null) {
                this.term = null;
            }
            else {
                this.term = c.getTerm();
            }
            updateUI();
            return true;
        }



        @Override
        public ModelIcon newIcon(ModelObject UI) {
            return icon = new NodeIcon(UI);
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        final static ColorArray green = new ColorArray(64, new Color(0.2f, 0.4f, 0.2f, 0.25f), new Color(0.25f, 0.8f, 0.25f, 0.9f));

        protected void updateUI() {
            float p = 0;
            if (concept != null) {

                p = concept.getPriority();
            }
            else {
                p = 0.5f;
            }
            if (p < 0) p = 0;

            if (p!=priority) {

                priority = p;

                Color c = green.get(priority);
                icon.getBody().setPaint(c); // Color.getHSBColor(p, 0.7f, 0.7f));
                icon.getBody().setTransparency(0.5f + (0.5f * c.getAlpha()) / 256f);


            }

            if (layoutPeriod > 0) {
                double x = getCoordinates().getEntry(0);
                double y = getCoordinates().getEntry(1);
                if (Double.isFinite(x) && Double.isFinite(y)) {
                    ui.animateToPositionScaleRotation(x, y, 1.0f + priority, 0, (long)(layoutPeriod*2f));
                }
                //System.out.println(x + " " + y);
                layoutPeriod = -1;
            }
            else {

            }


        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            //real time mode
            if (endTime - lastUIUpdate > minUpdateTime) {
                updateUI();
                lastUIUpdate = endTime;
            }
        }


        @Override
        public void reset(boolean randomize) {

            lastUIUpdate = -1;
        }
    }




    @Override
    public void init() throws Exception {


        NAR nar = new NAR(new Default(32, 1, 1));
        nar.input("<a --> {b}>.");
        nar.input("<b --> c>.");
        nar.input("<[c] --> a>.");
        nar.run(200);


        UINetwork networkUI = (UINetwork) addNodeModel(newGraph(nar));
        NodeViewer window = networkUI.openViewer(Window.WindowState.MAXIMIZED);

        float fps = 10.0f;

        java.util.Timer timer = new java.util.Timer("", false);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float dt = 1f;
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
