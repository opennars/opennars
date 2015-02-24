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
public class QUpdateProcedure implements UpdateProcedure {
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
        double vtm1 = Utils.v(f, s[0], num).getV();
        double vt = Utils.v(f, s[1], num).getV();
        gr = f.parameterGradient(gr, Utils.join(s[0], a[0]));

        //ArrayRealVector.combine: Returns a new vector representing a * this + b * y, the linear combination of this and y.
        ArrayRealVector deltas = gr.combine(
                reward + rLParameters.getGamma() * vt - qtm1,
                reward + rLParameters.getGamma() * vt - vtm1,
                context.e
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

        context.e = (ArrayRealVector) gr.subtract(context.e).mapMultiply(
                rLParameters.getGamma() * rLParameters.getLambda()
        );

        context.previousDeltas = deltas;
    }
}
