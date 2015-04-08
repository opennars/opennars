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
public interface DiffableFunction {

    public double value(double[] xValues);

    public double partialDerive(double[] xValues, int parameterIndex);
}
