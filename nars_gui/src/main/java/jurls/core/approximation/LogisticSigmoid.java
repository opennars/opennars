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
public class LogisticSigmoid implements DiffableFunction, Functional {

    private final DiffableFunction x;

    public LogisticSigmoid(DiffableFunction x) {
        this.x = x;
    }

    @Override
    public double value() {
        return 1 / (1 + Math.exp(-x.value()));
    }

    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
       sb.append("(tanh(");    x.valueExpression(sb, f);  sb.append("))");
       return sb;     
    }
    
    @Override
    public double partialDerive(Scalar parameter) {
        final double y = value();
        return x.partialDerive(parameter) * y * (1 - y);
    }

    @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction v) {
        return null;
    }

    @Override
    public Object[] getDependencies() {
        return new Object[] { x };
    }
    
}
