/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

import objenome.util.TypeUtil;

/**
 * Returns Double, specifically
 */
abstract public class Numeric1d<X extends Node,Y extends Number> extends Numeric1<X> {

    public Numeric1d() {
        super();
    }

    
    public Numeric1d(X child) {
        super(child);
    }
        
 
    /**
     * Returns this function node's return type for the given child input types.
     * If there is one input type of a numeric type then the return type will be
     * Double. In all other cases this method will return <code>null</code> to
     * indicate that the inputs are invalid.
     *
     * @return the Double class or null if the input type is invalid.
     */
    @Override
    public Class dataType(Class... inputTypes) {
        if ((inputTypes.length == 1) && TypeUtil.isNumericType(inputTypes[0])) {
            return Double.class;
        } else {
            return null;
        }
    }
    
    public double value() {
        return (double) evaluate();
    }

    @Override
    public Node normalize() {

        double a = getChildConstantValue(0);
        if (Double.isFinite(a)) return new Literal(value(a));

        return this;
    }

}
