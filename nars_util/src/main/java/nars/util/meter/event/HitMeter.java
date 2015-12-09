/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import nars.util.meter.FunctionMeter;
import org.apache.commons.lang3.mutable.MutableLong;

import java.io.Serializable;

/**
 *
 * @author me
 */
@JsonSerialize(using = ToStringSerializer.class)
public class HitMeter extends FunctionMeter<Long> implements Serializable {
    
    private boolean autoReset;
    public final MutableLong hits = new MutableLong();
    
    public HitMeter(String id, boolean autoReset) {
        super(id);
        this.autoReset = autoReset;
    }

    @Override
    public String toString() {
        return signalID(0) + '=' + hits.getValue();
    }

    public HitMeter(String id) {
        this(id, true);
    }    
    
    public HitMeter reset() {
        hits.setValue(0);
        return this;
    }

    public long hit() {
        hits.add(1);
        return hits.getValue();
    }

    public long hit(int n) {
        hits.add(n); return hits.getValue();
    }
    
    public long count() {
        return hits.getValue();
    }
    
    @Override
    public Long getValue(Object key, int index) {
        long c = count();
        if (autoReset) {
            reset();
        }
        return c;        
    }

    /** whether to reset the hit count after the count is stored in the Metrics */
    public void setAutoReset(boolean autoReset) {
        this.autoReset = autoReset;
    }
    
    public boolean getAutoReset() { return autoReset; }
    
    
    
}
