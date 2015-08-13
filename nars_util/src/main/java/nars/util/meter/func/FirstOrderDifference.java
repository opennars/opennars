/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter.func;

import com.gs.collections.impl.list.mutable.primitive.DoubleArrayList;
import nars.util.meter.Metrics;
import nars.util.meter.Signal;

import java.util.List;

/**
 * Computes the numeric first-order difference for the last 2 contiguous numeric
 * entries of a specific other column in a Metrics
 */
public class FirstOrderDifference extends DependsOnColumn {

    public FirstOrderDifference(Metrics metrics, String source) {
        super(metrics, source, 1);
        

    }

    @Override
    protected String getColumnID(Signal dependent, int i) {
        return dependent.id + ".change";
    }


    
    
    @Override
    public Number getValue(Object key, int ignored) {
        
        List nv = newestValues(sourceColumn, 2);

        DoubleArrayList values = Metrics.doubles(nv);
                
        if (values.size()<2) return null;
                
        double currentValue = values.get(0);
        double prevValue = values.get(1);
        
        return currentValue - prevValue;
    }

    
}
