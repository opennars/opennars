/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific 
language governing rights and limitations under the License.

The Original Code is "SpikePatternImpl.java". Description: 
"Default implementation of SpikePattern"

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
 * Created on 22-Jun-2006
 */
package ca.nengo.util.impl;

import ca.nengo.util.MU;
import ca.nengo.util.SpikePattern;

/**
 * Default implementation of SpikePattern. 
 * 
 * @author Bryan Tripp
 */
public class SpikePatternImpl implements SpikePattern {

	private static final long serialVersionUID = 1L;
	
	int[] myIndices;
	float[][] mySpikeTimes;
	
	/**
	 * @param neurons Number of neurons in the Ensemble that this SpikePattern belongs to
	 */
	public SpikePatternImpl(int neurons) {
		myIndices = new int[neurons];
		
		mySpikeTimes = new float[neurons][];		
		for (int i = 0; i < neurons; i++) {
			mySpikeTimes[i] = new float[100];
		}
	}
	
	/**
	 * @param neuron Index of neuron
	 * @param time Spike time
	 */
	public void addSpike(int neuron, float time) {
		if (myIndices[neuron] == mySpikeTimes[neuron].length) {
			mySpikeTimes[neuron] = expand(mySpikeTimes[neuron]);
		}
		
		mySpikeTimes[neuron][myIndices[neuron]++] = time;
	}

	/**
	 * @see ca.nengo.util.SpikePattern#getNumNeurons()
	 */
	public int getNumNeurons() {
		return myIndices.length;
	}

	/**
	 * @see ca.nengo.util.SpikePattern#getSpikeTimes(int)
	 */
	public float[] getSpikeTimes(int neuron) {
		return contract(mySpikeTimes[neuron], myIndices[neuron]);
	}
	
	private static float[] expand(float[] list) {
		float[] result = new float[Math.round((float) list.length * 1.5f)]; //grow by 50%
		System.arraycopy(list, 0, result, 0, list.length);
		return result;
	}
	
	private static float[] contract(float[] list, int index) {
		float[] result = new float[index];
		System.arraycopy(list, 0, result, 0, index);
		return result;
	}

	@Override
	public SpikePattern clone() throws CloneNotSupportedException {
		SpikePatternImpl result = (SpikePatternImpl) super.clone();
		result.myIndices = myIndices.clone();
		result.mySpikeTimes = MU.clone(mySpikeTimes);
		return result;
	}

}
