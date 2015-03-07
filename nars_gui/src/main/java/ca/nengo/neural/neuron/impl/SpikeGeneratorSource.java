/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SpikeGeneratorOrigin.java". Description:
"An Origin that obtains output from an underlying SpikeGenerator"

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
 * Created on May 23, 2006
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;

/**
 * An Origin that obtains output from an underlying SpikeGenerator. This is a good Origin to use as
 * the main (axonal) output of a spiking neuron. This Origin may produce SpikeOutput or RealOutput
 * depending on whether it is running in DEFAULT or CONSTANT_RATE SimulationMode.
 * 
 * @author Bryan Tripp
 */
public class SpikeGeneratorSource implements NSource<InstantaneousOutput> {

    private static final long serialVersionUID = 1L;

    private String myName;
    private final Node myNode;
    private final SpikeGenerator myGenerator;
    private InstantaneousOutput myOutput;
    private boolean myRequiredOnCPU;

    /**
     * @param node The parent Node
     * @param generator The SpikeGenerator from which this Origin is to obtain output.
     */
    public SpikeGeneratorSource(Node node, SpikeGenerator generator) {
        myNode = node;
        myGenerator = generator;
        myOutput = new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 0);
        myName = Neuron.AXON;
    }

    /**
     * @return Neuron.AXON
     * @see ca.nengo.model.NSource#getName()
     */
    public String getName() {
        return myName;
    }
    
    public void setName(String name) {
    	myName = name;
    }

    /**
     * @return 1
     * @see ca.nengo.model.NSource#getDimensions()
     */
    public int getDimensions() {
        return 1;
    }

    /**
     * @param times Passed on to the run() or runConstantRate() method of the wrapped SpikeGenerator
     * 		depending on whether the SimulationMode is DEFAULT or CONSTANT_RATE (in the latter case
     * 		only the first value is used).
     * @param current Passed on like the times argument.
     * @throws SimulationException Arising From the underlying SpikeGenerator, or if the given times
     * 		or values arrays have length 0 when in CONSTANT_RATE mode (the latter because the first
     * 		entries must be extracted).
     */
    public void run(float[] times, float[] current) {
        myOutput = myGenerator.run(times, current);
    }

    /**
     * Returns spike values or real-valued spike rate values, depending on whether the mode
     * is SimulationMode.DEFAULT or SimulationMode.CONSTANT_RATE.
     * 
     * @see ca.nengo.model.NSource#get()
     */
    public InstantaneousOutput get() {
        return myOutput;
    }
    
    public void accept(InstantaneousOutput val) {
        myOutput = val;
    }

    /**
     * @see ca.nengo.model.NSource#getNode()
     */
    public Node getNode() {
        return myNode;
    }

    /**
     * @return Spike generator
     */
    public SpikeGenerator getGenerator(){
        return myGenerator;
    }

    /**
     * Need this to fix bug where the generator's mode is changed, but
     * myOutput is still of the type of the old mode
     * 
     * @param mode Target simulation mode
     * @see ca.nengo.neural.neuron.Neuron#setMode(ca.nengo.model.SimulationMode)
     */
    public void setMode(SimulationMode mode) {
        if (mode == SimulationMode.CONSTANT_RATE || mode == SimulationMode.RATE){
            myOutput = new RealOutputImpl(new float[]{0.0f}, Units.SPIKES_PER_S, 0);
        } else {
            myOutput = new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 0);
        }
    }

    @Override
    public SpikeGeneratorSource clone() throws CloneNotSupportedException {
        SpikeGeneratorSource result = (SpikeGeneratorSource) super.clone();
        result.myOutput = myOutput.clone();
        return result;
    }
    
	public SpikeGeneratorSource clone(Node node) throws CloneNotSupportedException {
		return this.clone();
	}	
    
    public void setRequiredOnCPU(boolean val){
        myRequiredOnCPU = val;
    }
    
    public boolean getRequiredOnCPU(){
        return myRequiredOnCPU;
    }
    
    public void reset(boolean randomize) {
    	myGenerator.reset(randomize);
    	if (myOutput instanceof RealOutputImpl){
            myOutput = new RealOutputImpl(new float[]{0.0f}, Units.SPIKES_PER_S, 0);
        } else {
            myOutput = new SpikeOutputImpl(new boolean[]{false}, Units.SPIKES, 0);
        }
    }

}
