/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DecodedTermination.java". Description:
"A Termination of decoded state vectors onto an NEFEnsemble"

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

package ca.nengo.neural.nef.impl;

import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.LinearSystem;
import ca.nengo.dynamics.impl.CanonicalModel;
import ca.nengo.dynamics.impl.LTISystem;
import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.neuron.SynapticIntegrator;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import java.util.Properties;

/**
 * <p>A Termination of decoded state vectors onto an NEFEnsemble. A DecodedTermination
 * performs a linear transformation on incoming vectors, mapping them into the
 * space of the NEFEnsemble to which this Termination belongs. A DecodedTermination
 * also applies linear PSC dynamics (typically exponential decay) to the resulting
 * vector.</p>
 *
 * <p>Non-linear dynamics are not allowed at this level. This is because the vector input
 * to an NEFEnsemble only has meaning in terms of the decomposition of synaptic weights
 * into decoding vectors, transformation matrix, and encoding vectors. Linear PSC dynamics
 * actually apply to currents, but if everything is linear we can re-order the dynamics
 * and the encoders for convenience (so that the dynamics seem to operate on the
 * state vectors). In contrast, non-linear dynamics must be modeled within each Neuron,
 * because all inputs to a non-linear dynamical process must be taken into account before
 * the effect of any single input is known.</p>
 *
 * @author Bryan Tripp
 */
public class DecodedTarget implements NTarget<InstantaneousOutput>, Resettable, Probeable {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(DecodedTarget.class);

	/**
	 * Name of Probeable output state.
	 */
	public static final String OUTPUT = "output";

	private Node myNode;
	private String myName;
	private int myOutputDimension;
	private float[][] myTransform;
	private LinearSystem myDynamicsTemplate;
	private LinearSystem[] myDynamics;
	private Integrator myIntegrator;
	private Units[] myNullUnits;
	private RealSource myInputValues;
	private float myTime;
	private float[] myOutputValues;
	private boolean myTauMutable;
	private DecodedTarget myScalingTermination;
	private float[] myStaticBias;
	private float myTau;
	private boolean myModulatory;
	private float[][] myInitialState;
	private boolean myValuesSet;

	/**
	 * @param node The parent Node
	 * @param name The name of this Termination
	 * @param transform A matrix that maps input (which has the dimension of this Termination)
	 * 		onto the state space represented by the NEFEnsemble to which the Termination belongs
	 * @param dynamics Post-synaptic current dynamics (single-input single-output). Time-varying
	 * 		dynamics are OK, but non-linear dynamics don't make sense here, because other
	 * 		Terminations may input onto the same neurons.
	 * @param integrator Numerical integrator with which to solve dynamics
	 * @throws StructuralException If dynamics are not SISO or given transform is not a matrix
	 */
	public DecodedTarget(Node node, String name, float[][] transform, LinearSystem dynamics, Integrator integrator)
			throws StructuralException {

		if (dynamics.getInputDimension() != 1 || dynamics.getOutputDimension() != 1) {
			throw new StructuralException("Dynamics must be single-input single-output");
		}

		myOutputDimension = transform.length;
		setTransform(transform);

		myNode = node;
		myName = name;
		myIntegrator = integrator;

		//we save a little time by not reporting units to the dynamical system at each step
		myNullUnits = new Units[dynamics.getInputDimension()];
		myOutputValues = new float[transform.length];
		
		myValuesSet = false;

		setDynamics(dynamics);
		myScalingTermination = null;
	}

	//copies dynamics for to each dimension
	private synchronized void setDynamics(int dimension) {
		LinearSystem[] newDynamics = new LinearSystem[dimension];
		for (int i = 0; i < newDynamics.length; i++) {
			try {
				newDynamics[i] = (LinearSystem) myDynamicsTemplate.clone();

				//maintain state if there is state
				if (myDynamics != null && myDynamics[i] != null) {
					newDynamics[i].setState(myDynamics[i].getState());
				}
			} catch (CloneNotSupportedException e) {
				throw new Error("The clone() operation is not supported by the given dynamics object");
			}
		}
		myDynamics = newDynamics;

		//zero corresponding initial state if necessary
		if (myInitialState == null || myInitialState[0].length != newDynamics[0].getState().length) {
			initInitialState();
		}
	}

	/**
	 * @param bias Intrinsic bias that is added to inputs to this termination
	 */
	public void setStaticBias(float[] bias) {
		if (bias.length != myTransform.length) {
			throw new IllegalArgumentException("Bias must have length " + myTransform.length);
		}
		myStaticBias = bias;
	}

	/**
	 * @return Static bias vector (a copy)
	 */
	public float[] getStaticBias() {
		float[] result = new float[myStaticBias.length];
		System.arraycopy(myStaticBias, 0, result, 0, result.length);
		return result;
	}

