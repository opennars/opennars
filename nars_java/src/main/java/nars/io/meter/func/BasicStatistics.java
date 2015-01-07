/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter.func;

import nars.io.meter.Metrics;
import nars.io.meter.Signal;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * Uses apache commons math 
 */
public class BasicStatistics extends DependsOnColumn  {
    
    private final SummaryStatistics stat;

    public BasicStatistics(Metrics metrics, int sourceColumn) {
        super(metrics, sourceColumn, 2);
        stat = new SummaryStatistics();
    }
    
//    public void setWindowSize(int w) {
//        stat.
//    }

    
    @Override
    protected String getColumnID(Signal dependent, int i) {
        switch (i) {
            case 0: return dependent.id + "_mean";
            case 1: return dependent.id + "_max";
        }
        return null;
    }

    @Override
    protected Object getValue(Object key, int index) {
        if (index == 0) {
            Object nextValue = newestValue();
            if (nextValue instanceof Number) {
                stat.addValue( ((Number)nextValue).doubleValue() );
            }
        }
        switch (index) {
            case 0: return stat.getMean();
            case 1: return stat.getMax();
        }
        
        return null;
    }
    
}
