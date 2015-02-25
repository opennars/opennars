/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "LIFSpikeGenerator.java". Description:
"A leaky-integrate-and-fire model of spike generation"

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

import ca.nengo.math.PDF;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.model.*;
import ca.nengo.neural.impl.PreciseSpikeOutputImpl;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.TimeSeries1D;
import ca.nengo.util.impl.TimeSeries1DImpl;

import java.util.Properties;

/**
 * <p>A leaky-integrate-and-fire model of spike generation. From Koch, 1999,
 * the subthreshold model is: C dV(t)/dt + V(t)/R = I(t). When V reaches
 * a threshold, a spike occurs (spike-related currents are not modelled).</p>
 *
 * <p>For simplicity we take Vth = R = 1, which does not limit the behaviour
 * of the model, although transformations may be needed if it is desired to
 * convert to more realistic parameter ranges. </p>
 *
 * @author Bryan Tripp
 */
public class LIFSpikeGenerator implements SpikeGenerator, Probeable {

	private static final long serialVersionUID = 1L;

	private static final float R = 1;
	private static final float Vth = 1;

	private float myMaxTimeStep;
	private float myTauRC;
	private float myTauRef;
	private final float myInitialVoltage;

	private float myVoltage;
	private float myTimeSinceLastSpike;
//	private float myTauRefNext; //varies randomly to avoid bias and synchronized variations due to floating point comparisons

	private float myPreviousVoltage; //for linear interpolation of when spike occurs

	private float[] myTime;
	private float[] myVoltageHistory;

	private SimulationMode myMode;
	private SimulationMode[] mySupportedModes;

	private static final float[] ourNullTime = new float[0];
	private static final float[] ourNullVoltageHistory = new float[0];
	private static final float ourMaxTimeStepCorrection = 1.01f;

	/**
	 * Uses default values.
	 */
	public LIFSpikeGenerator() {
		this(.0005f, .02f, .002f);
	}

	/**
	 * @param maxTimeStep maximum integration time step (s). Shorter time steps may be used if a
	 * 		run(...) is requested with a length that is not an integer multiple of this value.
	 * @param tauRC resistive-capacitive time constant (s)
	 * @param tauRef refracory period (s)
	 */
	public LIFSpikeGenerator(float maxTimeStep, float tauRC, float tauRef) {
		this(maxTimeStep, tauRC, tauRef, 0);
	}

	/**
	 * @param maxTimeStep Maximum integration time step (s). Shorter time steps may be used if a
	 * 		run(...) is requested with a length that is not an integer multiple of this value.
	 * @param tauRC Resistive-capacitive time constant (s)
	 * @param tauRef Refracory period (s)
	 * @param initialVoltage Initial condition on V
	 */
	public LIFSpikeGenerator(float maxTimeStep, float tauRC, float tauRef, float initialVoltage) {
		setMaxTimeStep(maxTimeStep);
		myTauRC = tauRC;
		myTauRef = tauRef;
//		myTauRefNext = tauRef;
		myInitialVoltage = initialVoltage;
		myPreviousVoltage = myInitialVoltage;

		myMode = SimulationMode.DEFAULT;
		mySupportedModes = new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.CONSTANT_RATE, SimulationMode.RATE, SimulationMode.PRECISE};

