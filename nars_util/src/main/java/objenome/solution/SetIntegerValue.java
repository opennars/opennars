/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.solution;

import objenome.goal.Between;
import objenome.goal.DecideNumericValue.DecideIntegerValue;

/**
 *
 * @author me
 */
public class SetIntegerValue extends SetConstantValue<Integer> implements SetNumericValue {
    
    public SetIntegerValue(DecideIntegerValue d, @Between(min=0, max=1) double normalizedValue) {
        super(d);
        
        setNormalizedValue(normalizedValue);
    }
        
    @Override
    public Integer getValue() {
        return intValue();
    }

    @Override
    public Integer getMin() {
        return ((DecideIntegerValue)problem).min;
    }

    @Override
    public Integer getMax() {
        return ((DecideIntegerValue)problem).max;
    }
    
    public void setNormalizedValue(@Between(min=0, max=1) double v) {
        setValue(v * (getMax() - getMin()) + getMin());
    }
    
    @Override
    public void setValue(double d) {
        set(d);
    }

    
    @Override
    public Number getNumber() {
        return getValue();
    }
    
    @Override
    public void mutate() {
        setValue( Math.random() * (getMax() - getMin()) + getMin() );
    }
    
}