	/**
	 * @param values Only RealOutput is accepted.
	 *
	 * @see ca.nengo.model.NTarget#apply(ca.nengo.model.InstantaneousOutput)
	 */
	public void apply(InstantaneousOutput values) throws SimulationException {
		if (values.getDimension() != getDimensions()) {
			throw new SimulationException("Dimension of input (" + values.getDimension()
					+ ") does not equal dimension of this Termination (" + getDimensions() + ')');
		}

		if ( !(values instanceof RealSource) ) {
			throw new SimulationException("Only real-valued input is accepted at a DecodedTermination");
		}

		RealSource ro = (RealSource) values;
		myInputValues = new RealOutputImpl(MU.sum(ro.getValues(), myStaticBias), ro.getUnits(), ro.getTime());

		if (!myValuesSet) {
            myValuesSet = true;
        }
	}

	/**
	 * @param startTime Simulation time at which running is to start
	 * @param endTime Simulation time at which running is to end
	 */
	public void run(float startTime, float endTime) throws SimulationException {
		if (myDynamics == null) {
			setDynamics(myOutputDimension);
		}

		if (!myValuesSet) {
			ourLogger.warn("Input values not set on termination " + myName + ".  Assuming input of zero.");
			apply(new RealOutputImpl(new float[getDimensions()], Units.UNK, 0.0f));
		}

		float[][] transform = getTransform();
		if (myScalingTermination != null) {
			float scale = myScalingTermination.getOutput()[0];
			transform = MU.prod(transform, scale);
		}
		float[] dynamicsInputs = MU.prod(transform, myInputValues.getValues());
		float[] result = new float[dynamicsInputs.length];

		for (int i = 0; i < myDynamics.length; i++) {
			float[] inVal  = new float[]{dynamicsInputs[i]};
			if(myTau <= endTime-startTime) {
				TimeSeries inSeries = new TimeSeriesImpl(new float[]{startTime, endTime}, new float[][]{inVal, inVal}, myNullUnits);
				TimeSeries outSeries = myIntegrator.integrate(myDynamics[i], inSeries);
				result[i] = outSeries.getValues()[outSeries.getValues().length-1][0];
			}
			else {
				//save the overhead on the integration, and just do it all in one step
				float[] dxdt = myDynamics[i].f(startTime, inVal);
				myDynamics[i].setState(MU.sum(myDynamics[i].getState(), MU.prod(dxdt, endTime-startTime)));
				result[i] = myDynamics[i].g(endTime, inVal)[0];
			}
		}

		myTime = endTime;
		myOutputValues = result;
	}

	/**
	 * This method should be called after run(...).
	 *
	 * @return Output of dynamical system -- of interest at end of run(...)
	 */
	public float[] getOutput() {
		return myOutputValues;
	}

	/**
	 * @return Latest input to Termination (pre transform and dynamics)
	 */
	public RealSource get() {
		return myInputValues;
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
		return myTransform[0].length;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		resetInitialState();
		myInputValues = new RealOutputImpl(new float[getDimensions()], Units.UNK, 0);
		myValuesSet = false;
	}

	private void resetInitialState() {
		for (int i = 0; myDynamics != null && i < myDynamics.length; i++) {
			float[] state = myInitialState != null ? myInitialState[i] : new float[myDynamics[i].getState().length];
			myDynamics[i].setState(state);
		}
	}

	/**
	 * @return Initial states of dynamics (one row per output dimension)
	 */
	public float[][] getInitialState() {
		if (myInitialState == null) {
            initInitialState();
        }
		return MU.clone(myInitialState);
	}

	/**
	 * @param state Initial state of dynamics (dimension of termination output X dimension of dynamics state)
	 */
	public void setInitialState(float[][] state) {
		if (state.length != myDynamics.length) {
			throw new IllegalArgumentException("Must give one state vector for each output dimension");
		}
		if (!MU.isMatrix(state) || state[0].length != myDynamicsTemplate.getState().length) {
			throw new IllegalArgumentException("Each state vector must be length " + myDynamicsTemplate.getState().length);
		}

		myInitialState = state;
		resetInitialState();
	}

	private void initInitialState() {
		myInitialState = new float[myOutputDimension][];
		for (int i = 0; i < myOutputDimension; i++) {
			myInitialState[i] = new float[myDynamics[i].getState().length];
		}
	}

	/**
	 * @return The matrix that maps input (which has the dimension of this Termination)
	 * 		onto the state space represented by the NEFEnsemble to which the Termination belongs
	 */
	public float[][] getTransform() {
		return MU.clone(myTransform);
	}

