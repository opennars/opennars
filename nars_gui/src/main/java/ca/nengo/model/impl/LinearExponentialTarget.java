/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LinearExponentialTermination.java". Description:
"A Termination at which incoming spikes induce exponentially decaying post-synaptic
  currents that are combined linearly"

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

package ca.nengo.model.impl;

import ca.nengo.model.*;
import ca.nengo.neural.PreciseSpikeOutput;
import ca.nengo.neural.SpikeOutput;

import java.util.Random;

/**
 * <p>A Termination at which incoming spikes induce exponentially decaying post-synaptic
 * currents that are combined linearly. Real-valued spike rate inputs have approximately
 * the same effect over time as actual (boolean) spike inputs at the same rate.</p>
 *
 * <p>Each input is weighted (weights specified in the constructor) so that the time integral
 * of the post-synaptic current arising from one spike equals the weight. The time integral
 * of post-synaptic current arising from real-valued input of 1 over a period of 1s also
 * equals the weight. This means that spike input and spike-rate input have roughly the
 * same effects.</p>
 *
 * @author Bryan Tripp
 */
public class LinearExponentialTarget implements PlasticNodeTarget {

    private static final long serialVersionUID = 1L;

    private Node myNode;
    private final String myName;
    private float myTauPSC;
    private boolean myModulatory;

    private float[] myInitialWeights;
    private float[] myWeights;
    private float[] myWeightProbabilities;
    private Random random;

    private float myCurrent = 0;
    private float myNetSpikeInput;
    private float myNetRealInput;
    private float[] myPreciseSpikeInputTimes;
    private float myIntegrationTime; // for keeping track of how far into the integration we are, so
    // we know which precise spikes have and have not been dealt with
    private InstantaneousOutput myRawInput;

    /**
     * @param node The parent Node
     * @param name Name of the Termination (must be unique within the Neuron or Ensemble to
     * 		which it is attached)
     * @param weights Ordered list of synaptic weights of each input channel
     * @param tauPSC Time constant of exponential post-synaptic current decay
     */
    public LinearExponentialTarget(Node node, String name, float[] weights, float tauPSC) {
        myNode = node;
        myName = name;
        myWeights = weights;
        saveWeights();
        myTauPSC = tauPSC;
        myModulatory = false;
    }

    /**
     * Resets current to 0 (randomize arg is ignored).
     *
     * @see ca.nengo.model.Resettable#reset(boolean)
     */
    public void reset(boolean randomize) {
        myCurrent = 0;
        myRawInput = null;
        myNetRealInput = 0;
        myNetSpikeInput = 0;
        myPreciseSpikeInputTimes=null;
        myIntegrationTime = 0;
        myWeights = myInitialWeights.clone();
    }

    public void saveWeights() {
        myInitialWeights = myWeights.clone();
    }

    /**
     * @see ca.nengo.model.NTarget#getName()
     */
    public String getName() {
        return myName;
    }

    /**
     * @see ca.nengo.model.NTarget#getDimensions()
     */
    public int getDimensions() {
        return myWeights.length;
    }

    /**
     * @return List of synaptic weights for each input channel
     */
    public float[] getWeights() {
        return myWeights;
    }

    /**
     * @param weights The new synaptic weights for each input channel
     */
    public void setWeights(float[] weights, boolean save) {
        if(weights.length != myInitialWeights.length) {
            System.err.println("Error, dimensions don't match in setWeights, ignoring new weights");
            return;
        }

        myWeights = weights.clone();

        if (save) {
            saveWeights();
        }
    }
    
    /**
     * This modifies the weights in-place, rather than creating new ones, so will usually
     * be faster than calling setWeights.
     * 
     * @param change The change in the synaptic weights for each input channel
     */
    public void modifyWeights(float[] change, boolean save) {
        if(change.length != myInitialWeights.length) {
            System.err.println("Error, dimensions don't match in modifyWeights, ignoring new weights");
            return;
        }

        for(int i=0; i < change.length; i++)
        	myWeights[i] += change[i];

        if (save) {
            saveWeights();
        }
    }
    

