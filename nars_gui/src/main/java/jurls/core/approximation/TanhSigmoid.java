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
public class TanhSigmoid implements DiffableFunction, Functional {

    private final DiffableFunction x;

    public TanhSigmoid(DiffableFunction x) {
        this.x = x;
    }

    @Override
    public double value() {
        return Math.tanh(x.value());
    }

    @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
       sb.append("(tanh(");    x.valueExpression(sb, f);  sb.append("))");
       return sb;     
    }
        
    @Override
    public double partialDerive(Scalar parameter) {
        double y = value();
        return x.partialDerive(parameter) * (1 - y) * (1 + y);
    }

    @Override
    public Object[] getDependencies() {
        return new Object[] { x };
    }


}
