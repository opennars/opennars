package ca.nengo.ui.test;

import ca.nengo.math.impl.PoissonPDF;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.NoiseFactory;
import ca.nengo.neural.neuron.impl.LIFSpikeGenerator;
import ca.nengo.neural.neuron.impl.LinearSynapticIntegrator;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.WorldObject;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.ui.models.plot.LinePlot;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestPlotNode extends Nengrow {
    public static final float RESOLUTION_SEC = .001f;

    //https://github.com/nengo/nengo_1.4/blob/master/simulator-ui/docs/simplenodes.rst

    @Override
    public void init() throws Exception {
        NetworkImpl network = new NetworkImpl();

        SpikingNeuron sn = new SpikingNeuron(
                new LinearSynapticIntegrator(RESOLUTION_SEC, Units.ACU),
                new  LIFSpikeGenerator(.001f, .02f, .002f), 1, 0.9f, "A");

        network.addNode(sn.setNoise(NoiseFactory.makeRandomNoise(100f, new PoissonPDF(100f))));

        network.addNode(new LinePlot("plot1"));


        UINetwork networkUI = (UINetwork) addNodeModel(network);

        networkUI.doubleClicked();


        new Timer(25, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT();
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
