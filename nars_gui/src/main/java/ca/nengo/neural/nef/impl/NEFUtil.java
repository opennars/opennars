/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "NEFUtil.java". Description: 
"Utility methods for related to Neural Engineering Framework"

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
 * Created on 12-Mar-08
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.model.RealSource;
import ca.nengo.model.SimulationException;
import ca.nengo.model.SimulationMode;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFNode;
import ca.nengo.util.MU;

/**
 * Utility methods for related to Neural Engineering Framework. 
 * 
 * @author Bryan Tripp
 */
public class NEFUtil {
	
	/**
	 * Calculates an input-output mapping for an ensemble. 
	 * 
	 * @param origin The origin from which to take the output (must belong to an NEFEnsemble)
	 * @param input Set of inputs directly into the ensemble (not through termination mapping/dynamics) 
	 * @param mode SimulationMode in which to calculate the mapping. If DIRECT or CONSTANT_RATE, each input 
	 * 		is treated separately and causes an independent output. Otherwise inputs are applied at 1ms time 
	 * 		steps in a simulation, and neuron states are maintained across steps.  
	 * @return Outputs from the given Origin for given inputs
	 */
	public static float[][] getOutput(DecodedSource origin, float[][] input, SimulationMode mode) {
		float[][] output = null;
		float dt = .001f;
		
		try {
			if ( !(origin.getNode() instanceof NEFGroup) ) {
				throw new RuntimeException("This calculation can only be performed with origins that belong to NEFEnsembles.");
			}
			
			NEFGroup ensemble = (NEFGroup) origin.getNode();
			float[][] encoders = ensemble.getEncoders();

			output = new float[input.length][];

			NEFNode[] nodes = (NEFNode[]) ensemble.getNodes();
			
			synchronized (ensemble){
				SimulationMode oldMode = ensemble.getMode();			
				ensemble.setMode(mode);
				
				for (int i = 0; i < input.length; i++) {
					if (mode.equals(SimulationMode.DIRECT) || mode.equals(SimulationMode.EXPRESS)) {
						origin.run(input[i], 0f, 1f);
						output[i] = ((RealSource) origin.get()).getValues();
					} else {
						for (int j = 0; j < nodes.length; j++) {
							float radialInput = 0;
							if (ensemble instanceof NEFGroupImpl) {
								NEFGroupImpl impl = (NEFGroupImpl) ensemble;
								radialInput = impl.getRadialInput(input[i], j);
							} else {
								radialInput = MU.prod(input[i], encoders[j]);
							}
							nodes[j].setRadialInput(radialInput);
							if (mode.equals(SimulationMode.CONSTANT_RATE)) {
								nodes[j].run(0f, 0f);
							} else {
								nodes[j].run((float) i * dt, (float) (i+1) * dt);
							}
						}
						origin.run(null, (float) i * dt, (float) (i+1) * dt);
						output[i] = ((RealSource) origin.get()).getValues();
					}				
				}
				ensemble.setMode(oldMode);
			}
			
		} catch (SimulationException e) {
			throw new RuntimeException("Can't plot origin error", e);
		}
		return output;
	}
}
