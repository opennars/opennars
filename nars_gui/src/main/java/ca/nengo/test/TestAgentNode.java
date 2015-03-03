package ca.nengo.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.model.node.UINetwork;
import ca.nengo.ui.model.plot.AbstractWidget;
import ca.nengo.ui.model.plot.LinePlot;
import ca.nengo.ui.model.plot.StringView;
import ca.nengo.ui.model.widget.PadNode;
import ca.nengo.ui.model.widget.SliderNode;
import ca.nengo.util.ScriptGenException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

/**
 * Created by me on 3/3/15.
 */
public class TestAgentNode extends Nengrow {


    float time = 0;

    public static void main(String[] args) {
        new TestAgentNode();
    }

    public Node newBagNodeDemo() throws StructuralException {
        NetworkImpl network = new NetworkImpl();

        network.addNode(new AgentNode("NARBot1"));

        network.addNode(new StringView("Text1"));
        network.addNode(new LinePlot("Plot1"));
        network.addNode(new SliderNode("A", 0, 0, 1f));
        network.addNode(new SliderNode("B", 0, 0, 50f));
        network.addNode(new PadNode("XY", 2, 0, 8, 4, 0, 8));

        return network;
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

    public static class AgentNode extends AbstractWidget {

        float animationLerpRate = 0.5f; //LERP interpolation rate

        double heading = 0, cheading = 0.57;
        double cx, cy, x, y;
        double time = 0;

        public AgentNode(String name) {
            super(name);


            cx = x = 0;
            cy = y = 0;


        }



        public void forward(double dist) {
            x += Math.cos(heading) * dist;
            y += Math.sin(heading) * dist;

        }

        public void say(String message) {
            ui.showPopupMessage(message);
        }

        public double rotate(double dA) {
            heading += dA;
            return heading;
        }

        @Override
        public boolean isResizable() {
            return false;
        }

        @Override
        public Rectangle2D getInitialBounds() {
            return new Rectangle2D.Double(-32,-32,64,64);
        }

        protected boolean canMove() {
            return !ui.isSelected();
        }

        @Override
        protected void paint(PaintContext paintContext, double width, double height) {

            Graphics2D g = paintContext.getGraphics();

            float scale = (float) Math.sin(time * 10f) * 0.05f + 1.0f;

            double ww = width * scale;
            double hh = height * scale;

            if (canMove()) {
                cx = (cx * (1.0f - animationLerpRate)) + (x * animationLerpRate);
                cy = (cy * (1.0f - animationLerpRate)) + (y * animationLerpRate);
                cheading = (cheading * (1.0f - animationLerpRate / 2.0f)) + (heading * animationLerpRate / 2.0f);



                ui.translate(cx, cy);
                //ui.animateToPosition(x, y, 0.15f);
                //space.translate(cx, cy);
            }
            else {
                //freeze in place
                x = cx;
                y = cy;
                heading = cheading;
            }

            g.setPaint(Color.ORANGE);
            g.fillOval((int) (-ww / 2), (int) (-hh / 2), (int) (ww), (int) hh);


            //eyes
            g.setPaint(Color.BLUE);
            g.rotate(cheading);
            int eyeDiam = (int)(width * 0.2f);
            int eyeSpace = (int)(0.15f*width);
            int eyeDist = (int)(0.4f*width);
            g.fillOval(eyeDist, -eyeSpace-eyeDiam/2, eyeDiam, eyeDiam);
            g.fillOval(eyeDist, eyeSpace-eyeDiam/2, eyeDiam, eyeDiam);
            g.rotate(0);

        }


        @Override
        public void run(float startTime, float endTime) throws SimulationException {


            time = endTime;

            rotate( (endTime-startTime)*24f  );
            forward( Math.random() * 0.01 );

            if (Math.random() < 0.02) {
                say("hi!");
            }
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return "";
        }

    }

}
