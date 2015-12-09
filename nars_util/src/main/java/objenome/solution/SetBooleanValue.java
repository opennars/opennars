/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import objenome.goal.DecideNumericValue.DecideBooleanValue;

/**
 * Boolean backed by a double, 0..0.5 = false, 0.5..1.0 = true
 */
public class SetBooleanValue extends SetConstantValue<Boolean> implements SetNumericValue {
    
    public SetBooleanValue(DecideBooleanValue d, boolean b) {
        super(d);        
        setValue(b ? 1.0 : 0.0);
    }
    
    @Override
    public Boolean getValue() {
        return doubleValue() > 0.5;
    }

    @Override
    public Double getMin() {
        return 0.0d;
    }

    @Override
    public Double getMax() {
        return 1.0d;
    }

    @Override
    public Number getNumber() {
        return getValue() ? 1.0 : 0.0;
    }

    @Override
    public void setValue(double d) {
        set(d);
    }

    @Override
    public void mutate() {
        setValue( Math.random() * (getMax() - getMin()) + getMin() );
    }
    
}
