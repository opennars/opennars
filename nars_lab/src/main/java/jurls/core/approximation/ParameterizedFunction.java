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

    double value(double[] xs);

    void parameterGradient(double[] output, double... xs);

    void addToParameters(double[] deltas);

    void learn(double[] xs, double y);

    int numberOfParameters();

    int numberOfInputs();

    double minOutputDebug();

    double maxOutputDebug();

    default double getParameter(int i) {
        return 0;
    }
}
