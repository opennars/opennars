/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.utils;

import jurls.core.approximation.ParameterizedFunction;

/**
 *
 * @author thorsten
 */
public class Utils {

    public static final double zeroEpsilon = 0.000001;

    public static double[] join(double[] state, int action, double[] result) {
        if ((result == null) || (result.length!=state.length+1)) {
            result = new double[state.length + 1];
        }
        System.arraycopy(state, 0, result, 0, state.length);
        result[result.length - 1] = action;
        return result;
    }
    
    @Deprecated public static double[] join(double[] state, int action) {
        return join(state, action, null);
    }

    public static double q(ParameterizedFunction f, double[] state, int action) {
        return f.value(join(state, action));
    }

    public static ActionValuePair v(ParameterizedFunction f, double[] state, int num) {
        double max = Double.NEGATIVE_INFINITY;
        int maxa = 0;

        for (int i = 0; i < num; ++i) {
            double _q = q(f, state, i);
            if (_q > max) {
                max = _q;
                maxa = i;
            }
        }

        return new ActionValuePair(maxa, max);
    }

    public static double lengthSquare(double[] v) {
        double s = 0;
        for (double x : v) {
            s += x * x;
        }
        return s;
    }

    public static double length(double[] v) {
        return Math.sqrt(lengthSquare(v));
    }

    public static int checkAction(int numActions, double[] behaviourLearnerOutput) {
        assert behaviourLearnerOutput.length == 1;

        int action = (int) Math.round(behaviourLearnerOutput[0]);
        if (action >= numActions) {
            action = numActions - 1;
        }
        if (action < 0) {
            action = 0;
        }

        return action;
    }
}
