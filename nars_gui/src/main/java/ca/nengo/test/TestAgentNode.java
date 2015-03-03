package ca.nengo.test;

import ca.nengo.model.AgentNode;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.plot.StringView;
import ca.nengo.ui.model.widget.PadNode;
import ca.nengo.ui.model.widget.SliderNode;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

/**
 * Created by me on 3/3/15.
 */
public class TestAgentNode extends Nengrow {


    float time = 0;

    public static void main(String[] args) {
        new TestAgentNode();
    }

    public Node newAgentNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        AgentNode an = new AgentNode("NARBot1") {
            @Override
            public void run(float startTime, float endTime) throws SimulationException {
                super.run(startTime, endTime);

                time = endTime;

                rotate( (endTime-startTime)*24f  );
                forward( Math.random() * 10 );

                if (Math.random() < 0.02) {
                    say("hi!");
                }

            }
        };
        an.setMovementBounds(new Rectangle2D.Double(0, 0, 500.01, 500.01));
        network.addNode(an);

        network.addNode(new StringView("Text1"));
        network.addNode(new LinePlot("Plot1"));
        network.addNode(new SliderNode("A", 0, 0, 1f));
        network.addNode(new SliderNode("B", 0, 0, 50f));
        network.addNode(new PadNode("XY", 2, 0, 8, 4, 0, 8));

        return network;
    }

    @Override
    public void init() throws Exception {


        UINetwork networkUI = (UINetwork) addNodeModel(newAgentNodeDemo());
        networkUI.doubleClicked();


        new Timer(50, new ActionListener() {

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

}
