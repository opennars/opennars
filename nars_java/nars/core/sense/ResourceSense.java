package nars.core.sense;

import nars.core.Memory;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.NanoTimeDurationTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;

/**
 * Awareness of available and consumed resources, such as:
 * real-time, computation time, memory, energy, I/O, etc..
 */
public class ResourceSense extends AbstractSense {
    
    public final MemoryUseTracker CYCLE_RAM_USED;
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME; //the cpu time of each cycle
    public final NanoTimeDurationTracker CYCLE; //the duration of the cycle
    
    

    public ResourceSense() {
        super();
        
        
        add(CYCLE = new NanoTimeDurationTracker("cycle"));   
        
        
        add(CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used"));
        CYCLE_RAM_USED.setSampleResolution(128);
        CYCLE_RAM_USED.setSampleWindow(128);
                
        add(CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time"));
        CYCLE_CPU_TIME.setSampleResolution(128);
        CYCLE_CPU_TIME.setSampleWindow(128);        
    }

    
    @Override
    public void sense(Memory memory) {
        //DataSet cycle = CYCLE.get();
        double cycleTimeMS = CYCLE.getValue();
        double cycleTimeMeanMS = CYCLE.get().mean();
        {
            put("cycle.frequency.hz", cycleTimeMS == 0 ? 0 : (1000.0 / cycleTimeMS) );
            put("cycle.frequency_potential.mean.hz", (cycleTimeMeanMS == 0) ? 0 : (1000.0 / cycleTimeMeanMS) );
        }
        {
            //DataSet d = MEMORY_CYCLE_RAM_USED.get();
            put("cycle.ram_use.delta_Kb.sampled", CYCLE_RAM_USED.getValue());
        }
        {
            //DataSet d = CYCLE_CPU_TIME.get();
            put("cycle.cpu_time.mean", CYCLE_CPU_TIME.get().mean() );
        }
        
    }

    
}
