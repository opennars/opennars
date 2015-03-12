package ca.nengo.test;

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
        b.doubleClicked();

//        PStyledText ps = new PStyledText();
//        getWorld().getSky().getPiccolo().addChild(ps);
//        ps.raiseToTop();
//        ps.setBounds(0, 0, 500, 500);
//        ps.getDocument().insertString(0, "abc", null);
//        ps.syncWithDocument();


        /*
        StringBuffer html = new StringBuffer();
        html.append("<p style='margin-bottom: 10px;'>");
        html.append("This is an example <a href='#testing'>of what can</a> be done with PHtml.");
        html.append("</p>");
        html.append("<p>It supports:</p>");

        final PHtmlView htmlNode = new PHtmlView(html.toString());
        htmlNode.setTextColor(Color.WHITE);
        htmlNode.setBounds(0, 0, 400, 400);
        getWorld().getGround().getPiccolo().addChild(htmlNode);
        */






//        final JSlider js = new JSlider(0, 100);
//        js.addChangeListener(new ChangeListener() {
//            public void stateChanged(final ChangeEvent e) {
//                System.out.println("e = " + e);
//            }
//        });
//        js.setBorder(BorderFactory.createTitledBorder("Test JSlider"));
//        final PSwing pSwing = new PSwing(js);
//        pSwing.translate(100, 100);
//        WorldObject o;
//        p.addChild(pSwing);
//        getWorld().addChild(p);
//        pSwing.raiseToTop();


        //getWorld().addChild(new TextEdit("te1", "THIS IS A SENTENCE"));


        new Timer(35, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    float dt = getSimulationDT(); //myStepSize
                    a.node().run(time, time + dt);
                    b.node().run(time, time + dt);
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