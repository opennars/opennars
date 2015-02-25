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

import ca.nengo.model.InstantaneousOutput;
import ca.nengo.model.Node;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.model.StructuralException;
import ca.nengo.model.impl.LinearExponentialTarget;
import ca.nengo.neural.neuron.Neuron;

/**
 * <p>A PlasticTermination implementing a PlasticityRule that accepts spiking input.</p>
 *
 * <p>Spiking input must be dealt with in order to run learning rules in
 * a spiking SimulationMode. Spiking input is also the only way to simulate spike-timing-dependent
 * plasticity.</p>
 *
 * @author Bryan Tripp
 * @author Jonathan Lai
 */
public class STDPTarget extends PlasticGroupTarget {

    private static final long serialVersionUID = 1L;
    // Remember 2 spikes in the past, for triplet based learning rules
    private static final int HISTORY_LENGTH = 2;

    private float myLastTime = 0.0f;

    private float[][] myPreSpikeHistory;
    private float[][] myPostSpikeHistory;
    private boolean[] myPreSpiking;
    private boolean[] myPostSpiking;

    private float[] myPostTrace1;
    private float[] myPostTrace2;
    private float[] myPreTrace1;
    private float[] myPreTrace2;

    private final float myA2Minus = 6.6e-3f;
    private final float myA3Minus = 3.1e-3f;
    private final float myTauMinus = 33.7f;
    private final float myTauX = 101.0f;
    private final float myA2Plus = 8.8e-11f;
    private final float myA3Plus = 5.3e-2f;
    private final float myTauPlus = 16.8f;
    private final float myTauY = 125.0f;

    /**
     * @param node The parent Node
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public STDPTarget(Node node, String name, LinearExponentialTarget[] nodeTerminations) throws StructuralException {
        super(node, name, nodeTerminations);
        setOriginName(Neuron.AXON);
        int preLength = nodeTerminations[0].getDimensions();
        int postLength = nodeTerminations.length;

        myPostTrace1 = new float[postLength];
        myPostTrace2 = new float[postLength];
        myPreTrace1 = new float[preLength];
        myPreTrace2 = new float[preLength];
    }

    @Override
    public void setOriginState(String name, InstantaneousOutput state, float time) throws StructuralException {
        if (myOriginName == null) {
            throw new StructuralException("Origin name not set in STDPTermination");
        }
        if (!(state instanceof SpikeOutput)) {
            throw new StructuralException("Origin must be Spiking in STDPTermination");
        }

        if (!name.equals(myOriginName)) { return; }

        updateHistory(myPostSpiking, myPostSpikeHistory, (SpikeOutput)state, time);
    }

    private void updateInput(float time) throws StructuralException {
        InstantaneousOutput input = this.get();

        if (!(input instanceof SpikeOutput)) {
            throw new StructuralException("Termination must be Spiking in STDPTermination");
        }

        updateHistory(myPreSpiking, myPreSpikeHistory, (SpikeOutput)input, time);
    }


    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
    }

    private static void updateHistory(boolean[] spiking,
            float[][] spikeHistory, SpikeOutput state, float time) {
        if (spikeHistory[0].length != state.getDimension()) {
            throw new IllegalArgumentException("Expected activity of dimension "
                    + spikeHistory[0].length + ", got dimension " + state.getDimension());
        }

        boolean[] spikes = state.getValues();
        for (int i = 0; i < spikes.length; i++) {
            if (spikes[i]) {
                for (int j = HISTORY_LENGTH-1; j > 0; j--) {
                    spikeHistory[j][i] = spikeHistory[j-1][i];
                }
                spikeHistory[0][i] = time;
                spiking[i] = true;
            } else {
                spiking[i] = false;
            }
        }

    }

    public void updateTransform(float time, int start, int end)
            throws StructuralException {
        if (myLastTime < time) {
            myLastTime = time;
            this.updateInput(time);
        }

        // before dOmega
        for (int post_i = 0; post_i < myPostTrace1.length; post_i++) {
            if (myPostSpiking[post_i]) {
                myPostTrace1[post_i] += 1.0f;
            }
            myPostTrace1[post_i] -= myPostTrace1[post_i] / myTauMinus;
            if (myPostTrace1[post_i] < 0.0f) {myPostTrace1[post_i] = 0.0f;}
        }

        for (int pre_i = 0; pre_i < myPreTrace1.length; pre_i++) {
            if (myPreSpiking[pre_i]) {
                myPreTrace1[pre_i] += 1.0f;
            }
            myPreTrace1[pre_i] -= myPreTrace1[pre_i] / myTauPlus;
            if (myPreTrace1[pre_i] < 0.0f) {myPreTrace1[pre_i] = 0.0f;}
        }

        //dOmega
        float[][] transform = this.getTransform();

        for (int post_i = start; post_i < end; post_i++) {
            for (int pre_i = 0; pre_i < transform[post_i].length; pre_i++) {
                if (myPreSpiking[pre_i]) {
                    transform[post_i][pre_i] += preDeltaOmega(time - myPostSpikeHistory[0][post_i],
                            time - myPreSpikeHistory[1][pre_i], transform[post_i][pre_i], post_i, pre_i);
                }
                if (myPostSpiking[post_i]) {
                    transform[post_i][pre_i] += postDeltaOmega(time - myPostSpikeHistory[0][post_i],
                            time - myPreSpikeHistory[1][pre_i], transform[post_i][pre_i], post_i, pre_i);
                }
            }
        }

        // after dOmega
        for (int pre_i = 0; pre_i < myPreTrace2.length; pre_i++) {
            if (myPreSpiking[pre_i]) {
                myPreTrace2[pre_i] += 1.0f;
            }
            myPreTrace2[pre_i] -= myPreTrace2[pre_i] / myTauX;
            if (myPreTrace2[pre_i] < 0.0f) {myPreTrace2[pre_i] = 0.0f;}
        }

        for (int post_i = 0; post_i < myPostTrace2.length; post_i++) {
            if (myPostSpiking[post_i]) {
                myPostTrace2[post_i] += 1.0f;
            }
            myPostTrace2[post_i] -= myPostTrace2[post_i] / myTauY;
            if (myPostTrace2[post_i] < 0.0f) {myPostTrace2[post_i] = 0.0f;}
        }


    }

    private float preDeltaOmega(float timeSinceDifferent, float timeSinceSame,
            float currentWeight, int postIndex, int preIndex) {
        float result = myPostTrace1[postIndex] * (myA2Minus + myPreTrace2[preIndex] * myA3Minus);

        return myLearningRate * result;


    }

    private float postDeltaOmega(float timeSinceDifferent, float timeSinceSame,
            float currentWeight, int postIndex, int preIndex) {
        float result = myPreTrace1[preIndex] * (myA2Plus + myPostTrace2[postIndex] * myA3Plus);

        return -1 * myLearningRate * result;

    }


    @Override
    public PlasticGroupTarget clone() throws CloneNotSupportedException {
        STDPTarget result = (STDPTarget) super.clone();
        result.myPostSpikeHistory = myPostSpikeHistory.clone();
        result.myPostSpiking = myPostSpiking.clone();
        return result;
    }
}
