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

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.PlasticNodeTarget;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.neuron.impl.SpikingNeuron;

/**
 * A termination that learns only on presynaptic spikes.
 *
 * @author Trevor Bekolay
 */
public class PreLearnTarget extends ModulatedPlasticGroupTarget {

    private static final long serialVersionUID = 1L;

    private float myLastTime = 0.0f;
    private boolean[] myInSpiking;
    private final float[] myGain;
    private final float[][] myEncoders;

    /**
     * @param ensemble The ensemble this termination belongs to
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public PreLearnTarget(NEFGroup ensemble, String name, PlasticNodeTarget[] nodeTerminations) throws StructuralException {
        super(ensemble, name, nodeTerminations);
        myEncoders = ensemble.getEncoders();
        myGain = new float[nodeTerminations.length];
        for (int i = 0; i < nodeTerminations.length; i++) {
            SpikingNeuron neuron = (SpikingNeuron) nodeTerminations[i].getNode();
            myGain[i] = neuron.getScale();
        }
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
        super.reset(randomize);
        myLastTime = 0.0f;

        if (myInSpiking == null) { return; }
        for (int i=0; i < myInSpiking.length; i++) {
        	myInSpiking[i] = false;
        }
    }

    private void updateInput() {
        InstantaneousOutput input = this.get();
        myInSpiking = ((SpikeOutput) input).getValues();
    }

    /**
     * @see PlasticGroupTarget#updateTransform(float, int, int)
     */
    @Override
    public void updateTransform(float time, int start, int end) throws StructuralException {
        if (myModTermName == null || myOriginName == null) {
            throw new StructuralException("Origin name not set in PESTermination");
        }

        if (myLastTime < time) {
            this.updateInput();
            myLastTime = time;
        }

        float[][] transform = this.getTransform();

        for (int i = start; i < end; i++) {
            for (int j = 0; j < transform[i].length; j++) {
                float e = 0.0f;
                for (int k = 0; k < myModInput.length; k++) {
                    e += myModInput[k] * myEncoders[i][k];
                }

                if (myInSpiking[j]) {
                	transform[i][j] += deltaOmega(1.0f,time,transform[i][j],myGain[i],e);
                }
            }
        }

        this.setTransform(transform, false);
    }

    private float deltaOmega(float input, float time, float currentWeight, float gain, float e) {
        return myLearningRate * input * e * gain;
    }

    @Override
    public PreLearnTarget clone() throws CloneNotSupportedException {
        PreLearnTarget result = (PreLearnTarget) super.clone();
        return result;
    }
}
