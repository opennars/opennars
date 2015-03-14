/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "DecodedOrigin.java". Description:
"An Origin of functions of the state variables of an NEFEnsemble"

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
 * Created on 2-Jun-2006
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.config.PropretiesUtil;
import ca.nengo.config.Configurable;
import ca.nengo.config.Configuration;
import ca.nengo.config.impl.ConfigurationImpl;
import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import ca.nengo.math.impl.FixedSignalFunction;
import ca.nengo.math.impl.WeightedCostApproximator;
import ca.nengo.model.*;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.SpikeOutput;
import ca.nengo.neural.nef.DecodableGroup;
import ca.nengo.neural.nef.ExpressModel;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.plasticity.ShortTermPlastic;
import ca.nengo.util.MU;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.VectorGenerator;
import ca.nengo.util.impl.RandomHypersphereVG;
import ca.nengo.util.impl.TimeSeries1DImpl;
import ca.nengo.util.impl.TimeSeriesImpl;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * An Origin of functions of the state variables of an NEFEnsemble.
 *
 * TODO: how do units fit in. define in constructor? ignore?
 * TODO: select nodes make up decoded origin
 *
 * @author Bryan Tripp
 */
public class DecodedSource implements NSource<InstantaneousOutput>, Resettable, SimulationMode.ModeConfigurable, Noise.Noisy, Configurable, ShortTermPlastic {

	private static final long serialVersionUID = 1L;

	private static final Logger ourLogger = LogManager.getLogger(DecodedSource.class);

	private Node myNode; //parent node
	private String myName;
	private Node[] myNodes;
	private String myNodeOrigin;
	private Function[] myFunctions;
	private float[][] myDecoders;
	private SimulationMode myMode;
	private RealSource myOutput;
	private Noise myNoise = null;
	private Noise[] myNoises = null;
	private DynamicalSystem mySTPDynamicsTemplate;
	private DynamicalSystem[] mySTPDynamics;
	private Integrator myIntegrator;
	private float[] mySTPHistory;
	private float myTime;
	private boolean myRequiredOnCPU;
	private ExpressModel myExpressModel;

	/**
	 * With this constructor, decoding vectors are generated using default settings.
	 *
	 * @param node The parent Node
	 * @param name Name of this Origin
	 * @param nodes Nodes that belong to the NEFEnsemble from which this Origin arises
	 * @param nodeOrigin Name of the Origin on each given node from which output is to be decoded
	 * @param functions Output Functions on the vector that is represented by the NEFEnsemble
	 * 		(one Function per dimension of output). For example if the Origin is to output
	 * 		x1*x2, where the ensemble represents [x1 x1], then one 2D function would be
	 * 		needed in this list. The input dimension of each function must be the same as the
	 * 		dimension of the state vector represented by this ensemble.
	 * @param approximator A LinearApproximator that can be used to approximate new functions as a weighted sum of the node outputs.
	 * @throws StructuralException if functions do not all have the same input dimension (we
	 * 		don't check against the state dimension at this point)
	 */
	public DecodedSource(Node node, String name, Node[] nodes, String nodeOrigin, Function[] functions, LinearApproximator approximator)
			throws StructuralException {

		checkFunctionDimensions(functions);

		myNode = node;
		myName = name;
		myNodes = nodes;
		myNodeOrigin = nodeOrigin;
		myFunctions = functions;
		myDecoders = findDecoders(nodes, functions, approximator);
		myMode = SimulationMode.DEFAULT;
		myIntegrator = new EulerIntegrator(.001f);

		reset(false);
	}

