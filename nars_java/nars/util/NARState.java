package nars.util;

import java.util.HashMap;
import java.util.TreeMap;
import nars.core.NAR;
import nars.entity.Concept;
import nars.storage.AbstractBag;

/**
 * Contains information about the state of a NAR, including measurements 
 * and statistics.
 * 
 */
public class NARState extends TreeMap<Long, HashMap<String, Object>> {
    
    public final NAR nar;   
    
    public NARState(final NAR n) {
        this.nar = n;
        
        measure();        
    }
    
    protected static HashMap<String,Object> newData() {
        return new HashMap<String,Object>();
    }
    
    public HashMap<String,Object> measure(/*...which measurements to record */) {
        long now = nar.getTime();
        HashMap<String, Object> data = newData();
        
        AbstractBag<Concept> concepts = nar.memory.concepts;
        
        //..
        data.put("concepts.AveragePriority", nar.memory.concepts.getAveragePriority());
        data.put("concepts.Total", concepts.size());
        data.put("concepts.Mass", concepts.getMass());
        
        put(now, data);        
        return data;
    }

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
