/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class Sum implements DiffableFunction<DiffableFunction> {

    private final DiffableFunction[] xs;

    public Sum(DiffableFunction... xs) {
        this.xs = xs;
    }

    @Override
    public double value() {
        double s = 0;
        for (final DiffableFunction x : xs) {
            s += x.value();
        }
        return s;
    }

    @Override
    public double partialDerive(final Scalar parameter) {
        double s = 0;
        for (final DiffableFunction x : xs) {
            s += x.partialDerive(parameter);
        }
        return s;
    }
    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
        return summation(sb, f, null);
    }
 
    
    
    public StringBuilder summation(StringBuilder sb, DiffableSymbols f, DiffableFunction partial) {
        List<String> addends = new ArrayList(xs.length);
        
        StringBuilder tb = new StringBuilder(); //temporary
        
        for (final DiffableFunction x : xs) {
            
            tb.setLength(0);
            
            String addend = 
                    partial!=null ? 
                    x.partialDeriveExpression(tb, f, partial).toString()
                    :
                    
                    x.valueExpression(tb, f).toString();
            
            
            if (addend.isEmpty() || addend.equals("0"))
                continue;
            
            addends.add(addend);            
        }
        
        if (addends.isEmpty()) return sb.append("0");
        
        sb.append('(');       

        Collections.sort(addends); //sort them to natural order to maximize common subterm folding
        
        int i = 0;        
        for (String s : addends) {
            sb.append(s);
            if (i!=addends.size()-1)
                sb.append('+');
            i++;
        }
        sb.append(')');
        return sb;
    }

    
    @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction parameter) {
        return summation(sb, f, parameter);
    }
    
    @Override
    public Object[] getDependencies() {        
        return xs;
    }
    
    @Override
    public void replace(int index, DiffableFunction r) {
        xs[index] = r;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(getDependencies());
                
    }    

    
        
}
