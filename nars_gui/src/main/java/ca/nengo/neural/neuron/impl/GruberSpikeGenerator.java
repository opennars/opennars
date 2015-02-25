package ca.nengo.neural.neuron.impl;

import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.AbstractDynamicalSystem;
import ca.nengo.dynamics.impl.RK45Integrator;
import ca.nengo.math.CurveFitter;
import ca.nengo.math.Function;
import ca.nengo.math.impl.LinearCurveFitter;
import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.impl.SpikeOutputImpl;
import ca.nengo.neural.neuron.SpikeGenerator;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeries1DImpl;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.Properties;

/**
 * Model of spike generation in medium-spiny striatal neurons from: Gruber, Solla, Surmeier & Houk (2003)
 * Modulation of striatal single units by expected reward: a spiny neuron model displaying dopamine-induced
 * bistability, J Neurophysiol 90: 1095-1114.
 *
 * @author Bryan Tripp
 */
public class GruberSpikeGenerator implements SpikeGenerator, Probeable {

	/**
	 * String that is used for membrane potential
	 */
	public static final String MEMBRANE_POTENTIAL = "Vm";

	private static final long serialVersionUID = 1L;

	private static final float ourResetPotential = -60;
	private static final float Vf = -58;
	private static final float Vf_h = -55;
	private static final float Vf_c = 25f; //this is published as 2.5

	private final GruberDynamics myDynamics;
	private final Integrator myIntegrator;
	private float myDopamine;
	private float myLastSpikeTime;
	private TimeSeries myMembranePotentialHistory;
	private final Function mySteadyStateVmFunction;
	private SimulationMode myMode;
	private final SimulationMode[] mySupportedModes;

	/**
	 * Create a spike generator that follows Gruber et al.'s
	 * medium-spiny striatal neuron model.
	 */
	public GruberSpikeGenerator() {
		myDynamics = new GruberDynamics(ourResetPotential);
		myIntegrator = new RK45Integrator();
		myLastSpikeTime = -1;
		myMembranePotentialHistory = new TimeSeries1DImpl(new float[]{0}, new float[]{0}, Units.mV);
		mySteadyStateVmFunction = getSteadyStateVmFunction();

		myMode = SimulationMode.DEFAULT;
		mySupportedModes = new SimulationMode[]{SimulationMode.DEFAULT, SimulationMode.RATE, SimulationMode.CONSTANT_RATE};
	}

	private Function getSteadyStateVmFunction() {
		CurveFitter fitter = new LinearCurveFitter();

		float[] current = new float[]{0f, .25f, .5f, .75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f, 2.5f, 2.75f, 3f, 3.5f, 4f, 5f, 6f, 8f, 10f, 15f, 20f, 30f, 40f, 50f, 60f};
		float[] Vm = new float[current.length];
		float[] rt = new float[current.length];
		for (int i = 0; i < current.length; i++) {
			TimeSeries input = new TimeSeriesImpl(new float[]{0, 0.5f},
					new float[][]{new float[]{current[i], 1.0f}, new float[]{current[i], 1.0f}}, new Units[]{Units.uAcm2, Units.UNK});
			TimeSeries output = myIntegrator.integrate(myDynamics, input);
			Vm[i] = output.getValues()[output.getValues().length - 1][0];
			reset(false);
//			Plotter.plot(output, "simulation "+i);
			rt[i] = getRefreactoryTime(Vm[i]);
		}

		Function result = fitter.fit(current, Vm);
//		Plotter.plot(result, 0, .1f, 60, "current -> Vm");
//		Plotter.plot(current, rt, "current -> rt");
		return result;
	}

	/**
	 * @param dopamine Dopamine concentration (between 1 and 1.4)
	 */
	public void setDopamine(float dopamine) {
		if (dopamine<0) {
            dopamine=0;
        }
		myDopamine = dopamine;
	}

	/**
	 * @see ca.nengo.neural.neuron.SpikeGenerator#run(float[], float[])
	 */
	public InstantaneousOutput run(float[] time, float[] current) {
		InstantaneousOutput result = null;

		if (myMode.equals(SimulationMode.CONSTANT_RATE)) {
			float Vm = mySteadyStateVmFunction.map(new float[]{current[0]});

			float rate = 0;
			if (Vm > Vf) {
                rate = 1f / getRefreactoryTime(Vm);
            }

			myMembranePotentialHistory = new TimeSeries1DImpl(new float[]{time[0]}, new float[]{Vm}, Units.mV);
			result = new RealOutputImpl(new float[]{rate}, Units.SPIKES_PER_S, time[time.length-1]);
		} else {
			float[][] input = new float[current.length][];
			for (int i = 0; i < input.length; i++) {
				input[i] = new float[]{current[i], myDopamine};
			}

			TimeSeries output = myIntegrator.integrate(myDynamics,
					new TimeSeriesImpl(time, input, new Units[]{Units.uAcm2, Units.UNK}));

			myMembranePotentialHistory = output;

			if (myMode.equals(SimulationMode.RATE)) {
				float Vm = output.getValues()[0][0];
				float rate = (Vm > Vf) ? 1f / getRefreactoryTime(Vm) : 0;
				result = new RealOutputImpl(new float[]{rate}, Units.SPIKES_PER_S, time[time.length-1]);
			} else {
				boolean spike = false;
				for (int i = 0; i < output.getTimes().length && !spike; i++) { //note: this only allows 1 spike / step
					float Vm = output.getValues()[i][0];
					if (Vm > Vf) {
						float refractoryTime = getRefreactoryTime(Vm);
						if (output.getTimes()[i] - myLastSpikeTime >= refractoryTime) {
							spike = true;
							myLastSpikeTime = output.getTimes()[i];
						}
					}
				}
				result = new SpikeOutputImpl(new boolean[]{spike}, Units.SPIKES, time[time.length-1]);
			}
		}

		return result;
	}

