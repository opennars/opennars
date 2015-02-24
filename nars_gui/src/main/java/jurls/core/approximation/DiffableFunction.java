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
public interface DiffableFunction<F extends Functional> extends ScalarFunction, Functional<F> {
    public double partialDerive(Scalar parameter);

    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction v);

    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f);
}
