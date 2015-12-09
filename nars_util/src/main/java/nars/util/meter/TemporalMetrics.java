/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author me
 */
public class TemporalMetrics<O> extends Metrics<Double,O> {

    public TemporalMetrics(int historySize) {
        super(historySize);
    }


    public List<SignalData> getSignalDatas() {
        List<SignalData> l = new ArrayList();
        
        for (Signal sv : getSignals()) {            
            l.add( newSignalData(sv.id) );
        }
        return l;
    }

    /** allows updating with an integer/long time, because it will be converted
     * to double internally
     */
    public void update(long integerTime) {
        update((double)integerTime);
    }    
    
}
