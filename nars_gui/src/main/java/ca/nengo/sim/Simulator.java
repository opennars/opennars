/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "Simulator.java". Description: 
"Runs simulations of a Network"

The Initial Developer of the Original Code is Bryan Tripp & Centre for Theoretical Neuroscience, University of Waterloo. Copyright (C) 2006-2008. All Rights Reserved.

Alternatively, the contents of this file may be used under the terms of the GNU 
Public License license (the GPL License), in which case the provisions of GPL 
License are applicable  instead of those above. If you wish to allow use of your 
version of this file only under the terms of the GPL License and not to allow 
others to use your version of this file under the MPL, indicate your decision 
by deleting the provisions above and replace  them with the notice and other 
provisions required by the GPL License.  If you do not delete the provisions above,
a recipient may use your version of this file under either the MPL or the GPL License.
*/

/*
 * Created on May 19, 2006
 */
package ca.nengo.sim;

import ca.nengo.model.Network;
import ca.nengo.model.Probeable;
import ca.nengo.model.SimulationException;
import ca.nengo.util.Probe;
import ca.nengo.util.VisiblyMutable;

/**
 * Runs simulations of a Network. 
 * 
 * @author Bryan Tripp
 */
public interface Simulator extends VisiblyMutable, Cloneable {

	/**
	 * Initializes the Simulator with a given Network, after which changes to the 
	 * Network MAY OR MAY NOT BE IGNORED. This is because the Simulator is free to 
	 * either run the given Neurons/Ensembles, or to make copies of them and run 
	 * the copies. (The latter is likely in a clustered implementation.) If you make
	 * changes to the Network after initializing a Simulator with it, initialize 
	 * again. If you want the Network to change somehow mid-simulation (e.g. you 
	 * want to remove some neurons from an Ensemble to test robustness), these 
	 * changes should be performed by the Ensembles or Neurons themselves, i.e. they 
	 * should be an explicit part of the model.   
	 * 
	 * @param network Network to set up for simulation
	 */
	public void initialize(Network network);
	
	/**
	 * Resets all Nodes in the simulated Network.
	 *  
	 * @param randomize True indicates reset to random initial condition (see 
	 * 		Resettable.reset(boolean)).
	 * @param saveWeights True indicates that the weights on
	 * 		LinearExponentialTerminations should be saved rather than reset
	 */
	public void resetNetwork(boolean randomize, boolean saveWeights);

	/**
	 * @param nodeName Name of a Probeable Node from which state is to be probed
	 * @param state The name of the state variable to probe
	 * @param record Probe retains history if true
	 * @return A Probe connected to the specified Node
	 * @throws SimulationException if the referenced Node can not be found, or is not Probeable, or does 
	 * 		not have the specified state variable
	 */
	public Probe addProbe(String nodeName, String state, boolean record) throws SimulationException; 

	/**
	 * @param ensembleName Name of Ensemble containing a Probeable Neuron from which state is to be probed 
	 * @param neuronIndex Index of the Neuron (from 0) within the specified Ensemble
	 * @param state The name of the state variable to probe
	 * @param record Probe retains history if true
	 * @return A Probe connected to the specified Neuron
	 * @throws SimulationException if the referenced Neuron can not be found, or is not Probeable, or does 
	 * 		not have the specified state variable
	 */
	public Probe addProbe(String ensembleName, int neuronIndex, String state, boolean record) throws SimulationException;
	

	/**
	 * @param ensembleName
	 *            Name of Ensemble the target belongs to. Null, if the target is
	 *            a top-level node
	 * @param target
	 *            Probeable target
	 * @param state
	 *            The name of the state variable to probe
	 * @param record
	 *            Probe retains history if true
	 * @return A Probe connected to the specified Neuron
	 * @throws SimulationException
	 *             if the referenced Neuron can not be found, or is not
	 *             Probeable, or does not have the specified state variable
	 */
	public Probe addProbe(String ensembleName, Probeable target, String state,
			boolean record) throws SimulationException;

	/**
	 * @param probe
	 *            Probe to be removed
	 * @throws SimulationException
	 *             if the referenced probe cannot be removed
	 */
	public void removeProbe(Probe probe) throws SimulationException;
	
	
	/**
	 * Resets all probes in the network, recursively including subnetworks.
	 */
	public void resetProbes();
	
	/**
	 * Runs the Network for the given time range. The states of all components of the 
	 * Network are assumed to be consistent with the given start time. So, you could 
	 * reset to the t=0 state, and then immediately start running from t=100, but the 
	 * results may not make sense. 
	 * 
	 * @param startTime Simulation time at which running starts
	 * @param endTime Simulation time at which running stops
	 * @param stepSize Length of time step at which the Network is run. This determines the 
	 * 		frequency with which outputs are passed between Ensembles, but individual 
	 * 		Neurons may run with different and/or variable time steps.
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime, float stepSize) throws SimulationException;
	
	/**
	 * Runs the Network for the given time range. The states of all components of the 
	 * Network are assumed to be consistent with the given start time. So, you could 
	 * reset to the t=0 state, and then immediately start running from t=100, but the 
	 * results may not make sense. 
	 * 
	 * @param startTime Simulation time at which running starts
	 * @param endTime Simulation time at which running stops
	 * @param stepSize Length of time step at which the Network is run. This determines the 
	 * 		frequency with which outputs are passed between Ensembles, but individual 
	 * 		Neurons may run with different and/or variable time steps.
	 * @param topLevel true if the network being run is the top level network, false if it 
	 * 		is a subnetwork
	 * @throws SimulationException if a problem is encountered while trying to run
	 */
	public void run(float startTime, float endTime, float stepSize, boolean topLevel) throws SimulationException;
	
	/**
	 * @return List of Probes that have been added to this Simulator.  
	 */
	public Probe[] getProbes();
	
	/**
	 * @param listener A Simulator listener to be added
	 */
	public void addSimulatorListener(SimulatorListener listener);
	
	/**
	 * @param listener A Simulator listener to be removed
	 */
	public void removeSimulatorListener(SimulatorListener listener);
	
	/**
	 * @return An independent copy of the Simulator. The copy has the same type and parameters, 
	 * 		but doesn't reference any Network, contain any Probes, or have any SimulatorListeners  
	 * @throws CloneNotSupportedException
	 */
	public Simulator clone() throws CloneNotSupportedException;
}
