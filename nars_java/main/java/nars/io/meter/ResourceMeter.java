package nars.io.meter;

import nars.core.Memory;
import nars.util.meter.data.DataContainer;
import nars.util.meter.sensor.MemoryUseTracker;
import nars.util.meter.sensor.NanoTimeDurationTracker;
import nars.util.meter.sensor.ThreadCPUTimeTracker;

/**
 * Awareness of available and consumed resources, such as:
 * real-time, computation time, memory, energy, I/O, etc..
 */
public class ResourceMeter extends AbstractMeter {
    
    public final MemoryUseTracker CYCLE_RAM_USED;
    public final ThreadCPUTimeTracker CYCLE_CPU_TIME; //the cpu time of each cycle
    public final NanoTimeDurationTracker CYCLE; //the duration of the cycle
    
    

    public ResourceMeter() {
        super();        
        
        add(CYCLE = new NanoTimeDurationTracker("cycle") {
            @Override public void commit(DataContainer d, Memory m) {
                double cycleTimeMS = getValue();
                double cycleTimeMeanMS = get().mean();
                
                d.put("cycle.frequency.hz", 
                        cycleTimeMS == 0 ? 0 : (1000.0 / cycleTimeMS) );
                d.put("cycle.frequency_potential.mean.hz", 
                        (cycleTimeMeanMS == 0) ? 0 : (1000.0 / cycleTimeMeanMS) );
            }            
        });      
        
        add(CYCLE_RAM_USED = new MemoryUseTracker("memory.cycle.ram_used") {
            @Override public void init() {
                setSampleResolution(128).setSampleWindow(128);
            }            
            @Override public void commit(DataContainer d, Memory m) {
                d.put("cycle.ram_use.delta_Kb.sampled", getValue());
            }
        });
        
        
        add(CYCLE_CPU_TIME = new ThreadCPUTimeTracker("memory.cycle.cpu_time") {
            @Override public void init() {
                setSampleResolution(128).setSampleWindow(128);
            }            
            @Override public void commit(DataContainer d, Memory m) {
                d.put("cycle.cpu_time.mean", get().mean() );
            }            
        });
    }

    
    
}
