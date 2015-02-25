/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "RealPlasticityRule.java". Description:
"A basic implementation of PlasticityRule for real valued input"

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
 * Created on 30-Jan-2007
 */
package ca.nengo.neural.plasticity.impl;

import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.Node;
import ca.nengo.model.PlasticNodeTarget;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.util.MU;

/**
 * A termination whose transformation evolves according to the PES rule.
 *
 * The learning rate is defined by an AbstractRealLearningFunction (see its declaration for
 * the inputs it receives). This learning rate function is applied to each In each case, the presynaptic-variable
 * input to the function is the corresponding dimension of input to the Termination. The postsynaptic variable is taken
 * as the corresponding dimension of the Origin NEFEnsemble.X. This implementation supports only a single separate
 * modulatory variable, though it can be multi-dimensional. This is also user-defined, as some other Termination
 * onto the same NEFEnsemble.
 *
 * TODO: test
 *
 * @author Bryan Tripp
 * @author Jonathan Lai
 * @author Trevor Bekolay
 */
public class hPESTarget extends PESTarget {

    private static final long serialVersionUID = 1L;
    
    private static final float THETA_TAU = 20.0f;  // tau for theta filtering
    // Attempt to make BCM the same order of magnitude as PES
    private static final float SCALING_FACTOR = 20000.0f;
    private float[] myInitialTheta;
    private float[] myTheta;
    
    private float mySupervisionRatio = 0.5f;

    /**
     * @param ensemble The ensemble this termination belongs to
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public hPESTarget(NEFGroup ensemble, String name, PlasticNodeTarget[] nodeTerminations, float[] initialTheta) throws StructuralException {
        super(ensemble, name, nodeTerminations);
        setOriginName(Neuron.AXON);
        
        // If initial theta not passed in, randomly generate
        if (initialTheta == null) {
            IndicatorPDF uniform = new IndicatorPDF(0.00005f, 0.00015f);
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
    
    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
    	super.reset(randomize);
        System.arraycopy(myInitialTheta, 0, myTheta, 0, myTheta.length);
    }

    /**
	 * @return How heavily weighted towards supervision
	 */
	public float getSupervisionRatio() {
		return mySupervisionRatio;
	}

	/**
	 * @param mySupervisionRatio How heavily weighted towards supervision;
	 *   between 0.0 (all unsupervised) and 1.0 (all supervised).
	 */
	public void setSupervisionRatio(float supervisionRatio) {
		mySupervisionRatio = supervisionRatio;
	}

    /**
     * @see PlasticGroupTarget#updateTransform(float, int, int)
     */
    @Override
    public void updateTransform(float time, int start, int end) throws StructuralException {
        if (myModTermName == null || myOriginName == null) {
            throw new StructuralException("Origin name not set in hPESTermination");
        }

        if (myFilteredInput == null || myFilteredOutput == null) {
        	return;
        }

        //update omega
        float[][] delta = deltaOmega(start, end);
        modifyTransform(delta, false, start, end);
        
        // update theta based on theta's time constant
        final float decay = (float) Math.exp(-0.001f / THETA_TAU);
        final float update = 1.0f - decay;
        for (int i = start; i < end; i++) {
            myTheta[i] *= decay;
            myTheta[i] += myFilteredOutput[i] * update;
        }
    }

    protected float[][] deltaOmega(int start, int end) {
    	float[][] supervised = super.deltaOmega(start, end);
    	if(mySupervisionRatio >= 1.0)
    		return supervised;
    	
    	float[][] unsupervised = new float[supervised.length][supervised[0].length];
    	for(int postIx=start; postIx < end; postIx++) //this should be able to be matrix-ified as well
    		for(int preIx=0; preIx < unsupervised[postIx-start].length; preIx++) {
    			unsupervised[postIx-start][preIx] = myFilteredInput[preIx] * myFilteredOutput[postIx] * 
    			(myFilteredOutput[postIx] - myTheta[postIx]) * myGain[postIx] * myLearningRate * SCALING_FACTOR;
    		}
    	
    	return MU.sum(MU.prod(supervised,mySupervisionRatio), MU.prod(unsupervised, 1-mySupervisionRatio));
    	
    }
    
    @Override
    public hPESTarget clone(Node node) throws CloneNotSupportedException {
    	throw new CloneNotSupportedException("hPESTermination not cloneable yet.");
    }

}