/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.util.meter;

/**
 * Instance of a column header in a Metrics table; indicates what appears
 * in the column index of rows.
 * May cache re-usable metadata specific to the signal shared by several SignalData views (ex: min, max)
 */
public class Signal implements Comparable<Signal> {
    public final String id;
    public String unit;

    private double min, max;


    public Signal(String id) {
        this(id, null);
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
        return id.equals(((Signal) obj).id);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(Signal o) {
        return id.compareTo(o.id);
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    void setMin(double newMin) {
        min = newMin;
    }

    void setMax(double newMax) {
        max = newMax;
    }

    void resetBounds() {
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
    }

//        void invalidateBounds() {
//            min = max = Double.NaN;
//        }
//
//        boolean isInvalidatedBounds() {
//            return (Double.isNaN(min));
//        }
//    

}
