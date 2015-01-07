/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

/**
 * Effectively a column header in a Metrics table; indicates what appears
 * in the column index of rows
 */
public class Signal implements Comparable<Signal> {
    public String id;
    public String unit;
    
    private double min, max;

    public Signal(String id) {
        this.id = id;
        this.unit = null;
    }

    public Signal(String id, String unit) {
        this.id = id;
        this.unit = unit;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return id.equals(((Signal)obj).id);
    }
    
    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Signal o) {
        return id.compareTo(o.id);
    }

    public double[] getBounds() {
        return new double[] { getMin(), getMax()  };
    }
    
    public double getMin() {
        return min;
    }
    
    public double getMax() {
        return max;
    }
    
    void setMin(double newMin) { this.min = newMin; }
    void setMax(double newMax) { this.max = newMax; }
}
