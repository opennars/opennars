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
import nars.logic.entity.Concept;
import nars.logic.entity.Item;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;
import org.apache.commons.math3.util.FastMath;
import org.jgraph.graph.DefaultEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;


public class TestNARGraph extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst


    static int n = 0;
    public static Node newGraph(NAR n) {
        NARGraphNode network = new NARGraphNode(n);


        return network;
    }

    public static class NARGraphNode extends NetworkImpl implements UIBuilder {

        private final NARGraph<NARGraphVertex,DefaultEdge> graph;
        private final DefaultGrapher grapher;
        private final NAR nar;

        public NARGraphVertex vertex(final Item o) {
            return (NARGraphVertex) getNode(o.name().toString());
        }

        public NARGraphNode(NAR n) {
            super();

            this.grapher = new DefaultGrapher(false,false,false,false,0,true,false) {

                @Override
                public Object addVertex(NARGraph g, Object o) {
                    try {
                        NARGraphVertex x = newNode(o);
                        if (x!=null) {
                            addNode(x);
                            graph.addVertex(x);
                            return x;
                        }
                    } catch (StructuralException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public Object addEdge(NARGraph g, Object source, Object target, Object edge) {
                    if ((source instanceof Item) && (target instanceof Item)) {
                        final NARGraphVertex v1 = vertex((Item) source);
                        final NARGraphVertex v2 = vertex((Item) target);
                        if (v1!=null && v2!=null)
                            return graph.addEdge(v1, v2, new DefaultEdge(edge) {
                                @Override public Object getSource() {
                                    return v1;
                                }
                                @Override public Object getTarget() {
                                    return v2;
                                }
                            });
                    }
                    return null;
                }
            };
            
            this.graph = new NARGraph();
            this.nar = n;


            updateItems();
        }

        public void updateItems() {
            for (Concept c : nar.memory.concepts) {
                grapher.onConcept(graph, c);
            }
            grapher.finish(graph);


        }

        public void updateStyle() {

        }



        private NARGraphVertex newNode(Object x) {
            if (x instanceof Concept) {
                return new ConceptNode((Concept)x);
            }
            return null; //new ObjectNode("o" + n++, x);
        }

        @Override
        public UINARGraph newUI(double width, double height) {
            return new UINARGraph(this);
        }

    }
    public static class UINARGraph extends UINetwork {

        float arrowHeadScale = 1f / 16f;

        private final NARGraphNode nargraph;
        private final NARGraph<NARGraphVertex, DefaultEdge> graph;

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

        public Polygon drawArrow(final Graphics2D g, final Color color, final float thick, final int x1, final int y1, final int x2, final int y2, float destinationRadius) {
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


                Polygon p = new Polygon(); //TODO recycle this .reset()
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


            for (final DefaultEdge e : graph.edgeSet()) {

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
                drawArrow(g, getEdgeColor(e), 256f, (int)sx, (int)sy, (int)tx, (int)ty, targetRadius);
            }
        }

        final Color tempColor = new Color(255, 128, 0, 120);

        public Color getEdgeColor(DefaultEdge e) {
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

    abstract public static class NARGraphVertex<V extends Item> extends AbstractWidget {

        private final V vertex;


        public NARGraphVertex(V vertex) {
            super(vertex.name().toString());
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
            return vertex.name().toString();
        }

    }

    public static class ConceptNode extends NARGraphVertex {
        public final Concept concept;
        private NodeIcon icon;
        private float priority;


        public ConceptNode(Concept concept) {
            super(concept);
            this.concept = concept;


            updateStyle();
        }


        @Override
        public ModelIcon newIcon(ModelObject UI) {
            return icon = new NodeIcon(UI);
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        protected void updateStyle() {
            float p = concept.getPriority();
            if (priority!=p) {

                icon.getBody().setPaint(Color.getHSBColor(p, 0.7f, 0.7f));
                ui.setScale(1.0 + p);

                priority = p;
            }

        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            updateStyle();
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





        new Timer(150, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
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
        new TestNARGraph();
    }


}
