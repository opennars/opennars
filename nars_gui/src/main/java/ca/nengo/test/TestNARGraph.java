package ca.nengo.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.object.model.ModelObject;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.icon.NodeIcon;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.viewer.NodeViewer;
import ca.nengo.util.ScriptGenException;
import nars.build.Default;
import nars.core.NAR;
import nars.logic.entity.Concept;
import nars.util.graph.DefaultGrapher;
import nars.util.graph.NARGraph;

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

    public static class NARGraphNode extends NetworkImpl {

        private final NARGraph<Node,Object> graph;
        private final DefaultGrapher grapher;
        private final NAR nar;

        public NARGraphNode(NAR n) {
            super();

            this.grapher = new DefaultGrapher() {

                @Override
                public Object addVertex(NARGraph g, Object o) {
                    Object vert = super.addVertex(g, o);
                    try {
                        Node x = getNode(vert);
                        addNode(x);
                        return x;
                    } catch (StructuralException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public Object addEdge(NARGraph g, Object source, Object target, Object edge) {
                    return super.addEdge(g, source, target, edge);
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
        }

        public void updateStyle() {

        }



        private Node getNode(Object x) {
            if (x instanceof Concept) {
                return new ConceptNode((Concept)x);
            }
            return new ObjectNode("o" + n++, x);
        }
    }

    public static class ConceptNode extends AbstractWidget {
        public final Concept concept;
        private NodeIcon icon;


        public ConceptNode(Concept concept) {
            super("concept_" + concept.name());
            this.concept = concept;


        }

        @Override
        public WorldObject newIcon(ModelObject UI) {
            return icon = new NodeIcon(UI);
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        protected void paint(PaintContext paintContext, double width, double height) {

        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {
            float p = concept.getPriority();


            icon.getBody().setPaint(Color.getHSBColor(p, 0.7f, 0.7f));

            icon.setScale(1.0 + p);
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return concept.toString();
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
        NodeViewer window = networkUI.openViewer();
        window.zoomToFit();




        new Timer(150, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
                    networkUI.getModel().run(time, time + dt);
                    time += dt;
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
