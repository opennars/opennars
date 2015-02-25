package ca.nengo.ui.test;

import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.models.math.JuRLsFunctionApproximator;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.ui.models.plot.FunctionPlot;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestFunctionPlot extends Nengrow {

    long time = 0;


    @Override
    public void init() throws Exception {
        NetworkImpl network = new NetworkImpl();
        network.addNode(new JuRLsFunctionApproximator("Approximator"));
        network.addNode(new FunctionPlot("plot1"));
        network.addNode( new SpikingNeuron(null, null, 1, 0.5f, "B"));


        UINetwork networkUI = (UINetwork) addNodeModel(network);

        networkUI.doubleClicked();

        network.run(0,0);


        new Timer(10, new ActionListener() {

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


    public static void main(String[] args) {
        new TestFunctionPlot();
    }
}
