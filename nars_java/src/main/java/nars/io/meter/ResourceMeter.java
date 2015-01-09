package nars.io.meter;

import nars.core.Memory;
import nars.io.meter.depr.data.DataContainer;
import nars.io.meter.event.DurationMeter;
import nars.io.meter.resource.MemoryUseTracker;
import nars.io.meter.resource.ThreadCPUTimeTracker;

/**
 * Awareness of available and consumed resources, such as:
 * real-time, computation time, memory, energy, I/O, etc..
 */
public class ResourceMeter {
    
    public final MemoryUseTracker CYCLE_RAM_USED = new MemoryUseTracker("ram.used");
    /** the cpu time of each cycle */
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME = new ThreadCPUTimeTracker("cpu.time"); 
 
    /** the duration of the cycle */
    public final DurationMeter CYCLE_DURATION = new DurationMeter("cycle.time", true, 1.0, false); 
     
   
}
