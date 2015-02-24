package ca.nengo.util.impl;

import ca.nengo.model.Node;
import ca.nengo.model.Projection;
import ca.nengo.model.SimulationException;
import ca.nengo.util.ThreadTask;

/**
 * A thread which uses an NEFGPUInterface to run GPU nodes and projections.
 *
 * @author Eric Crawford
 */
public class GPUThread extends NodeThread {

	final NEFGPUInterface myNEFGPUInterface;
	
	public GPUThread(NodeThreadPool nodePool, boolean interactive) {
		super(nodePool, new Node[0], 0, -1, new Projection[0], 0, -1, new ThreadTask[0], 0, -1);
		
		// create NEFGPUInterface from nodes and projections.
		// have to have some way to communicate which nodes and projections it decides are going to run on the GPU
		// so that the rest of the threads can run the remaining nodes and projections
		myNEFGPUInterface = new NEFGPUInterface(interactive);
	}
	
	public GPUThread(NodeThreadPool nodePool) {
		this(nodePool, false);
	}

	protected void runNodes(float startTime, float endTime) throws SimulationException{
		
		myNEFGPUInterface.step(startTime, endTime);
	}
	
	public NEFGPUInterface getNEFGPUInterface(){
		return myNEFGPUInterface;
	}
	
	protected void kill(){
		super.kill();
		myNEFGPUInterface.kill();
	}
}
