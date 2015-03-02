package ca.nengo.ui.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectNode;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.ToStringView;
import nars.build.Default;
import nars.control.DefaultCore;
import nars.core.NAR;
import nars.logic.entity.Item;
import nars.util.bag.Bag;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestBagNode extends Nengrow {


    public static Node newBagNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        NAR n = new NAR(new Default());
        n.addInput("<a --> b>.");
        n.addInput("<b --> c>.");
        n.run(10);

        network.addNode(new ObjectNode("Bag", new BagNode(((DefaultCore) n.memory.concepts).concepts)));

        network.addNode(new ToStringView("ToString1"));

        return network;
    }

    public static class BagNode<K,V extends Item<K>> {

        public final Bag<K, V> bag;

        public BagNode(Bag<K,V> b) {
            super();
            this.bag = b;
        }

        public void input(V item) {
            bag.put(item);
        }

        public V peek() {
            return bag.peekNext();
        }

    }

    @Override
    public void init() throws Exception {



        UINetwork networkUI = (UINetwork) addNodeModel(newBagNodeDemo());
        networkUI.doubleClicked();


        new Timer(25, new ActionListener() {

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
