/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
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
public class Metrics<RowKey,Cell> implements Iterable<Object[]> {

    final static int PRECISION = 4;
    public final static Gson json = new GsonBuilder()
             .registerTypeAdapter(Double.class, new JsonSerializer<Double>()  { 
                        public JsonElement serialize(Double value, Type theType, 
JsonSerializationContext context) { 
                                if (value.isNaN()) { 
                                        return new JsonPrimitive("NaN");
                                } else if (value.isInfinite()) { 
                                        return new JsonPrimitive(value);
                                } else { 
                                        return new JsonPrimitive(
                                                new BigDecimal(value).
                                                    setScale(PRECISION,
                                                    BigDecimal.ROUND_HALF_UP).stripTrailingZeros()); 
                                } 
                        } 
                }) 
                .create(); 

    
    public static void printJSONArray(PrintStream out, Object[] row, boolean includeBrackets) {
        String r = json.toJson(row);
        if (!includeBrackets) {
            r = r.substring(1, r.length()-1);
        }
        out.println(r);
        
    }

    /** generates the value of the first entry in each row */
    class RowKeyMeter extends FunctionMeter {

        public RowKeyMeter() {
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
        
        meters.add(new RowKeyMeter());
        numColumns++;
        
        signalList = null;
    }
    
    
    public void addMeter(Meter<? extends Cell> m) {
        meters.add(m);
        numColumns+= m.numSignals();
        signalList = null;
    }
    
    public void removeMeter(Meter<? extends Cell> m) {
        throw new RuntimeException("Removal not supported yet");
    }
    
    /** key could be a time, or some other unique-like identifying value */
    public void update(RowKey key) {
        nextRowKey = key;        
        
        Object[] nextRow = new Object[ numColumns ];
        append(nextRow); //append it so that any derivative columns further on can work with the most current data (in lower array indices) while the array is being formed

        int c = 0;
        for (Meter m : meters) {
            Cell[] v = ((Meter<? extends Cell>)m).sample(key);
            if (v == null) continue;
            int vl = v.length;

            if (c + vl > nextRow.length) 
                throw new RuntimeException("column overflow: " + m + " " + c + "+" + vl + ">" + nextRow.length);
            
            System.arraycopy(v, 0, nextRow, c, vl);
            c += vl; 
        }
        
    }
    
    protected void append(Object[] row) {
        if (row==null) return;        
        
        while (rows.size() >= history)
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


    public List<Object> getNewSignalValues(int column, int num) {
        List<Object> l = new ArrayList(num);
        Iterator<Object[]> r = reverseIterator();        
        while (r.hasNext() && num > 0) {
            l.add(r.next()[column]);
            num--;
        }
        return l;
    }
    
    public String[] getSignalIDs() {
        String[] r = new String[getSignals().size()];
        int i = 0;
        for (Signal s : getSignals()) {
            r[i++] = s.id;
        }
        return r;
    }
        
    
    public void printCSV(PrintStream out) {
        printJSONArray(out, getSignalIDs(),false );
        for (Object[] row : this) {
            printJSONArray(out, row, false);
        }
    }


}
