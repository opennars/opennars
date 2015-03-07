/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "EnsembleTermination.java". Description:
"A Termination that is composed of Terminations onto multiple Nodes"

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
 * Created on 31-May-2006
 */
package ca.nengo.neural.plasticity.impl;

import ca.nengo.model.*;
import ca.nengo.model.impl.GroupTarget;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.nef.NEFGroup;

import java.util.Arrays;

/**
 * <p>A Termination that is composed of Terminations onto multiple Nodes.
 * The dimensions of the Terminations onto each Node must be the same.</p>
 *
 * <p>Physiologically, this might correspond to a set of n axons passing into
 * a neuron pool. Each neuron in the pool receives synaptic connections
 * from as many as n of these axons (zero weight is equivalent to no
 * connection). Sometimes we deal with this set of axons only in terms
 * of the branches they send to one specific Neuron (a Node-level Termination)
 * but here we deal with all branches (an Ensemble-level Termination).
 * In either case the spikes transmitted by the axons are the same.</p>
 *
 * TODO: test
 *
 * @author Trevor Bekolay
 * @author Jonathan Lai
 */
public abstract class PlasticGroupTarget extends GroupTarget {

    private static final long serialVersionUID = 1L;

    protected float myLearningRate = 5e-7f;
    private float myLastTime = 0.0f;
    protected boolean myLearning = true;
    protected String myOriginName;
    protected float[] myOutput;
    protected float[] myFilteredOutput;
    protected float[] myInput;
    protected float[] myFilteredInput;

    /**
     * @param node The parent Node
     * @param name Name of this Termination
     * @param nodeTerminations Node-level Terminations that make up this Termination. Must be
     *        all LinearExponentialTerminations
     * @throws StructuralException If dimensions of different terminations are not all the same
     */
    public PlasticGroupTarget(Node node, String name, PlasticNodeTarget[] nodeTerminations) throws StructuralException {
        super(node, name, nodeTerminations);
        setOriginName(NEFGroup.X); // Start with the X origin by default
        saveTransform();
    }

    /**
     * @return Name of Origin from which postsynaptic activity is drawn
     */
    public String getOriginName() {
        return myOriginName;
    }

    /**
     * @param originName Name of Origin from which postsynaptic activity is drawn
     */
    public void setOriginName(String originName) {
        myOriginName = originName;
    }

    /**
     * @param name Name of Origin from which postsynaptic activity is drawn
     * @param state State of named origin
     * @param time Current time
     * @throws StructuralException if Origin is not set
     *
     */
    public void setOriginState(String name, InstantaneousOutput state, float time) throws StructuralException {
        if (myOriginName == null) {
            throw new StructuralException("Origin name not set in PESTermination");
        }

        if (!name.equals(myOriginName)) { return; }

        if (myOutput == null) {
            myOutput = new float[state.getDimension()];
        }
        float integrationTime = 0.001f;
        updateRaw(myOutput, state, integrationTime);

        float tauPSC = getNodeTerminations()[0].getTau();
        if (myFilteredOutput == null) {
            myFilteredOutput = new float[state.getDimension()];
        }
        updateFiltered(myOutput, myFilteredOutput, tauPSC, integrationTime);
    }

    public void setTerminationState(float time) throws StructuralException {
        if (myLastTime >= time) { return; }

        InstantaneousOutput state = this.get();
        if (state == null) {
            if (myInput != null) {
                Arrays.fill(myInput, 0.0f);
            }
            if (myFilteredInput != null) {
                Arrays.fill(myFilteredInput, 0.0f);
            }
            return;
        }

        float integrationTime = 0.001f;
        if (myInput == null) {
            myInput = new float[state.getDimension()];
        }
        updateRaw(myInput, state, integrationTime);

        float tauPSC = getNodeTerminations()[0].getTau();
        if (myFilteredInput == null) {
            myFilteredInput = new float[state.getDimension()];
        }
        updateFiltered(myInput, myFilteredInput, tauPSC, integrationTime);
        myLastTime = time;
    }

    protected static void updateRaw(final float[] raw, final InstantaneousOutput state, final float integrationTime) throws StructuralException {
        if (state instanceof RealSource) {
            float[] values = ((RealSource) state).getValues();
            if (values.length != raw.length) {
                throw new StructuralException("State is length "
                        + values.length + "; should be " + raw.length);
            }
            System.arraycopy(values, 0, raw, 0, values.length);
        } else if (state instanceof SpikeOutput) {
            boolean[] values = ((SpikeOutput) state).getValues();
            if (values.length != raw.length) {
                throw new StructuralException("State is length "
                        + values.length + "; should be " + raw.length);
            }
            for (int i = 0; i < values.length; i++) {
                raw[i] = values[i] ? integrationTime : 0.0f;
            }
        } else {
            System.err.println("State not real or spiking.");
        }
    }

