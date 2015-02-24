package ca.nengo.util.impl;

import ca.nengo.model.Probeable;
import ca.nengo.model.SimulationException;
import ca.nengo.util.Probe;
import ca.nengo.util.ThreadTask;

/**
 * Implementation of a ThreadTask to multithread collection of data by probes. 
 * 
 * @author Eric Crawford
 */
public class ProbeTask implements ThreadTask {
	
	private final Probe myProbe;
	private final Probeable myParent;
	private boolean finished;
	
	public ProbeTask(Probeable parent, Probe probe){
		myProbe = probe;
		myParent = parent;
	}
	
	public void reset(boolean randomize) {
	}

	public Probeable getParent() {
		return myParent;
	}

	public boolean isFinished() {
		return finished;
	}

	public void run(float startTime, float endTime) throws SimulationException {
		myProbe.collect(endTime);
	}
	
   @Override
    public ProbeTask clone() throws CloneNotSupportedException {
        return (ProbeTask) super.clone();
    }
}
