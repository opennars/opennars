/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "SpikingNeuron.java". Description:
"A neuron model composed of a SynapticIntegrator and a SpikeGenerator"

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
 * Created on May 3, 2006
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.model.*;
import ca.nengo.model.impl.BasicSource;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.nef.NEFNode;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.neural.neuron.SynapticIntegrator;
import ca.nengo.util.*;
import ca.nengo.util.impl.TimeSeries1DImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * A neuron model composed of a SynapticIntegrator and a SpikeGenerator.
 *
 * @author Bryan Tripp
 */
public class SpikingNeuron implements Neuron, Probeable, NEFNode {

	private static final long serialVersionUID = 1L;

	/**
	 * Name of Origin representing unscaled and unbiased current entering the soma.
	 */
	public static final String CURRENT = "current";

	private SynapticIntegrator myIntegrator;
	private SpikeGenerator myGenerator;
	private SpikeGeneratorSource mySpikeOrigin;
	private BasicSource myCurrentOrigin;
	private float myUnscaledCurrent;
	private TimeSeries1D myCurrent;
	private String myName;
	private float myScale;
	private float myBias;
	private float myRadialInput;
	private String myDocumentation;
	private transient ArrayList<VisiblyChanges.Listener> myListeners;
	private Noise myNoise = null;


	/**
	 * Note: current = scale * (weighted sum of inputs at each termination) * (radial input) + bias.
	 *
	 * @param integrator SynapticIntegrator used to model dendritic/somatic integration of inputs
	 * 		to this Neuron
	 * @param generator SpikeGenerator used to model spike generation at the axon hillock of this
	 * 		Neuron
	 * @param scale A coefficient that scales summed input
	 * @param bias A bias current that models unaccounted-for inputs and/or intrinsic currents
	 * @param name A unique name for this neuron in the context of the Network or Ensemble to which
	 * 		it belongs
	 */
	public SpikingNeuron(SynapticIntegrator integrator, SpikeGenerator generator, float scale, float bias, String name) {
		if (integrator == null) {
			integrator = new LinearSynapticIntegrator(.001f, Units.ACU);
		}
		setIntegrator(integrator);

		if (generator == null) {
			generator = new LIFSpikeGenerator(.001f, .02f, .002f);
		}
		setGenerator(generator);

		myCurrentOrigin = new BasicSource(this, CURRENT, 1, Units.ACU);
		myCurrentOrigin.setValues(0, 0, new float[]{0});
		myName = name;
		myScale = scale;
		myBias = bias;
		myRadialInput = 0;
		myCurrent = new TimeSeries1DImpl(new float[]{0}, new float[]{0}, Units.UNK);
	}

	/**
	 * @see ca.nengo.neural.neuron.Neuron#run(float, float)
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		//TODO: this method could use some cleanup and optimization
		TimeSeries1D current = myIntegrator.run(startTime, endTime);

		float[] integratorOutput = current.getValues1D();
		float[] generatorInput = new float[integratorOutput.length];
		
		for (int i = 0; i < integratorOutput.length; i++) {
			myUnscaledCurrent = (myRadialInput + integratorOutput[i]);
			generatorInput[i] = myBias + myScale * myUnscaledCurrent;
			if (myNoise != null) {
				generatorInput[i] = myNoise.getValue(startTime, endTime, generatorInput[i]);
			}
		}

		myCurrent = new TimeSeries1DImpl(current.getTimes(), generatorInput, Units.UNK);

		mySpikeOrigin.run(myCurrent.getTimes(), generatorInput);
		myCurrentOrigin.setValues(startTime, endTime, new float[]{myUnscaledCurrent});
	}

	/**
	 * @see ca.nengo.neural.neuron.Neuron#getSources()
	 */
	public NSource<InstantaneousOutput>[] getSources() {
		return new NSource[]{mySpikeOrigin, myCurrentOrigin};
	}

	/**
	 * @see ca.nengo.neural.neuron.Neuron#getSource(java.lang.String)
	 */
	public NSource getSource(String name) throws StructuralException {
//		assert (name.equals(Neuron.AXON) || name.equals(CURRENT)); //this is going to be called a lot, so let's skip the exception
		//Shu: I added the exception back in because the UI needs it for reflection.
        switch (name) {
            case Neuron.AXON:
                return mySpikeOrigin;
            case CURRENT:
                return myCurrentOrigin;
            default:
                throw new StructuralException("Origin does not exist");
        }
	}

	/**
	 * @see ca.nengo.neural.neuron.Neuron#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		myGenerator.setMode(mode);
		mySpikeOrigin.setMode(mode);
	}

	/**
	 * @see ca.nengo.neural.neuron.Neuron#getMode()
	 */
	public SimulationMode getMode() {
		return myGenerator.getMode();
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myIntegrator.reset(randomize);
		myGenerator.reset(randomize);
		myCurrentOrigin.reset(randomize);
		mySpikeOrigin.reset(randomize);
		if (myNoise != null) {
            myNoise.reset(randomize);
        }
		myRadialInput = 0;
	}

