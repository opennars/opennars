/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "LIFNeuronFactory.java". Description: 
"A factory for leaky-integrate-and-fire neurons"

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
 * Created on 21-Jun-2006
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.math.PDF;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A factory for leaky-integrate-and-fire neurons. 
 * 
 * @author Bryan Tripp
 */
public class LIFNeuronFactory implements NodeFactory {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(LIFNeuronFactory.class);
	
	private float myTauRC;
	private float myTauRef;
	private PDF myMaxRate;
	private PDF myIntercept;
	
	private static final float ourMaxTimeStep = .00025f;
	private static final Units ourCurrentUnits = Units.ACU;

	/**
	 * @param tauRC Spike generator membrane time constant (s)  
	 * @param tauRef Spike generator refractory time (s)
	 * @param maxRate Maximum firing rate distribution (spikes/s)  
	 * @param intercept Level of summed input at which spiking begins (arbitrary current units) 
	 */
	public LIFNeuronFactory(float tauRC, float tauRef, PDF maxRate, PDF intercept) {
		myTauRC = tauRC;
		myTauRef = tauRef;
		myMaxRate = maxRate;
		myIntercept = intercept;
	}
	
	/**
	 * Uses default parameters. 
	 */
	public LIFNeuronFactory() {
		this(.02f, .001f, new IndicatorPDF(200, 400), new IndicatorPDF(-.9f, .9f));
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
	 * @see ca.nengo.model.impl.NodeFactory#make(String)
	 */
	public Neuron make(String name) throws StructuralException {
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
		SpikeGenerator generator = new LIFSpikeGenerator(ourMaxTimeStep, myTauRC, myTauRef);
		
		return new ExpandableSpikingNeuron(integrator, generator, scale, bias, name);		
	}

	/**
	 * @see ca.nengo.model.impl.NodeFactory#getTypeDescription()
	 */
	public String getTypeDescription() {
		return "LIF Neuron";
	}

}
