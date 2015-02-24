/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SimulationMode.java". Description: 
"Something that has runs in different SimulationModes"

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

package ca.nengo.model;

/**
 * A SimulationMode is a way in which a Neuron or Ensemble can be simulated.  
 * Different modes trade off between performance and realism. All Neurons and 
 * Ensembles must be able to run in DEFAULT mode. For other modes, there is a 
 * chain of fallback choices so that if the requested mode is not supported, 
 * the Neuron/Ensemble must run in the closest mode that it does support (which
 * may be the DEFAULT mode).  
 * 
 * @author Bryan Tripp
 */
public enum SimulationMode {

	/**
	 * The normal level of detail at which a Neuron/Ensemble runs (all Neuron/Ensembles must support 
	 * this mode).  
	 */
	DEFAULT(null),
	
	/**
	 * A spiking mode in which some precision is sacrificed for improved performance. For 
	 * example, a conductance model in APPROXIMATE mode might continue to use the full model 
	 * for subthreshold operation, but switch to a stereotyped template for spike generation,  
	 * to avoid the shorter time steps that are typically needed to model spiking.     
	 */
	APPROXIMATE(DEFAULT),
	
	/**
	 * Outputs that spike by default are instead expressed in terms of firing rates. 
	 */
	RATE(APPROXIMATE),

	/**
	 * Outputs that spike by default are expressed as rates that are constant for a 
	 * given constant input. This mode is useful for fast approximate simulations and also for calculating 
	 * decoders (see NEFEnsemble). If a Neuron can not run in this mode, then in order to find decoders, 
	 * simulations must be performed to see how the Neuron responds to various inputs.
	 */
	CONSTANT_RATE(RATE),
	
	/**
	 * Neurons are not used. Ensembles process represented variables directly rather than  
	 * approximations based on neural activity.   
	 */
	DIRECT(RATE),
	
	/**
	 * Neurons are not used. The latent variables in population activity are simulated. 
	 * This is the same as DIRECT mode, except that the large-scale effects of the neurons  
	 * are also simulated by modelling noise and static distortion. This is meant as a computationally 
	 * inexpensive mode with network behaviour that is as similar as possible to a spiking simulation.  
	 */
	EXPRESS(DEFAULT), 
	
	/**
	 * A higher level of precision than DEFAULT. The default level should be accurate for most purposes, 
	 * but this higher level of accuracy can serve as a way to verify that numerical issues are
	 * not impacting results (eg error tolerance in a Runge-Kutta integration may be tightened beyond 
	 * what is deemed necessary). Another way to increase precision, independently of using PRECISE mode, is 
	 * to simulate with a shorter network time step. 
	 */
	PRECISE(DEFAULT);
	
	private final SimulationMode myFallbackMode;
	
	private SimulationMode(SimulationMode fallbackMode) {
		myFallbackMode = fallbackMode;
	}
	
	/**
	 * @return The fallback mode to use if this mode can not be supported.
	 */
	public SimulationMode getFallbackMode() {
		if (myFallbackMode == null) {
			if (this.equals(SimulationMode.DEFAULT)) {
				throw new RuntimeException("DEFAULT has no fallback mode (everything must support DEFAULT mode)"); 
			} else {
				throw new Error("No fallback is defined for this non-default mode. This is a bug.");
			}
		}
		
		return myFallbackMode;
	}

	/**
	 * A convenience method for finding the closest supported mode to a requested mode. 
	 * 
	 * @param requested The requested mode
	 * @param supported A list of supported modes
	 * @return The supported mode that is closest to the requested mode in the fallback chain
	 */
	public static SimulationMode getClosestMode(SimulationMode requested, SimulationMode[] supported) {
		SimulationMode closest = requested;
		
		while ( !isSupported(closest, supported) ) {	
			closest = closest.getFallbackMode();
		}
		
		return closest;
	}
	
	private static boolean isSupported(SimulationMode requested, SimulationMode[] supported) {
		boolean is = false;
		for (int i = 0; !is && i < supported.length; i++) {
			is = (requested.equals(supported[i]));
		}
		return is;
	}
	
	/**
	 * Something that has runs in different SimulationModes. 
	 * 
	 * @author Bryan Tripp
	 */
	public static interface ModeConfigurable {

		/**
		 * Sets the object to run in either the given mode or the closest mode that it supports 
		 * (all ModeConfigurables must support SimulationMode.DEFAULT, and must default to this mode).
		 * 
		 * @param mode SimulationMode in which it is desired that the object runs. 
		 */
		public void setMode(SimulationMode mode);

		/**
		 * @return The SimulationMode in which the object is running  
		 */
		public SimulationMode getMode();
		
	}
	
}
