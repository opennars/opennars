/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.reinforcementlearning;

import jurls.core.approximation.ApproxParameters;
import jurls.core.approximation.ParameterizedFunction;
import jurls.core.utils.Utils;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author thorsten
 */
public class SARSAUpdateProcedure implements UpdateProcedure {
    private ArrayRealVector gr;

    @Override
    public void update(
            ApproxParameters approxParameters,
            RLParameters rLParameters,
            Context context,
            double reward,
            double[][] s,
            int[] a,
            ParameterizedFunction f,
           int num
    ) {
        double qtm1 = Utils.q(f, s[0], a[0]);
        double qt = Utils.q(f, s[1], a[1]);
        gr = f.parameterGradient(gr, Utils.join(s[1], a[1]));

        ArrayRealVector deltas = (ArrayRealVector) context.e.mapMultiply(
                reward + rLParameters.getGamma() * qt - qtm1
        );

        double l = deltas.getNorm();
        if (l < Utils.zeroEpsilon) {
            l = Utils.zeroEpsilon;
        }

        //ArrayRealVector.combine: Returns a new vector representing a * this + b * y, the linear combination of this and y.
        deltas = deltas.combine(
                approxParameters.getAlpha() / l,
                approxParameters.getMomentum(),
                context.previousDeltas
        );

        f.addToParameters(deltas);

        context.e = (ArrayRealVector) context.e.mapMultiply(
                rLParameters.getGamma() * rLParameters.getLambda()
        ).add(gr);

         context.previousDeltas = deltas;
    }
}
