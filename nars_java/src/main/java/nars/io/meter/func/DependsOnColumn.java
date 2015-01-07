/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter.func;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nars.io.meter.Metrics;
import nars.io.meter.Signal;
import nars.io.meter.SimpleMeter;

/**
 * @param Source Return type
 */
abstract public class DependsOnColumn<Source extends Object,Result extends Object> extends SimpleMeter<Result> {

    protected final int sourceColumn;
    protected final Metrics metrics;


    public DependsOnColumn(Metrics metrics, int sourceColumn, int numResults) {
        super("",numResults);
        
        int i = 0;
        for (Signal s : getSignals())
            s.id = getColumnID(s, i++);
        
        this.metrics = metrics;
        this.sourceColumn = sourceColumn;
    }

//    
    /*public Iterator<Object> signalIterator() {
        return metrics.getSignalIterator(sourceColumn, true);        
    }*/
    
    public Object newestValue() { 
        Iterator<Object[]> r = metrics.reverseIterator();
        if (r.hasNext())
            return r.next()[sourceColumn];
        return null;
    }
    //public List<Object> newestValues(int n) { 

    protected List<Object> newestValues(int column, int i) {
        return metrics.getNewSignalValues(column, i);
    }
    
    abstract protected String getColumnID(Signal dependent, int i);
    
}
