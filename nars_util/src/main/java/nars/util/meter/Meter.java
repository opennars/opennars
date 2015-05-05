/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter;

import java.util.List;

/** produces 1 or more signals */
public interface Meter<M> {

    
    public default void setActive(boolean active) {
    }

    /** the list of signals produced by this meter; this should not change
     * in quantity during operation
     */
    public List<Signal> getSignals();
    
    default Signal signal(int i) {
        return getSignals().get(i);
    }
    default String signalID(int i) {
        return getSignals().get(i).id;
    }
    
    /** convenience method for accessing the first of the signals, in case one needs the only signal */
    default Signal signalFirst() {
        return getSignals().get(0);
    }
    

    /**
     * @param key the current row's leading element, usually time
     * @return the values described by the signals, 
     * or null (no data). if any of the elements are null, that column's data 
     * point is not recorded (ie. NaN).
     */
    public M[] sample(Object key);
    
    default public int numSignals() { return getSignals().size(); }
    
    
}
