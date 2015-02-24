package ca.nengo.ui.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.BasicSource;
import ca.nengo.model.impl.DirectTarget;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.ui.Nengrow;
import ca.nengo.ui.lib.world.PaintContext;
import ca.nengo.ui.models.UIBuilder;
import ca.nengo.ui.models.UINeoNode;
import ca.nengo.ui.models.icons.EmptyIcon;
import ca.nengo.ui.models.nodes.UINetwork;
import ca.nengo.util.ScriptGenException;
import jurls.core.approximation.*;
import jurls.examples.approximation.RenderArrayFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;


public class TestFunctionPlot extends Nengrow {

    long time = 0;

    ParameterizedFunction parameterizedFunction;
    RenderArrayFunction input;

    public class Approximator extends AbstractNode  {

        private final BasicSource out;
        private final DirectTarget in;
        private final DiffableFunctionGenerator dfg;
        final int tmpWidth = 100;

        public Approximator(String name) {
            super(name);
            setSources(out = new BasicSource(this, "Approximation", 1, Units.UNK));
            setTargets(in = new DirectTarget(this, "Signal", 1));

            double[] ys = new double[10];
            for (int i = 0; i < ys.length; ++i) {
                ys[i] = Math.random();
            }
            input = new RenderArrayFunction(tmpWidth, Color.blue, ys);


            int numFeatures = 7;
            dfg = Generator.generateFourierBasis();

            parameterizedFunction = new OutputNormalizer(
                    new InputNormalizer(
                            new GradientFitter(
                                    new ApproxParameters(0.01, 0.1),
                                    new DiffableFunctionMarshaller(dfg, 1, numFeatures)
                            )
                    )
            );
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            final int numIterationsPerLoop = (int)((endTime - startTime)*1000f);
            for (int i = 0; i < numIterationsPerLoop; ++i) {
                double x = Math.random() * tmpWidth;
                parameterizedFunction.learn(new double[]{x}, input.compute(x));
            }
        }

        @Override
        public Node[] getChildren() {
            return new Node[0];
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }

        @Override
        public void reset(boolean randomize) {

        }

    }

    public class FunctionPlot extends AbstractNode implements UIBuilder {

        private final DirectTarget in;

        public FunctionPlot(String name) {
            super(name);

            setTargets(in = new DirectTarget(this, "Signal", 1));

        }

        public class FunctionPlotUI extends UINeoNode<FunctionPlot> {


            int numPoints = 32;
            private int[] xPoints;
            private int[] yPoints;

            public FunctionPlotUI() {
                super(FunctionPlot.this);

                setIcon(new EmptyIcon(this));
                setResizable(true);

                setBounds(0,0,250,70);
            }

            @Override
            public void paint(PaintContext paintContext) {
                super.paint(paintContext);

                paintFunction(paintContext, parameterizedFunction);
                //paintFunction(paintContext, input);

            }

            private void paintFunction(PaintContext paintContext, ParameterizedFunction parameterizedFunction) {
                if (parameterizedFunction == null) return;

                Graphics2D g = paintContext.getGraphics();

                int w = (int)(getBounds().getWidth());
                int h = (int)(getBounds().getHeight());

                g.setColor(Color.black);
                g.fillRect(0, 0, w, h);

                g.setColor(Color.GREEN);

                if (xPoints == null || xPoints.length != numPoints) {
                    xPoints = new int[numPoints];
                    yPoints = new int[numPoints];
                }

                int maxX = 100;
                for (int i = 0; i < numPoints; i++) {
                    double x = ((double)i/numPoints) * maxX; //tmp
                    double y = parameterizedFunction.value(x);
                    x = ((double)i/numPoints)* w;
                    y = (-y * h/2 + h/2);
                    xPoints[i] = (int)x;
                    yPoints[i] = (int)y;
                }

                g.drawPolyline(xPoints, yPoints, xPoints.length);

            }

            @Override
            public String getTypeName() {
                return "FunctionPlot";
            }
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {


        }

        @Override
        public Node[] getChildren() {
            return new Node[0];
        }

        @Override
        public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
            return null;
        }

        @Override
        public void reset(boolean randomize) {

        }

        @Override
        public UINeoNode newUI() {
            return new FunctionPlotUI();
        }
    }

        @Override
    public void init() throws Exception {
        NetworkImpl network = new NetworkImpl();
        network.addNode(new Approximator("Approximator"));
        network.addNode(new FunctionPlot("plot1"));


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
        new TestFunctionPlot();
    }
}
