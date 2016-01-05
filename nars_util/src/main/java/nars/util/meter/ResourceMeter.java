package nars.util.meter;

import nars.util.meter.event.DurationMeter;
import nars.util.meter.resource.MemoryUseTracker;
import nars.util.meter.resource.ThreadCPUTimeTracker;

/**
 * Awareness of available and consumed resources, such as: real-time,
 * computation time, memory, energy, I/O, etc..
 */
public class ResourceMeter {

	public final MemoryUseTracker CYCLE_RAM_USED = new MemoryUseTracker(
			"ram.used");
	/** the cpu time of each cycle */
	public final ThreadCPUTimeTracker CYCLE_CPU_TIME = new ThreadCPUTimeTracker(
			"cpu.time");

	/**
	 * the duration of frames (one or more cycles), in seconds at nanosecond
	 * resolution
	 */
	public final DurationMeter FRAME_DURATION = new DurationMeter("frame.time",
			true, 1.0, false);

}
