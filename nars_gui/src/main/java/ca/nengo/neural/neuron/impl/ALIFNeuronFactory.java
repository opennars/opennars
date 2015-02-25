/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "ALIFNeuronFactory.java". Description: 
"A factory for adapting leaky integrate-and-fire neurons"

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
 * Created on 12-May-07
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.Node;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A factory for adapting leaky integrate-and-fire neurons. 
 * 
 * @author Bryan Tripp
 */
public class ALIFNeuronFactory implements NodeFactory {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(ALIFNeuronFactory.class);
	
	private static final float ourMaxTimeStep = 0.001f;
	private static final Units ourCurrentUnits = Units.ACU;
	
	private PDF myMaxRate;
	private PDF myIntercept;
	private PDF myIncN;
	private float myTauRef;
	private float myTauRC;
	private float myTauN;
	
	/**
	 * @param maxRate Maximum firing rate distribution (spikes/s)  
	 * @param intercept Level of summed input at which spiking begins (arbitrary current units) 
	 * @param incN Increment of adaptation-related ion concentration with each spike (arbitrary units)
	 * @param tauRef Spike generator refractory time (s)
	 * @param tauRC Spike generator membrane time constant (s)  
	 * @param tauN Time constant of adaptation-related ion decay (s)
	 */
	public ALIFNeuronFactory(PDF maxRate, PDF intercept, PDF incN, float tauRef, float tauRC, float tauN) {
		myMaxRate = maxRate;
		myIntercept = intercept;
		myIncN = incN;
		myTauRef = tauRef;
		myTauRC = tauRC;
		myTauN = tauN;
	}
	
	/**
	 * Uses default parameters. 
	 */
	public ALIFNeuronFactory() {
		this(new IndicatorPDF(200, 400), new IndicatorPDF(-.9f, .9f), new IndicatorPDF(.1f, .2f), .001f, .02f, .2f);
	}

	/**
	 * @return Maximum firing rate distribution (spikes/s)
	 */
	public PDF getMaxRate() {
		return myMaxRate;
	}
	
	/**
	 * @param maxRate Maximum firing rate distribution (spikes/s)
	 */
	public void setMaxRate(PDF maxRate) {
		myMaxRate = maxRate;
	}
	
	/**
	 * @return Level of summed input at which spiking begins (arbitrary current units)
	 */
	public PDF getIntercept() {
		return myIntercept;
	}
	
	/**
	 * @param intercept Level of summed input at which spiking begins (arbitrary current units)
	 */
	public void setIntercept(PDF intercept) {
		myIntercept = intercept;
	}

	/**
	 * @return Increment of adaptation-related ion concentration with each spike (arbitrary units) 
	 */
	public PDF getIncN() {
		return myIncN;
	}
	
	/**
	 * @param incN Increment of adaptation-related ion concentration with each spike (arbitrary units)
	 */
	public void setIncN(PDF incN) {
		myIncN = incN;
	}
	
	/**
	 * @return Spike generator refractory time (s)
	 */
	public float getTauRef() {
		return myTauRef;
	}
	
	/**
	 * @param tauRef Spike generator refractory time (s) 
	 */
	public void setTauRef(float tauRef) {
		myTauRef = tauRef;
	}

	/**
	 * @return Spike generator membrane time constant (s)  
	 */
	public float getTauRC() {
		return myTauRC;
	}

	/**
	 * @param tauRC Spike generator membrane time constant (s)  
	 */
	public void setTauRC(float tauRC) {
		myTauRC = tauRC;
	}

	/**
	 * @return Time constant of adaptation-related ion decay (s)
	 */
	public float getTauN() {
		return myTauN;
	}
	
	/**
	 * @param tauN Time constant of adaptation-related ion decay (s)
	 */
	public void setTauN(float tauN) {
		myTauN = tauN;
	}

	/**
	 * @see ca.nengo.model.impl.NodeFactory#make(java.lang.String)
	 */
	public Node make(String name) throws StructuralException {
		float maxRate = myMaxRate.sample()[0];		
		float intercept = myIntercept.sample()[0];
		
		if (maxRate < 0) {
			throw new StructuralException("Max firing rate must be > 0");
		}
		if (maxRate > 1f / myTauRef) {
			ourLogger.warn("Decreasing maximum firing rate which was greater than inverse of refractory period");
			maxRate = (1f / myTauRef) - .001f;
		}
		
		float x = 1f / (1f - (float) Math.exp( (myTauRef - (1f / maxRate)) / myTauRC));
		float scale = (x - 1f) / (1f - intercept);
		
		float bias = 1f - scale * intercept;
		
		SynapticIntegrator integrator = new LinearSynapticIntegrator(ourMaxTimeStep, ourCurrentUnits);
		SpikeGenerator generator = new ALIFSpikeGenerator(myTauRef, myTauRC, myTauN, myIncN.sample()[0]);
		
		return new ExpandableSpikingNeuron(integrator, generator, scale, bias, name);		
	}

	/**
	 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
	 */
	public String getTypeDescription() {
		return "Adapting LIF Neuron";
	}

}
