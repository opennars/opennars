package ca.nengo.test;

import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.DefaultNetwork;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.widget.PadNode;
import ca.nengo.ui.model.widget.SliderNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestSliderNode extends Nengrow {

    long time = 0;


    @Override
    public void init() throws Exception {
        NetworkImpl network = new DefaultNetwork();

        network.addNode( new SliderNode("Slide", 0.25f, 0, 1f));

        network.addNode( new PadNode("Pad", 0.5f, 0, 1, 0.5, 0, 1f));
        network.addNode(new LinePlot("Activity"));


/*        network.addNode( new SpikingNeuron(null, null, 1, 0.5f, "B"));
        network.addNode( new GruberNeuronFactory(new GaussianPDF(), new GaussianPDF()).make("x"));*/


        UINetwork networkUI = (UINetwork) addNodeModel(network);

        networkUI.doubleClicked();

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


    public static void main(String[] args) {
        new TestSliderNode().newWindow(600, 600);
    }
}