		reset(false);
	}

	/**
	 * @return Maximum integration time step (s).
	 */
	public float getMaxTimeStep() {
		return myMaxTimeStep / ourMaxTimeStepCorrection;
	}

	/**
	 * @param max Maximum integration time step (s).
	 */
	public void setMaxTimeStep(float max) {
		myMaxTimeStep = max * ourMaxTimeStepCorrection; //increased slightly because float/float != integer
	}

	/**
	 * @return Resistive-capacitive time constant (s)
	 */
	public float getTauRC() {
		return myTauRC;
	}

	/**
	 * @param tauRC Resistive-capacitive time constant (s)
	 */
	public void setTauRC(float tauRC) {
		myTauRC = tauRC;
	}

	/**
	 * @return Refracory period (s)
	 */
	public float getTauRef() {
		return myTauRef;
	}

	/**
	 * @param tauRef Refracory period (s)
	 */
	public void setTauRef(float tauRef) {
		myTauRef = tauRef;
	}

	public void reset(boolean randomize) {
		myTimeSinceLastSpike = myTauRef;
		myVoltage = myInitialVoltage;
		myTime = ourNullTime;
		myVoltageHistory = ourNullVoltageHistory;
		myPreviousVoltage = myInitialVoltage;
	}

	/**
	 * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
	 */
	public InstantaneousOutput run(float[] time, float[] current) {
		InstantaneousOutput result = null;

		if (myMode.equals(SimulationMode.CONSTANT_RATE) || myMode.equals(SimulationMode.RATE)) {
			result = new RealOutputImpl(new float[]{doConstantRateRun(time[0], current[0])}, Units.SPIKES_PER_S, time[time.length-1]);
		} else if (myMode.equals(SimulationMode.PRECISE)) {
			result = new PreciseSpikeOutputImpl(new float[]{doPreciseSpikingRun(time, current)}, Units.SPIKES, time[time.length-1]);
		} else {
			//result = new SpikeOutputImpl(new boolean[]{doSpikingRun(time, current)}, Units.SPIKES, time[time.length-1]);
			result = new SpikeOutputImpl(new boolean[]{doPreciseSpikingRun(time, current)>=0}, Units.SPIKES, time[time.length-1]);
		}

		return result;
	}

