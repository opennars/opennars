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
public class ATanhSigmoid implements DiffableFunction, Functional {

    private final DiffableFunction x;

    public ATanhSigmoid(DiffableFunction x) {
        this.x = x;
    }

    @Override
    public double value() {
        return Math.atan(x.value());
    }
    
    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
       sb.append("(atan(");    x.valueExpression(sb, f);  sb.append("))");
       return sb;     
    }

    @Override
    public double partialDerive(Scalar parameter) {
        double y = x.value();        
        return x.partialDerive(parameter) * 1.0 / (1 + y * y);
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
