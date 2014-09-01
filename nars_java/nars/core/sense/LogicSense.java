package nars.core.sense;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nars.core.Memory;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.sensor.EventSensor;
import nars.util.meter.sensor.EventValueSensor;
import nars.util.meter.sensor.HitPeriodTracker;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.NanoTimeDurationTracker;
import nars.util.meter.sensor.SpanTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;

/**
 *
 * @author me
 */


public class LogicSense extends DefaultDataSet implements Serializable {
    
    final Map<String,Sensor> sensors = new HashMap<String,Sensor>();    
    
    private final Memory memory;
    long lastUpdate = -1;
    
    int sensorDrainPeriodCycles = 2048; //how often to reset sensors
    
    
    //public final Sensor CONCEPT_FIRE;
    public final NanoTimeDurationTracker IO_CYCLE;    
    public final NanoTimeDurationTracker MEMORY_CYCLE;
    public final HitPeriodTracker CYCLE;
    
    public final EventValueSensor TASK_IMMEDIATE_PROCESS_PRIORITY;
    public final HitPeriodTracker TASK_IMMEDIATE_PROCESS;
    
    public final EventValueSensor TASKLINK_FIRE;
    public final NanoTimeDurationTracker TASKLINK_REASON; //both duration and count
   
    public final MemoryUseTracker MEMORY_CYCLE_RAM_USED;
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME;
    //public final ThreadBlockTimeTracker CYCLE_BLOCK_TIME;
    private Object conceptNum;
    private Object conceptPriorityMean;
    private Object conceptPrioritySum;
    private Object conceptBeliefsSum;
    private Object conceptQuestionsSum;
    
    

    public LogicSense(Memory memory) {
        super(new ConcurrentHashMap());
        this.memory = memory;
        

        
        add(IO_CYCLE = new NanoTimeDurationTracker("io.cycle"));
        add(MEMORY_CYCLE = new NanoTimeDurationTracker("memory.cycle"));
        add(CYCLE = new HitPeriodTracker("cycle"));
        
        add(TASK_IMMEDIATE_PROCESS = new HitPeriodTracker("task.immediate_process"));
        add(TASK_IMMEDIATE_PROCESS_PRIORITY = new EventValueSensor("task.immediate_process.priority"));
        add(MEMORY_CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used"));
        add(CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time"));
        
        //add(CONCEPT_FIRE = new DefaultEventSensor("concept.fire"));
        add(TASKLINK_FIRE = new EventValueSensor("tasklink.fire"));
        add(TASKLINK_REASON = new NanoTimeDurationTracker("tasklink.reason"));
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
    public LogicSense update() {
        long time = memory.getTime();
        if (time == lastUpdate) {
            //already updated
            return this;
        }

        lastUpdate = time;                     

        put("concepts.count", conceptNum);
        put("concepts.priority.mean", conceptPriorityMean);        
        put("concepts.beliefs.sum", conceptBeliefsSum);
        put("concepts.questions.sum", conceptQuestionsSum);
        
        put("memory.noveltasks.total", memory.novelTasks.size());
        put("memory.newtasks.total", memory.newTasks.size());

        //TODO move to EmotionState
        put("emotion.happy", memory.emotion.happy());
        put("emotion.busy", memory.emotion.busy());

        
        DataSet cycle = CYCLE.get();
        double cycleMean = cycle.mean();
        {
            put("cycle.frequency.hz", (cycleMean == 0) ? 0 : (1000.0 / cycleMean) );
        }
        {
            DataSet d = IO_CYCLE.get();
            put("io.cycle.period.mean.pct", d.mean() / cycleMean );
        }
        {
            DataSet d = MEMORY_CYCLE.get();
            put("memory.cycle.period.mean.pct", d.mean() / cycleMean );
        }
        {
            DataSet d = MEMORY_CYCLE_RAM_USED.get();
            put("memory.cycle.ram_use.delta_Kb", d.mean() );
        }
        {
            DataSet d = CYCLE_CPU_TIME.get();
            put("memory.cycle.cpu_time.pct", d.mean() / cycleMean );
        }
        {
            DataSet fire = TASKLINK_FIRE.get();
            DataSet reason = TASKLINK_REASON.get();
            put("reason.fire.tasklink.priority.mean", fire.mean());
            put("reason.fire.tasklink.delta_count", (double)TASKLINK_FIRE.getDeltaHits());
            
            put("reason.tasklink.period.pct", reason.mean() / cycleMean);
            put("reason.tasklink.delta_count", (double)TASKLINK_REASON.getDeltaHits());
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
