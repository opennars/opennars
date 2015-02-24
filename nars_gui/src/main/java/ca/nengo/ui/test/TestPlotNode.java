package ca.nengo.ui.test;

import ca.nengo.math.impl.GaussianPDF;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.NoiseFactory;
import ca.nengo.model.neuron.impl.SpikingNeuron;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.ui.models.plot.LinePlot;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestPlotNode extends Nengrow {

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst

    @Override
    public void init() throws Exception {
        NetworkImpl network = new NetworkImpl();
        network.addNode(new SpikingNeuron(null, null, 1, 0.5f, "A").
                setNoise(NoiseFactory.makeRandomNoise(100f, new GaussianPDF())));
        network.addNode( new SpikingNeuron(null, null, 1, 0.5f, "B"));
        network.addNode(new LinePlot("plot1"));


        UINetwork networkUI = (UINetwork) addNodeModel(network);

        networkUI.doubleClicked();
        //addNodeModel(new SpikingNeuron(null, null, 1, 0, "C"));


        //networkUI.addNodeModel(new LinePlotUI("plot1"), 50d, 20d);

        network.run(0,0);


        new Timer(10, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = 0.001f; //myStepSize
                    network.run(time, time+dt);
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
//    default void run(float start, float stop) {
//        System.out.println(this + " " + getClass());
//        //if (getWorld()!=null)
//        for (WorldObject x : getChildren()) {
//            System.out.println(x + " " + x.getClass());
//            x.run(start, stop);
//            if (x instanceof ModelObject) {
//                ModelObject o = ((ModelObject) x);
//                Object mo = o.getModel();
//                if (mo instanceof Node) {
//                    try {
//                        ((Node)mo).run(start, stop);
//                    } catch (SimulationException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//    }

    public static void main(String[] args) {
        new TestPlotNode();
    }
}
