package ca.nengo.model.impl;

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.model.SimulationException;
import ca.nengo.model.Units;

import java.util.ArrayList;

/**
 * A LinearExponentialTermination where all inputs are delayed by a fixed number of
 * timesteps.
 * 
 * @author Daniel Rasmussen
 */
public class DelayedLinearExponentialTarget extends LinearExponentialTarget {
	
	private static final long serialVersionUID = 1L;
	
	private final int myDelay;
	private final ArrayList<InstantaneousOutput> myQueue;
	
	
	 /**
	  *  
	 * @param delay delay in timesteps between when input arrives at this termination and when it will be processed
	 * @see LinearExponentialTarget#LinearExponentialTarget(Node, String, float[], float)
	 */
	public DelayedLinearExponentialTarget(Node node, String name, float[] weights, float tauPSC, int delay) {
		super(node, name, weights, tauPSC);
		myDelay = delay;
		myQueue = new ArrayList<InstantaneousOutput>(myDelay*3);
		for(int i=0; i < myDelay; i++)
			myQueue.add(new RealOutputImpl(new float[weights.length], Units.UNK, 0.0f));
	}
	
	/**
	 * Adds a value to this termination's queue.  That value will not actually be
	 * processed until myDelay timesteps have called (we are assuming this function
	 * will be called once per timestep).
	 * 
	 * @see LinearExponentialTarget#apply(InstantaneousOutput)
	 */
	public void apply(InstantaneousOutput values) throws SimulationException {
		myQueue.add(values);
		InstantaneousOutput v = myQueue.remove(0);
		super.apply(v);
	}
}