	/**
	 * @param transform New transform
	 * @throws StructuralException If the transform is not a matrix or has the wrong size
	 */
	public void setTransform(float[][] transform) throws StructuralException {
		if ( !MU.isMatrix(transform) ) {
			throw new StructuralException("Given transform is not a matrix");
		}
		if (transform.length != myOutputDimension) {
			throw new StructuralException("This transform must have " + myOutputDimension + " rows");
		}

		myTransform = transform;

		if  (myStaticBias == null) {
			myStaticBias = new float[transform[0].length];
		} else {
			float[] newStaticBias = new float[transform[0].length];
			System.arraycopy(myStaticBias, 0, newStaticBias, 0, Math.min(myStaticBias.length, newStaticBias.length));
			myStaticBias = newStaticBias;
		}

		if (myDynamics != null && myDynamics.length != transform.length) {
			setDynamics(transform.length);
		}
	}

	/**
	 * @param t Termination to use for scaling?
	 */
	public void setScaling(DecodedTarget t) {
		myScalingTermination = t;
	}

	/**
	 * @return Termination used for scaling?
	 */
	public DecodedTarget getScaling() {
		return myScalingTermination;
	}

	/**
	 * @return The dynamics that govern each dimension of this Termination. Changing the properties
	 * 		of the return value will change dynamics of all dimensions, effective next run time.
	 */
	public LinearSystem getDynamics() {
		myDynamics = null; //caller may change properties so we'll have to re-clone at next run
		return myDynamicsTemplate;
	}

	/**
	 * @param dynamics New dynamics for each dimension of this Termination (effective immediately).
	 * 		This method uses a clone of the given dynamics.
	 */
	public void setDynamics(LinearSystem dynamics) {
		try {
			myDynamicsTemplate = (LinearSystem) dynamics.clone();
			setDynamics(myOutputDimension);

			//PSC time constant can be changed online if dynamics are LTI in controllable-canonical form
			myTauMutable = (dynamics instanceof LTISystem && CanonicalModel.isControllableCanonical((LTISystem) dynamics));

			//find PSC time constant (slowest dynamic mode) if applicable
			if (dynamics instanceof LTISystem) {
				myTau = CanonicalModel.getDominantTimeConstant((LTISystem) dynamics);
			} else {
				myTau = 0;
			}

		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return Slowest time constant of dynamics, if dynamics are LTI, otherwise 0
	 */
	public float getTau() {
		return myTau;
	}

	/**
	 * @param tau New time constant to replace current slowest time constant of dynamics
	 * @throws StructuralException if the dynamics of this Termination are not LTI in controllable
	 * 		canonical form
	 */
	public void setTau(float tau) throws StructuralException {
		if (!myTauMutable) {
			throw new StructuralException("This Termination has immutable dynamics "
				+ "(must be LTI in controllable-canonical form to change time constant online");
		}

		setDynamics(CanonicalModel.changeTimeConstant((LTISystem) myDynamicsTemplate, tau));
	}

	/**
	 * @see ca.nengo.model.NTarget#getModulatory()
	 */
	public boolean getModulatory() {
		return myModulatory;
	}

	/**
	 * @see ca.nengo.model.NTarget#setModulatory(boolean)
	 */
	public void setModulatory(boolean modulatory) {
		myModulatory = modulatory;
	}

	/**
	 * @see ca.nengo.model.Probeable#getHistory(java.lang.String)
	 */
	public TimeSeries getHistory(String stateName) throws SimulationException {
		if (stateName.equals(OUTPUT)) {
			return new TimeSeriesImpl(new float[]{myTime},
					new float[][]{myOutputValues}, Units.uniform(Units.UNK, myOutputValues.length));
		} else {
			throw new SimulationException("The state '" + stateName + "' is unknown");
		}
	}

	/**
	 * @see ca.nengo.model.Probeable#listStates()
	 */
	public Properties listStates() {
		Properties p = new Properties();
		p.setProperty(OUTPUT, "Output of the termination, after static transform and dynamics");
		return p;
	}

	/**
	 * @see ca.nengo.model.NTarget#getNode()
	 */
	public Node getNode() {
		return myNode;
	}

	protected void setNode(Node node) {
		myNode = node;
		if(myIntegrator instanceof SynapticIntegrator)
			((SynapticIntegrator)myIntegrator).setNode(node);
	}

	@Override
	public DecodedTarget clone() throws CloneNotSupportedException {
		return this.clone(myNode);
	}
	
	public DecodedTarget clone(Node node) throws CloneNotSupportedException {
		try {
			DecodedTarget result = (DecodedTarget)super.clone();
			result.setTransform(MU.clone(myTransform));
			result.setDynamics((LinearSystem) myDynamicsTemplate.clone());
			result.myIntegrator = myIntegrator.clone();
			if (myInputValues != null) {
                result.myInputValues = (RealSource) myInputValues.clone();
            }
			if (myOutputValues != null) {
                result.myOutputValues = myOutputValues.clone();
            }
			result.myScalingTermination = myScalingTermination; //refer to same copy
			result.myStaticBias = myStaticBias.clone();
			result.setNode(node);
			return result;
		} catch (StructuralException e) {
			throw new CloneNotSupportedException("Error cloning DecodedTermination: " + e.getMessage());
		}
	}

}