//	private boolean doSpikingRun(float[] time, float[] current) {
//		if (time.length < 2) {
//			throw new IllegalArgumentException("Arg time must have length at least 2");
//		}
//		if (time.length != current.length) {
//			throw new IllegalArgumentException("Args time and current must have equal length");
//		}
//
//		float len = time[time.length - 1] - time[0];
//		int steps = (int) Math.ceil(len / myMaxTimeStep);
//		float dt = len / steps;
//
//		myTime = new float[steps];
//		myVoltageHistory = new float[steps];
////		mySpikeTimes = new ArrayList(10);
//
//		int inputIndex = 0;
//
//		boolean spiking = false;
//		for (int i = 0; i < steps; i++) {
//			myTime[i] = time[0] + i*dt;
//
//			while (time[inputIndex+1] <= myTime[i]) {
//				inputIndex++;
//			}
//			float I = current[inputIndex];
//
//			float dV = (1 / myTauRC) * (I*R - myVoltage);
//			myTimeSinceLastSpike = myTimeSinceLastSpike + dt;
//			if (myTimeSinceLastSpike < myTauRefNext) {
//				dV = 0;
//			}
//			myVoltage = Math.max(0, myVoltage + dt*dV);
//			myVoltageHistory[i] = myVoltage;
//
//			if (myVoltage > Vth) {
//				spiking = true;
//				myTimeSinceLastSpike = 0;
//				myVoltage = 0;
//				myTauRefNext = myTauRef + myMaxTimeStep - 2 * (float) PDFTools.random() * myMaxTimeStep;
////				mySpikeTimes.add(new Float(myTime[i]));
//			}
//		}
//
//		return spiking;
//	}

	private float doPreciseSpikingRun(float[] time, float[] current) {
		if (time.length < 2) {
			throw new IllegalArgumentException("Arg time must have length at least 2");
		}
		if (time.length != current.length) {
			throw new IllegalArgumentException("Args time and current must have equal length");
		}

		float len = time[time.length - 1] - time[0];
		int steps = (int) Math.ceil(len / myMaxTimeStep);
		float dt = len / steps;

		myTime = new float[steps];
		myVoltageHistory = new float[steps];
//		mySpikeTimes = new ArrayList(10);

		int inputIndex = 0;

		float spikeTimeFromLastTimeStep=-1;
		for (int i = 0; i < steps; i++) {
			myTime[i] = time[0] + i*dt;

			while (time[inputIndex+1] <= myTime[i]) {
				inputIndex++;
			}
			float I = current[inputIndex];

			float dV = (1 / myTauRC) * (I*R - myVoltage);
			myTimeSinceLastSpike = myTimeSinceLastSpike + dt;
			if (myTimeSinceLastSpike < myTauRef) {
				dV = 0;
			} else if (myTimeSinceLastSpike < myTauRef+dt) {
				dV*=(myTimeSinceLastSpike-myTauRef)/dt;
			}
			myPreviousVoltage = myVoltage;
			myVoltage = Math.max(0, myVoltage + dt*dV);
			myVoltageHistory[i] = myVoltage;

			if (myVoltage >= Vth) {
				float dSpike=(Vth-myPreviousVoltage)*dt/(myVoltage-myPreviousVoltage);
				myTimeSinceLastSpike = dt-dSpike;

				spikeTimeFromLastTimeStep=i*dt+dSpike;
				myVoltage = 0;
			}
		}

		return spikeTimeFromLastTimeStep;
	}

	/**
	 * @return membrane voltage
	 */
	public float getVoltage() {
		return myVoltage;
	}

	//Note that no voltage history is available after a constant-rate run.
	private float doConstantRateRun(float time, float current) {
		myTime = ourNullTime;
		myVoltageHistory = ourNullVoltageHistory;

		//implicitly Vth == R == 1
		return current > 1 ? 1f / ( myTauRef - myTauRC * ((float) Math.log(1 - 1/current)) ) : 0;
	}

	/**
	 * @param current Given current
	 * @return Result of solving for activity given current
	 */
	public float constantRateRun(float current) {
		return current > 1 ? 1f / ( myTauRef - myTauRC * ((float) Math.log(1 - 1/current)) ) : 0;
	}

	/**
	 * @see Probeable#getHistory(String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		TimeSeries1D result = null;

		if (stateName.equals("V")) {
			result = new TimeSeries1DImpl(myTime, myVoltageHistory, Units.AVU);
		} else {
			throw new SimulationException("The state name " + stateName + " is unknown.");
		}

		return result;
	}

	/**
	 * @see Probeable#listStates()
	 */
	public Properties listStates() {
		Properties p = new Properties();
		p.setProperty("V", "membrane potential (arbitrary units)");
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
		myMode = SimulationMode.getClosestMode(mode, mySupportedModes);
	}

	@Override
	public SpikeGenerator clone() throws CloneNotSupportedException {
		LIFSpikeGenerator result = (LIFSpikeGenerator) super.clone();
		result.mySupportedModes = new SimulationMode[mySupportedModes.length];
		System.arraycopy(mySupportedModes, 0, result.mySupportedModes, 0, mySupportedModes.length);
		result.myTime = myTime.clone();
		result.myVoltageHistory = myVoltageHistory.clone();
		return result;
	}

	/**
	 * Creates LIFSpikeGenerators.
	 *
	 * @author Bryan Tripp
	 */
	public static class Factory implements SpikeGeneratorFactory {

		private static final long serialVersionUID = 1L;

		private static final float ourMaxTimeStep = .00025f;

		private PDF myTauRC;
		private PDF myTauRef;

		/**
		 * Set reasonable defaults
		 */
		public Factory() {
			myTauRef = new IndicatorPDF(.002f);
			myTauRC = new IndicatorPDF(.02f);
		}

		/**
		 * @return PDF of refractory periods (s)
		 */
		public PDF getTauRef() {
			return myTauRef;
		}

		/**
		 * @param tauRef PDF of refractory periods (s)
		 */
		public void setTauRef(PDF tauRef) {
			myTauRef = tauRef;
		}

		/**
		 * @return PDF of membrane time constants (s)
		 */
		public PDF getTauRC() {
			return myTauRC;
		}

		/**
		 * @param tauRC PDF of membrane time constants (s)
		 */
		public void setTauRC(PDF tauRC) {
			myTauRC = tauRC;
		}

		/**
		 * @see ca.nengo.neural.neuron.impl.SpikeGeneratorFactory#make()
		 */
		public SpikeGenerator make() {
			return new LIFSpikeGenerator(ourMaxTimeStep, myTauRC.sample()[0], myTauRef.sample()[0]);
		}

	}

}
