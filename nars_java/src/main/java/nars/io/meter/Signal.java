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
public class Signal {
    public String id;
    public String unit;

    public Signal(String id) {
        this.id = id;
        this.unit = null;
    }

    public Signal(String id, String unit) {
        this.id = id;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return id;
    }
    
    
    
}
