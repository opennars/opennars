/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "IzhikevichSpikeGenerator.java". Description:
"From Izhikevich, 2003, the model is:
  v' = 0.04vv + 5v + 140 - u + I
  u' = a(bv - u)

  If v >= 30 mV, then v := c and u := u + d (reset after spike)

  v represents the membrane potential;
  u is a membrane recovery variable;
  a, b, c, and d are modifiable parameters

  @author Hussein, Bryan"

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
 * Created on May 4, 2006
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.math.impl.InterpolatedFunction;
import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.TimeSeries1D;
import ca.nengo.util.impl.TimeSeries1DImpl;

import java.util.Properties;

/**
 * <p>From Izhikevich, 2003, the model is:<br>
 * v' = 0.04v*v + 5v + 140 - u + I <br>
 * u' = a(bv - u) </p>
 *
 * <p>If v >= 30 mV, then v := c and u := u + d (reset after spike)</p>
 *
 * <p>v represents the membrane potential;
 * u is a membrane recovery variable;
 * a, b, c, and d are modifiable parameters</p>
 *
 * @author Hussein, Bryan
 */
public class IzhikevichSpikeGenerator implements SpikeGenerator, Probeable {

	private static final long serialVersionUID = 1L;

	/**
	 * Preset parameter values corresponding to different cell types.
	 *
	 * @author Bryan Tripp
	 */
	public static enum Preset {

		/**
		 * Custom parameter set
		 */
		CUSTOM(0f, 0f, 0f, 0f),


		/**
		 * Default parameter set
		 */
		DEFAULT(0.02f, .2f, -65f, 2f),


		/**
		 * Parameter set for tonic firing
		 */
		REGULAR_SPIKING(0.02f, .2f, -65f, 8f),


		/**
		 * Parameter set for burst firing
		 */
		INTRINSICALLY_BURSTING(0.02f, .2f, -55f, 4f),


		/**
		 * Parameter set for "chattering" behaviour
		 */
		CHATTERING(0.02f, .2f, -50f, 2f),


		/**
		 * Parameter set for fast spiking
		 */
		FAST_SPIKING(.1f, .2f, -65f, 2f),


		//LOW_THRESHOLD_SPIKING(0.02f, .25f, -65f, 2f), //only b param given by Izhikevich

		//THALAMO_CORTICAL(0.02f, .2f, -65f, 2f), //parameters not given (two regimes)

		/**
         * Parameter set for a resonator
         */
		RESONATOR(0.1f, .26f, -65f, 2f);

		final float myA;
		final float myB;
		final float myC;
		final float myD;

		private Preset(float a, float b, float c, float d) {
			myA = a; myB = b; myC = c; myD = d;
		}

		/**
		 * @return Dynamics matrix
		 */
		public float getA() {
			return myA;
		}

		/**
		 * @return Input matrix
		 */
		public float getB() {
			return myB;
		}

		/**
		 * @return Output matrix
		 */
		public float getC() {
			return myC;
		}

		/**
		 * @return Passthrough matrix
		 */
		public float getD() {
			return myD;
		}
	}

	/**
	 * Voltage state variable
	 */
	public static final String V = "V";

	/**
	 * Recovery state variable
	 */
	public static final String U = "U";

