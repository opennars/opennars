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

        private final NARGraph<NARGraphVertex,Object> graph;
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

        private final NARGraphNode nargraph;
        private final NARGraph<NARGraphVertex, Object> graph;

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

        protected void drawEdges(PaintContext paintContext) {

            Graphics2D g = paintContext.getGraphics();

            g.setPaint(Color.WHITE);
            g.setStroke(new BasicStroke(1));

            for (Object e : graph.edgeSet()) {
                NARGraphVertex source = graph.getEdgeSource(e);
                if (source == null) continue;
                NARGraphVertex target = graph.getEdgeTarget(e);
                if (target == null) continue;


                double sx = source.ui.getIcon().getBody().getX();
                double sy = source.ui.getIcon().getBody().getY();
                double tx = target.ui.getIcon().getBody().getX();
                double ty = target.ui.getIcon().getBody().getY();


                g.drawLine((int)sx, (int)sy, (int)tx, (int)ty);
            }
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
        private double x, y;


        public NARGraphVertex(V vertex) {
            super(vertex.name().toString());
            this.vertex = vertex;

        }
        @Override
        protected void paint(PaintContext paintContext, double width, double height) {



        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
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
                icon.setScale(1.0 + p);

                priority = p;
            }

        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            //updateStyle();
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
