/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter.event;

import nars.util.meter.FunctionMeter;

import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author me
 */
public class HitMeter extends FunctionMeter<Long> {
    
    boolean autoReset;
    final AtomicLong hits = new AtomicLong();
    
    public HitMeter(String id, boolean autoReset) {
        super(id);
        this.autoReset = autoReset;
    }
    
    public HitMeter(String id) {
        this(id, true);
    }    
    
    public HitMeter reset() {
        hits.set(0);
        return this;
    }

    public long hit() {
        return hits.incrementAndGet();
    }
    public long hit(int n) {
        return hits.addAndGet(n);
    }
    
    public long count() {
        return hits.get();
    }
    
    @Override
    protected Long getValue(Object key, int index) {
        long c = hits.get();
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
