package ca.nengo.ui.test;

import ca.nengo.model.SimulationException;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by me on 2/25/15.
 */
public class MultiDemo extends Nengrow {

    long time = 0;


    @Override
    public void init() throws Exception {


        UINetwork a = (UINetwork) addNodeModel(TestFunctionPlot.newFunctionApproximationDemo());
        a.doubleClicked();
        UINetwork b = (UINetwork) addNodeModel(TestPlotNode.newPlotNodeDemo());
        a.doubleClicked();


        new Timer(35, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT(); //myStepSize
                    a.getModel().run(time, time + dt);
                    b.getModel().run(time, time + dt);
                    time += dt;
                } catch (SimulationException e1) {
                    e1.printStackTrace();
                }
                //cycle();
            }
        }).start();

    }

    public static void main(String... args) {
        new MultiDemo();
    }
}