	/**
	 * Available states include "I" (net current into SpikeGenerator) and the states of the
	 * SpikeGenerator.
	 *
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		TimeSeries result = null;
		if (stateName.equals("I")) {
			result = myCurrent;
		} else if (stateName.equals("rate")) {
			InstantaneousOutput output = mySpikeOrigin.get();
			float[] times = myCurrent.getTimes();
			float rate = 0;
			if (output instanceof RealSource) {
				rate = ((RealSource) output).getValues()[0];
			} else if (output instanceof SpikeOutput) {
				rate = ((SpikeOutput) output).getValues()[0] ? 1/(times[times.length-1]-times[0]) : 0;
			}
			result = new TimeSeries1DImpl(new float[]{times[times.length-1]}, new float[]{rate}, Units.SPIKES_PER_S);
		} else if (stateName.equals(CURRENT)) {
			float[] times = myCurrent.getTimes();
			result = new TimeSeries1DImpl(new float[]{times[times.length-1]}, new float[]{myUnscaledCurrent}, Units.ACU);
		} else if (myGenerator instanceof Probeable) {
			result = ((Probeable) myGenerator).getHistory(stateName);
		} else {
			throw new SimulationException("The state " + stateName + " is unknown");
		}
		return result;
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties p = (myGenerator instanceof Probeable) ? ((Probeable) myGenerator).listStates() : new Properties();
		p.setProperty("I", "Net current (arbitrary units)");
		p.setProperty("rate", "Firing rate");
		return p;
	}

	/**
	 * @see ca.nengo.model.Node#name()
	 */
	public String name() {
		return myName;
	}

	/**
	 * @param name The new name
	 */
	public void setName(String name) throws StructuralException {
		VisiblyChangesUtils.nameChanged(this, name(), name, myListeners);
		myName = name;
	}

	/**
	 * @return The coefficient that scales summed input
	 */
	public float getScale() {
		return myScale;
	}

	/**
	 * @param scale New scaling coefficient
	 */
	public void setScale(float scale) {
		myScale = scale;
	}

	/**
	 * @return The bias current that models unaccounted-for inputs and/or intrinsic currents
	 */
	public float getBias() {
		return myBias;
	}

	/**
	 * @param bias New bias current
	 */
	public void setBias(float bias) {
		myBias = bias;
	}

	/**
	 * @return The SynapticIntegrator used to model dendritic/somatic integration of inputs
	 * 		to this Neuron
	 */
	public SynapticIntegrator getIntegrator() {
		return myIntegrator;
	}

	/**
	 * @param integrator New synaptic integrator
	 */
	public void setIntegrator(SynapticIntegrator integrator) {
		myIntegrator = integrator;
		myIntegrator.setNode(this);
	}

	/**
	 * @return The SpikeGenerator used to model spike generation at the axon hillock of this
	 * 		Neuron
	 */
	public SpikeGenerator getGenerator() {
		return myGenerator;
	}

	/**
	 * @param generator New SpikeGenerator
	 */
	public void setGenerator(SpikeGenerator generator) {
		myGenerator = generator;
		mySpikeOrigin = new SpikeGeneratorSource(this, generator);
	}

	/**
	 * @see ca.nengo.model.Node#getTargets()
	 */
	public NTarget[] getTargets() {
		return myIntegrator.getTerminations();
	}

	/**
	 * @see ca.nengo.model.Node#getTarget(java.lang.String)
	 */
	public NTarget getTarget(String name) throws StructuralException {
		return myIntegrator.getTermination(name);
	}

	/**
	 * @see ca.nengo.neural.nef.NEFNode#setRadialInput(float)
	 */
	public void setRadialInput(float value) {
		myRadialInput = value;
	}

	/**
	 * @see ca.nengo.model.Node#getDocumentation()
	 */
	public String getDocumentation() {
		return myDocumentation;
	}

	/**
	 * @see ca.nengo.model.Node#setDocumentation(java.lang.String)
	 */
	public void setDocumentation(String text) {
		myDocumentation = text;
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#addChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void addChangeListener(Listener listener) {
		if (myListeners == null) {
			myListeners = new ArrayList<Listener>(2);
		}
		myListeners.add(listener);
	}

	/**
	 * @see ca.nengo.util.VisiblyChanges#removeChangeListener(ca.nengo.util.VisiblyChanges.Listener)
	 */
	public void removeChangeListener(Listener listener) {
		myListeners.remove(listener);
	}
	
	/**
	 * Called by subclasses when properties have changed in such a way that the
	 * display of the ensemble may need updating.
	 */
	protected void fireVisibleChangeEvent() {
		VisiblyChangesUtils.changed(this, myListeners);
	}

	@Override
	public SpikingNeuron clone() throws CloneNotSupportedException {
		SpikingNeuron result = (SpikingNeuron) super.clone();
		result.myCurrent = (TimeSeries1D) myCurrent.clone();

		result.myCurrentOrigin = myCurrentOrigin.clone();

		result.myGenerator = myGenerator.clone();

		result.myIntegrator = myIntegrator.clone();
		result.myIntegrator.setNode(result);

		result.myListeners = new ArrayList<Listener>(5);
		result.mySpikeOrigin = new SpikeGeneratorSource(result, result.myGenerator);

		if (myNoise!=null) {
            result.setNoise(myNoise.clone());
        }

		return result;
	}

	/**
	 * @param noise Noise object to apply to this neuron
	 */
	public SpikingNeuron setNoise(Noise noise) {
		myNoise = noise;
        return this;
	}

	/**
	 * @return Noise object applied to this neuron
	 */
	public Noise getNoise() {
		return myNoise;
	}

	public Node[] getChildren() {
		return Node.EMPTY;
	}

	public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
		return "";
	}
}
