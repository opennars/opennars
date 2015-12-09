/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter;

import java.util.Iterator;

/**
 * A view of a signal (column) of a Metrics; a Signal and access to its data
 */
public class SignalData {
    public final Signal signal;
    private final Metrics metric;
    final int index;
    private Object[] data;

    public SignalData(Metrics m, Signal s) {
        metric = m;
        signal = s;
        index = metric.getSignals().indexOf(s);
    }

    public Signal getSignal() {
        return signal;
    }

    public Object[] getDataCached() {
        if (data == null) {
            data = getData();
        }
        return data;
    }

    public Object[] getData() {
        return data = metric.getData(index, data);
    }

    public String getID() {
        return signal.id;
    }

    /** iterates any other signal in the metric's data, by its column ID's */
    public Iterator<Object[]> iterateOtherSignals(int... columns) {
        return metric.iterator(columns);
    }

    /** iterates this signal's data */
    public Iterator<Object[]> iterator() {
        return metric.iterator(getIndex());
    }

    /** iterates this signal's data in 1st index, along with one other column, in the 0th index */
    public Iterator<Object[]> iteratorWith(int columns) {
        return metric.iterator(columns, getIndex());
    }

    /** the index of this signal within the metrics */
    public int getIndex() {
        return index;
    }

    public double[] getBounds() {
        return new double[]{getMin(), getMax()};
    }

    public double getMin() {
        return getSignal().getMin();
    }

    public double getMax() {
        return getSignal().getMax();
    }

    public Metrics getMetric() {
        return metric;
    }
    
}
