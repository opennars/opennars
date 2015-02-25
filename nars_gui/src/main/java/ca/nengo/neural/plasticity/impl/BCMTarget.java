/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SpikePlasticityRule.java". Description:
"A PlasticityRule that accepts spiking input.

  Spiking input must be dealt with in order to run learning rules in
  a spiking SimulationMode"

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
 * Created on 28-May-07
 */
package ca.nengo.neural.plasticity.impl;

import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.Node;
import ca.nengo.model.PlasticNodeTarget;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.impl.SpikingNeuron;

/**
 * BCM rule
 *
 * @author Trevor Bekolay
 */
public class BCMTarget extends PlasticGroupTarget {

    private static final long serialVersionUID = 1L;
    
    private static final float THETA_TAU = 20.0f;  // tau for theta filtering
    // Attempt to make BCM the same order of magnitude as PES
    private static final float SCALING_FACTOR = 20000.0f;
    private float[] myInitialTheta;
    private float[] myTheta;
    private float[] myGain;

    public BCMTarget(Node node, String name, PlasticNodeTarget[] nodeTerminations, float[] initialTheta) throws StructuralException {
        super(node, name, nodeTerminations);
        setOriginName(Neuron.AXON);
        
        myGain = new float[nodeTerminations.length];
        for (int i = 0; i < nodeTerminations.length; i++) {
            SpikingNeuron neuron = (SpikingNeuron) nodeTerminations[i].getNode();
            myGain[i] = neuron.getScale();
        }
        
        // If initial theta not passed in, randomly generate
        if (initialTheta == null) {
        	IndicatorPDF uniform = new IndicatorPDF(0.00001f, 0.00002f);
        	myInitialTheta = new float[nodeTerminations.length];
        	for (int i = 0; i < myInitialTheta.length; i ++) {
        	    // Reasonable assumption: high gain, high theta
        		myInitialTheta[i] = uniform.sample()[0] * myGain[i];
        	}
        } else {
        	myInitialTheta = initialTheta;
        }
        myTheta = myInitialTheta.clone();
    }
    
    public float[] getTheta() {
    	return myTheta;
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
    	super.reset(randomize);
        System.arraycopy(myInitialTheta, 0, myTheta, 0, myTheta.length);
    }
    
    public void updateTransform(float time, int start, int end)
            throws StructuralException {
    	if (myOriginName == null) {
            throw new StructuralException("Origin name not set in BCMTermination");
        }
    	
    	if (myFilteredInput == null || myFilteredOutput == null) {
        	return;
        }
    	
    	// update omega
    	float[][] transform = this.getTransform();
        for (int postIx = start; postIx < end; postIx++) {
            for (int preIx = 0; preIx < transform[postIx].length; preIx++) {
                transform[postIx][preIx] += myFilteredInput[preIx] * myFilteredOutput[postIx]
                		* (myFilteredOutput[postIx] - myTheta[postIx])
                		* myGain[postIx] * myLearningRate * SCALING_FACTOR;
            }
        }
        this.setTransform(transform, false);
        
        // update theta based on theta's time constant
        final float decay = (float) Math.exp(-0.001f / THETA_TAU);
        final float update = 1.0f - decay;
        for (int i = start; i < end; i++) {
            myTheta[i] *= decay;
            myTheta[i] += myFilteredOutput[i] * update;
        }
    }

    @Override
    public PlasticGroupTarget clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException("BCMTermination not cloneable yet.");
    }
}