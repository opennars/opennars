package ca.nengo.util;

import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationException;

/**
 * Any task in a Network that can be run independently but belongs to a specific part of the Network.
 * Provides a way for objects in a network that normally run on one thread to run specific parts
 * in multiple threads (eg a Non-Decoded Termination adjusting the weight for every neuron)
 *
 * @author Jonathan Lai
 */
public interface ThreadTask extends Resettable, Cloneable {

    /**
     * @return If the task has finished running
     */
    public boolean isFinished();

    /**
	 * Runs the Task, updating the parent Node as needed
	 *
	 * @param startTime simulation time at which running starts (s)
	 * @param endTime simulation time at which running ends (s)
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
    public void run(float startTime, float endTime) throws SimulationException;

	/**
	 * @return An independent copy of the Task
	 * @throws CloneNotSupportedException if the superclass does not support cloning
	 */
    public ThreadTask clone() throws CloneNotSupportedException;
}
