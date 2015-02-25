/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "HillMuscle.java". Description:
"A Hill-type muscle model.

  Use RootFinder with function f_CE - f_SE, parameter l_SE, range 0 to breaking length"

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
 * Created on 25-Nov-2006
 */
package ca.nengo.neural.muscle.impl;

import ca.nengo.config.Configuration;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.math.Function;
import ca.nengo.math.RootFinder;
import ca.nengo.math.impl.AbstractFunction;
import ca.nengo.math.impl.ConstantFunction;
import ca.nengo.math.impl.NewtonRootFinder;
import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.model.Units;
import ca.nengo.plot.Plotter;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * A Hill-type muscle model.
 *
 * Use RootFinder with function f_CE - f_SE, parameter l_SE, range 0 to breaking length.
 * This finds force given activation and inputs. Could alternatively find dl_CE and have
 * state variables l_CE and activation?
 *
 * TODO: ref Keener & Sneyd
 * TODO: review -- this has been made to compile after model change but it might not make sense
 * TODO: implement getConfiguration()
 *
 * @author Bryan Tripp
 */
public class HillMuscle extends SkeletalMuscleImpl {

	private static final Logger ourLogger = LogManager.getLogger(HillMuscle.class);
	private static final long serialVersionUID = 1L;

	/**
	 * @param name Muscle name
	 * @param tauEA see Hill model
	 * @param maxIsometricForce see Hill model
	 * @param CEForceLength see Hill model
	 * @param CEForceVelocity see Hill model
	 * @param SEForceLength see Hill model
	 * @throws StructuralException if Dynamics creation fails
	 */
	public HillMuscle(String name, float tauEA, float maxIsometricForce, Function CEForceLength,
	        Function CEForceVelocity, Function SEForceLength) throws StructuralException {
		super(name, new Dynamics(tauEA, maxIsometricForce, CEForceLength, CEForceVelocity, SEForceLength, false));
	}

	/**
	 * @param angle Muscle angle
	 * @param velocity Muscle velocity
	 */
	public void setInputs(float angle, float velocity) {
		// TODO Auto-generated method stub

	}

	/**
	 * @return Torque
	 */
	public float getTorque() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void run(float startTime, float endTime) throws SimulationException {
		// TODO Auto-generated method stub

	}

	/**
	 * @param excitation Excitation
	 */
	public void setExcitation(float excitation) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args commandline args
	 */
	public static void main(String[] args) {

		final float rl = 0.2f; //resting length

		float tauEA = .05f;
		Function CEForceLength = new ConstantFunction(1, 1f);

		final float vmax = -2f;
		final float af = 1f; //shaping parameter between 0.1 and 1
		Function CEForceVelocity = new AbstractFunction(1) {
			private static final long serialVersionUID = 1L;
			public float map(float[] from) {
				return Math.min(1.3f, (1 - from[0]/vmax) / (1 + from[0]/(vmax*af)));
			}
		};

		Function SEForceLength = new AbstractFunction(1) {
			private static final long serialVersionUID = 1L;
			public float map(float[] from) {
				return 200f * ( (float) Math.exp(200f*from[0]) - 1f );
			}
		};

		//Plotter.plot(CEForceLength, 0f, rl*.01f, rl*2f, "CE Force-Length");
		//Plotter.plot(CEForceVelocity, vmax, .1f, -vmax, "CE Force-Velocity");
		//Plotter.plot(SEForceLength, 0f, rl*.001f, rl*.1f, "SE Force-Length");

		Dynamics d = new Dynamics(tauEA, 5000f, CEForceLength, CEForceVelocity, SEForceLength, false);
		d.setState(new float[]{.001f, rl});
		Integrator i = new EulerIntegrator(.0001f);

//		TimeSeries input = new TimeSeriesImpl(new float[]{0f, 1f},
//				new float[][]{new float[]{1, rl, 0f}, new float[]{1, rl, 0f}},
//				new Units[]{Units.UNK, Units.M, Units.M_PER_S});
		TimeSeries input = new TimeSeriesImpl(new float[]{0f, .001f, .5f},
				new float[][]{new float[]{1, rl, 0f}, new float[]{0, rl, 0f}, new float[]{0, rl, 0f}},
				new Units[]{Units.UNK, Units.M, Units.M_PER_S});

		long startTime = System.currentTimeMillis();
		TimeSeries output = i.integrate(d, input);
		ourLogger.info("Elapsed time: " + (System.currentTimeMillis() - startTime));

		Plotter.plot(output, "Force");
	}

	/**
	 * Dynamical system for the Hill muscle model
	 */
	public static class Dynamics implements DynamicalSystem {

