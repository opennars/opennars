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
public class Product implements DiffableFunction<DiffableFunction> {

    private DiffableFunction a;
    private DiffableFunction b;


    public Product(DiffableFunction a, DiffableFunction b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public double value() {
        return a.value() * b.value();
    }

    @Override
    public StringBuilder valueExpression(StringBuilder sb, DiffableSymbols f) {
        sb.append('(');
        a.valueExpression(sb, f);
        sb.append('*');
        b.valueExpression(sb, f);
        sb.append(')');
        return sb;
    }
    
    

    @Override
    public double partialDerive(final Scalar parameter) {
        
        //caches member fields as local variables, a pedantic peephole optimization that cant hurt
        final DiffableFunction A = this.a;
        final DiffableFunction B = this.b;
        
        return A.partialDerive(parameter) * B.value() + 
                A.value() * B.partialDerive(parameter);
    }

    @Override
    public StringBuilder partialDeriveExpression(StringBuilder sb, DiffableSymbols f, DiffableFunction parameter) {

        String factorB = b.valueExpression(new StringBuilder(), f).toString();
        boolean bIsZero = factorB.equals("0");
        
        String factordA = null;
        if (!bIsZero)
            factordA = a.partialDeriveExpression(new StringBuilder(), f, parameter).toString();
        
        boolean daIsZero = factordA==null || factordA.equals("0");
        
        String factorA = a.valueExpression(new StringBuilder(), f).toString();
        boolean aIsZero = factorA.equals("0");
        
        String factordB = null;
        if (!aIsZero)
            factordB = b.partialDeriveExpression(new StringBuilder(), f, parameter).toString();
        
        boolean dbIsZero = factordB==null || factordB.equals("0");
        
        if ((daIsZero && dbIsZero) || (aIsZero && bIsZero)) return sb.append("0");
        if (!daIsZero && !dbIsZero && !aIsZero && !bIsZero) {            
            sb.append('(');            
                exprPair( sb, exprMult(sb, factordA, factorB).toString(), exprMult(sb, factordB, factorA).toString(), '+'  );
            sb.append(')');            
        }
        else if (!daIsZero && !bIsZero) {
            sb.append('(');        
                exprMult(sb, factordA, factorB);
            sb.append(')');
        }
        else if (!dbIsZero && !aIsZero) {
            sb.append('(');        
                exprMult(sb, factordB, factorA);
            sb.append(')');
        }
        else {
            sb.append('0');
        }
            
            
        return sb;
    }

    public static StringBuilder exprPair(StringBuilder sb, String a, String b, char operand) {
        if (a.compareTo(b) > 0) {
            sb.append(a).append('*').append(b);
        }
        else {
            sb.append(b).append('*').append(a);
        }    
        return sb;
    }
    
    /** assembles a product fragment of an expression, sorting the subterms by their string's natural ordering */
    public static StringBuilder exprMult(StringBuilder sb, String a, String b) {
        return exprPair(sb, a, b, '*');
    }
    
    @Override
    public Object[] getDependencies() {        
        return new Object[] { a, b };        
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(getDependencies());
                
    }

    public DiffableFunction getA() {
        return a;
    }

//    public void setA(DiffableFunction a) {
//        this.a = a;
//    }

    public DiffableFunction getB() {
        return b;
    }

//    public void setB(DiffableFunction b) {
//        this.b = b;
//    }
    
    
    @Override
    public void replace(int index, DiffableFunction r) {
        if (index == 0)
            a = r;
        else if (index == 1)
            b = r;
        else
            throw new RuntimeException("Invalid index " + index + " for replacing in " + this);
    }    
    
    
        
}
