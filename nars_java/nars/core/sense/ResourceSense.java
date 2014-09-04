package nars.core.sense;

import nars.core.Memory;
import nars.util.meter.sensor.HitPeriodTracker;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.NanoTimeDurationTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;

/**
 * Awareness of available and consumed resources, such as:
 * real-time, computation time, memory, energy, I/O, etc..
 */
public class ResourceSense extends AbstractSense {
    
    public final MemoryUseTracker MEMORY_CYCLE_RAM_USED;
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME; //the cpu time of each cycle
    public final NanoTimeDurationTracker IO_CYCLE;    //duration of the I/O component of each cycle
    public final NanoTimeDurationTracker MEMORY_CYCLE; //duration of the working component of cycle
    public final NanoTimeDurationTracker CYCLE; //the duration of the cycle
    public final HitPeriodTracker CYCLE_REAL;  //the real time between each cycle
    

    public ResourceSense() {
        super();
        
        add(IO_CYCLE = new NanoTimeDurationTracker("io.cycle"));
        add(MEMORY_CYCLE = new NanoTimeDurationTracker("memory.cycle"));
        
        add(CYCLE = new NanoTimeDurationTracker("cycle"));   
        add(CYCLE_REAL = new HitPeriodTracker("cycle"));   
        CYCLE.setSampleWindow(64);
        

        
        add(MEMORY_CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used"));
        MEMORY_CYCLE_RAM_USED.setSampleResolution(16);
        MEMORY_CYCLE_RAM_USED.setSampleWindow(128);
                
        add(CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time"));
        CYCLE_CPU_TIME.setSampleResolution(16);
        CYCLE_CPU_TIME.setSampleWindow(128);        
    }

    
    @Override
    public void sense(Memory memory) {
        //DataSet cycle = CYCLE.get();
        double cycleTimeMS = CYCLE.getValue();
        double cycleTimeMeanMS = CYCLE.get().mean();
        {
            double cr = CYCLE_REAL.getValue();
            put("cycle.frequency.hz", cr == 0 ? 0 : (1000.0 / cr) );
            put("cycle.frequency_potential.mean.hz", (cycleTimeMeanMS == 0) ? 0 : (1000.0 / cycleTimeMeanMS) );
        }
        {
            //DataSet d = IO_CYCLE.get();
            double mv = MEMORY_CYCLE.getValue();            
            double r = (mv > 0) ? IO_CYCLE.getValue() / mv : 0;
            put("io.to_memory.ratio", r );
        }
        {
            //DataSet d = MEMORY_CYCLE_RAM_USED.get();
            put("cycle.ram_use.delta_Kb.sampled", MEMORY_CYCLE_RAM_USED.getValue());
        }
        {
            //DataSet d = CYCLE_CPU_TIME.get();
            put("cycle.cpu_time.mean", CYCLE_CPU_TIME.get().mean() );
        }
        
    }
    
}
