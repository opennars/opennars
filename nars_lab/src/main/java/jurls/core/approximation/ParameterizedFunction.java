/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten
 */
public interface ParameterizedFunction {

    public double value(double[] xs);

    public void parameterGradient(double[] output, double... xs);

    public void addToParameters(double[] deltas);

    public void learn(double[] xs, double y);

    public int numberOfParameters();

    public int numberOfInputs();

    public double minOutputDebug();

    public double maxOutputDebug();

    default double getParameter(int i) {
        return 0;
    }
}