    /**
     * @param probs The new synaptic vesicle release probabilities for each input channel
     */
    public void setWeightProbabilities(float[] probs) {
        if(probs.length != myInitialWeights.length)
        {
            System.err.println("Error, dimensions don't match in setWeightProbabilities, ignoring probabilities");
            return;
        }
        if (random==null) {
            random=new Random();
        }

        myWeightProbabilities = probs;
    }

    /**
     * @return List of synaptic release probabilities for each input channel
     */
    public float[] getWeightProbabilities() {
        return myWeightProbabilities;
    }

    /**
     * @return The most recent input to the Termination
     */
    public InstantaneousOutput get() {
        return myRawInput;
    }

    /**
     * @return The most recent output of the Termination (after summation and dynamics)
     */
    public float getOutput() {
        return myCurrent;
    }

    /**
     * @param values Can be either SpikeOutput or RealOutput
     * @see ca.nengo.model.NTarget#apply(ca.nengo.model.InstantaneousOutput)
     */
    public void apply(InstantaneousOutput values) throws SimulationException {
        if (values.getDimension() != getDimensions()) {
            throw new SimulationException("Input must have dimension " + getDimensions());
        }

        myRawInput = values;

        myPreciseSpikeInputTimes = (values instanceof PreciseSpikeOutput) ? ((PreciseSpikeOutput)values).getSpikeTimes() : null;
        myIntegrationTime = 0; // start at the beginning of these spike times (given as an offset increasing from the previous time step)
        myNetSpikeInput = (values instanceof SpikeOutput && myPreciseSpikeInputTimes==null) ? combineSpikes((SpikeOutput) values, myWeights) : 0;

        // convert precise spike times that happen right at the beginning of the time window
        //  to be handled separately (we really don't need this, but I'm paranoid about losing
        //  single spikes that happen right at the step boundaries)
        if (myPreciseSpikeInputTimes!=null) {
            if (myWeightProbabilities!=null) {
                for (int i=0; i<myPreciseSpikeInputTimes.length; i++) {
                    if ((myPreciseSpikeInputTimes[i]==0f) && (random.nextFloat()<myWeightProbabilities[i])) {
                        myNetSpikeInput+=myWeights[i];
                    }
                }
            } else {
                for (int i=0; i<myPreciseSpikeInputTimes.length; i++) {
                    if (myPreciseSpikeInputTimes[i]==0f) {
                        myNetSpikeInput+=myWeights[i];
                    }
                }
            }
        }

        myNetRealInput = (values instanceof RealSource) ? combineReals((RealSource) values, myWeights) : 0;
    }

    /**
     * Updates net post-synaptic current for this Termination according to new inputs and exponential
     * dynamics applied to previous inputs.
     *
     * The arguments provide flexibility in updating the current, in terms of whether spike inputs are
     * applied, for how long real-valued inputs are applied, and for how long the net current decays
     * exponentially. A usage example follows:
     *
     * Suppose the SynapticIntegrator that contains this Termination models each network time step in three
     * steps of its own. Suppose also that the SynapticIntegrator uses updateCurrent() to find the current
     * at the beginning and end of each network time step, and at the two points in between. A reasonable way
     * for the SynapticIntegrator to use updateCurrent() in this scenario would be as follows (the variable
     * tau represents 1/3 of the length of the network time step):
     *
     * <ol><li>At the beginning of the network time step call updateCurrent(true, tau, 0) to model the
     * application of spikes and real-valued inputs from the previous time step, without decaying them.</li>
     * <li>To advance to each of the two intermediate times call updateCurrent(false, tau, tau). Spikes
     * are not re-applied (a given spike should only be applied once). Real-valued inputs are continuous in
     * time, so they are integrated again. Currents also begin to decay.</li>
     * <li>At the end of the network time step call updateCurrent(false, 0, tau). Real-valued inputs for this
     * time interval are not applied at the end of this network time step, since they will be applied at the
     * (identical) beginning of the next network time step. </li><ol>
     *
     * <p>The essential points are that spikes are only applied once during a network time step, and that
     * the total integration and decay times over a network time step both equal the length of the network
     * time step.</p>
     *
     * @param applySpikes True if spike inputs are to be applied
     * @param integrationTime Time over which real-valued inputs are to be integrated
     * @param decayTime Time over which post-synaptic currents are to decay
     * @return Net synaptic current flowing into this termination after specified input and decay
     */
    public float updateCurrent(boolean applySpikes, float integrationTime, float decayTime) {
        if (decayTime > 0) {
            //TODO: is there a correction we can do here when tau isn't much larger than the timestep? (will decay to zero if tau=step)
            myCurrent = myCurrent - myCurrent * ( 1f/myTauPSC ) * decayTime;
        }
        if (myPreciseSpikeInputTimes!=null) {
            updatePreciseSpikeCurrent(integrationTime);
        }

        if (applySpikes) {
            myCurrent = myCurrent + myNetSpikeInput / myTauPSC; //normalized so that unweighted PSC integral is 1
        }
        if (integrationTime > 0) {
            //normalized so that real input x has same current integral as x spike inputs/s (with same weight)
            myCurrent = myCurrent + myNetRealInput * integrationTime / myTauPSC; //this might be normalizing in the wrong direction
        }

        return myCurrent;
    }