	/**
	 * With this constructor decoding vectors are specified by the caller.
	 *
	 * @param node The parent Node
	 * @param name As in other constructor
	 * @param nodes As in other constructor
	 * @param nodeOrigin Name of the Origin on each given node from which output is to be decoded
	 * @param functions As in other constructor
	 * @param decoders Decoding vectors which are scaled by the main output of each Node, and
	 * 		then summed, to estimate the same function of the ensembles state vector that is
	 * 		defined by the 'functions' arg. The 'functions' arg is still needed, because in DIRECT
	 * 		SimulationMode, these functions are used directly. The 'decoders' arg allows the caller
	 * 		to provide decoders that are generated with non-default methods or parameters (eg an
	 * 		unusual number of singular values). Must be a matrix with one row per Node and one
	 * 		column per function.
	 * @throws StructuralException If dimensions.length != neurons.length, decoders is not a matrix
	 * 		(ie all elements with same length), or if the number of columns in decoders is not equal
	 * 		to the number of functions
	 */
	public DecodedSource(Node node, String name, Node[] nodes, String nodeOrigin, Function[] functions, float[][] decoders, int dummy) throws StructuralException {
		checkFunctionDimensions(functions);

		if (!MU.isMatrix(decoders)) {
			throw new StructuralException("Elements of decoders do not all have the same length");
		}

		if (decoders[0].length != functions.length) {
			throw new StructuralException("Number of decoding functions and dimension of decoding vectors must be the same");
		}

		if (decoders.length != nodes.length) {
			throw new StructuralException("Number of decoding vectors and Neurons must be the same");
		}

		myNode = node;
		myName = name;
		myNodes = nodes;
		myNodeOrigin = nodeOrigin;
		myFunctions = functions;
		myDecoders = decoders;
		myMode = SimulationMode.DEFAULT;
		myIntegrator = new EulerIntegrator(.001f);

		reset(false);
	}
	
	/**
	 * With this constructor the target is a signal over time rather than a function.
	 * 
	 * @param node The parent Node
	 * @param name As in other constructor
	 * @param nodes As in other constructor
	 * @param nodeOrigin Name of the Origin on each given node from which output is to be decoded
	 * @param targetSignal Signal over time that this origin should produce.
	 * @param approximator A LinearApproximator that can be used to approximate new signals as a weighted sum of the node outputs.
	 */
	public DecodedSource(Node node, String name, Node[] nodes, String nodeOrigin, TimeSeries targetSignal, LinearApproximator approximator) {
		
		myNode = node;
		myName = name;
		myNodes = nodes;
		myNodeOrigin = nodeOrigin;
		myFunctions = new FixedSignalFunction[targetSignal.getDimension()];
		for(int i=0; i < targetSignal.getDimension(); i++) //these are only used in direct mode
			myFunctions[i] = new FixedSignalFunction(targetSignal.getValues(), i);
		myDecoders = findDecoders(nodes, MU.transpose(targetSignal.getValues()), approximator);
		myMode = SimulationMode.DEFAULT;
		myIntegrator = new EulerIntegrator(.001f);
		
		reset(false);
	}

	/**
	 * @return Simplified model of deviations from DIRECT mode that are associated with spiking simulations
	 */
	public ExpressModel getExpressModel() {
		return myExpressModel;
	}
	
	/**
	 * @param em Simplified model of deviations from DIRECT mode that are associated with spiking simulations
	 */
	public void setExpressModel(ExpressModel em) {
		myExpressModel = em;
	}
	
	/**
	 * @see ca.nengo.config.Configurable#getConfiguration()
	 */
	public Configuration getConfiguration() {
		ConfigurationImpl result = PropretiesUtil.defaultConfiguration(this);
		//result.renameProperty("sTPDynamics", "STPDynamics");
		return result;
	}

	/**
	 * @return Mean-squared error of this origin over 500 randomly selected points
	 */
	public float[] getError() {
		return getError(500);
	}

	/**
	 * @param samples The number of input vectors the error is sampled over
	 * @return Mean-squared error of this origin over randomly selected points
	 */

	public float[] getError(int samples){
		float[] result = new float[getDimensions()];

		if (myNode instanceof NEFGroup) {
			NEFGroup ensemble = (NEFGroup) myNode;

			VectorGenerator vg = new RandomHypersphereVG(false, 1, 0);
			float[][] unscaled = vg.genVectors(samples, ensemble.getDimension());
			float[][] input = new float[unscaled.length][];
			for (int i = 0; i < input.length; i++) {
				input[i] = MU.prodElementwise(unscaled[i], ensemble.getRadii());
			}

			float[][] idealOutput = NEFUtil.getOutput(this, input, SimulationMode.DIRECT);
			float[][] actualOutput = NEFUtil.getOutput(this, input, SimulationMode.CONSTANT_RATE);

			float[][] error = MU.transpose(MU.difference(actualOutput, idealOutput));
			for (int i = 0; i < error.length; i++) {
				result[i] = MU.prod(error[i], error[i]) / error[i].length;
			}
		} else {
			ourLogger.warn("Can't calculate error of a DecodedOrigin unless it belongs to an NEFEnsemble");
		}

		return result;
	}

