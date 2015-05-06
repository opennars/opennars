/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op.math;

import objenome.op.Scalar;
import objenome.op.DiffableFunction;
import objenome.op.ScalarFunction;

/**
 *
 * @author thorsten
 */
public class ExpDiff extends Exp<ScalarFunction> implements DiffableFunction{

    private final DiffableFunction x;

    public ExpDiff(ScalarFunction x) {
        super(x);
        this.x = x;
    }
    
    @Override
    public double value() {
        return Math.exp(x.value());
    }

    @Override
    public double partialDerive(Scalar parameter) {
        return x.partialDerive(parameter) * Math.exp(x.value());
    }
    
}
