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

import ca.nengo.model.Node;
import ca.nengo.model.PlasticNodeTarget;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
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
public class PESTarget extends ModulatedPlasticGroupTarget {

    private static final long serialVersionUID = 1L;

    protected float[] myGain;
    private float[][] myEncoders;
    private float[][] myScaledEncoders;

    private boolean myOja = false; // Apply Oja smoothing?

    /**
     * @param ensemble The ensemble this termination belongs to
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public PESTarget(NEFGroup ensemble, String name, PlasticNodeTarget[] nodeTerminations) throws StructuralException {
        super(ensemble, name, nodeTerminations);
        myEncoders = ensemble.getEncoders();
        myGain = new float[nodeTerminations.length];
        for (int i = 0; i < nodeTerminations.length; i++) {
            SpikingNeuron neuron = (SpikingNeuron) nodeTerminations[i].getNode();
            myGain[i] = neuron.getScale();
        }
        
        calcScaledEncoders();
    }

    /**
     * @return Name of Origin from which post-synaptic activity is drawn
     */
    public boolean getOja() {
        return myOja;
    }

    /**
     * @param oja Should this termination use Oja smoothing?
     */
    public void setOja(boolean oja) {
        myOja = oja;
    }
    
    /**
     * @param learningRate Learning rate of the termination
     */
    public void setLearningRate(float learningRate) {
    	super.setLearningRate(learningRate);
    	
    	calcScaledEncoders();
    }
    
    /**
     * Calculate encoders scaled by the learning rate and gain (to be used in the transform update).
     */
    private void calcScaledEncoders() {
    	myScaledEncoders = MU.prod(MU.diag(MU.prod(myGain, myLearningRate)), myEncoders);
    }

    /**
     * @see PlasticGroupTarget#updateTransform(float, int, int)
     */
    @Override
    public void updateTransform(float time, int start, int end) throws StructuralException {
        if (myModTermName == null || myOriginName == null) {
            throw new StructuralException("Origin name not set in PESTermination");
        }

        if (myFilteredInput == null) {
        	return;
        }
        
        float[][] delta = deltaOmega(start, end);
        modifyTransform(delta, false, start, end);
        
    }

    protected float[][] deltaOmega(int start, int end) {
    	float[][] encoders = MU.copy(myScaledEncoders, start, 0, end-start, -1);
//    	float[][] delta = MU.outerprod(MU.prod(encoders, myFilteredModInput), myFilteredInput);
    	float[] encodedError = MU.prod(encoders, myFilteredModInput);
    	float[][] delta = new float[end-start][myFilteredInput.length];
    	for(int i=0; i < delta.length; i++)
    		if(encodedError[i] != 0.0) {
    			delta[i] = MU.prod(myFilteredInput, encodedError[i]);
    		}
    	
    	
    	if(!myOja)
    		return delta;
    	else {
	    	float[][] transform = MU.copy(getTransform(), start, 0, end-start, -1);
	    	float[] output = MU.prod(MU.prodElementwise(myOutput,myOutput), myLearningRate);
	    	float[][] oja = MU.zero(delta.length, delta[0].length);
	    	for(int i=0; i < output.length; i++)
	    		oja = MU.sum(oja, MU.prod(transform, output[i]));
	    	
	    	return MU.difference(delta, oja);
    	}
    }
    
    @Override
    public PESTarget clone(Node node) throws CloneNotSupportedException {
        PESTarget result = (PESTarget)super.clone(node);
        result.myFilteredInput = (myFilteredInput != null) ? myFilteredInput.clone() : null;
//        result.myFilteredInput = null;
        result.myGain = myGain.clone();
        result.myEncoders = MU.clone(myEncoders);
        result.myScaledEncoders = MU.clone(myScaledEncoders);
        return result;
    }

}
