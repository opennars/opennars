/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten
 *
 * Parameter search with PID controllers. It was worth a try :)
 */
public class PIDFitter implements ParameterizedFunction {

    private static class PID {

        private double integral = 0;
        private double prevE = 0;
        private double prevDelta = 0;

        public double compute(double error) {
            double deltaE = error - prevE;
            prevE = error;
            double deltaDelta = deltaE - prevDelta;
            prevDelta = deltaE;
            integral += error;

            return 0.01 * integral + 1 * error + 2 * deltaE + 4 * deltaDelta;
        }
    }

    private final ParameterizedFunction parameterizedFunction;
    private final PID[] pids;
    private final double[] gradient;

    public PIDFitter(ParameterizedFunction parameterizedFunction) {
        this.parameterizedFunction = parameterizedFunction;
        pids = new PID[parameterizedFunction.numberOfParameters()];
        for (int i = 0; i < pids.length; ++i) {
            pids[i] = new PID();
        }
        gradient = new double[parameterizedFunction.numberOfParameters()];
    }

    @Override
    public void learn(double[] xs, double y) {
        double q = parameterizedFunction.value(xs);
        double e = y - q;
        parameterGradient(gradient, xs);

        for (int i = 0; i < pids.length; ++i) {
            gradient[i] = 0.00001 * pids[i].compute(Math.signum(gradient[i]) * e);
        }

        /*
         double l = Utils.length(gradient.getDataRef());
         if (l == 0) {
         l = 1;
         }
         gradient.mapMultiplyToSelf(0.01 / l);
         */
        parameterizedFunction.addToParameters(gradient);
    }

    @Override
    public double value(double[] xs) {
        return parameterizedFunction.value(xs);
    }

    @Override
    public int numberOfParameters() {
        return parameterizedFunction.numberOfParameters();
    }

    @Override
    public int numberOfInputs() {
        return parameterizedFunction.numberOfInputs();
    }

    @Override
    public void parameterGradient(double[] output, double[] xs) {
        parameterizedFunction.parameterGradient(output, xs);
    }

    @Override
    public void addToParameters(double[] deltas) {
        parameterizedFunction.addToParameters(deltas);
    }

    @Override
    public double minOutputDebug() {
        return parameterizedFunction.minOutputDebug();
    }

    @Override
    public double maxOutputDebug() {
        return parameterizedFunction.maxOutputDebug();
    }
}
