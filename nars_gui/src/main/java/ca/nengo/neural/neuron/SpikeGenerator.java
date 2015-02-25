/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SpikeGenerator.java". Description:
"Spike generation model, ie a component of a neuron model that receives driving current
  and generates spikes"

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

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Resettable;
import ca.nengo.model.SimulationMode;

import java.io.Serializable;

/**
 * Spike generation model, ie a component of a neuron model that receives driving current
 * and generates spikes.
 *
 * @author Bryan Tripp
 */
public interface SpikeGenerator extends Resettable, Serializable, SimulationMode.ModeConfigurable, Cloneable {

	/**
	 * Runs the model for a given time segment. The total time is meant to be
	 * short (eg 1/2ms), in that the output of the model is either a spike
	 * or no spike during this period of simulation time.
	 *
	 * <p>The model is responsible for maintaining its internal state, and the
	 * state is assumed to be consistent with the start time. That is, if a caller
	 * calls run({.001 .002}, ...) and then run({.501 .502}, ...), the results may
	 * not make any sense, but this is not the model's responsibility. Absolute
	 * times are provided to support explicitly time-varying models, and for the
	 * convenience of Probeable models.</p>
	 *
	 * @param time Array of points in time at which input current is defined. This includes
	 * 		at least the start and end times, and possibly intermediate times. (The SpikeGenerator
	 * 		model can use its own time step -- these times are only used to define the input.)
	 * @param current Driving current at each given point in time (assumed to be constant
	 * 		until next time point)
	 * @return true If there is a spike between the first and last times, false otherwise
	 */
	public InstantaneousOutput run(float[] time, float[] current);

	/**
	 * @return Valid clone
	 * @throws CloneNotSupportedException if clone can't be made
	 */
	public SpikeGenerator clone() throws CloneNotSupportedException;

}
