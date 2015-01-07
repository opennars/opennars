/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.io.meter;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A tabular data store where each (# indexed) column represents a different
 * type of value, and each row is a that value sampled/recorded at a different
 * time point (first column).
 * 
 */
public class Metrics<RowKey extends Object,Cell extends Object> implements Iterable<Object[]> {

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

    
    /**
    *  Calculates a 2-tuple with the following data:
    *   0: minimum value among all columns in given signals
    *   1: maximum value among all columns in given signals
    * 
    * @param data
    * @return 
    */
    public static double[] getBounds(Iterable<SignalData> data) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
    
        for (SignalData ss : data) {
            double a = ss.getMin();
            double b = ss.getMax();
            if (a < min) min = a;
            if (b > max) max = b;
        }
        return new double[] { min, max };        
    }
    
    /*  Calculates a 2-tuple with the following data:
    *   0: minimum value among specific column
    *   1: maximum value among specific column
    */ 
    public double[] getBounds(int signal) {
        return getSignal(signal).getBounds();
    }

    public SignalData getSignalData(String n) {
        Signal s = getSignal(n);
        if (s == null) return null;
        return new SignalData(this, s);
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
    transient private Map<Signal, Integer> signalIndex = new HashMap();
    
    int numColumns;
    
    /** capacity */
    int history;

    public Metrics(int historySize) {
        super();
        this.history = historySize;
        
        addMeter(new RowKeyMeter());
    }
    

    public void clear() {
        clearData();
        clearSignals();
    }
    
    public void clearSignals() {
        numColumns = 0;
        signalList = null;
        signalIndex = null;
    }
    
    public void clearData() {
        rows.clear();
    }
    
    public void addMeter(Meter<? extends Cell> m) {
        meters.add(m);
        numColumns+= m.numSignals();
        signalList = null;
        signalIndex = null;
    }
    
    public void removeMeter(Meter<? extends Cell> m) {
        throw new RuntimeException("Removal not supported yet");
    }
    
    /** key could be a time, or some other unique-like identifying value */
    public <R extends RowKey> void update(R key) {
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
    
    public Map<Signal,Integer> getSignalIndex() {
        if (signalIndex == null) {
            int i = 0;
            for (Signal s : getSignals()) {
                signalIndex.put(s, i++);
            }
        }
        return signalIndex;
    }
    
    public int getIndex(Signal s) {
        return getSignalIndex().get(s);
    }
    public int getIndex(String s) {
        return getSignalIndex().get(s);
    }
    
    public Signal getSignal(int index) {
       return getSignals().get(index); 
    }
    public Signal getSignal(String s) {
       return getSignals().get(getIndex(s)); 
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
    
    public static class SignalData {
        public final Signal signal;
        private final Metrics metric;
        private final int index;
        private Object[] data;

        public SignalData(Metrics m, Signal s) {
            this.metric = m;
            this.signal = s;
            this.index = metric.getSignals().indexOf(s);
        }

        public Signal getSignal() {
            return signal;
        }
        
        public Object[] getDataCached() { return data; }
        
        public Object[] getData() {        
            return this.data = metric.getData(index, data);
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

        private double getMin() {
            return signal.getMin();
        }

        private double getMax() {
            return signal.getMax();
        }

        
    }
    
    public Object[] getData(int signal, Object[] c) {        
        if ((c == null) || (c.length != numRows() )) 
            c = new Object[ numRows() ];
        
        int r = 0;
        for (Object[] row : this) {
            c[r++] = row[signal];
        }
        
        return c;
    }
    
    public Object[] getData(int signal) {        
        return getData(signal, null);
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
    
    public Iterator<Object[]> iterator(final int... columns) {
        return Iterators.transform(iterator(), new Function<Object[], Object[]>() {

            Object[] next = new Object[columns.length];

            @Override
            public Object[] apply(Object[] f) {
                
                if (columns.length == 1) {
                    //fast 1-argument
                    next[0] = f[columns[0]];
                    return next;
                }
                
                int j = 0;
                for (int c : columns) {
                    next[j++] = f[c];
                }
                return next;
            }
            
        });
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
