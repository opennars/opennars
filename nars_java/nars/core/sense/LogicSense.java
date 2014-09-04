package nars.core.sense;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import nars.core.Memory;
import nars.util.meter.Sensor;
import nars.util.meter.data.DataSet;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.sensor.AbstractSpanTracker;
import nars.util.meter.sensor.AbstractTracker;
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
    
    int allSensorResetPeriodCycles = 2048; //how often to reset all sensors
    
    
    //public final Sensor CONCEPT_FIRE;
    public final NanoTimeDurationTracker IO_CYCLE;    
    public final NanoTimeDurationTracker MEMORY_CYCLE;
    public final NanoTimeDurationTracker CYCLE; //the duration of the cycle
    public final HitPeriodTracker CYCLE_REAL;  //the real time between each cycle
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME; //the cpu time of each cycle
    
    public final EventValueSensor TASK_IMMEDIATE_PROCESS_PRIORITY;
    public final HitPeriodTracker TASK_IMMEDIATE_PROCESS;
    
    public final EventValueSensor TASKLINK_FIRE;
    public final EventValueSensor TASKTERMLINK_REASON; //both duration and count
    
    public final EventValueSensor OUTPUT_TASK;
    
    public final EventValueSensor CONCEPT_NEW;
    
    public final MemoryUseTracker MEMORY_CYCLE_RAM_USED;
    
    //public final ThreadBlockTimeTracker CYCLE_BLOCK_TIME;
    private Object conceptNum;
    private Object conceptPriorityMean;
    private Object conceptPrioritySum;
    private Object conceptBeliefsSum;
    private Object conceptQuestionsSum;
    
    
    
    

    public LogicSense(Memory memory) {
        super(new TreeMap<>());
    
        this.memory = memory;
        
        add(IO_CYCLE = new NanoTimeDurationTracker("io.cycle"));
        add(MEMORY_CYCLE = new NanoTimeDurationTracker("memory.cycle"));
        
        add(CYCLE = new NanoTimeDurationTracker("cycle"));   
        add(CYCLE_REAL = new HitPeriodTracker("cycle"));   
        CYCLE.setSampleWindow(64);
        
        
        add(TASK_IMMEDIATE_PROCESS = new HitPeriodTracker("task.immediate_process"));
        add(TASK_IMMEDIATE_PROCESS_PRIORITY = new EventValueSensor("task.immediate_process.priority"));
        add(MEMORY_CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used"));
        MEMORY_CYCLE_RAM_USED.setSampleResolution(16);
        MEMORY_CYCLE_RAM_USED.setSampleWindow(128);
                
        add(CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time"));
        CYCLE_CPU_TIME.setSampleResolution(16);
        CYCLE_CPU_TIME.setSampleWindow(128);
                
        //add(CONCEPT_FIRE = new DefaultEventSensor("concept.fire"));
        add(TASKLINK_FIRE = new EventValueSensor("tasklink.fire"));
        TASKLINK_FIRE.setSampleWindow(32);
        
        add(OUTPUT_TASK = new EventValueSensor("output.task"));
        OUTPUT_TASK.setSampleWindow(32);

        add(CONCEPT_NEW = new EventValueSensor("concept.new"));
        CONCEPT_NEW.setSampleWindow(32);
        
        add(TASKTERMLINK_REASON = new EventValueSensor("tasklink.reason"));
        TASKTERMLINK_REASON.setSampleWindow(32);
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
        //put("memory.newtasks.total", memory.newTasks.size()); //redundant with output.tasks below

        //TODO move to EmotionState
        put("emotion.happy", memory.emotion.happy());
        put("emotion.busy", memory.emotion.busy());

        
        //DataSet cycle = CYCLE.get();
        double cycleTimeMS = CYCLE.getValue();
        double cycleTimeMeanMS = CYCLE.get().mean();
        {
            put("cycle.frequency.hz", (1000.0 / CYCLE_REAL.getValue()) );
            put("cycle.frequency_potential.mean.hz", (cycleTimeMeanMS == 0) ? 0 : (1000.0 / cycleTimeMeanMS) );
        }
        {
            //DataSet d = IO_CYCLE.get();
            put("io.to_memory.ratio", IO_CYCLE.getValue() / MEMORY_CYCLE.getValue() );
        }
        {
            //DataSet d = MEMORY_CYCLE_RAM_USED.get();
            put("cycle.ram_use.delta_Kb.sampled", MEMORY_CYCLE_RAM_USED.getValue());
        }
        {
            //DataSet d = CYCLE_CPU_TIME.get();
            put("cycle.cpu_time.mean", CYCLE_CPU_TIME.get().mean() );
        }
        {
            DataSet fire = TASKLINK_FIRE.get();
            //DataSet reason = TASKLINK_REASON.get();
            put("reason.fire.tasklink.priority.mean", fire.mean());
            put("reason.fire.tasklinks.delta", TASKLINK_FIRE.getDeltaHits());
            
            //only makes sense as a mean, since it occurs multiple times during a cycle
            put("reason.tasktermlink.priority.mean", TASKTERMLINK_REASON.get().mean());
            
            put("reason.tasktermlinks", TASKTERMLINK_REASON.getHits());
        }
        {
            put("output.tasks", OUTPUT_TASK.getHits());
            put("output.tasks.budget.mean", OUTPUT_TASK.get().mean());
        }
        {
            put("concept.new", CONCEPT_NEW.getHits());
            put("concept.new.complexity.mean", CONCEPT_NEW.get().mean());
        }
        
        if (time % allSensorResetPeriodCycles == 0)
            updateSensors(true);

        return this;
    }
    
    public void updateSensors(final boolean reset) {
        for (final Sensor s : sensors.values()) {
            if (reset) {
                if (s instanceof AbstractSpanTracker) {
                    if (((AbstractTracker)s).getSampleWindow() > 0)
                        continue;                
                }
                s.getSession().drainData();
            }
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
