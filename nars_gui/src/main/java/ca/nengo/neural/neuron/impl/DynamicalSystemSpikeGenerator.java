/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DynamicalSystemSpikeGenerator.java". Description:
"A SpikeGenerator for which spiking dynamics are expressed in terms of a DynamicalSystem"

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
 * Created on 2-Apr-07
 */
package ca.nengo.neural.neuron.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.dynamics.impl.SimpleLTISystem;
import ca.nengo.math.CurveFitter;
import ca.nengo.math.Function;
import ca.nengo.math.impl.LinearCurveFitter;
import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeries1DImpl;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.Arrays;
import java.util.Properties;

/**
 * A SpikeGenerator for which spiking dynamics are expressed in terms of a DynamicalSystem.
 *
 * @author Bryan Tripp
 */
public class DynamicalSystemSpikeGenerator implements SpikeGenerator, Probeable {

	private static final long serialVersionUID = 1L;

	/**
	 * Default name for dynamics?
	 */
	public static final String DYNAMICS = "dynamics";

	private DynamicalSystem myDynamics;
	private Integrator myIntegrator;
	private TimeSeries myDynamicsOutput;
	private int myVDim;
	private float mySpikeThreshold;
	private float myMinIntraSpikeTime;
	private float myLastSpikeTime;
	private SimulationMode myMode;
	private SimulationMode[] mySupportedModes;
	private Function myConstantRateFunction;
	private boolean myConstantRateFunctionOK; //false if there are relevant configuration changes since function calculated
	private float[] myCurrents;
	private float myTransientTime;

	/**
	 * @param dynamics A DynamicalSystem that defines the dynamics of spike generation.
	 * @param integrator An integrator with which to simulate the DynamicalSystem
	 * @param voltageDim Dimension of output that corresponds to membrane potential
	 * @param spikeThreshold Threshold membrane potential at which a spike is considered to have occurred
	 * @param minIntraSpikeTime Minimum time between spike onsets. If there appears to be a spike onset at the
	 * 		beginning of a timestep, this value is used to determine whether this is just the continuation of a spike
	 * 		onset that was already registered in the last timestep
	 */
	public DynamicalSystemSpikeGenerator(DynamicalSystem dynamics, Integrator integrator, int voltageDim, float spikeThreshold, float minIntraSpikeTime) {
		myDynamics = dynamics;
		myIntegrator = integrator;
		myVDim = voltageDim;
		mySpikeThreshold = spikeThreshold;
		myMinIntraSpikeTime = minIntraSpikeTime;

		Units[] units = new Units[dynamics.getOutputDimension()];
		for (int i = 0; i < units.length; i++) {
			units[i] = dynamics.getOutputUnits(i);
		}
		myDynamicsOutput = new TimeSeriesImpl(new float[]{0}, MU.uniform(1, dynamics.getOutputDimension(), 0), units);

		myMode = SimulationMode.DEFAULT;
		mySupportedModes = new SimulationMode[]{SimulationMode.DEFAULT};
	}

