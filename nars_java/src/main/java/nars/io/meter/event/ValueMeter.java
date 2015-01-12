/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter.event;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Stores the latest provided value for retrieval by a Metrics 
 */
public class ValueMeter extends SourceFunctionMeter<Double> {
    
    boolean autoReset;
    final AtomicDouble val = new AtomicDouble();
    private final String name;
    
    
    
    public ValueMeter(String id, boolean autoReset) {
        super(id);
        this.name = id;
        this.autoReset = autoReset;
    }
    
    
    public ValueMeter(String id) {
        this(id, false);
    }    


    public ValueMeter reset() {
        set(Double.NaN);
        return this;
    }
    
    /** returns the previous value, or NaN if none were set  */
    public double set(double newValue) {
        double oldValue = val.get();
        val.set(newValue);
        return oldValue;
    }

    
    @Override
    protected Double getValue(Object key, int index) {
        double c = val.get();
        if (autoReset) {
            reset();
        }
        return c;        
    }

    /** whether to reset to NaN after the count is next stored in the Metrics */
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
    }
    
    public boolean getAutoReset() { return autoReset; }


}
