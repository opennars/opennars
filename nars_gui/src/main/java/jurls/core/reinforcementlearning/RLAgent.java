/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import java.util.Arrays;
import java.util.Random;
import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.utils.ActionValuePair;
import jurls.core.utils.Utils;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public class RLAgent {

    private final ParameterizedFunction parameterizedFunction;
    private final UpdateProcedure updateProcedure;
    private final double[][] s = new double[2][];
    private final int[] a = new int[2];
    private final int numActions;
    private final Random random = new Random();
    private final RLParameters rLParameters;
    private final ApproxParameters approxParameters;
    private final UpdateProcedure.Context context = new UpdateProcedure.Context();
    private final ActionSelector actionSelector;
    public int numIterations = 0;

    public RLAgent(
            ParameterizedFunction parameterizedFunction,
            UpdateProcedure updateProcedure,
            ActionSelector actionSelector,
            int numActions,
            double[] s0,
            ApproxParameters approxParameters,
            RLParameters rLParameters
    ) {
        this.parameterizedFunction = parameterizedFunction;
        this.updateProcedure = updateProcedure;
        this.actionSelector = actionSelector;
        this.numActions = numActions;
        context.previousDeltas = new ArrayRealVector(parameterizedFunction.numberOfParameters());
        context.e = new ArrayRealVector(parameterizedFunction.numberOfParameters());
        this.approxParameters = approxParameters;
        this.rLParameters = rLParameters;
        push(s0);
    }

    private void push(double[] _s) {
        s[0] = s[1];
        s[1] = _s;
    }

    private void push(int _a) {
        a[0] = a[1];
        a[1] = _a;
    }

    public void learn(double[] state, double reward) {
        push(state);
        push(chooseAction(state));
        updateProcedure.update(approxParameters, rLParameters, context, reward, s, a, parameterizedFunction, numActions);
        numIterations++;
    }

    public int chooseAction() {
        return a[1];
    }

    public ActionValuePair[] getActionProbabilities(double[] state) {
        ActionValuePair[] actionValuePairs = new ActionValuePair[numActions];

        for (int i = 0; i < numActions; ++i) {
            actionValuePairs[i] = new ActionValuePair(
                    i, 
                    Utils.q(parameterizedFunction, state, i)
            );
         }

        return actionSelector.fromQValuesToProbabilities(actionValuePairs);
    }

    public int chooseAction(double[] state) {
        ActionValuePair[] actionProbabilityPairs = getActionProbabilities(state);
        Arrays.sort(actionProbabilityPairs, (ActionValuePair o1, ActionValuePair o2) -> (int) Math.signum(o1.getV() - o2.getV()));

        double x = Math.random();
        int i = -1;

        while (x > 0) {
            ++i;
            x -= actionProbabilityPairs[i].getV();
        }

        return actionProbabilityPairs[i].getA();
    }
}