	/**
	 * Creates a SpikeGenerator that supports CONSTANT_RATE mode. The rate for a given driving current is estimated by
	 * interpolating steady-state spike counts for simulations with different driving currents (given in the currents arg).
	 *
	 * @param dynamics A DynamicalSystem that defines the dynamics of spike generation.
	 * @param integrator An integrator with which to simulate the DynamicalSystem
	 * @param voltageDim Dimension of output that corresponds to membrane potential
	 * @param spikeThreshold Threshold membrane potential at which a spike is considered to have occurred
	 * @param minIntraSpikeTime Minimum time between spike onsets. If there appears to be a spike onset at the
	 * 		beginning of a timestep, this value is used to determine whether this is just the continuation of a spike
	 * 		onset that was already registered in the last timestep
	 * @param currentRange Range of driving currents at which to simulate to find steady-state firing rates for CONSTANT_RATE mode
	 * @param transientTime Simulation time to ignore before counting spikes when finding steady-state rates
	 */
	public DynamicalSystemSpikeGenerator(DynamicalSystem dynamics, Integrator integrator, int voltageDim, float spikeThreshold, float minIntraSpikeTime, float[] currentRange, float transientTime) {
		myDynamics = dynamics;
		myIntegrator = integrator;
		myVDim = voltageDim;
		mySpikeThreshold = spikeThreshold;
		myMinIntraSpikeTime = minIntraSpikeTime;
		setCurrentRange(new float[]{currentRange[0], currentRange[currentRange.length-1]});
		myTransientTime = transientTime;
		setConstantRateFunction();

		Units[] units = new Units[dynamics.getOutputDimension()];
		for (int i = 0; i < units.length; i++) {
			units[i] = dynamics.getOutputUnits(i);
		}
		myDynamicsOutput = new TimeSeriesImpl(new float[]{0}, MU.uniform(1, dynamics.getOutputDimension(), 0), units);

		myMode = SimulationMode.DEFAULT;
		mySupportedModes = new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.CONSTANT_RATE};
	}

	/**
	 * Uses default parameters to allow later configuration.
	 */
	public DynamicalSystemSpikeGenerator() {
		this(getDefaultDynamics(), new EulerIntegrator(.001f), 0, 0.99f, .002f, new float[]{0, 1}, .5f);
	}

	/**
	 * @return A DynamicalSystem that defines the dynamics of spike generation.
	 */
	public DynamicalSystem getDynamics() {
		return myDynamics;
	}

	/**
	 * @param dynamics A DynamicalSystem that defines the dynamics of spike generation.
	 */
	public void setDynamics(DynamicalSystem dynamics) {
		myDynamics = dynamics;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return An integrator with which to simulate the DynamicalSystem
	 */
	public Integrator getIntegrator() {
		return myIntegrator;
	}

	/**
	 * @param integrator An integrator with which to simulate the DynamicalSystem
	 */
	public void setIntegrator(Integrator integrator) {
		myIntegrator = integrator;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return Dimension of output that corresponds to membrane potential
	 */
	public int getVoltageDim() {
		return myVDim;
	}

	/**
	 * @param dim Dimension of output that corresponds to membrane potential
	 */
	public void setVoltageDim(int dim) {
		if (dim < 0 || dim >= myDynamics.getOutputDimension()) {
			throw new IllegalArgumentException(dim
					+ " is out of range for dynamics with output of dimension " + myDynamics.getOutputDimension());
		}
		myVDim = dim;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return Threshold membrane potential at which a spike is considered to have occurred
	 */
	public float getSpikeThreshold() {
		return mySpikeThreshold;
	}

	/**
	 * @param threshold Threshold membrane potential at which a spike is considered to have occurred
	 */
	public void setSpikeThreshold(float threshold) {
		mySpikeThreshold = threshold;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return Minimum time between spike onsets. If there appears to be a spike onset at the
	 * 		beginning of a timestep, this value is used to determine whether this is just the continuation of a spike
	 * 		onset that was already registered in the last timestep
	 */
	public float getMinIntraSpikeTime() {
		return myMinIntraSpikeTime;
	}

	/**
	 * @param min Minimum time between spike onsets.
	 */
	public void setMinIntraSpikeTime(float min) {
		myMinIntraSpikeTime = min;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return Range of driving currents at which to simulate to find steady-state firing rates for CONSTANT_RATE mode
	 */
	public float[] getCurrentRange() {
		return new float[]{myCurrents[0], myCurrents[myCurrents.length - 1]};
	}

	/**
	 * @param range Range of driving currents at which to simulate to find steady-state firing rates for CONSTANT_RATE mode
	 */
	public void setCurrentRange(float[] range) {
		if (range.length != 2) {
			throw new IllegalArgumentException("Expected range of length 2: [low high]");
		}
		myCurrents = MU.makeVector(range[0], (range[1]-range[0])/20f, range[1]);
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return Simulation time to ignore before counting spikes when finding steady-state rates
	 */
	public float getTransientTime() {
		return myTransientTime;
	}

	/**
	 * @param transientTime Simulation time to ignore before counting spikes when finding steady-state rates
	 */
	public void setTransientTime(float transientTime) {
		myTransientTime = transientTime;
		myConstantRateFunctionOK = false;
	}

	/**
	 * @return True if this SpikeGenerator supports CONSTANT_RATE simulation mode
	 */
	public boolean getConstantRateModeSupported() {
		return mySupportedModes.length == 2;
	}

	private static DynamicalSystem getDefaultDynamics() {
		return new SimpleLTISystem(
				new float[]{2*(float)Math.PI, 2*(float)Math.PI},
				new float[][]{new float[1], new float[]{1}},
				new float[][]{new float[]{1, 0}},
				new float[2],
				Units.uniform(Units.UNK, 1));
	}

	private void setConstantRateFunction() {
		//make sure currents are in ascending order
		Arrays.sort(myCurrents);

		SimulationMode mode = myMode;
		myMode = SimulationMode.DEFAULT;
		float dt = .001f;
		float simTime = 1f;
		float[] rates = new float[myCurrents.length];
		for (int i = 0; i < myCurrents.length; i++) {
			countSpikes(myCurrents[i], dt, myTransientTime);
			rates[i] = countSpikes(myCurrents[i], dt, simTime) / simTime;
		}

		CurveFitter cf = new LinearCurveFitter();
		Function result = cf.fit(myCurrents, rates);
		myConstantRateFunction = result;
		myConstantRateFunctionOK = true;
		myMode = mode;
	}

	private int countSpikes(float current, float dt, float time) {
		int steps = (int) Math.ceil(time / dt);
		int spikes = 0;
		for (int i = 0; i < steps; i++) {
			SpikeOutput output = (SpikeOutput) run(new float[]{i*dt, (i+1)*dt}, new float[]{current, current});
			if (output.getValues()[0]) {
                spikes += 1;
            }
		}
		return spikes;
	}

	/**
	 * Runs the spike generation dynamics and returns a spike if membrane potential rises above spike threshold.
	 *
	 * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
	 */
	public InstantaneousOutput run(float[] time, float[] current) {
		if (myMode.equals(SimulationMode.CONSTANT_RATE)) {
			if (!myConstantRateFunctionOK) {
                setConstantRateFunction();
            }
			float rate = myConstantRateFunction.map(new float[]{current[current.length-1]});
			return new RealOutputImpl(new float[]{rate}, Units.SPIKES_PER_S, time[time.length-1]);
		} else {
			boolean spike = false;

			myDynamicsOutput = myIntegrator.integrate(myDynamics, new TimeSeries1DImpl(time, current, Units.uAcm2));
			float[][] values = myDynamicsOutput.getValues();

			for (int i = 0; i < values.length && !spike; i++) {
				if (values[i][myVDim] >= mySpikeThreshold) {
					boolean crossingThreshold = i > 0 && values[i-1][myVDim] < mySpikeThreshold;
					boolean nonDuplicateAtStart
						= i == 0 && values[1][myVDim] > values[0][myVDim] && myDynamicsOutput.getTimes()[0] > myLastSpikeTime + myMinIntraSpikeTime;

					if (crossingThreshold || nonDuplicateAtStart) {
						spike = true;
						myLastSpikeTime = myDynamicsOutput.getTimes()[i];
					}
				}
			}

			return new SpikeOutputImpl(new boolean[]{spike}, Units.SPIKES, time[time.length-1]);
		}
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myDynamics.setState(new float[myDynamics.getState().length]);
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

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		if (stateName.equals(DYNAMICS)) {
			return myDynamicsOutput;
		} else {
			throw new SimulationException("Unknown state: " + stateName);
		}
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties result = new Properties();
		result.setProperty(DYNAMICS, "Result of spike generation dynamics");
		return result;
	}

	@Override
	public SpikeGenerator clone() throws CloneNotSupportedException {
		DynamicalSystemSpikeGenerator result = (DynamicalSystemSpikeGenerator) super.clone();
		result.myConstantRateFunction = myConstantRateFunction.clone();
		result.myCurrents = myCurrents.clone();
		result.myDynamics = myDynamics.clone();
		result.myDynamicsOutput = myDynamicsOutput.clone();
		result.myIntegrator = myIntegrator.clone();

		result.mySupportedModes = new SimulationMode[mySupportedModes.length];
		System.arraycopy(mySupportedModes, 0, result.mySupportedModes, 0, mySupportedModes.length);

		return result;
	}

}
