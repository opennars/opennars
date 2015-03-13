package ca.nengo.test;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.math.JuRLsFunctionApproximator;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.FunctionPlot;
import ca.nengo.ui.model.widget.SliderNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestFunctionPlot extends Nengrow {

    long time = 0;


    @Override
    public void init() throws Exception {
        NetworkImpl network = newFunctionApproximationDemo();


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

    public static NetworkImpl newFunctionApproximationDemo() throws StructuralException {
        NetworkImpl network = new DefaultNetwork("Function Approximation");

        network.addNode( new SliderNode("Detail Level", 8f, 3, 24f));
        network.addNode(new JuRLsFunctionApproximator("Fourier Approximator Test"));
        network.addNode(new FunctionPlot("Function Plot"));
        return network;
    }


    public static void main(String[] args) {
        new TestFunctionPlot();
    }
}
