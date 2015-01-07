/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A tabular data store where each (# indexed) column represents a different
 * type of value, and each row is a that value sampled/recorded at a different
 * time point (first column).
 * 
 */
public class Metrics<RowKey> implements Iterable<Object[]> {


    
    /** generates the value of the first entry in each row */
    class RowKey extends SimpleMeter<RowKey> {

        public RowKey() {
            super("time");
        }

        @Override
        protected RowKey getValue(Object key, int index) {
            return nextRowKey;
        }
        
    }
    
    private RowKey nextRowKey = null;
    
    /** the columns of the table */
    private List<Meter<?>> meters = new ArrayList<>();
    private ArrayDeque<Object[]> rows = new ArrayDeque<>();
    
    transient private List<Signal> signalList = new ArrayList<>();
    
    int numColumns = 0;
    
    /** capacity */
    int history;

    public Metrics(int historySize) {
        this.history = historySize;
        
        addMeter(new RowKey());
    }
    
    
    public void addMeter(Meter m) {
        meters.add(m);
        numColumns+= m.numSignals();
    }
    
    public void removeMeter(Meter m) {
        throw new RuntimeException("Removal not supported yet");
    }
    
    /** key could be a time, or some other unique-like identifying value */
    public void update(RowKey key) {
        nextRowKey = key;        
        
        Object[] nextRow = new Object[ numColumns ];
        append(nextRow); //append it so that any derivative columns further on can work with the most current data (in lower array indices) while the array is being formed

        int c = 0;
        for (Meter<?> m : meters) {
            Object[] v = m.sample(key);
            int vl = v.length;
            System.arraycopy(v, 0, nextRow, c, vl);
            c += vl; 
        }
        
    }
    
    protected void append(Object[] row) {
        if (row==null) return;        
        
        while (rows.size() + 1 >= history)
            rows.removeFirst();
        
        rows.addLast(row);            
    }
    
    public List<Signal> getSignals() {
        if (signalList == null) {
            signalList = new ArrayList(numColumns);
            for (Meter<?> m : meters)
                signalList.addAll(m.getSignals());
        }
        return signalList;        
    }
    
    public Signal getSignal(int column) {
       return getSignals().get(column); 
    }
    
    public Object[] rowFirst() { return rows.getFirst(); }
    public Object[] rowLast() { return rows.getLast(); }
    public int numRows() { return rows.size(); }
    
    @Override
    public Iterator<Object[]> iterator() {        
        return rows.iterator();
    }
    
    public Iterator<Object[]> reverseIterator() {        
        return rows.descendingIterator();
    }
    
    public Object[] getSignalData(int i) {
        Object[] c = new Object[ numRows() ];
        int r = 0;
        for (Object[] row : this) {
            c[r++] = row[i];
        }
        return c;
    }
    
    //Table<Signal,Object (row),Object (value)> getSignalTable(int columns...)
    
    /*    public Signal getSignal(int column) {
        int p = 0;
        for (Meter<?> m : meters) {
            int s = m.numSignals();            
            if (column < p + s) {
                return m.getSignals().get(column - p);
            }
            p += s;
        }
        return null;
    }*/
    
    public Iterator<Object> iterateSignal(int column, boolean reverse) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static List<Double> doubles(List<Object> l) {
        List<Double> r = new ArrayList();
        for (Object o : l)
            if (o instanceof Number) r.add(((Number)o).doubleValue());
        return r;
    }


    public List<Object> getNewSignalValues(int num) {
        List<Object> l = new ArrayList(num);
        Iterator<Object[]> r = reverseIterator();        
        while (r.hasNext() && num > 0) {
            l.add(r.next());
            num--;
        }
        return l;
    }     
    


}
