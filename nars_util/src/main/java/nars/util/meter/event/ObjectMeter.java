/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter.event;


/**
 * Stores the latest provided value of an object instance
 */
public class ObjectMeter<X> extends SourceFunctionMeter<X> {

    boolean autoReset;
    X val = null;
    private final String name;



    public ObjectMeter(String id, boolean autoReset) {
        super(id);
        name = id;
        this.autoReset = autoReset;
    }


    public ObjectMeter(String id) {
        this(id, false);
    }    


    public ObjectMeter reset() {
        set(null);
        return this;
    }
    
    /** returns the previous value, or NaN if none were set  */
    public X set(X newValue) {
        X oldValue = val;
        val = newValue;
        return oldValue;
    }

    /** current stored value */
    public X get() { return val; }

    
    @Override
    public X getValue(Object key, int index) {
        X c = val;
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

    @Override
    public String toString() {
        return name + super.toString();
    }
}
