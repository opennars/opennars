/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

import objenome.op.math.MathNode;
import objenome.util.TypeUtil;
import org.apache.commons.math3.analysis.UnivariateFunction;

import java.util.function.Function;

/**
 * 1-input numeric function.
 * Takes all numeric inputs
 * Produces numeric output of the same type as input
 */
public abstract class Numeric1 extends MathNode implements UnivariateFunction, Function<Double,Number> {
    
    protected Numeric1() {
    }
    
    protected Numeric1(Node child) {
        super(child);
    }
    
    @Override
    public String getIdentifier() {
        return getClass().getSimpleName().toUpperCase();
    }
    
    @Override
    public abstract double value(double x);
    
    /** returns the one scalar input */
    public Node input() {
        return getChild(0);
    }
    
    /**
     * Evaluates this function. The child node is evaluated, the result of which
     * must be a numeric type (one of Double, Float, Long, Integer). The result
     * is raised to the power of 2 and returned as the same type as the input.
     *
     * @return the result of evaluating the child squared
     */
    @Override
    public double asDouble() {

        return value( input().asDouble() );
//
//        if (c instanceof Long) {
//            return (long) result;
//        } else if (c instanceof Float) {
//            return (float) result;
//        } else if (c instanceof Integer) {
//            return (int) result;
//        } else if (c instanceof Double) {
//            return result;
//        } else {
//            return Double.NaN;
//        }
    }
    
    /**
     * Returns this function node's return type for the given child input types.
     * If there is one input type of a numeric type then the return type will be
     * that numeric type. In all other cases this method will return
     * <code>null</code> to indicate that the inputs are invalid.
     *
     * @return a numeric class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        return (inputTypes.length == 1) && TypeUtil.isNumericType(inputTypes[0]) ? inputTypes[0] : null;
    }

    /** it's better to use value(x) directly and not this since it involves boxing */
    @Override public Double apply(Double x) {
        return value(x);
    }


    @Override
    public Node normalize() {

        double a = getChildConstantValue(0);
        if (Double.isFinite(a)) return new Doubliteral(value(a));

        return super.normalize();
    }
}
