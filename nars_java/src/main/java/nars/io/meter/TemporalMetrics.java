/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class TemporalMetrics<O extends Object> extends Metrics<Double,O> {

    public TemporalMetrics(int historySize) {
        super(historySize);
    }

    /** adds all meters which exist as fields of a given object (via reflection) */
    public void addMeters(Object obj) {
        Class c = obj.getClass();
        Class meter = Meter.class;
        for (Field f : c.getFields()) {
            
//System.out.println("field: " + f.getType() + " " + f.isAccessible() + " " + Meter.class.isAssignableFrom( f.getType() ));
            
            if ( meter.isAssignableFrom( f.getType() ) ) {
                try {
                    Meter m = (Meter)f.get(obj);
                    addMeter(m);
                } catch (Exception ex) {
                    System.err.println(ex);  
                } 
            }
        }
    }

    public List<SignalData> getSignalDatas() {
        List<SignalData> l = new ArrayList();
        
        for (Signal sv : getSignals()) {            
            l.add( newSignalData(sv.id) );
        }
        return l;
    }

    
}