	/**
	 * @param noise New output noise model (defaults to no noise)
	 */
	public void setNoise(Noise noise) {
		myNoise = noise;
		myNoises = new Noise[getDimensions()];
		for (int i = 0; i < myNoises.length; i++) {
			myNoises[i] = myNoise.clone();
		}
	}
	
	/**
	 * @param noises New output noise model for each dimension of output
	 * @throws SimulationException 
	 */
	public void setNoises(Noise[] noises) throws SimulationException {
		if(noises.length != getDimensions()) {
			throw new SimulationException("Provided noises do not match dimension of origin");
		}
		myNoise = noises[0];
		myNoises = new Noise[getDimensions()];
        System.arraycopy(noises, 0, myNoises, 0, myNoises.length);
	}

	/**
	 * @return Noise with which output of this Origin is corrupted
	 */
	public Noise getNoise() {
		return myNoise;
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	public void reset(boolean randomize) {
		float time = (myOutput == null) ? 0 : myOutput.getTime();
		myOutput = new RealOutputImpl(new float[myFunctions.length], Units.UNK, time);

		if (myNoise != null) {
            myNoise.reset(randomize);
        }
		if (myNoises != null) {
			for (Noise myNoise2 : myNoises) {
				myNoise2.reset(randomize);
			}
		}

		mySTPHistory = new float[myNodes.length];
	}

	private static float[][] findDecoders(Node[] nodes, Function[] functions, LinearApproximator approximator)  {
		float[][] result = new float[nodes.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[functions.length];
		}

		for (int j = 0; j < functions.length; j++) {
			float[] coeffs = approximator.findCoefficients(functions[j]);
			for (int i = 0; i < nodes.length; i++) {
				result[i][j] = coeffs[i];
			}
		}

		return result;
	}
	
	private static float[][] findDecoders(Node[] nodes, float[][] targetSignal, LinearApproximator approximator)  {
		float[][] result = new float[nodes.length][];
		for (int i = 0; i < result.length; i++) {
			result[i] = new float[targetSignal.length];
		}

		for (int j = 0; j < targetSignal.length; j++) {
			float[] coeffs = ((WeightedCostApproximator)approximator).findCoefficients(targetSignal[j]);
			for (int i = 0; i < nodes.length; i++) {
				result[i][j] = coeffs[i];
			}
		}

		return result;
	}

	private static void checkFunctionDimensions(Function[] functions) throws StructuralException {
		int dim = functions[0].getDimension();
		for (int i = 1; i < functions.length; i++) {
			if (functions[i].getDimension() != dim) {
				throw new StructuralException("Functions must all have the same input dimension");
			}
		}
	}

	/**
	 * @see ca.nengo.model.NSource#getName()
	 */
	public String getName() {
		return myName;
	}

	/**
	 * @see ca.nengo.model.NSource#getDimensions()
	 */
	public int getDimensions() {
		return myFunctions.length;
	}

	/**
	 * @return Decoding vectors for each Node
	 */
	public float[][] getDecoders() {
		return myDecoders;
	}

	/**
	 * @see ca.nengo.neural.plasticity.ShortTermPlastic#getSTPDynamics()
	 */
	public DynamicalSystem getSTPDynamics() {
		try {
			return mySTPDynamicsTemplate == null ? null : mySTPDynamicsTemplate.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Provides access to copy of dynamics for an individual node, to allow node-by-node
	 * parameterization.
	 *
	 * @param i Node number
	 * @return Dynamics of short-term plasticity for the specified node
	 */
	public DynamicalSystem getSTPDynamics(int i) {
		return mySTPDynamics[i];
	}

	/**
	 * @see ca.nengo.neural.plasticity.ShortTermPlastic#setSTPDynamics(ca.nengo.dynamics.DynamicalSystem)
	 */
	public void setSTPDynamics(DynamicalSystem dynamics) {
		if (dynamics == null) {
			mySTPDynamics = new DynamicalSystem[myNodes.length];
		} else {
			if (dynamics.getInputDimension() != 1 || dynamics.getOutputDimension() != 1) {
				throw new IllegalArgumentException("Short-term-plasticity dynamics must be single-input-single-output");
			}

			mySTPDynamics = new DynamicalSystem[myNodes.length];
			try {
				mySTPDynamicsTemplate = dynamics.clone();
				for (int i = 0; i < mySTPDynamics.length; i++) {
					mySTPDynamics[i] = mySTPDynamicsTemplate.clone();
				}
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * @param decoders New decoding vectors (row per Node)
	 */
	public void setDecoders(float[][] decoders) {
		assert MU.isMatrix(decoders);
		assert myDecoders.length == decoders.length;
		assert myDecoders[0].length == decoders[0].length;

		myDecoders = decoders;
	}

	/**
	 * @param mode Requested simulation mode
	 */
	public void setMode(SimulationMode mode) {
		myMode = mode;
	}

	/**
	 * @return The mode in which the Ensemble is currently running.
	 */
	public SimulationMode getMode() {
		return myMode;
	}

	/**
	 * Must be called at each time step after Nodes are run and before getValues().
	 *
	 * @param state Idealized state (as defined by inputs) which can be fed into (idealized) functions
	 * 		that make up the Origin, when it is running in DIRECT mode. This is not used in other modes,
	 * 		and can be null.
	 * @param startTime simulation time of timestep onset
	 * @param endTime simulation time of timestep end
	 * @throws SimulationException If the given state is not of the expected dimension (ie the input
	 * 		dimension of the functions provided in the constructor)
	 */
	public void run(float[] state, float startTime, float endTime) throws SimulationException {
		if (state != null && state.length != myFunctions[0].getDimension()) {
			throw new SimulationException("Origin dimension is " + myFunctions[0].getDimension() + 
					" but state dimension is " + state.length);
		}

		float[] values = new float[myFunctions.length];
		float stepSize = endTime - startTime;

		mySTPHistory = new float[myNodes.length];
		if (myMode == SimulationMode.DIRECT) {
			for (int i = 0; i < values.length; i++) {
				values[i] = myFunctions[i].map(state);
			}
		} else if (myMode == SimulationMode.EXPRESS) {
			for (int i = 0; i < values.length; i++) {
				values[i] = myFunctions[i].map(state);
			}
			
			//create default ExpressModel if necessary ...
			if (myExpressModel == null) {
				myExpressModel = new DefaultExpressModel(this);
			}
			
			values = myExpressModel.getOutput(startTime, state, values);
		} else {
			for (int i = 0; i < myNodes.length; i++) {
				try {
					Object o = myNodes[i].getSource(myNodeOrigin).get();

					float val = 0;
					if (o instanceof SpikeOutput) {
						val = ((SpikeOutput) o).getValues()[0] ? 1f / stepSize : 0f;
					} else if (o instanceof RealSource) {
						val = ((RealSource) o).getValues()[0];
					} else {
						throw new Error("Node output is of type " + o.getClass().getName()
							+ ". DecodedOrigin can only deal with RealOutput and SpikeOutput, so it apparently has to be updated");
					}

					float[] decoder = getDynamicDecoder(i, val, startTime, endTime);
					for (int j = 0; j < values.length; j++) {
						values[j] += val * decoder[j];
					}
				} catch (StructuralException e) {
					throw new SimulationException(e);
				}
			}
		}
		
		if (myNoise != null) {
			for (int i = 0; i < values.length; i++) {
				values[i] = myNoises[i].getValue(startTime, endTime, values[i]);
			}
		}

		myTime = endTime;
		myOutput = new RealOutputImpl(values, Units.UNK, endTime);
	}

	private float[] getDynamicDecoder(int i, float input, float startTime, float endTime) {
		float[] result = myDecoders[i];
		if (mySTPDynamicsTemplate != null) { //TODO: could use a NullDynamics here instead of null (to allow nulling in config tree)
			//TODO: could recycle a mutable time series here to avoid object creation
			TimeSeries inputSeries = new TimeSeries1DImpl(new float[]{startTime, endTime}, new float[]{input, input}, Units.UNK);
			TimeSeries outputSeries = myIntegrator.integrate(mySTPDynamics[i], inputSeries);
			float scaleFactor = outputSeries.getValues()[outputSeries.getValues().length-1][0];
			mySTPHistory[i] = scaleFactor;
			result = MU.prod(result, scaleFactor);
		}
		return result;
	}

	protected TimeSeries getSTPHistory() {
		if (mySTPHistory == null) {
            mySTPHistory = new float[myNodes.length];
        }
		return new TimeSeriesImpl(new float[]{myTime}, new float[][]{mySTPHistory}, Units.uniform(Units.UNK, mySTPHistory.length));
	}

	/**
	 * @see ca.nengo.model.NSource#get()
	 */
	public InstantaneousOutput get()  {
		return myOutput;
	}
	
	/**
	 * @see ca.nengo.model.NSource#setValues()
	 */
	public void accept(InstantaneousOutput val){
		if(val instanceof RealSource)
			myOutput = (RealSource) val;
	}

	/**
	 * @param ro Values to be set
	 */
	public void setValues(RealSource ro) {
		myOutput = ro;
		myTime = ro.getTime();
	}

	/**
	 * @return List of Functions approximated by this DecodedOrigin
	 */
	public Function[] getFunctions() {
		return myFunctions;
	}

	/**
	 * @return Name of Node-level Origin on which this DecodedOrigin is based
	 */
	protected String getNodeOrigin() {
		return myNodeOrigin;
	}

	/**
	 * @see ca.nengo.model.NSource#getNode()
	 */
	public Node getNode() {
		return myNode;
	}

	@Override
	public DecodedSource clone() throws CloneNotSupportedException {
		return this.clone(myNode);
	}
	
	public DecodedSource clone(Node node) throws CloneNotSupportedException {
		if (!(node instanceof DecodableGroup)) {
			throw new CloneNotSupportedException("Error cloning DecodedOrigin: Invalid node type");
		}
				
		try {
			DecodableGroup de = (DecodableGroup) node;

			DecodedSource result = (DecodedSource) super.clone();
			result.setDecoders(MU.clone(myDecoders));
			
			Function[] functions = new Function[myFunctions.length];
			for (int i = 0; i < functions.length; i++) {
				functions[i] = myFunctions[i].clone();
			}
			result.myFunctions = functions;
			
			result.myNodeOrigin = myNodeOrigin;
			result.myNodes = de.getNodes();
			result.myNode = de;
			result.myOutput = (RealSource) myOutput.clone();
            if (myNoise != null) {
			    result.setNoise(myNoise.clone());
            }
			result.setMode(myMode);
			return result;
		} catch (CloneNotSupportedException e) {
			throw new CloneNotSupportedException("Error cloning DecodedOrigin: " + e.getMessage());
		}
	}

	/**
	 * Rescales the decoders.  Useful if the radius changes but you don't want to regenerate the decoders.
	 *
	 * @param scale vector to multiply each decoder by
	 */
	public void rescaleDecoders(float[] scale) {
		for (int i=0;i<myDecoders.length; i++) {
			for (int j=0; j<scale.length; j++) {
				myDecoders[i][j]*=scale[j];
			}
		}
	}

	/**
	 * Recalculates the decoders
	 * @param approximator approximator?
	 */
	public void rebuildDecoder(LinearApproximator approximator) {
		myDecoders = findDecoders(myNodes, myFunctions, approximator);
	}

	/**
	 * Changes the set of nodes and recalculates the decoders
	 * @param nodes Nodes to replace existing nodes
	 * @param approximator approximator?
	 */
	public void redefineNodes(Node[] nodes, LinearApproximator approximator) {
		myNodes=nodes;
		rebuildDecoder(approximator);
	}
	
	public void setRequiredOnCPU(boolean val){
        myRequiredOnCPU = val;
    }
    
    public boolean getRequiredOnCPU(){
        return myRequiredOnCPU;
    }
}
