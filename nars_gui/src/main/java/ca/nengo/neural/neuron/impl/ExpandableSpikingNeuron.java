/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "ExpandableSpikingNeuron.java". Description:
"A SpikingNeuron with an ExpandableSynapticIntegrator."

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
 * Created on 15-Mar-07
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.model.ExpandableNode;
import ca.nengo.model.StructuralException;
import ca.nengo.model.NTarget;
import ca.nengo.neural.neuron.ExpandableSynapticIntegrator;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A SpikingNeuron with an ExpandableSynapticIntegrator.
 *
 * @author Bryan Tripp
 */
public class ExpandableSpikingNeuron extends SpikingNeuron implements ExpandableNode {

	private static final long serialVersionUID = 1L;
	private static final Logger ourLogger = LogManager.getLogger(ExpandableSpikingNeuron.class);

	private ExpandableSynapticIntegrator mySynapticIntegrator;

	/**
	 * Note: current = scale * (weighted sum of inputs at each termination) * (radial input) + bias.
	 *
	 * @param integrator SynapticIntegrator used to model dendritic/somatic integration of inputs
	 * 		to this Neuron <b>(must be Plastic)</b>
	 * @param generator SpikeGenerator used to model spike generation at the axon hillock of this
	 * 		Neuron
	 * @param scale A coefficient that scales summed input
	 * @param bias A bias current that models unaccounted-for inputs and/or intrinsic currents
	 * @param name A unique name for this neuron in the context of the Network or Ensemble to which
	 * 		it belongs
	 */
	public ExpandableSpikingNeuron(SynapticIntegrator integrator, SpikeGenerator generator, float scale, float bias, String name) {
		super(integrator, generator, scale, bias, name);

		if ( !(getIntegrator() instanceof ExpandableSynapticIntegrator) ) {
			ourLogger.warn("Given SynapticIntegrator is not an ExpandableSynapticIntegrator (expansion-related methods will fail");
		}

		mySynapticIntegrator = (ExpandableSynapticIntegrator) integrator;
	}

	/**
	 * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
	 */
    public NTarget addTarget(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException {
    	if ( !(mySynapticIntegrator instanceof ExpandableSynapticIntegrator) ) {
			throw new StructuralException("Underlying SynapticIntegrator is not expandable");
		}
		if (weights.length != 1) {
			throw new StructuralException("Weights matrix must have one row (has " + weights.length + ')');
		}

		fireVisibleChangeEvent();
		
		return mySynapticIntegrator.addTermination(name, weights[0], tauPSC, modulatory);
	}
    
    public NTarget addDelayedTermination(String name, float[][] weights, float tauPSC, float delay, boolean modulatory) throws StructuralException {
    	if ( !(mySynapticIntegrator instanceof LinearSynapticIntegrator) ) {
			throw new StructuralException("Underlying SynapticIntegrator is not a LinearSynapticIntegrator");
		}
		if (weights.length != 1) {
			throw new StructuralException("Weights matrix must have one row (has " + weights.length + ')');
		}

		fireVisibleChangeEvent();
		
		return ((LinearSynapticIntegrator)mySynapticIntegrator).addTermination(name, weights[0], tauPSC, delay, modulatory);
    }
    

	/**
	 * @see ca.nengo.model.ExpandableNode#getDimension()
	 */
    public int getDimension() {
		return 1;
	}

	/**
	 * @see ca.nengo.model.ExpandableNode#removeTarget(java.lang.String)
	 */
    public NTarget removeTarget(String name) throws StructuralException {
		if ( !(mySynapticIntegrator instanceof ExpandableSynapticIntegrator) ) {
			throw new StructuralException("Underlying SynapticIntegrator is not expandable");
		}

		fireVisibleChangeEvent();
		
		return mySynapticIntegrator.removeTermination(name);
	}

	/**
	 * @return SynapticIntegrator for this neuron
	 */
	public ExpandableSynapticIntegrator getSynapticIntegrator() {
		return mySynapticIntegrator;
	}
	
	public ExpandableSpikingNeuron clone() throws CloneNotSupportedException {
		ExpandableSpikingNeuron result = (ExpandableSpikingNeuron) super.clone();
		result.mySynapticIntegrator = mySynapticIntegrator.clone();
		return result;
	}

}