	private static final SimulationMode[] ourSupportedModes = new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.RATE};

	private static final float myMaxTimeStep = .001f;
	private static final float Vth = 30;

	private double myA;
	private double myB;
	private double myC;
	private double myD;
	private final double myInitialVoltage;
	private Preset myPreset;

	private double myVoltage;
	private double myRecovery;

	private float[] myTime;
	private float[] myVoltageHistory;
	private float[] myRecoveryHistory;
	private float mySpikeRate;

	private SimulationMode myMode;

	private static final float[] ourNullTime = new float[0];
	private static final float[] ourNullVoltageHistory = new float[0];
	private static final float[] ourNullRecoveryHistory = new float[0];

	/**
	 * Constructor using "default" parameters
	 */
	public IzhikevichSpikeGenerator() {
		this(0.02f, .2f, -65f, 2f);
	}

	/**
	 * @param preset A set of parameter values corresponding to a predefined cell type
	 */
	public IzhikevichSpikeGenerator(Preset preset) {
		this(preset.getA(), preset.getB(), preset.getC(), preset.getD());
		myPreset = preset;
	}

	/**
	 * @param a time scale of recovery variable
	 * @param b sensitivity of recovery variable
	 * @param c voltage reset value
	 * @param d recovery variable reset change
	 */
	public IzhikevichSpikeGenerator(float a, float b, float c, float d) {
		this(a, b, c, d, -65);
	}

	/**
	 * @param a time scale of recovery variable
	 * @param b sensitivity of recovery variable
	 * @param c voltage reset value
	 * @param d recovery variable reset change
	 * @param initialVoltage initial voltage value (varying across neurons can prevent synchrony
	 * 		at start of simulation)
	 */
	public IzhikevichSpikeGenerator(float a, float b, float c, float d, float initialVoltage) {
		myA = a;
		myB = b;
		myC = c;
		myD = d;
		myInitialVoltage = initialVoltage;

		myMode = SimulationMode.DEFAULT;
		myPreset = Preset.CUSTOM;

		reset(false);
	}

	/**
	 * @return An enumerated parameter value preset
	 */
	public Preset getPreset() {
		return myPreset;
	}

	/**
	 * @param preset An enumerated parameter value preset
	 */
	public void setPreset(Preset preset) {
		myPreset = preset;
		myA = preset.getA();
		myB = preset.getB();
		myC = preset.getC();
		myD = preset.getD();
	}

	/**
	 * @return time scale of recovery variable
	 */
	public float getA() {
		return (float) myA;
	}

	/**
	 * @param a time scale of recovery variable
	 */
	public void setA(float a) {
		if (a != myA) {
            myPreset = Preset.CUSTOM;
        }
		myA = a;
	}

	/**
	 * @return sensitivity of recovery variable
	 */
	public float getB() {
		return (float) myB;
	}

	/**
	 * @param b sensitivity of recovery variable
	 */
	public void setB(float b) {
		if (b != myB) {
            myPreset = Preset.CUSTOM;
        }
		myB = b;
	}

	/**
	 * @return voltage reset value
	 */
	public float getC() {
		return (float) myC;
	}

	/**
	 * @param c voltage reset value
	 */
	public void setC(float c) {
		if (c != myC) {
            myPreset = Preset.CUSTOM;
        }
		myC = c;
	}

	/**
	 * @return recovery variable reset change
	 */
	public float getD() {
		return (float) myD;
	}

	/**
	 * @param d recovery variable reset change
	 */
	public void setD(float d) {
		if (d != myD) {
            myPreset = Preset.CUSTOM;
        }
		myD = d;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myVoltage = myInitialVoltage;
		myRecovery = myB*myVoltage;
		myTime = ourNullTime;
		myVoltageHistory = ourNullVoltageHistory;
		myRecoveryHistory = ourNullRecoveryHistory;
	}

	/**
	 * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
	 */
	public InstantaneousOutput run(float[] time, float[] current) {
		assert time.length >= 2;
		assert time.length == current.length;
		
		boolean spiking = doSpikingRun(time, current);
		if(myMode==SimulationMode.RATE)
		{
			float spikeRate = 0f;
			if(spiking)
				spikeRate = 1f/(time[time.length-1] - time[0]);
			mySpikeRate = mySpikeRate*0.99f + spikeRate*0.01f; //using a moving average (this is fairly ad-hoc)
			return new RealOutputImpl(new float[]{mySpikeRate}, Units.SPIKES_PER_S, time[time.length-1]);
		}
		else 
			return new SpikeOutputImpl(new boolean[]{spiking}, Units.SPIKES, time[time.length-1]);
	}

	private boolean doSpikingRun(float[] time, float[] current) {
		float len = time[time.length - 1] - time[0];
		int steps = (int) Math.ceil((len - 1e-5) / myMaxTimeStep);
		float dt = len / steps;

		myTime = new float[steps];
		myVoltageHistory = new float[steps];
		myRecoveryHistory = new float[steps];

		boolean spiking = false;
		for (int i = 0; i < steps; i++) {
			myTime[i] = time[0] + i*dt;
			double I = InterpolatedFunction.interpolate(time, current, myTime[i]+dt/2f);

			if (myVoltage >= Vth) {
				spiking = true;
				myVoltage = myC;
				myRecovery = myRecovery + myD;
			}

			myVoltage += 500 * dt * (0.04*myVoltage*myVoltage + 5*myVoltage + 140 - myRecovery + I);
			myVoltage += 500 * dt * (0.04*myVoltage*myVoltage + 5*myVoltage + 140 - myRecovery + I);
			myVoltageHistory[i] = (float) myVoltage;

			myRecovery += 1000 * dt * (myA*(myB*myVoltage - myRecovery));
			myRecoveryHistory[i] = (float) myRecovery;
		}

		return spiking;
	}
	
	/**
	 * @return membrane voltage
	 */
	public float getVoltage() {
		return (float)myVoltage;
	}

	/**
	 * @see Probeable#getHistory(String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		TimeSeries1D result = null;

        switch (stateName) {
            case V:
                result = new TimeSeries1DImpl(myTime, myVoltageHistory, Units.AVU);
                break;
            case U:
                result = new TimeSeries1DImpl(myTime, myRecoveryHistory, Units.UNK);
                break;
            default:
                throw new SimulationException("The state name " + stateName + " is unknown.");
        }

		return result;
	}

	/**
	 * @see Probeable#listStates()
	 */
	public Properties listStates() {
		Properties p = new Properties();
		p.setProperty(V, "Membrane potential (arbitrary units)");
		p.setProperty(U, "Recovery variable (arbitrary units)");
		return p;
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#getMode()
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * @see ca.nengo.model.SimulationMode.ModeConfigurable#setMode(ca.nengo.model.SimulationMode)
	 */
	public void setMode(SimulationMode mode) {
		myMode = SimulationMode.getClosestMode(mode, ourSupportedModes);
	}

	@Override
	public SpikeGenerator clone() throws CloneNotSupportedException {
		IzhikevichSpikeGenerator result = (IzhikevichSpikeGenerator) super.clone();
		result.myTime = myTime.clone();
		result.myVoltageHistory = myVoltageHistory.clone();
		result.myRecoveryHistory = myRecoveryHistory.clone();
		return result;
	}

}
