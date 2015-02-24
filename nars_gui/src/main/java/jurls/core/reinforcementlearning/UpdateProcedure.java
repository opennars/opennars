/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.ParameterizedFunction;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public interface UpdateProcedure {

    public static class Context {

        public ArrayRealVector e;
        public ArrayRealVector previousDeltas;
    }

    public void update(
            ApproxParameters approxParameters,
            RLParameters rLParameters,
            Context context,
            double reward,
            double[][] s,
            int[] a,
            ParameterizedFunction f,
            int numActions);
}