		private static final long serialVersionUID = 1L;

		private final float myTauEA;
		private final float myMaxIsometricForce;
		private final Function myCEForceLength;
		private final Function myCEForceVelocity;
		private final Function mySEForceLength;

		private final RootFinder myRootFinder;

		private final Units[] myUnits;
		private float[] myState;

		/**
		 * @param tauEA see Hill model
		 * @param maxIsometricForce Isometric force produced by CE at maximal activation and optimal length
		 * @param CEForceLength see Hill model
		 * @param CEForceVelocity see Hill model
		 * @param SEForceLength see Hill model
		 * @param torque true indicates a torque muscle (input in rads, output in Nm); false indicates
		 * 		a linear muscle (input in m, output in N)
		 */
		public Dynamics(float tauEA, float maxIsometricForce, Function CEForceLength, Function CEForceVelocity, Function SEForceLength, boolean torque) {
			myTauEA = tauEA;
			myMaxIsometricForce = maxIsometricForce;
			myCEForceLength = CEForceLength;
			myCEForceVelocity = CEForceVelocity;
			mySEForceLength = SEForceLength;
			myUnits = new Units[]{torque ? Units.Nm : Units.N};

			myRootFinder = new NewtonRootFinder(20, true);
		}


		/**
		 * @return Configuration
		 */
		public Configuration getConfiguration() {
			return null;
		}

		/**
		 * @param t Simulation time (s)
		 * @param u Input: [excitation (0-1), muscle-tendon length, muscle-tendon lengthening velocity]
		 *
		 * @see ca.nengo.dynamics.DynamicalSystem#f(float, float[])
		 */
		public float[] f(float t, float[] u) {
			float a = myState[0]; //activation

			//first-order excitation-activation dynamics ...
			float dadt = (u[0] - a) / myTauEA;

			//CE-SE dynamics
			float lenCE = myState[1];
			float lenSE = u[1] - lenCE;

			float force = mySEForceLength.map(new float[]{lenSE});
			float lm = myCEForceLength.map(new float[]{lenCE}); //length multiplier
			final float vm = Math.min(1.3f, force / (myMaxIsometricForce * a * lm)); //TODO: fix this


			ourLogger.info("force: " + force + " lm: " + lm + " vm: " + vm + " a: " + a + " dadt: " + dadt);

			//find velocity corresponding to this multiplier
			final Function fv = myCEForceVelocity;
			Function f = new AbstractFunction(1) {
				private static final long serialVersionUID = 1L;

				public float map(float[] from) {
					float result = fv.map(from) - vm;
					//System.out.println("from: " + from[0] + " result: " + result);
					return result;
				}
			};
			float dlCEdt = myRootFinder.findRoot(f, -2f, 2f, 0.001f); //velocity of CE

			return new float[]{dadt, dlCEdt};
		}

		/**
		 * @param t Simulation time (s)
		 * @param u Input: [excitation (0-1), muscle-tendon length, muscle-tendon lengthening velocity]
		 *
		 * @see ca.nengo.dynamics.DynamicalSystem#g(float, float[])
		 */
		public float[] g(float t, float[] u) {
			float lenSE = u[1] - myState[1];
			return new float[]{mySEForceLength.map(new float[]{lenSE})};
		}

		/**
		 * @return [activation, CE length]
		 * @see ca.nengo.dynamics.DynamicalSystem#getState()
		 */
		public float[] getState() {
			return myState;
		}

		/**
		 * @param state [activation, CE length]
		 * @see ca.nengo.dynamics.DynamicalSystem#setState(float[])
		 */
		public void setState(float[] state) {
			assert state.length == 2;
			myState = state;
		}

		/**
		 * @return 3 (activation, muscle-tendon length, muscle-tendon velocity)
		 * @see ca.nengo.dynamics.DynamicalSystem#getInputDimension()
		 */
		public int getInputDimension() {
			return 3;
		}

		/**
		 * @return 1 (force)
		 * @see ca.nengo.dynamics.DynamicalSystem#getOutputDimension()
		 */
		public int getOutputDimension() {
			return 1;
		}

		/**
		 * @see ca.nengo.dynamics.DynamicalSystem#getOutputUnits(int)
		 */
		public Units getOutputUnits(int outputDimension) {
			return myUnits[outputDimension];
		}

		/**
		 * @see ca.nengo.dynamics.DynamicalSystem#clone()
		 */
		public DynamicalSystem clone() throws CloneNotSupportedException {
			boolean torque = myUnits[0].equals(Units.Nm);
			return new Dynamics(myTauEA, myMaxIsometricForce, myCEForceLength, myCEForceVelocity, mySEForceLength, torque);
		}

	}

}
