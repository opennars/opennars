package nars.core.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import nars.core.Memory;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.sensor.EventSensor;
import nars.util.meter.sensor.EventValueSensor;
import nars.util.meter.sensor.HitPeriodTracker;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.SpanTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;

/**
 *
 * @author me
 */


public class LogicState extends DefaultDataSet implements Serializable {
    
    final Map<String,Sensor> sensors = new HashMap<String,Sensor>();    
    
    private final Memory memory;
    long lastUpdate = -1;
    
    int sensorDrainPeriodCycles = 64; //how often to reset sensors
    
    
    //public final Sensor CONCEPT_FIRE;
    //public final HitPeriodTracker IO_CYCLE;
    public final HitPeriodTracker MEMORY_CYCLE_WORKING;
    public final EventValueSensor TASK_IMMEDIATE_PROCESS_PRIORITY;
    public final HitPeriodTracker TASK_IMMEDIATE_PROCESS;
    public final MemoryUseTracker MEMORY_CYCLE_RAM_USED;
    public final ThreadCPUTimeTracker MEMORY_CYCLE_CPU_TIME;
    private Object conceptNum;
    private Object conceptPriorityMean;
    private Object conceptPrioritySum;
    private Object conceptBeliefsSum;
    private Object conceptQuestionsSum;

    public LogicState(Memory memory) {
        super(new LinkedHashMap());
        this.memory = memory;
        

        
        add(MEMORY_CYCLE_WORKING = new HitPeriodTracker("memory.cycle.working"));
        
        add(TASK_IMMEDIATE_PROCESS = new HitPeriodTracker("task.immediate_process"));
        add(TASK_IMMEDIATE_PROCESS_PRIORITY = new EventValueSensor("task.immediate_process.priority"));
        add(MEMORY_CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used"));
        add(MEMORY_CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time"));
        
        //add(CONCEPT_FIRE = new DefaultEventSensor("concept.fire"));
        
    }
    
    protected void add(Sensor s) { 
        sensors.put(s.name(), s); 
    }
    
    public SpanTracker getSensorSpan(final String name) { 
        Sensor s = sensors.get(name); 
        if (s instanceof SpanTracker)
            return ((SpanTracker)s);
        return null;
    }
    public EventSensor getSensorEvent(final String name) { 
        Sensor s = sensors.get(name); 
        if (s instanceof EventSensor)
            return ((EventSensor)s);
        return null;
    }

    /** returns the same instance */
    public LogicState update() {
        long time = memory.getTime();
        if (time == lastUpdate) {
            //already updated
            return this;
        }

        lastUpdate = time;                     

        put("concepts.count", conceptNum);
        put("concepts.priority.mean", conceptPriorityMean);
        put("concepts.priority.sum", conceptPrioritySum);
        put("concepts.beliefs.sum", conceptBeliefsSum);
        put("concepts.questions.sum", conceptQuestionsSum);
        
        put("memory.noveltasks.total", memory.novelTasks.size());
        put("memory.newtasks.total", memory.newTasks.size());

        //TODO move to EmotionState
        put("emotion.happy", memory.emotion.happy());
        put("emotion.busy", memory.emotion.busy());

        
        {
            DataSet d = MEMORY_CYCLE_WORKING.get();
            put("memory.cycle.working", d.hits() );
            put("memory.cycle.working.period.mean.ms", d.mean()  );
            
            
        }
        {
            DataSet d = MEMORY_CYCLE_RAM_USED.get();
            put("memory.cycle.ram_use.delta_Kb", d.mean() );
        }
        {
            DataSet d = MEMORY_CYCLE_CPU_TIME.get();
            put("memory.cycle.cpu_time.ms", d.mean() );
        }
        
        if (time % sensorDrainPeriodCycles == 0)
            updateSensors(true);

        return this;
    }
    
    public void updateSensors(final boolean reset) {
        for (final Sensor s : sensors.values()) {
            if (reset)
                s.getSession().drainData();
            else
                s.getSession().collectData();
        }        
    }
     
    public static class UnknownSensorException extends RuntimeException {

        public UnknownSensorException(final String name) {
            super("LogicState: Unknown EventSensor " + name);
        }    
        
    }
    
    public void incident(final String name) {
        final EventSensor e = getSensorEvent(name);
        if (e == null)
            throw new UnknownSensorException(name);
        e.event();
    }
    
    public void print() {
        for (Map.Entry<String, Object> a : this.dataMap.entrySet()) {
            System.out.println(a.getKey() + " = " + a.getValue());
        }
        System.out.println();
    }

    public void setConceptBeliefsSum(Object conceptBeliefsSum) {
        this.conceptBeliefsSum = conceptBeliefsSum;
    }

    public void setConceptNum(Object conceptNum) {
        this.conceptNum = conceptNum;
    }

    public void setConceptPriorityMean(Object conceptPriorityMean) {
        this.conceptPriorityMean = conceptPriorityMean;
    }

    public void setConceptPrioritySum(Object conceptPrioritySum) {
        this.conceptPrioritySum = conceptPrioritySum;
    }

    public void setConceptQuestionsSum(Object conceptQuestionsSum) {
        this.conceptQuestionsSum = conceptQuestionsSum;
    }
    
    
}
