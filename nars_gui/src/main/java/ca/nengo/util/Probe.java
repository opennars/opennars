/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "Probe.java". Description: 
"Reads state variables from Probeable objects (eg membrane potential from a Neuron).
  Collected data can be displayed during a simluation or kept for plotting afterwards"

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
package ca.nengo.util;

import ca.nengo.model.Probeable;
import ca.nengo.model.SimulationException;
import ca.nengo.util.impl.ProbeTask;

/**
 * Reads state variables from Probeable objects (eg membrane potential from a Neuron).
 * Collected data can be displayed during a simluation or kept for plotting afterwards.   
 * 
 * @author Bryan Tripp
 */
//TODO implement as a Source
public interface Probe<K> {

	/**
	 * @param ensembleName
	 *            Name of the Ensemble the target object belongs to. Null, if
	 *            the target is a top-level node.
	 * @param target
	 *            The object about which state history is to be collected
	 * @param stateName
	 *            The name of the state variable to collect
	 * @param record
	 *            If true, getData() returns history since last connect() or
	 *            reset(), otherwise getData() returns most recent sample
	 * @throws SimulationException
	 *             if the given target does not have the given state
	 */
	public void connect(K ensembleName, Probeable target,
			String stateName, boolean record) throws SimulationException;

	/**
	 * @param target
	 *            The object about which state history is to be collected
	 * @param stateName
	 *            The name of the state variable to collect
	 * @param record
	 *            If true, getData() returns history since last connect() or
	 *            reset(), otherwise getData() returns most recent sample
	 * @throws SimulationException
	 *             if the given target does not have the given state
	 */
	public void connect(Probeable target, String stateName, boolean record) throws SimulationException;
	
	/**
	 * Clears collected data. 
	 */
	public void reset();
	
	/**
	 * Processes new data. To be called after every Network time step. 
	 */
	public void collect(float time);	
	
	/**
	 * @param rate Rate in samples per second. The default is one sample per network time step, and it is 
	 * 		not possible to sample faster than this (specifying a higher sampling rate has no effect).   
	 */
	public void setSamplingRate(float rate);

	/**
	 * @return All collected data since last reset()
	 */
	public TimeSeries getData();

	/**
	 * @return The object about which state history is to be collected
	 */
	public Probeable getTarget();

	/**
	 * @return The name of the state variable to collect
	 */
	public String getStateName();

	/**
	 * @return Whether the target the node is attached to is inside an Ensemble
	 */
	public boolean isInEnsemble();

	/**
	 * @return The name of the Ensemble the target the Probe is attached to is
	 *         in. Null if it's not in one
	 */
	public String getEnsembleName();
	
	/**
	 * @return The probe task that is runs this probe. 
	 */
	public ProbeTask getProbeTask();
}
