/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core;

import jurls.core.approximation.ParameterizedFunction;
import jurls.core.utils.Utils;
import nars.util.Texts;

/**
 *
 * @author thorsten
 */
public abstract class LearnerAndActor {

    private int numIterations = 0;
    private int iterationsPerSecond = 0;
    private int iterationsPerSecondCounter = 0;
    protected ParameterizedFunction parameterizedFunction = null;
    private long t0 = System.currentTimeMillis();
    private final StringBuilder stringBuilder = new StringBuilder();

    public ParameterizedFunction getFunction() { return parameterizedFunction; }

    public abstract int learnAndAction(double[] nextState, double nextReward, double[] previousState, int previousAction);

    protected void updateCounters() {
        numIterations++;
        iterationsPerSecondCounter++;

        long t1 = System.currentTimeMillis();
        if (t1 - t0 > 1000) {
            t0 = t1;
            iterationsPerSecond = iterationsPerSecondCounter;
            iterationsPerSecondCounter = 0;
        }
    }

    @SuppressWarnings("HardcodedFileSeparator")
    public String getDebugString(int indent) {
        String ind = Utils.makeIndent(indent);
        stringBuilder.setLength(0);
        stringBuilder.append(ind).append('@').append(numIterations);
        stringBuilder.append(" (").append(iterationsPerSecond).append("/s)\n");
        if (parameterizedFunction != null) {
            stringBuilder.append(ind).append(" QMin: ").append(Texts.n4(parameterizedFunction.minOutputDebug())).append('\n');
            stringBuilder.append(ind).append(" QMax: ").append(Texts.n4(parameterizedFunction.maxOutputDebug())).append('\n');
        }
        return stringBuilder.toString();
    }

    public abstract void stop();
}
