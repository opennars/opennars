/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SynapticIntegrator.java". Description:
"Model of synaptic integration in a dendritic tree and soma.

  The model receives input from external sources (normally other neurons)
  and produces a net current which can be fed into a SpikeGenerator
  and/or can produce other outputs of a Neuron"

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
 * Created on May 4, 2006
 */
package ca.nengo.neural.neuron;

import ca.nengo.model.Node;
import ca.nengo.model.Resettable;
import ca.nengo.model.StructuralException;
import ca.nengo.model.NTarget;
import ca.nengo.util.TimeSeries1D;

import java.io.Serializable;

/**
 * <p>Model of synaptic integration in a dendritic tree and soma.</p>
 *
 * <p>The model receives input from external sources (normally other neurons)
 * and produces a net current which can be fed into a <code>SpikeGenerator</code>
 * and/or can produce other outputs of a Neuron.
 *
 * @author Bryan Tripp
 */
public interface SynapticIntegrator extends Resettable, Serializable, Cloneable {

	/**
	 * @return List of distinct inputs (eg sets of synapses from different ensembles).
	 */
	public NTarget[] getTerminations();

	/**
	 * @param name Name of a Termination onto this SynapticIntegrator
	 * @return The named Termination if it exists
	 * @throws StructuralException if the named Termination does not exist
	 */
	public NTarget getTermination(String name);

	/**
	 * This method should be called by the neuron that incorporates this SynapticIntegrator
	 * (Terminations need a reference to this).
 	 *
	 * @param node The node to which the SynapticIntegrator belongs
	 */
	public void setNode(Node node);

	/**
	 * <p>Runs the model for a given time interval. Input to each Termination
	 * should be set prior to calling this method, and is held constant during a run.</p>
	 *
	 * <p>The model is responsible for maintaining its internal state, and the
	 * state is assumed to be consistent with the start time. That is, if a caller
	 * calls run(0, 1, ...) and then run(5, 6, ...), the results may not
	 * make any sense, but this is not the model's responsibility. Start
	 * and end times are provided to support explicitly time-varying models,
	 * and for the convenience of Probeable models.</p>
	 *
	 * <p>Note that a run(...) is expected to cover a very short interval of time,
	 * e.g. 1/2 ms, during which inputs can be assumed to be constant. Normally
	 * a number of neurons in a network will run for this short length of time,
	 * possibly with diverse or varying internal time steps, and at the end of this
	 * time will communicate spikes to each other and then start again. </p>
	 *
	 * @param startTime Simulation time at which running starts (s)
	 * @param endTime Simulation time at which running ends (s)
	 * @return Time series of net current, including at least the start and end times, and
	 * 		optionally other times. Generally speaking additional values should be
	 * 		provided if the current varies substantially during the interval, but it is
	 * 		left to the implementation to interpret 'substantially'.
	 */
	public TimeSeries1D run(float startTime, float endTime);

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public SynapticIntegrator clone() throws CloneNotSupportedException;
}
