package ca.nengo.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.plot.StringView;
import ca.nengo.ui.model.widget.PadNode;
import ca.nengo.ui.model.widget.SliderNode;
import nars.NAR;
import nars.bag.Bag;
import nars.event.NARReaction;
import nars.io.out.Output;
import nars.cycle.DefaultCycle;
import nars.nar.Default;
import nars.budget.Item;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;


public class TestBagNode extends Nengrow {



    public Node newBagNodeDemo() throws StructuralException {
        NetworkImpl network = new DefaultNetwork<>();

        ObjectNode narUI;
        NARNode narNode;
        network.addNode(narUI = new ObjectNode("NAR", narNode = new NARNode()));



        ObjectNode bagUI;
        network.addNode(bagUI = new ObjectNode("Concepts Bag", new BagNode()));
        //network.addNode(bagUI = new ObjectGraphNode("NARObj", narNode.n));

        //network.addNode(new ObjectNode("BagUI", new ObjectGraphController(bagUI)));

//        network.addProjection(
//                narUI.getSource("public nars.util.bag.Bag<nars.logic.entity.Term, nars.logic.entity.Concept> ca.nengo.ui.test.TestBagNode$NARNode.concepts()"),
//                bagUI.getTarget("Concepts Bag_public void ca.nengo.ui.test.TestBagNode$BagNode.setBag(nars.util.bag.Bag<K, V>)"));

        network.addNode(new ObjectNode("NAR Output", new NAROutput(narNode.n)));

        network.addNode(new StringView("Text1"));
        network.addNode(new LinePlot("Plot1"));
        network.addNode(new SliderNode("A", 0, 0, 1f));
        network.addNode(new SliderNode("B", 0, 0, 50f));
        network.addNode(new PadNode("XY", 2, 0, 8, 4, 0, 8));

        return network;
    }

//    public static class ObjectGraphController {
//        private final ObjectGraphNode.UIObjectGraphNode ui;
//
//        public ObjectGraphController(ObjectGraphNode ogn) {
//
//            ui = ogn.newUI(64,64);
//        }
//
//        public void setEquilibriumDistance(Double d) {
//            if (d!=null) {
//                if (ui.bodyLayout.getEquilibriumDistance()!=d) {
//                    ui.bodyLayout.reset();
//                    ui.bodyLayout.setEquilibriumDistance(d);
//                    ui.layoutChildren();
//                    ui.repaint();
//                }
//
//            }
//        }
//
//        public void setMaxDistance(Double d) {
//            if (d!=null) {
//                if (ui.bodyLayout.getMaxRepulsionDistance()!=d) {
//                    ui.bodyLayout.reset();
//                    ui.bodyLayout.setMaxRepulsionDistance(d);
//                    ui.layoutChildren();
//                    ui.repaint();
//                }
//
//            }
//
//        }
//        public void setAttractiveStrength(Double d) {
//            if (d!=null) {
//                if (ui.bodyLayout.getAttractionStrength()!=d) {
//                    ui.bodyLayout.reset();
//                    ui.bodyLayout.setAttractionStrength(d);
//                    ui.layoutChildren();
//                    ui.repaint();
//                }
//            }
//        }
//        public void setRepulsion(Double d) {
//            if (d!=null) {
//                if (ui.bodyLayout.getRepulsiveWeakness()!=d) {
//                    ui.bodyLayout.reset();
//                    ui.bodyLayout.setRepulsiveWeakness(d);
//                    ui.layoutChildren();
//                    ui.repaint();
//                }
//            }
//        }
//    }

    public static class NARNode {
        final NAR n;

        public NARNode() {
            n = new NAR(new Default());
            n.input("<a --> b>.");
            n.input("<b --> c>. :|:");
            n.runWhileNewInput(10);
        }

        public void setFramesPerSecond(Double fps) {
            if (fps !=null) {
                n.frame(fps.intValue());
            }
        }

        public Bag/*<Term,Concept>*/ concepts() {
            return ((DefaultCycle) n.memory.cycle).concepts;
        }
    }

    public static class NAROutput extends NARReaction {

        public NAROutput(NAR n) {
            super(n, Output.DefaultOutputEvents);
        }

        private String currentOutput = "";

        @Override
        public void event(Class event, Object[] args) {
            currentOutput = event.getSimpleName() + ": " + Arrays.toString(args);
        }

        public String getOutput() {
            return currentOutput;
        }
    }

    public static class BagNode<K,V extends Item<K>> {

        public Bag<K, V> bag;

        public BagNode() {
            super();
        }

        public void setBag(Bag<K,V> bag) {
            this.bag = bag;
        }

        public void input(V item) {
            if (bag!=null)
                bag.put(item);
        }

        public double[] getHistogram() {
            if (bag!=null)
                return bag.getPriorityHistogram(6); //TODO re-use pre-allocated array
            return null;
        }

        public V peekNext() {
            if (bag!=null)
                return bag.peekNext();
            return null;
        }

        public double getPriorityMean() {
            if (bag!=null)
                return bag.getPriorityMean();
            return 0;
        }

        public int size() {
            if (bag!=null)
                return bag.size();
            return 0;
        }
        public double getPrioritySum() {
            if (bag!=null)
                return bag.getPrioritySum();
            return 0;
        }
    }

    @Override
    public void init() throws Exception {



        UINetwork networkUI = (UINetwork) addNodeModel(newBagNodeDemo());
        networkUI.doubleClicked();



        new Timer(100, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
                    networkUI.node().run(time, time + dt);
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
        new TestBagNode();
    }


}
