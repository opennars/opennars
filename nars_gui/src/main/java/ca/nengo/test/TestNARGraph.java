package ca.nengo.test;

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
import org.apache.commons.math3.util.FastMath;
import org.jgraph.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.UUID;


public class TestNARGraph extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    public static Node newGraph(NAR n) {
        NARGraphNode network = new NARGraphNode(n);


        return network;
    }

    public static class NARGraphNode extends NetworkImpl implements UIBuilder {

        private final NARGraph<NARGraphVertex,UIEdge> graph = new NARGraph();
        private final NAR nar;
        private final ConceptReaction conceptReaction;
        private UINARGraph ui;

        public synchronized NARGraphVertex vertex(final Object x, boolean createIfNotExist) {
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
                    vertex = new TermNode((Concept)x);
                }
                else if (x instanceof Terms.Termable) {
                    vertex = new TermNode((Terms.Termable)x);
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
                    NARGraphVertex node = vertex(c, false);
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
            if (graph.removeVertex(node)) {
                super.removeNode(node.vertex.toString());
            }
        }



        @Override
        public UINARGraph newUI(double width, double height) {
            return this.ui = new UINARGraph(this);
        }

        public static class UIEdge<V> extends DefaultEdge {
            public final V s, t;
            private final Object e;
            public Shape shape;

            public UIEdge(V s, V t, Object e) {
                super();
                this.s = s;
                this.t = t;

                if (e instanceof NARGraph.NAREdge) {
                    this.e = ((NARGraph.NAREdge)e).getObject();
                }
                else {
                    this.e = e;
                }
            }

            @Override
            public Object getSource() {
                return s;
            }

            @Override
            public Object getTarget() {
                return t;
            }
        }

        private class MyGrapher extends DefaultGrapher {

            public MyGrapher() {
                super(false, false, false, false, 0, true, false);
            }

            @Override
            public Object addVertex(Object o) {
                if (super.addVertex(o)!=null)
                    return vertex(o, true);
                return null;
            }

            @Override
            public Object addEdge(NARGraph g, Object source, Object target, Object edge) {
                final NARGraphVertex v1 = vertex(source, true);
                if (v1 == null) return null;
                final NARGraphVertex v2 = vertex(target, true);
                if (v2 == null) return null;

                return graph.addEdge(v1, v2, new UIEdge(v1, v2, edge));
            }
        }
    }
    public static class UINARGraph extends UINetwork {

        float arrowHeadScale = 1f / 16f;

        private final NARGraphNode nargraph;
        private final NARGraph<NARGraphVertex, NARGraphNode.UIEdge> graph;

        public UINARGraph(NARGraphNode n) {
            super(n);
            this.nargraph = n;
            this.graph = nargraph.graph;
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


            for (final NARGraphNode.UIEdge e : graph.edgeSet()) {

                NARGraphVertex source = (NARGraphVertex) e.getSource();
                if (source == null) continue;
                NARGraphVertex target = (NARGraphVertex) e.getTarget();
                if (target == null) continue;

                if ((!source.ui.getVisible() || !target.ui.getVisible() )) {
                    continue;
                }

                double sx = source.ui.getCenterX();
                double sy = source.ui.getCenterY();
                double tx = target.ui.getCenterX();
                double ty = target.ui.getCenterY();

                final float targetRadius = (float) target.ui.getWidth()/2f;
                //g.drawLine((int)sx, (int)sy, (int)tx, (int)ty);
                e.shape = drawArrow(g, (Polygon)e.shape, getEdgeColor(e), 256f, (int)sx, (int)sy, (int)tx, (int)ty, targetRadius);
            }
        }

        final Color tempColor = new Color(255, 128, 0, 120);

        public Color getEdgeColor(NARGraphNode.UIEdge e) {

            final Object x = e.e;
            if (x instanceof TermLink) {
                float p = ((TermLink)x).budget.getPriority();
                return new Color(p, 0, 0, p);
            }
            else if (e.e instanceof TaskLink) {
                float p = ((TermLink)x).budget.getPriority();
                return new Color(0, p, 0, p);
            }
            return tempColor;
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


        public NARGraphVertex(V vertex) {
            super(vertex.toString());
            this.vertex = vertex;

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

    }

    /** a node which can represent a pre-Term, a Term, or a Concept */
    public static class TermNode extends NARGraphVertex {
        String text;
        Terms.Termable term = null;
        Concept concept = null;


        private NodeIcon icon;
        private float priority = -1;

        public TermNode() {
            this("");
        }

        public TermNode(String text) {
            super(UUID.randomUUID().toString());
            this.text = text;
            this.term = null;
            setConcept(null);
        }

        public TermNode(Concept c) {
            super(c.getTerm());
            setConcept(c);
        }

        public TermNode(Terms.Termable term) {
            super(term);
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

        protected void updateUI() {
            if (concept != null) {

                float p = concept.getPriority();
                if (priority != p) {

                    icon.getBody().setPaint(Color.getHSBColor(p, 0.7f, 0.7f));
                    icon.getBody().setTransparency(0.8f);
                    ui.setScale(1.0 + p);

                    priority = p;
                }
            }

        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            updateUI();
        }


        @Override
        public void reset(boolean randomize) {

        }
    }




    @Override
    public void init() throws Exception {


        NAR nar = new NAR(new Default());
        nar.input("<a --> {b}>.");
        nar.input("<b --> c>.");
        nar.input("<[c] --> a>.");
        nar.run(200);


        UINetwork networkUI = (UINetwork) addNodeModel(newGraph(nar));
        NodeViewer window = networkUI.openViewer(Window.WindowState.MAXIMIZED);





        new Timer(50, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = 0.05f;
                    networkUI.getModel().run(time, time + dt);
                    time += dt;
                    nar.step(1);
                } catch (SimulationException e1) {
                    e1.printStackTrace();
                }
                //cycle();
            }
        }).start();

    }

    float time = 0;



    public static void main(String[] args) {
        new TestNARGraph().window(800, 600);
    }


}
