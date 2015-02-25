package ca.nengo.ui.models.math;

import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.AbstractNode;
import ca.nengo.model.impl.ObjectSource;
import ca.nengo.model.impl.ObjectTarget;
import ca.nengo.util.ScriptGenException;
import jurls.core.approximation.*;
import jurls.examples.approximation.RenderArrayFunction;

import java.awt.*;
import java.util.HashMap;

/**
* Created by me on 2/24/15.
*/
public class JuRLsFunctionApproximator extends AbstractNode {

    private final ObjectSource<ParameterizedFunction[]> out;
    private final ObjectTarget<UnaryDoubleFunction> in;
    private final DiffableFunctionGenerator dfg;
    private final ApproxParameters approx;

    ParameterizedFunction approximation;
    RenderArrayFunction input;

    public JuRLsFunctionApproximator(String name) {
        super(name);
        out = new ObjectSource(this, "Approximation");
        setOutputs(out);
        setInputs(in = new ObjectTarget(this, "Signal", ParameterizedFunction[].class));

        int pieceWiseFeatures = 16;
        double[] ys = new double[pieceWiseFeatures];
        for (int i = 0; i < ys.length; ++i) {
            ys[i] = (Math.random()-0.5f)*2d;
        }
        input = new RenderArrayFunction(1.0, Color.blue, ys);



        int numFeatures = 15;
        dfg = Generator.generateFourierBasis();

        approximation = new OutputNormalizer(
                new InputNormalizer(
                        new GradientFitter(
                                approx = new ApproxParameters(0.005, 0.1),
                                new DiffableFunctionMarshaller(dfg, 1, numFeatures)
                        )
                )
        );

        out.accept(new ParameterizedFunction[] { input, approximation});
    }

    @Override
    public void run(float startTime, float endTime) throws SimulationException {



        input.mutate((endTime - startTime)*10f, 0.0002f);

        approx.setAlpha( Math.min(0.2f, (endTime - startTime)) );
        final int numIterationsPerLoop = 1;
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
