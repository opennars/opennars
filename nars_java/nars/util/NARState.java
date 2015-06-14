package nars.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import nars.core.NAR;
import nars.entity.Concept;

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
        return new HashMap<>();
    }
    
    public HashMap<String,Object> measure(/*...which measurements to record */) {
        long now = nar.getTime();
        HashMap<String, Object> data = newData();
        try{
            Collection<? extends Concept> concepts = nar.memory.getConcepts();

            double averagePriority = 0, mass = 0;
            //TODO calculate
            
            data.put("concepts.AveragePriority", averagePriority);
            data.put("concepts.Total", concepts.size());
            data.put("concepts.Mass", mass);
            data.put("beliefs.Total", totalBeliefs(concepts));
            data.put("questions.Total", totalQuestions(concepts));
            data.put("novelTasks.Total", nar.memory.novelTasks.size());
            data.put("newTasks.Total", nar.memory.newTasks.size());

            data.put("emotion.happy", nar.memory.emotion.happy());
            data.put("emotion.busy", nar.memory.emotion.busy());

            put(now, data);   
        }
        catch(Exception ex) {}
        return data;
    }

    @Override
    public Object clone() {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    /** count number of beliefs of a set of concepts */
    public static int totalBeliefs(Iterable<? extends Concept> concepts) {
        int t = 0;
        for (Concept c : concepts)
            t += c.beliefs.size();        
        return t;
    }
    /** count number of beliefs of a set of concepts */
    public static int totalQuestions(Iterable<? extends Concept> concepts) {
        int t = 0;
        for (Concept c : concepts)
            t += c.questions.size();        
        return t;
    }
    
}