	private float getRefreactoryTime(float Vm) {
		return 0.05f * 1f / (1f + (float) Math.exp((Vm - Vf_h)/Vf_c));
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		myDynamics.setState(new float[]{ourResetPotential});
		myLastSpikeTime = -1;
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		if(stateName.equals(MEMBRANE_POTENTIAL)) {
			return myMembranePotentialHistory;
		} else {
			throw new SimulationException("State name " + stateName + " is unknown");
		}
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties p = new Properties();
		p.setProperty(MEMBRANE_POTENTIAL, "Membrane potential (mV)");
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
		throw new CloneNotSupportedException();
	}



	/**
	 * Implements dynamics of Gruber et al. bistable model of medium spiny neuron.
	 * State corresponds to membrane potential, and output is firing rate, as a static
	 * function of membrane potential.
	 *
	 * @author Bryan Tripp
	 */
	public static class GruberDynamics extends AbstractDynamicalSystem {

		private static final long serialVersionUID = 1L;

	    private static final float Cm = 1;
	    private static final float E_K = -90;
	    private static final float g_L  = .008f;

	    private static final float VKir2_h = -111;
	    private static final float VKir2_c = -11;
	    private static final float gbar_Kir2 = 1.2f;

	    private static final float VKsi_h = -13.5f;
	    private static final float VKsi_c = 11.8f;
	    private static final float gbar_Ksi = 0.45f;

	    private static final float VLCa_h = -35;
	    private static final float VLCa_c = 6.1f;
	    private static final float Pbar_LCa = 4.2f;
	    private static final float Ca_o = .002f;
	    private static final float Ca_i = .01f;
	    private static final float R = 8.315f;
	    private static final float F = 96480;
	    private static final float T = 273.15f + 20f; //Kelvin

		/**
		 * @param resetPotential Potential at which membrane starts (is and reset to)
		 */
		public GruberDynamics(float resetPotential) {
			super(new float[]{resetPotential});
		}

		/**
		 * @param u [driving current (~ 0 to 2); dopamine (~ 1 to 1.4)]
		 *
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#f(float, float[])
		 */
		public float[] f(float t, float[] u) {
			float I_s = u[0];
			float mu = u[1];
			float Vm = getState()[0];

		    float L_Kir2 = 1f / (1f + (float) Math.exp(-(Vm-VKir2_h)/VKir2_c));
		    float L_Ksi = 1f / (1f + (float) Math.exp(-(Vm-VKsi_h)/VKsi_c));
		    float L_LCa = 1f / (1f + (float) Math.exp(-(Vm-VLCa_h)/VLCa_c));

		    float P_LCa = Pbar_LCa * L_LCa;

		    float x = (float) Math.exp(-2f*(Vm/1000f)*F/(R*T));

		    float I_Kir2 = gbar_Kir2 * L_Kir2 * (Vm - E_K);
		    float I_Ksi = gbar_Ksi * L_Ksi * (Vm - E_K);
		    float I_LCa = P_LCa * (4f*(Vm/1000f)*F*F/(R*T)) * ( (Ca_i - Ca_o*x) / (1f - x) ) / 700f; //TODO: 700 factor is to match published plot
		    float I_L = g_L * (Vm - E_K);

		    float dVm = -(1000/Cm) * (mu*(I_Kir2 + I_LCa) + I_Ksi + I_L - I_s);

		    return new float[]{dVm};
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#g(float, float[])
		 */
		public float[] g(float t, float[] u) {
			return getState();
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#getInputDimension()
		 */
		public int getInputDimension() {
			return 1;
		}

		/**
		 * @see ca.nengo.dynamics.impl.AbstractDynamicalSystem#getOutputDimension()
		 */
		public int getOutputDimension() {
			return 1;
		}

		@Override
		public Units getOutputUnits(int outputDimension) {
			return Units.mV;
		}

	}

}