    protected static void updateFiltered(final float[] raw, final float[] filtered, final float tauPSC, final float integrationTime) {
        final float decay = (float) Math.exp(-integrationTime / tauPSC);
        final float update = 1.0f - decay;
        for (int i = 0; i < raw.length; i++) {
            filtered[i] *= decay;
            filtered[i] += raw[i] * update;
        }
    }

    /**
     * @return The transformation matrix, which is made up of the
     *   weight vectors for each of the PlasticNodeTerminations within.
     *   This can be thought of as the connection weight matrix in most cases.
     */
    public float[][] getTransform() {
        NTarget[] terms = this.getNodeTerminations();
        float[][] transform = new float[terms.length][];
        for (int postIx = 0; postIx < terms.length; postIx++) {
            PlasticNodeTarget pnt = (PlasticNodeTarget) terms[postIx];
            transform[postIx] = pnt.getWeights();
        }

        return transform;
    }

    /**
     * @param transform The transformation matrix, which can be thought of as
     *   the connection weight matrix in most cases. This will be passed through
     *   to set the weight vectors on each PlasticNodeTermination within.
     */
    public void setTransform(float[][] transform, boolean save) {
        NTarget[] terms = this.getNodeTerminations();
        for(int postIx = 0; postIx < terms.length; postIx++) {
            PlasticNodeTarget pnt = (PlasticNodeTarget) terms[postIx];
            pnt.setWeights(transform[postIx], save);
        }
    }
    
    /**
     * Modifies the transformation weights in-place.
     * 
     * @param change The change in the transformation matrix
     * @param save Whether or not to save the new transformation matrix
     * @param start Row in transformation matrix to start modifications
     * @param end Row in transformation matrix to end modifications
     */
    public void modifyTransform(float[][] change, boolean save, int start, int end) {
        NTarget[] terms = this.getNodeTerminations();
        for(int postIx = start; postIx < end; postIx++) {
            PlasticNodeTarget pnt = (PlasticNodeTarget) terms[postIx];
            pnt.modifyWeights(change[postIx-start], save);
        }
    }

    /**
     * Saves the weights in the PlasticNodeTerminations within.
     */
    public void saveTransform() {
        NTarget[] terms = this.getNodeTerminations();
        for (NTarget term : terms) {
            ((PlasticNodeTarget) term).saveWeights();
        }
    }

    /**
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    @Override
    public void reset(boolean randomize) {
        super.reset(randomize);
        // super calls reset on each node, which should reset the weights that
        // were saved in saveTransform()
        myLearning = true;
        if (myOutput != null) {
            Arrays.fill(myOutput, 0.0f);
        }
        if (myFilteredOutput != null) {
            Arrays.fill(myFilteredOutput, 0.0f);
        }
        if (myInput != null) {
            Arrays.fill(myInput, 0.0f);
        }
        if (myFilteredInput != null) {
            Arrays.fill(myFilteredInput, 0.0f);
        }
        myLastTime = 0.0f;
    }

    /**
     * @param time Current time
     * @param start The start index of the range of transform values to update (for multithreading)
     * @param end The end index of the range of transform values to update (for multithreading)
     * @throws StructuralException if
     */
    public abstract void updateTransform(float time, int start, int end) throws StructuralException;

    /**
     * @see ca.nengo.model.impl.GroupTarget#get()
     */
    @Override
    public InstantaneousOutput get() {
        NTarget[] terms = this.getNodeTerminations();
        PlasticNodeTarget pnt = (PlasticNodeTarget) terms[0];

        return pnt.get();
    }

    /**
     * @return The output currents from the PlasticNodeTermination being wrapped
     */
    public float[] getOutputs() {
        NTarget[] terms = this.getNodeTerminations();
        float[] currents = new float[terms.length];
        for (int i = 0; i < terms.length; i++) {
            PlasticNodeTarget pnt = (PlasticNodeTarget) terms[i];
            currents[i] = pnt.getOutput();
        }

        return currents;
    }

    /**
     * @return Learning rate of the termination
     */
    public float getLearningRate() {
        return myLearningRate;
    }

    /**
     * @param learningRate Learning rate of the termination
     */
    public void setLearningRate(float learningRate)
    {
        myLearningRate = learningRate;
    }

    /**
     * @return Whether or not the termination is currently learning
     */
    public boolean getLearning() {
        return myLearning;
    }

    /**
     * @return Filtered output
     */
    public float[] getFilteredOutput() {
        return myFilteredOutput;
    }

    /**
     * @param learning Turn learning on or off for this termination
     */
    public void setLearning(boolean learning)
    {
        myLearning = learning;
    }

    @Override
    public PlasticGroupTarget clone(Node node) throws CloneNotSupportedException {
        PlasticGroupTarget result = (PlasticGroupTarget)super.clone(node);
        result.myOutput = (myOutput != null) ? myOutput.clone() : null;
        //    	result.myOutput = null;
        result.saveTransform();
        return result;
    }
}
