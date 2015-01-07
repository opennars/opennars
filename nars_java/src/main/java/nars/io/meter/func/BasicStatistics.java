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
public class BasicStatistics extends DependsOnColumn<Number,Double>  {
    
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
            case 0: return dependent.id + ".mean";
            case 1: return dependent.id + ".max";
        }
        return null;
    }

    @Override
    protected Double getValue(Object key, int index) {
        if (index == 0) {
            Number nextValue = newestValue();            
            stat.addValue( (nextValue).doubleValue() );            
        }
        switch (index) {
            case 0: return stat.getMean();
            case 1: return stat.getMax();
        }
        
        return null;
    }
    
}
