/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.Arrays;

/**
 *
 * @author thorsten
 */
public class Sine implements DiffableFunction, Functional {

    private final DiffableFunction x;

    public Sine(DiffableFunction x) {
        this.x = x;
    }

    @Override
    public double value() {
        return Math.sin(x.value());
    }

    @Override
    public double partialDerive(Scalar parameter) {
        return x.partialDerive(parameter) * Math.cos(x.value());
    }



    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
        sb.append("(sin(");    x.valueExpression(sb, f);  sb.append("))");
        return sb;
    }

        @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction parameter) {
        String factor = x.partialDeriveExpression(new StringBuilder(), f, parameter).toString();
        if (factor.equals("0")) return sb.append("0");
        
        sb.append('(');
        sb.append(factor);
        sb.append('*');
        sb.append("(cos(");    x.valueExpression(sb, f);  sb.append("))");
        sb.append(')');                
        return sb;
    }

    
    @Override
    public Object[] getDependencies() {
        return new Object[] { x };
    }
   
    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(getDependencies());
                
    }        

}
