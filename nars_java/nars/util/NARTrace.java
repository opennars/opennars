package nars.util;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nars.core.Events;
import nars.core.Events.ConceptAdd;
import nars.core.Events.InferenceEvent;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.sense.MultiSense;
import nars.entity.Concept;
import nars.entity.Task;
import nars.gui.NARSwing;
import nars.gui.output.chart.TimeSeries;
import nars.inference.MemoryObserver;

/**
 * Records all sensors, output, and trace events in an indexed data structure for runtime or subsequent analysis of a NAR's execution telemetry.
 */
public class NARTrace extends MemoryObserver implements Serializable {

    /**
     * utility method for diagnosing stack overflow errors caused by unbounded
     * recursion or other phenomena
     */
    public static boolean guardStack(int alertDepth, String methodname, Object... args) {
        StackTraceElement[] st = new Exception().getStackTrace();
        if (st.length < 1 + alertDepth) {
            return false;
        }
        for (int i = 1; i < alertDepth; i++) {
            //look for a series of equal (TODO: or cyclic) method names
            if (!st[i].getMethodName().contains(methodname)) {
                return false;
            }
        }
        return true;
    }

    final int chartHistorySize = 5000;
    
    public final Map<Concept, List<InferenceEvent>> concept = new HashMap();
    public final TreeMap<Long, List<InferenceEvent>> time = new TreeMap();
    public final Map<String, TimeSeries> charts = new TreeMap();

    private long t;
    public final NAR nar;
    public final MultiSense senses;



    public static class OutputEvent extends InferenceEvent {

        public final Class channel;
        public final Object[] signal;

        public OutputEvent(long when, Class channel, Object... signal) {
            super(when);
            this.channel = channel;
            this.signal = signal;
        }

        @Override
        public String toString() {
            return ((Class)channel).getSimpleName() + ": " + 
                    (signal.length > 1 ? Arrays.toString(signal) : signal[0]);
        }
        
        public Class getType() {
            return channel;
        }
        

    }

    public static interface HasLabel {
        public String toLabel();
    }
    
    public static enum AddOrRemove {
        Add, Remove
    }
    
    public static class TaskEvent extends InferenceEvent implements HasLabel {

        public final Task task;
        public final AddOrRemove type;
        public final String reason;
        public float priority;

        public TaskEvent(Task t, long when, AddOrRemove type, String reason) {
            super(when);
            this.task = t;
            this.type = type;
            this.reason = reason;
            this.priority = t.getPriority();
        }

        @Override
        public String toString() {
            return "Task " + type + " (" + reason + "): " + task.toStringExternal();
        }
        @Override
        public String toLabel() {
            return "Task " + type + " (" + reason + "):\n" + task.name();
        }
    }

    
    public NARTrace(NAR n) {
        super(n, true);
        this.nar = n;
        
        Memory memory = nar.memory;
        
        senses = new MultiSense(memory.logic, memory.resource);
        senses.setActive(true);
        senses.update(memory);        
        
        for (String x : senses.keySet()) {
            TimeSeries ch = new TimeSeries(x, NARSwing.getColor(x+"_EsfDF_SDF_SD", 0.8f, 0.8f), chartHistorySize);
            charts.put(x, ch);            
        }
        
    }
    
    

    public void addEvent(InferenceEvent e) {
        List<InferenceEvent> timeslot = time.get(t);
        if (timeslot == null) {
            timeslot = new ArrayList();
            time.put(t, timeslot);
        }
        timeslot.add(e);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void reset() {
        time.clear();
        concept.clear();
    }
    
    @Override
    public void event(final Class event, final Object[] arguments) {
        if (event == Events.TaskAdd.class) {
            onTaskAdd((Task)arguments[0], (String)arguments[1]);
        }
        else if (event == Events.TaskRemove.class) {
            onTaskRemove((Task)arguments[0], (String)arguments[1]);
        }
        else
            super.event(event, arguments);

    }
    
    @Override
    public void onConceptAdd(Concept concept) {
        if (this.concept.containsKey(concept)) 
            throw new RuntimeException(this + " adding duplicate concept: " + concept);

        ConceptAdd cc = new ConceptAdd(concept, t);
        addEvent(cc);

        List<InferenceEvent> lc = new ArrayList(1);
        lc.add(cc);
                
        this.concept.put(concept, lc);
    }

    @Override
    public void onCycleStart(long clock) {
        this.t = clock;
    }

    @Override
    public void onCycleEnd(long time) {
        senses.update(nar.memory);
        
        for (Map.Entry<String, TimeSeries> e : charts.entrySet()) {
            String f = e.getKey();            
            TimeSeries ch = e.getValue();
            Object value = senses.get(f);
            
            if (value instanceof Double) {                    
                ch.push(time, ((Number) value).floatValue());
            }
            else if (value instanceof Float) {
                ch.push(time, ((Number) value).floatValue());
            }
            else if (value instanceof Integer) {
                ch.push(time, ((Number) value).floatValue());
            }
            else if (value instanceof Long) {
                ch.push(time, ((Number) value).floatValue());
            }            
        }        
    }

    @Override
    public void onTaskAdd(Task task, String reason) {
        TaskEvent ta = new TaskEvent(task, t, AddOrRemove.Add, reason);
        addEvent(ta);
    }

    @Override
    public void onTaskRemove(Task task, String reason) {
        TaskEvent tr = new TaskEvent(task, t, AddOrRemove.Remove, reason);
        addEvent(tr);
    }

    @Override
    public void output(Class channel, Object... signal) {
        addEvent(new OutputEvent(t, channel, signal));
    }
    
    public void printTime() {
        printTime(System.out);
    }

    public void printTime(PrintStream out) {
        for (Long w : time.keySet()) {
            List<InferenceEvent> events = time.get(w);
            if (events.isEmpty()) {
                continue;
            }

            out.println(w + " ---------\\");
            for (InferenceEvent e : events) {
                System.out.println("  " + e);
            }
            out.println(w + " ---------/\n");

        }
    }

}
