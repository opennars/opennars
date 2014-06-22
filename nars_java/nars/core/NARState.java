package nars.core;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Contains information about the state of a NAR, including measurements 
 * and statistics.
 * 
 */
public class NARState extends TreeMap<Long, HashMap<String, Object>> {
    
    public final NAR nar;   
    
    public NARState(NAR n) {
        this.nar = n;
        
    }
    
    public void measure(/*...which measurements to record */) {
        
    }
    
}
