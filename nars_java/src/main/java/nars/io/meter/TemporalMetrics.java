/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

/**
 *
 * @author me
 */
public class TemporalMetrics<Cell> extends Metrics<Double,Cell> {

    public TemporalMetrics(int historySize) {
        super(historySize);
    }

    public Object getSignalData(int i, int i0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