    /**
     *
     * @param integrationTime The amount of time covered by this integration step.
     */
    private void updatePreciseSpikeCurrent(float integrationTime) {
        float endTime=myIntegrationTime+integrationTime;
        float epsilon=0.0000001f;

        if (myWeightProbabilities!=null) {
            for (int i=0; i<myPreciseSpikeInputTimes.length; i++)
            {
                float time=myPreciseSpikeInputTimes[i];
                if (time>myIntegrationTime && (time<=endTime+epsilon) && (random.nextFloat()<myWeightProbabilities[i])) {
                    myCurrent+=myWeights[i]*(1f/myTauPSC-((endTime-time)/(myTauPSC*myTauPSC)));
                }
            }

        } else {
            for (int i=0; i<myPreciseSpikeInputTimes.length; i++)
            {
                float time=myPreciseSpikeInputTimes[i];
                if (time>myIntegrationTime && (time<=endTime+epsilon)) {
                    myCurrent+=myWeights[i]*(1f/myTauPSC-((endTime-time)/(myTauPSC*myTauPSC)));
                }
            }
        }
        myIntegrationTime=endTime;
    }

    private float combineSpikes(SpikeOutput input, float[] weights) {
        float result = 0;
        boolean[] spikes = input.getValues();

        if (myWeightProbabilities!=null) {
            for (int i = 0; i < spikes.length; i++) {
                if (spikes[i] && (random.nextFloat()<myWeightProbabilities[i])) {
                    result += weights[i];
                }
            }
        } else {
            for (int i = 0; i < spikes.length; i++) {
                if (spikes[i]) {
                    result += weights[i];
                }
            }
        }

        return result;
    }

    private float combineReals(RealSource input, float[] weights) {
        float result = 0;
        float[] reals = input.getValues();

        for (int i = 0; i < reals.length; i++) {
            result += weights[i] * reals[i];
        }

        return result;
    }

    /**
     * @see ca.nengo.model.NTarget#getNode()
     */
    public Node getNode() {
        return myNode;
    }

    /**
     * @param node Parent node
     */
    public void setNode(Node node) {
        myNode = node;
    }

    /**
     * @see ca.nengo.model.NTarget#getModulatory()
     */
    public boolean getModulatory() {
        return myModulatory;
    }

    /**
     * @see ca.nengo.model.NTarget#getTau()
     */
    public float getTau() {
        return myTauPSC;
    }

    /**
     * @see ca.nengo.model.NTarget#setModulatory(boolean)
     */
    public void setModulatory(boolean modulatory) {
        myModulatory = modulatory;
    }

    /**
     * @see ca.nengo.model.NTarget#setTau(float)
     */
    public void setTau(float tau) throws StructuralException {
        myTauPSC = tau;
    }

    @Override
    public LinearExponentialTarget clone() throws CloneNotSupportedException {
    	return this.clone(myNode);
    }
    
	public LinearExponentialTarget clone(Node node) throws CloneNotSupportedException {
		LinearExponentialTarget result = (LinearExponentialTarget) super.clone();
		result.myNode = node;
		result.myWeights = myWeights.clone();
		result.saveWeights();
//		result.myWeightProbabilities = myWeightProbabilities.clone();
		result.myRawInput = (myRawInput != null) ? myRawInput.clone() : null;
//		result.myRawInput = null;
		return result;
	}

}
