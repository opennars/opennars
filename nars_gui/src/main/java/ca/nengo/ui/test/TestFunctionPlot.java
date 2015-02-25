package ca.nengo.ui.test;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.ObjectSource;
import ca.nengo.model.impl.ObjectTarget;
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


    public class Approximator extends AbstractNode  {

        private final ObjectSource<ParameterizedFunction[]> out;
        private final ObjectTarget<UnaryDoubleFunction> in;
        private final DiffableFunctionGenerator dfg;

        ParameterizedFunction approximation;
        RenderArrayFunction input;

        public Approximator(String name) {
            super(name);
            out = new ObjectSource(this, "Approximation");
            setOutputs(out);
            setInputs(in = new ObjectTarget(this, "Signal"));

            double[] ys = new double[10];
            for (int i = 0; i < ys.length; ++i) {
                ys[i] = (Math.random()-0.5f)*2d;
            }
            input = new RenderArrayFunction(1.0, Color.blue, ys);



            int numFeatures = 7;
            dfg = Generator.generateFourierBasis();

            approximation = new OutputNormalizer(
                    new InputNormalizer(
                            new GradientFitter(
                                    new ApproxParameters(0.01, 0.1),
                                    new DiffableFunctionMarshaller(dfg, 1, numFeatures)
                            )
                    )
            );

            out.accept(new ParameterizedFunction[] { input, approximation});
        }

        @Override
        public void run(float startTime, float endTime) throws SimulationException {

            final int numIterationsPerLoop = (int)((endTime - startTime)*1000f);
            for (int i = 0; i < numIterationsPerLoop; ++i) {
                double x = Math.random() * 1.0;
                approximation.learn(new double[]{x}, input.compute(x));
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

        private final ObjectTarget<ParameterizedFunction[]> in;

        public FunctionPlot(String name) {
            super(name);

            setInputs(in = new ObjectTarget(this, "Signal"));


        }

        public class FunctionPlotUI extends UINeoNode<FunctionPlot> {


            int numPoints = 32;
            private int[][] xPoints;
            private int[][] yPoints;

            public FunctionPlotUI() {
                super(FunctionPlot.this);

                xPoints = new int[2][];
                yPoints = new int[2][];

                setIcon(new EmptyIcon(this));
                setResizable(true);
                setSelected(true);

                setBounds(0,0,250,150);
            }

            @Override
            public void paint(PaintContext paintContext) {
                super.paint(paintContext);


                ParameterizedFunction[] funcs = in.get();
                if (funcs!=null) {

                    Graphics2D g = paintContext.getGraphics();

                    int w = (int)(getBounds().getWidth());
                    int h = (int)(getBounds().getHeight());

                    g.setColor(Color.black);
                    g.fillRect(0, 0, w, h);

                    for (int i = 0; i < funcs.length; i++)
                        paintFunction(g, i, funcs[i]);
                }

            }

            private void paintFunction(Graphics2D g, int f,  ParameterizedFunction parameterizedFunction) {
                if (parameterizedFunction == null) return;


                int w = (int)(getBounds().getWidth());
                int h = (int)(getBounds().getHeight());


                if (f == 0) {
                    g.setColor(Color.GREEN);
                }
                else {
                    g.setColor(Color.YELLOW);
                }

                if (xPoints[f] == null || xPoints[f].length != numPoints) {
                    xPoints[f] = new int[numPoints];
                    yPoints[f] = new int[numPoints];
                }

                for (int i = 0; i < numPoints; i++) {
                    double x = ((double)i/numPoints) * 1.0;
                    double y = parameterizedFunction.value(x);
                    x = ((double)i/numPoints)* w;
                    y = (-y * h/2 + h/2);
                    xPoints[f][i] = (int)x;
                    yPoints[f][i] = (int)y;
                }

                g.drawPolyline(xPoints[f], yPoints[f], xPoints[f].length);

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
