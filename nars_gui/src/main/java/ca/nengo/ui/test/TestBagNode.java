package ca.nengo.ui.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.plot.StringView;
import ca.nengo.ui.model.widget.SliderNode;
import nars.build.Default;
import nars.control.DefaultCore;
import nars.core.NAR;
import nars.io.Output;
import nars.logic.entity.Item;
import nars.util.bag.Bag;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;


public class TestBagNode extends Nengrow {



    public Node newBagNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        ObjectNode narUI;
        network.addNode(narUI = new ObjectNode("NAR", new NARNode()));



        ObjectNode bagUI;
        network.addNode(bagUI = new ObjectNode("Concepts Bag", new BagNode()));

//        network.addProjection(
//                narUI.getSource("public nars.util.bag.Bag<nars.logic.entity.Term, nars.logic.entity.Concept> ca.nengo.ui.test.TestBagNode$NARNode.concepts()"),
//                bagUI.getTarget("Concepts Bag_public void ca.nengo.ui.test.TestBagNode$BagNode.setBag(nars.util.bag.Bag<K, V>)"));

        //network.addNode(new ObjectNode("NAR Output", new NAROutput(n)));

        network.addNode(new StringView("Text1"));
        network.addNode(new LinePlot("Plot1"));
        network.addNode(new SliderNode("NAR FPS", 0, 0, 2.0f));

        return network;
    }

    public static class NARNode {
        final NAR n;

        public NARNode() {
            n = new NAR(new Default());
            n.addInput("<a --> b>.");
            n.addInput("<b --> c>. :|:");
            n.run(10);
        }

        public void setFramesPerSecond(Double fps) {
            if (fps !=null) {
                n.step( fps.intValue());
            }
        }

        public Bag/*<Term,Concept>*/ concepts() {
            return ((DefaultCore) n.memory.concepts).concepts;
        }
    }

    public static class NAROutput extends Output {

        public NAROutput(NAR n) {
            super(n);
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
                return bag.getPriorityDistribution(6); //TODO re-use pre-allocated array
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


    public void cycle() {


        for (WorldObject x : this.getNengoWorld().getChildren()) {
                    //System.out.println( x.getChildren() );
            //x.run(time, time+1);
        }
    }

    public static void main(String[] args) {
        new TestBagNode();
    }


}
