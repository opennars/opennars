/*
The contents of this file are subject to the Mozilla Public License Version 1.1
(the "License"); you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis, WITHOUT
WARRANTY OF ANY KIND, either express or implied. See the License for the specific
language governing rights and limitations under the License.

The Original Code is "NEFEnsembleImpl.java". Description:
"Default implementation of NEFEnsemble"

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
 * Created on 31-May-2006
 */
package ca.nengo.neural.nef.impl;

import ca.nengo.dynamics.DynamicalSystem;
import ca.nengo.dynamics.Integrator;
import ca.nengo.dynamics.LinearSystem;
import ca.nengo.dynamics.impl.EulerIntegrator;
import ca.nengo.dynamics.impl.SimpleLTISystem;
import ca.nengo.math.ApproximatorFactory;
import ca.nengo.math.Function;
import ca.nengo.math.LinearApproximator;
import ca.nengo.math.impl.IndicatorPDF;
import ca.nengo.math.impl.WeightedCostApproximator;
import ca.nengo.model.*;
import ca.nengo.model.impl.NodeFactory;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.NEFGroupFactory;
import ca.nengo.neural.nef.NEFNode;
import ca.nengo.neural.neuron.Neuron;
import ca.nengo.neural.neuron.impl.LIFNeuronFactory;
import ca.nengo.neural.neuron.impl.LIFSpikeGenerator;
import ca.nengo.neural.neuron.impl.SpikeGeneratorSource;
import ca.nengo.neural.neuron.impl.SpikingNeuron;
import ca.nengo.neural.plasticity.impl.*;
import ca.nengo.util.MU;
import ca.nengo.util.ScriptGenException;
import ca.nengo.util.TimeSeries;
import ca.nengo.util.impl.LearningTask;
import ca.nengo.util.impl.TimeSeriesImpl;

import java.util.*;
/**
 * Default implementation of NEFEnsemble.
 *
 * TODO: links to NEF documentation
 * TODO: test
 *
 * @author Bryan Tripp
 */
public class NEFGroupImpl extends DecodableGroupImpl implements NEFGroup {

	//private static Logger ourLogger = Logger.getLogger(NEFEnsembleImpl.class);

	private static final long serialVersionUID = 1L;

	/**
	 * Append to bias termination names
	 */
	public static final String BIAS_SUFFIX = " (bias)";


	/**
	 * Append to interneuron names
	 */
	public static final String INTERNEURON_SUFFIX = " (interneuron)";

	private final int myDimension;
	private float[][] myEncoders;

	private Map<String, LinearApproximator> myDecodingApproximators;
	private boolean myReuseApproximators;
	private float[][] myUnscaledEvalPoints;
	private float[][] myEvalPoints;
	private float[] myRadii;
	private float[] myInverseRadii;
	private boolean myRadiiAreOne;
	private DynamicalSystem myDirectModeDynamics;
	private Integrator myDirectModeIntegrator;
	private List<SimulationMode> myFixedModes;

	private NEFGroupFactory myEnsembleFactory;

	private boolean myUseGPU;

	/**
	 * @param name Unique name of Ensemble
	 * @param nodes Nodes that make up the Ensemble
	 * @param encoders List of encoding vectors (one for each node). All must have same length
	 * @param factory Source of LinearApproximators to use in decoding output
	 * @param evalPoints Vector inputs at which output is found to produce DecodedOrigins
	 * @param radii Radius for each dimension
	 * @throws StructuralException if there are a different number of Nodes than encoding vectors or if not
	 * 		all encoders have the same length
	 */
	public NEFGroupImpl(String name, NEFNode[] nodes, float[][] encoders, ApproximatorFactory factory, float[][] evalPoints, float[] radii)
			throws StructuralException {

		super(name, nodes, factory);

		if (nodes.length != encoders.length) {
			throw new StructuralException("There are " + nodes.length + " Nodes but "
					+ encoders.length + " encoding vectors");
		}

		myDimension = encoders[0].length;
		for (int i = 1; i < encoders.length; i++) {
			if (encoders[i].length != myDimension) {
				throw new StructuralException("Encoders have different lengths");
			}
		}
		myEncoders = encoders;

		myDecodingApproximators = new HashMap<String, LinearApproximator>(10);
		myReuseApproximators = true;
		myUnscaledEvalPoints = evalPoints;
		setRadii(radii);
		myFixedModes = null;

		myDirectModeIntegrator = new EulerIntegrator(.001f);

		myUseGPU = true;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroup#getRadii()
	 */
    public float[] getRadii() {
		return myRadii.clone();
	}

	/**
	 * @param use Use GPU?
	 */
	public void setUseGPU(boolean use) {
		myUseGPU = use;
	}

	/**
	 * @return Using GPU?
	 */
	public boolean getUseGPU() {
		return myUseGPU && (getMode() == SimulationMode.DEFAULT || getMode() == SimulationMode.RATE);
	}

	/**
	 * @param radii A list of radii of encoded area along each dimension; uniform
	 * 		radius along each dimension can be specified with a list of length 1
	 * @throws StructuralException if getConstantOutputs throws exception
	 */
	public void setRadii(float[] radii) throws StructuralException {

		if (radii.length != getDimension() && radii.length != 1) {
			throw new IllegalArgumentException("radius vector must have length " + getDimension()
					+ " or 1 (for uniform radius)");
		}

		if (radii.length == 1 && getDimension() != 1) {
			float uniformRadius = radii[0];
			radii = MU.uniform(1, getDimension(), uniformRadius)[0];
		}

		myEvalPoints = new float[myUnscaledEvalPoints.length][];
		for (int i = 0; i < myUnscaledEvalPoints.length; i++) {
			myEvalPoints[i] = new float[myUnscaledEvalPoints[i].length];
			for (int j = 0; j < myUnscaledEvalPoints[i].length; j++) {
				myEvalPoints[i][j] = myUnscaledEvalPoints[i][j] * radii[j];
			}
		}



		float[] oldRadii=null;
		if (myRadii!=null) {
			oldRadii=new float[radii.length];
            System.arraycopy(myRadii, 0, oldRadii, 0, radii.length);
		}


		myRadii = radii;

		myInverseRadii = new float[radii.length];
		myRadiiAreOne = true;
		for (int i = 0; i < radii.length; i++) {
			myInverseRadii[i] = 1f / radii[i];
			if (Math.abs(radii[i]-1f) > 1e-10) {
                myRadiiAreOne = false;
            }
		}

		myDecodingApproximators.clear();

		// update the decoders for any existing origins
		NSource[] sources = getSources();
		for (NSource source2 : sources) {
			if (source2 instanceof DecodedSource) {
				DecodedSource origin=((DecodedSource) source2);
				if (oldRadii!=null && origin.getName().equals(NEFGroup.X)) {
					// Just rescale the X origin
					float scale[]=new float[radii.length];
					for (int j=0; j<radii.length; j++) {
						scale[j]=myRadii[j]/oldRadii[j];
					}
					origin.rescaleDecoders(scale);
				} else {
					String nodeOrigin=origin.getNodeOrigin();
					// recalculate the decoders
					if (!myReuseApproximators || !myDecodingApproximators.containsKey(nodeOrigin)) {
						float[][] outputs = getConstantOutputs(myEvalPoints, nodeOrigin);
						LinearApproximator approximator = getApproximatorFactory().getApproximator(myEvalPoints, outputs);
						myDecodingApproximators.put(nodeOrigin, approximator);
					}

					origin.rebuildDecoder(myDecodingApproximators.get(nodeOrigin));
				}
				
				if (origin.getExpressModel() != null) {
					try {
						origin.getExpressModel().update();
					} catch (SimulationException e) {
						throw new StructuralException("Can't update ExpressModel for radius change", e);
					}
				}
			}
		}


	}

	/**
	 * Note: by-products of decoding are sometimes cached, so if these are changed it may be
	 * necessary to call setReuseApproximators(false) for the change to take effect.
	 *
	 * @param evalPoints Points in the encoded space at which node outputs are evaluated for
	 * 		establishing new DecodedOrigins.
	 */
	public void setEvalPoints(float[][] evalPoints) {
		if (!MU.isMatrix(evalPoints) || evalPoints[0].length != getDimension()) {
			throw new IllegalArgumentException("Expected eval points of length "
					+ getDimension() + " (was " + evalPoints[0].length + ')');
		}

		myEvalPoints = evalPoints;
	}

    /**
     * @return a copy of the evaluation points
     */
    public float[][] getEvalPoints(){
        return myEvalPoints.clone();
    }

	/**
	 * @param dynamics DynamicalSystem that models internal neuron dynamics at the ensemble level, when
	 * 		the ensemble runs in direct mode. The input and output dimensions must equal the dimension of the
	 * 		ensemble.
	 */
	public void setDirectModeDynamics(DynamicalSystem dynamics) {
		if (dynamics != null &&
				(dynamics.getInputDimension() != getDimension() || dynamics.getOutputDimension() != getDimension())) {
			throw new IllegalArgumentException("Input and output dimensions must be " + getDimension());
		}

		myDirectModeDynamics = dynamics;
	}

	/**
	 * @return Dynamics that apply in direct mode
	 */
	public DynamicalSystem getDirectModeDynamics() {
		return myDirectModeDynamics;
	}

	/**
	 * @return Integrator used in direct mode
	 */
	public Integrator getDirectModeIntegrator() {
		return myDirectModeIntegrator;
	}

	/**
	 * @param integrator Integrator to use in direct mode
	 */
	public void setDirectModeIntegrator(Integrator integrator) {
		myDirectModeIntegrator = integrator;
	}

	/**
	 * @param evalPoints Vector points at which to find output (each one must have same dimension as
	 * 		encoder)
	 * @param origin Name of Origin from which to collect output for each Node
	 * @return Output of each Node at each evaluation point (1st dimension corresponds to Node)
	 * @throws StructuralException If CONSTANT_RATE is not supported by any Node
	 */
	protected float[][] getConstantOutputs(float[][] evalPoints, String origin) throws StructuralException {
		NEFNode[] nodes = (NEFNode[]) getNodes();
		float[][] result = new float[nodes.length][];

		for (int i = 0; i < nodes.length; i++) {
			try {
				result[i] = getConstantOutput(i, evalPoints, origin);
			} catch (SimulationException e) {
				throw new StructuralException("Node " + i + " does not have the Origin " + origin);
			}
		}

		return result;
	}

	/**
	 * @param nodeIndex Index of Node for which to find output at various inputs
	 * @param evalPoints Vector points at which to find output (each one must have same dimension as
	 * 		encoder)
	 * @param origin Name of Origin from which to collect output
	 * @return Output of indexed Node at each evaluation point
	 * @throws StructuralException If CONSTANT_RATE is not supported by the given Node
	 * @throws SimulationException If the Node does not have an Origin with the given name
	 */
	protected float[] getConstantOutput(int nodeIndex, float[][] evalPoints, String origin)
			throws StructuralException, SimulationException {

		float[] result = new float[evalPoints.length];

		NEFNode node = (NEFNode) getNodes()[nodeIndex];
		synchronized (node) {
			SimulationMode mode = node.getMode();

			node.setMode(SimulationMode.CONSTANT_RATE);
			if ( !node.getMode().equals(SimulationMode.CONSTANT_RATE) ) {
				throw new StructuralException(
					"To find decoders using this method, all Nodes must support CONSTANT_RATE simulation mode");
			}

			for (int i = 0; i < result.length; i++) {
				node.setRadialInput(getRadialInput(evalPoints[i], nodeIndex));

				node.run(0f, 0f);

				RealSource output = (RealSource) node.getSource(origin).get();
				result[i] = output.getValues()[0];
			}

			node.setMode(mode);
		}

		return result;
	}
	
	/**
	 * Similar to getConstantOutputs, but uses a time series as input to each neuron rather than a single point.
	 * 
	 * @param evalPoints Signals over which to evaluate outputs.  Each signal can have dimension
	 * 			equal to the number of nodes in the population (each dimension is the input to one node),
	 * 			or dimension equal to the dimension of this population (a single input for the whole population).
	 * @param origin Name of Origin from which to collect output for each Node
	 * @return Output of each Node over each evaluation signal (1st dimension corresponds to Node, 2nd to signal, 3rd to time)
	 * @throws StructuralException If RATE is not supported by any Node
	 */
	protected float[][][] getSignalOutputs(TimeSeries[] evalSignals, String origin) throws StructuralException
	{
		NEFNode[] nodes = (NEFNode[]) getNodes();
		float[][][] result = new float[nodes.length][evalSignals.length][evalSignals[0].getTimes().length];
		
		for (int i = 0; i < nodes.length; i++) {
			float[][] output;
			try {
				output = getSignalOutput(i, evalSignals, origin);
			} catch (SimulationException e) {
				throw new StructuralException("Node " + i + " does not have the Origin " + origin);
			}

            System.arraycopy(output, 0, result[i], 0, output.length);
		}

		return result;
		
	}
	
	/**
	 * Similar to getConstantOutput, but uses a time series as input to each neuron rather than a single point.
	 * 
	 * @param nodeIndex Index of Node for which to find output at various inputs
	 * @param evalPoints Signals over which to evaluate outputs.  Each signal can have dimension
	 * 			equal to the number of nodes in the population (each dimension is the input to one node),
	 * 			or dimension equal to the dimension of this population (a single input for the whole population).
	 * @param origin Name of Origin from which to collect output
	 * @return Output of indexed Node over each evaluation signal.
	 * @throws StructuralException If RATE is not supported by the given Node
	 * @throws SimulationException If the Node does not have an Origin with the given name
	 */
	protected float[][] getSignalOutput(int nodeIndex, TimeSeries[] evalSignals, String origin) throws StructuralException, SimulationException
	{
		float[][] result = new float[evalSignals.length][];

		NEFNode node = (NEFNode) getNodes()[nodeIndex];
		synchronized (node) {
			SimulationMode mode = node.getMode();

			node.setMode(SimulationMode.RATE);
			if ( !node.getMode().equals(SimulationMode.RATE) ) {
				throw new StructuralException(
					"To find decoders using this method, all Nodes must support RATE simulation mode");
			}

			for (int i = 0; i < evalSignals.length; i++) {
				node.reset(false);
				float[][] vals = evalSignals[i].getValues();
				float[] times = evalSignals[i].getTimes();
				float dt = times[1] - times[0]; //note: we assume dt is the same across the signal
				
				result[i] = new float[times.length];
				for(int t=0; t < times.length; t++)
				{
					//two possibilities: evaluation signal represents a separate signal for each node (first case),
					//		or evaluation signal presents a single value and a separate input is calculated for each
					//		node using that node's encoder.
					if(vals[t].length == getNodes().length) 
						node.setRadialInput(vals[t][nodeIndex]);
					else
						node.setRadialInput(getRadialInput(vals[t], nodeIndex));
						
	
					node.run(times[t], times[t]+dt);
	
					RealSource output = (RealSource) node.getSource(origin).get();
					result[i][t] = output.getValues()[0];
				}
			}

			node.setMode(mode);
		}

		return result;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroup#getDimension()
	 */
    @Override
    public int getDimension() {
		return myDimension;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroup#getEncoders()
	 */
    public float[][] getEncoders() {
		return MU.clone(myEncoders);
	}

	/**
	 * @param encoders New encoding vectors (row per Node)
	 */
	public void setEncoders(float[][] encoders) {
		assert MU.isMatrix(encoders);
		assert encoders.length == getNodes().length;
		assert encoders[0].length == getDimension();

		myEncoders = encoders;
	}

	/**
	 * @return True if LinearApproximators for a Node Origin are re-used for decoding multiple decoded Origins.
	 */
	public boolean getReuseApproximators() {
		return myReuseApproximators;
	}

	/**
	 * @param reuse True if LinearApproximators for a Node Origin are re-used for decoding multiple decoded Origins.
	 */
	public void setReuseApproximators(boolean reuse) {
		myReuseApproximators = reuse;
	}

	/**
	 * @see ca.nengo.neural.nef.NEFGroup#addDecodedOrigin(java.lang.String, Function[], String)
	 */
    public NSource addDecodedOrigin(String name, Function[] functions, String nodeOrigin) throws StructuralException {
		if (!myReuseApproximators || !myDecodingApproximators.containsKey(nodeOrigin)) {
			float[][] outputs = getConstantOutputs(myEvalPoints, nodeOrigin);
			LinearApproximator approximator = getApproximatorFactory().getApproximator(myEvalPoints, outputs);
			myDecodingApproximators.put(nodeOrigin, approximator);
		}

		DecodedSource result = new DecodedSource(this, name, getNodes(), nodeOrigin, functions, myDecodingApproximators.get(nodeOrigin));
		
		return addDecodedOrigin(result);
	}
    
    /**
     * Similar to addDecodedOrigin, but uses a target signal and evaluation signals (over time) rather than a target function
     * and evaluation points.
     * 
     * @param name Name of origin
     * @param targetSignal signal that the origin should produce
     * @param evalSignals evaluation signals used to calculate decoders
     * @param nodeOrigin origin from which to draw output from each node
     * @return the new DecodedOrigin created
     */
    public NSource addDecodedSignalOrigin(String name, TimeSeries targetSignal, TimeSeries[] evalSignals, String nodeOrigin) throws StructuralException {
    	float[][][] evalSignalsF = new float[evalSignals.length][][];
    	for(int i=0; i < evalSignals.length; i++)
    		evalSignalsF[i] = MU.transpose(evalSignals[i].getValues());
    	
    	
    	float[][][] outputs = getSignalOutputs(evalSignals, nodeOrigin);
    	LinearApproximator approximator = ((WeightedCostApproximator.Factory)getApproximatorFactory()).getApproximator(evalSignalsF, outputs);
    	
    	DecodedSource result = new DecodedSource(this, name, getNodes(), nodeOrigin, targetSignal, approximator);
    	
    	return addDecodedOrigin(result);
    }
    
    /**
     * Adds the given DecodedOrigin to this ensemble.
     * 
     * @param o the origin to be added
     * @return the new origin
     */
    public NSource addDecodedOrigin(DecodedSource o) {
    	o.setMode(getMode());
    	myDecodedOrigins.put(o.getName(), o);
    	fireVisibleChangeEvent();
    	return o;
    }

	/**
	 * @see ca.nengo.neural.nef.NEFGroup#addBiasOrigin(ca.nengo.model.NSource, int, java.lang.String, boolean)
	 */
    public BiasSource addBiasOrigin(NSource existing, int numInterneurons, String name, boolean excitatory) throws StructuralException {
		if ( !(existing instanceof DecodedSource) ) {
			throw new StructuralException("A DecodedOrigin is needed to make a BiasOrigin");
		}

		DecodedSource o = (DecodedSource) existing;
		BiasSource result = new BiasSource(this, name, getNodes(), o.getNodeOrigin(),
				getConstantOutputs(myEvalPoints, o.getNodeOrigin()), numInterneurons, excitatory);
		result.setMode(getMode());
		if (getSource(name)!=null) {
			removeDecodedOrigin(name);
		}

		myDecodedOrigins.put(result.getName(), result);
		fireVisibleChangeEvent();
		return result;
	}

	@Override
    public NTarget addDecodedTermination(String name, float[][] matrix, float tauPSC,
            boolean isModulatory) throws StructuralException {
    	 if (matrix.length != myDimension) {
             throw new StructuralException("Output dimension " + matrix.length + " doesn't equal ensemble dimension " + myDimension);
         }
    	 return super.addDecodedTermination(name, matrix, tauPSC, isModulatory);
	}

	@Override
    public NTarget addDecodedTermination(String name, float[][] matrix, float[] tfNumerator, float[] tfDenominator,
            float passthrough, boolean isModulatory) throws StructuralException {
	    if (matrix.length != myDimension) {
	        throw new StructuralException("Output dimension " + matrix.length + " doesn't equal ensemble dimension " + myDimension);
	    }
	    return super.addDecodedTermination(name, matrix, tfNumerator,tfDenominator, passthrough, isModulatory);
	}

   /**
     * @param name Unique name for the Termination (in the scope of this Node)
     * @param weights Each row is used as a 1 by m matrix of weights in a new termination on the nth expandable node
     * @param tauPSC Time constant with which incoming signals are filtered. (All Terminations have
     *      this property, but it may have slightly different interpretations per implementation.)
     * @param modulatory If true, inputs to the Termination are not summed with other inputs (they
     *      only have modulatory effects, eg on plasticity, which must be defined elsewhere).
     * @return Termination that was added
     * @throws StructuralException if weight matrix dimensionality is incorrect
     * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
     */
    public synchronized NTarget addPESTermination(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException {
        //TODO: check name for duplicate
        if (myExpandableNodes.length != weights.length) {
            throw new StructuralException(weights.length + " sets of weights given for "
                    + myExpandableNodes.length + " expandable nodes");
        }

        int dimension = weights[0].length;

        NTarget[] components = new NTarget[myExpandableNodes.length];
        for (int i = 0; i < myExpandableNodes.length; i++) {
            if (weights[i].length != dimension) {
                throw new StructuralException("Equal numbers of weights are needed for termination onto each node");
            }

            components[i] = myExpandableNodes[i].addTarget(name, new float[][]{weights[i]}, tauPSC, modulatory);
        }

        PlasticGroupTarget result;

        // Make sure that the components are plastic, otherwise make a non-plastic termination
        if (isPopulationPlastic(components)) {
            PlasticNodeTarget[] pnts = new PlasticNodeTarget[components.length];
            for (int i=0; i<components.length; i++) {
                pnts[i] = (PlasticNodeTarget) components[i];
            }

            result = new PESTarget(this, name, pnts);

            // Set the number of tasks equal to the number of threads
            int numTasks = ca.nengo.util.impl.NodeThreadPool.getNumJavaThreads();
            numTasks = numTasks < 1 ? 1 : numTasks;

            LearningTask[] tasks = new LearningTask[numTasks];

            int termsPerTask = (int) Math.ceil((float) components.length / (float) numTasks);
            int termOffset = 0;
            int termStartIndex, termEndIndex;

            for (int i = 0; i < numTasks; i++) {
                termStartIndex = termOffset;
                termEndIndex = components.length - termOffset >= termsPerTask ? termOffset + termsPerTask : components.length;
                termOffset += termsPerTask;

                tasks[i] = new LearningTask(this, result, termStartIndex, termEndIndex);
            }
            addTasks(tasks);
        } else {
            throw new StructuralException("Ensemble contains non-plastic node terminations");
        }

        myPlasticEnsembleTerminations.put(name, result);
        fireVisibleChangeEvent();

        return result;
    }
    
    /**
     * @param name Unique name for the Termination (in the scope of this Node)
     * @param weights Each row is used as a 1 by m matrix of weights in a new termination on the nth expandable node
     * @param tauPSC Time constant with which incoming signals are filtered. (All Terminations have
     *      this property, but it may have slightly different interpretations per implementation.)
     * @param modulatory If true, inputs to the Termination are not summed with other inputs (they
     *      only have modulatory effects, eg on plasticity, which must be defined elsewhere).
     * @return Termination that was added
     * @throws StructuralException if weight matrix dimensionality is incorrect
     * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
     */
    public synchronized NTarget addHPESTermination(String name, float[][] weights, float tauPSC, boolean modulatory, float[] theta) throws StructuralException {
        //TODO: check name for duplicate
        if (myExpandableNodes.length != weights.length) {
            throw new StructuralException(weights.length + " sets of weights given for "
                    + myExpandableNodes.length + " expandable nodes");
        }

        int dimension = weights[0].length;

        NTarget[] components = new NTarget[myExpandableNodes.length];
        for (int i = 0; i < myExpandableNodes.length; i++) {
            if (weights[i].length != dimension) {
                throw new StructuralException("Equal numbers of weights are needed for termination onto each node");
            }

            components[i] = myExpandableNodes[i].addTarget(name, new float[][]{weights[i]}, tauPSC, modulatory);
        }

        PlasticGroupTarget result;

        // Make sure that the components are plastic, otherwise make a non-plastic termination
        if (isPopulationPlastic(components)) {
            PlasticNodeTarget[] pnts = new PlasticNodeTarget[components.length];
            for (int i=0; i<components.length; i++) {
                pnts[i] = (PlasticNodeTarget) components[i];
            }

            result = new hPESTarget(this, name, pnts, theta);

            // Set the number of tasks equal to the number of threads
            int numTasks = ca.nengo.util.impl.NodeThreadPool.getNumJavaThreads();
            numTasks = numTasks < 1 ? 1 : numTasks;

            LearningTask[] tasks = new LearningTask[numTasks];

            int termsPerTask = (int) Math.ceil((float) components.length / (float) numTasks);
            int termOffset = 0;
            int termStartIndex, termEndIndex;

            for (int i = 0; i < numTasks; i++) {
                termStartIndex = termOffset;
                termEndIndex = components.length - termOffset >= termsPerTask ? termOffset + termsPerTask : components.length;
                termOffset += termsPerTask;

                tasks[i] = new LearningTask(this, result, termStartIndex, termEndIndex);
            }
            addTasks(tasks);
        } else {
            throw new StructuralException("Ensemble contains non-plastic node terminations");
        }

        myPlasticEnsembleTerminations.put(name, result);
        fireVisibleChangeEvent();

        return result;
    }
    
    /**
     * @param name Unique name for the Termination (in the scope of this Node)
     * @param weights Each row is used as a 1 by m matrix of weights in a new termination on the nth expandable node
     * @param tauPSC Time constant with which incoming signals are filtered. (All Terminations have
     *      this property, but it may have slightly different interpretations per implementation.)
     * @param modulatory If true, inputs to the Termination are not summed with other inputs (they
     *      only have modulatory effects, eg on plasticity, which must be defined elsewhere).
     * @return Termination that was added
     * @throws StructuralException if weight matrix dimensionality is incorrect
     * @see ca.nengo.model.ExpandableNode#addTarget(java.lang.String, float[][], float, boolean)
     */
    public synchronized NTarget addBCMTermination(String name, float[][] weights, float tauPSC, boolean modulatory, float[] theta) throws StructuralException {
        //TODO: check name for duplicate
        if (myExpandableNodes.length != weights.length) {
            throw new StructuralException(weights.length + " sets of weights given for "
                    + myExpandableNodes.length + " expandable nodes");
        }

        int dimension = weights[0].length;

        NTarget[] components = new NTarget[myExpandableNodes.length];
        for (int i = 0; i < myExpandableNodes.length; i++) {
            if (weights[i].length != dimension) {
                throw new StructuralException("Equal numbers of weights are needed for termination onto each node");
            }

            components[i] = myExpandableNodes[i].addTarget(name, new float[][]{weights[i]}, tauPSC, modulatory);
        }

        PlasticGroupTarget result;

        // Make sure that the components are plastic, otherwise make a non-plastic termination
        if (isPopulationPlastic(components)) {
            PlasticNodeTarget[] pnts = new PlasticNodeTarget[components.length];
            for (int i=0; i < components.length; i++) {
                pnts[i] = (PlasticNodeTarget) components[i];
            }

            result = new BCMTarget(this, name, pnts, theta);

            // Set the number of tasks equal to the number of threads
            int numTasks = ca.nengo.util.impl.NodeThreadPool.getNumJavaThreads();
            numTasks = numTasks < 1 ? 1 : numTasks;

            LearningTask[] tasks = new LearningTask[numTasks];

            int termsPerTask = (int) Math.ceil((float) components.length / (float) numTasks);
            int termOffset = 0;
            int termStartIndex, termEndIndex;

            for (int i = 0; i < numTasks; i++) {
                termStartIndex = termOffset;
                termEndIndex = components.length - termOffset >= termsPerTask ? termOffset + termsPerTask : components.length;
                termOffset += termsPerTask;

                tasks[i] = new LearningTask(this, result, termStartIndex, termEndIndex);
            }
            addTasks(tasks);
        } else {
            throw new StructuralException("Ensemble contains non-plastic node terminations");
        }

        myPlasticEnsembleTerminations.put(name, result);
        fireVisibleChangeEvent();

        return result;
    }

    public synchronized NTarget addPreLearnTermination(String name, float[][] weights, float tauPSC, boolean modulatory) throws StructuralException {
        //TODO: check name for duplicate
        if (myExpandableNodes.length != weights.length) {
            throw new StructuralException(weights.length + " sets of weights given for "
                    + myExpandableNodes.length + " expandable nodes");
        }

        int dimension = weights[0].length;

        NTarget[] components = new NTarget[myExpandableNodes.length];
        for (int i = 0; i < myExpandableNodes.length; i++) {
            if (weights[i].length != dimension) {
                throw new StructuralException("Equal numbers of weights are needed for termination onto each node");
            }

            components[i] = myExpandableNodes[i].addTarget(name, new float[][]{weights[i]}, tauPSC, modulatory);
        }

        PlasticGroupTarget result;

        // Make sure that the components are plastic, otherwise make a non-plastic termination
        if (isPopulationPlastic(components)) {
            PlasticNodeTarget[] pnts = new PlasticNodeTarget[components.length];
            for (int i=0; i<components.length; i++) {
                pnts[i] = (PlasticNodeTarget) components[i];
            }

            result = new PreLearnTarget(this, name, pnts);

            // Set the number of tasks equal to the number of threads
            int numTasks = ca.nengo.util.impl.NodeThreadPool.getNumJavaThreads();
            numTasks = numTasks < 1 ? 1 : numTasks;

            LearningTask[] tasks = new LearningTask[numTasks];

            int termsPerTask = (int) Math.ceil((float) components.length / (float) numTasks);
            int termOffset = 0;
            int termStartIndex, termEndIndex;

            for (int i = 0; i < numTasks; i++) {
                termStartIndex = termOffset;
                termEndIndex = components.length - termOffset >= termsPerTask ? termOffset + termsPerTask : components.length;
                termOffset += termsPerTask;

                tasks[i] = new LearningTask(this, result, termStartIndex, termEndIndex);
            }
            addTasks(tasks);
        } else {
            throw new StructuralException("Ensemble contains non-plastic node terminations");
        }

        myPlasticEnsembleTerminations.put(name, result);
        fireVisibleChangeEvent();

        return result;
    }


	/**
	 * @see ca.nengo.neural.nef.NEFGroup#addBiasTerminations(DecodedTarget, float, float[][], float[][])
	 */
    public BiasTarget[] addBiasTerminations(DecodedTarget baseTermination, float interneuronTauPSC, float[][] biasDecoders, float[][] functionDecoders) throws StructuralException {
		float[][] transform = baseTermination.getTransform();

		float[] biasEncoders = new float[myEncoders.length];
		for (int j = 0; j < biasEncoders.length; j++) {
			float max = 0;
			for (int i = 0; i < functionDecoders.length; i++) {
				float x = - MU.prod(myEncoders[j], MU.prod(transform, functionDecoders[i])) / biasDecoders[i][0];
				if (x > max) {
                    max = x;
                }
			}
			biasEncoders[j] = max;
		}

		float baseTauPSC = baseTermination.getTau();
		EulerIntegrator integrator = new EulerIntegrator(Math.min(interneuronTauPSC, baseTauPSC) / 10f);

		float scale = 1 / interneuronTauPSC; //output scaling to make impulse integral = 1
		LinearSystem interneuronDynamics = new SimpleLTISystem(
				new float[]{-1f/interneuronTauPSC},
				new float[][]{new float[]{1f}},
				new float[][]{new float[]{scale}},
				new float[]{0f},
				new Units[]{Units.UNK}
		);

		String biasName = baseTermination.getName()+BIAS_SUFFIX;
		String interName = baseTermination.getName()+INTERNEURON_SUFFIX;

		BiasTarget biasTermination = null;
		try {
			LinearSystem baseDynamics = (LinearSystem) baseTermination.getDynamics().clone();
			biasTermination = new BiasTarget(this, biasName, baseTermination.getName(), baseDynamics, integrator, biasEncoders, false);
		} catch (CloneNotSupportedException e) {
			throw new StructuralException("Can't clone dynamics for bias termination", e);
		}
		BiasTarget interneuronTermination = new BiasTarget(this, interName, baseTermination.getName(), interneuronDynamics, integrator, biasEncoders, true);

		biasTermination.setModulatory(baseTermination.getModulatory());
		interneuronTermination.setModulatory(baseTermination.getModulatory());

		myDecodedTerminations.put(biasName, biasTermination);
		myDecodedTerminations.put(interName, interneuronTermination);
		fireVisibleChangeEvent();

		return new BiasTarget[]{biasTermination, interneuronTermination};
	}

	/**
	 * @see ca.nengo.model.Group#run(float, float)
	 */
	@Override
    public void run(float startTime, float endTime) throws SimulationException {
		synchronized (this) {
			try{
				float[] state = new float[myDimension];
				Map<String, Float> bias = new HashMap<String, Float>(5);

				//run terminations and sum state ...
				DecodedTarget[] dts = super.getDecodedTerminations();
				for (DecodedTarget t : dts) {
					t.run(startTime, endTime);
					float[] output = t.getOutput();

					boolean isModulatory = t.getModulatory();
					//TODO: handle modulatory bias input
					if (t instanceof BiasTarget) {
						String baseName = ((BiasTarget) t).getBaseTerminationName();
						if (!bias.containsKey(baseName)) {
                            bias.put(baseName, new Float(0));
                        }
						if (!isModulatory) {
                            bias.put(baseName, new Float(bias.get(baseName).floatValue() + output[0]));
                        }
					} else {
						if (!isModulatory) {
                            state = MU.sum(state, output);
                        }
					}

				}

				if ( getMode().equals(SimulationMode.DIRECT) || getMode().equals(SimulationMode.EXPRESS)) {
					//run ensemble dynamics if they exist (e.g. to model adaptation)
					if (myDirectModeDynamics != null) {
						TimeSeries dynamicsInput = new TimeSeriesImpl(new float[]{startTime, endTime},
								new float[][]{state, state}, Units.uniform(Units.UNK, state.length));
						TimeSeries dynamicsOutput = myDirectModeIntegrator.integrate(myDirectModeDynamics, dynamicsInput);
						state = dynamicsOutput.getValues()[dynamicsOutput.getValues().length-1];
					}

					NSource[] sources = getSources();
					for (NSource source : sources) {
						if (source instanceof DecodedSource) {
							((DecodedSource) source).run(state, startTime, endTime);
						}
					}
					setTime(endTime);
					// TODO Have plasticity work in DIRECT mode
				} else {
					//multiply state by encoders (cosine tuning), set radial input of each Neuron and run ...
					Node[] nodes = getNodes();
					for (int i = 0; i < nodes.length; i++) {
						((NEFNode) nodes[i]).setRadialInput(getRadialInput(state, i) +
						        getBiasInput(bias, myDecodedTerminations, i));
					}
					super.run(startTime, endTime);
				}
			} catch (SimulationException e) {
				e.setEnsemble(name());
				throw e;
			}
		}
	}

	// @param bias Bias input (related to avoidance of negative weights with interneurons)
	private static float getBiasInput(Map<String, Float> bias, Map<String, DecodedTarget> dt, int node) {
		float sumBias = 0;
		Iterator<String> it = bias.keySet().iterator();
		while (it.hasNext()) {
			String baseName = it.next();
			float netBias = bias.get(baseName).floatValue();
			float biasEncoder = ((BiasTarget) dt.get(baseName+BIAS_SUFFIX)).getBiasEncoders()[node];
			sumBias += netBias * biasEncoder;
		}
		return sumBias;
	}

	/**
	 * @param state State vector
	 * @param node Node number
	 * @return Radial input to the given node
	 */
	public float getRadialInput(float[] state, int node) {
		//scale state to unit circle if necessary
		if (!myRadiiAreOne) {
            state = MU.prodElementwise(state, myInverseRadii);
        }
		return MU.prod(state, myEncoders[node]);
	}

	/**
	 * @see ca.nengo.model.Group#setMode(ca.nengo.model.SimulationMode)
	 */
	@Override
    public void setMode(SimulationMode mode) {
		if(myFixedModes != null && !myFixedModes.contains(mode))
			return;
		
		super.setMode(mode);

		NSource[] sources = getSources();
		for (NSource source : sources) {
			if (source instanceof DecodedSource) {
				((DecodedSource) source).setMode(mode);
			}
		}
	}

	/**
	 * When this method is called, the mode of this node is fixed and cannot be changed by
	 * subsequent setMode(...) calls.
	 */
	public void fixMode() {
		fixMode(new SimulationMode[]{getMode()});
	}
	
	/**
	 * Set the allowed simulation modes.
	 */
	public void fixMode(SimulationMode[] modes) {
		myFixedModes = Arrays.asList(modes);
	}

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
	@Override
    public void reset(boolean randomize) {
		super.reset(randomize);

		

		if (myDirectModeDynamics != null) {
			myDirectModeDynamics.setState(new float[myDirectModeDynamics.getState().length]);
		}
	}

    public void setEnsembleFactory(NEFGroupFactory factory) {
		myEnsembleFactory=factory;
	}

    public NEFGroupFactory getEnsembleFactory() {
		return myEnsembleFactory;
	}
    
    public LinearApproximator getDecodingApproximator(String nodeName) {
    	return myDecodingApproximators.get(nodeName);
    }

    public int getNodeCount() {
		return getNodes().length;
	}

	/**
	 * @return number of neurons (same as getNodeCount)
	 */
	public int getNeuronCount() {
		return getNodes().length;
	}

    public synchronized void setNodeCount(int n) throws StructuralException {
		if (myEnsembleFactory==null) {
			throw new StructuralException("Error changing node count: EnsembleFactory has not been set");
		}
		if (n<1) {
			throw new StructuralException("Error changing node count: Cannot have "+n+" neurons");
		}


		NEFNode[] nodes = new NEFNode[n];

		NodeFactory nodeFactory=myEnsembleFactory.getNodeFactory();

		for (int i = 0; i < n; i++) {
			Node node = nodeFactory.make("node" + i);
			if ( !(node instanceof NEFNode) ) {
				throw new StructuralException("Nodes must be NEFNodes");
			}
			nodes[i] = (NEFNode) node;

			nodes[i].setMode(SimulationMode.CONSTANT_RATE);
			if ( !nodes[i].getMode().equals(SimulationMode.CONSTANT_RATE) ) {
				throw new StructuralException("Neurons in an NEFEnsemble must support CONSTANT_RATE mode");
			}

			nodes[i].setMode(getMode());
		}
		redefineNodes(nodes);

		myEncoders = myEnsembleFactory.getEncoderFactory().genVectors(n, getDimension());


		myDecodingApproximators.clear();

		// update the decoders for any existing origins
		NSource[] sources = getSources();
		for (NSource source2 : sources) {
			if (source2 instanceof DecodedSource) {
				DecodedSource origin=((DecodedSource) source2);
				String nodeOrigin=origin.getNodeOrigin();
				// recalculate the decoders
				if (!myReuseApproximators || !myDecodingApproximators.containsKey(nodeOrigin)) {
					float[][] outputs = getConstantOutputs(myEvalPoints, nodeOrigin);
					LinearApproximator approximator = getApproximatorFactory().getApproximator(myEvalPoints, outputs);
					myDecodingApproximators.put(nodeOrigin, approximator);
				}
					origin.redefineNodes(nodes,myDecodingApproximators.get(nodeOrigin));
			}
		}

		fireVisibleChangeEvent();


	}

	@Override
	public Properties listStates() {
		Properties p = super.listStates();

		for (NSource o : getSources()) {
			if (o instanceof DecodedSource) {
				p.setProperty(o.getName() + ":STP", "Decoder scaling due to short-term plasticity");
			}
		}

		for (NTarget t : getTargets()) {
            if (t instanceof DecodedTarget) {
                p.setProperty(t.getName() + ":STP", "Decoder scaling due to short-term plasticity");
            }
        }

		return p;
	}

    @Override
    public String toScript(HashMap<String, Object> scriptData) throws ScriptGenException {
        StringBuilder py = new StringBuilder(String.format("%s.make('%s', %d, %d", 
                    scriptData.get("netName"), 
                    name(),
                    getNodes().length, 
                    myDimension));

        NodeFactory nodeFactory = myEnsembleFactory.getNodeFactory();
        if (nodeFactory instanceof LIFNeuronFactory) {
            LIFNeuronFactory neuronFactory = (LIFNeuronFactory)nodeFactory;

            if (!(neuronFactory.getMaxRate() instanceof IndicatorPDF) ||
                !(neuronFactory.getIntercept() instanceof IndicatorPDF)) {
                throw new ScriptGenException("Max Rate or Intercept for LIF Neuron Factory not specified as a uniform range");
            }

            py.append(String.format(", tau_rc=%.3f, tau_ref=%.3f, max_rate=(%.1f, %.1f), intercept=(%.1f, %.1f)", 
                        neuronFactory.getTauRC(), 
                        neuronFactory.getTauRef(), 
                        ((IndicatorPDF)neuronFactory.getMaxRate()).getLow(), 
                        ((IndicatorPDF)neuronFactory.getMaxRate()).getHigh(), 
                        ((IndicatorPDF)neuronFactory.getIntercept()).getLow(), 
                        ((IndicatorPDF)neuronFactory.getIntercept()).getHigh()));
        } else {
            throw new ScriptGenException("Neuron Factory not supported. Only LIF Neuron Factory is supported");
        }

        py.append(String.format(", radius=%.2f)\n", myRadii[0]));
        return py.toString();
    }

	@Override
    public NEFGroupImpl clone() throws CloneNotSupportedException {
		NEFGroupImpl result = (NEFGroupImpl) super.clone();

		result.myEncoders = MU.clone(myEncoders);

		// TODO: why do I have to set this?  If I don't pasted ensembles fail to modify correctly
		//       when radius is changed.
		result.myReuseApproximators = false;

		result.myDecodingApproximators = new HashMap<String, LinearApproximator>(5);
		result.myEncoders = MU.clone(myEncoders);
		result.myEvalPoints = MU.clone(myEvalPoints);
		result.myInverseRadii = myInverseRadii.clone();
		result.myRadii = myRadii.clone();
		result.myUnscaledEvalPoints = MU.clone(myUnscaledEvalPoints);
		return result;
	}

	/**
	 * Releases any memory that can be freed.  Should be called after all origins are created for this ensemble
	 */
    public void releaseMemory() {
		myDecodingApproximators.clear();
	}

	/**
	 * TODO: figure out why I have to add these so that it will show up in the Configure menu
	 *     (nodeCount doens't appear for some rule)
	 * @param count number of desired neurons
	 * @throws StructuralException if factory doesn't exist or can't add that many
	 */
	public void setNeurons(int count) throws StructuralException {
	    setNodeCount(count);
	}

	/**
	 * @return number of neurons
	 */
	public int getNeurons() {
	    return getNodeCount();
	}


    /**
     *  Used to get static neuron data (data that doesn't change each step) and give it to the GPU.
     *  Data is returned in an array.
     *  neuronData[0] = numNeurons
     *  neuronData[1] = tauRC
     *  neuronData[2] = tauRef
     *  neuronData[3] = tauPSC
     *  neuronData[4] = maxTimeStep
     *  neuronData[5 ... 4 + numNeurons] = bias for each neuron
     *  neuronData[5 + numNeurons ... 4 + 2 * numNeurons] = scale for each neuron
     * @return [numNeurons, tauRC, taurRef, tauPSC, maxTimeStep, bias*, scale*]
	 */
	public float[] getStaticNeuronData(){

		int numNeurons = getNeurons();

		float[] neuronData = new float[5 + 2 * numNeurons];
		neuronData[0] = numNeurons;

		Node[] nodes = getNodes();
		SpikingNeuron[] neurons = new SpikingNeuron[nodes.length];

		for(int i = 0; i < nodes.length; i++){
			neurons[i] = (SpikingNeuron) nodes[i];
		}

		SpikingNeuron neuron = neurons[0];
		SpikeGeneratorSource origin;
		try {
			origin = (SpikeGeneratorSource) neuron.getSource(Neuron.AXON);
		} catch (StructuralException e) {
			e.printStackTrace();
			return null;
		}

		LIFSpikeGenerator generator = (LIFSpikeGenerator) origin.getGenerator();

		neuronData[1] = generator.getTauRC();
		neuronData[2] = generator.getTauRef();
		if (myPlasticEnsembleTerminations.size() > 0) {
			neuronData[3] = neuron.getTargets()[0].getTau();
		} else {
			neuronData[3] = 0;
		}
		neuronData[4] = generator.getMaxTimeStep();

		int i = 0;
		for(; i < numNeurons; i++)
		{
			neuronData[i + 5] = neurons[i].getBias();
			neuronData[i + 5 + numNeurons] = neurons[i].getScale();
		}

		return neuronData;
	}

	/**
	 * Stops a given percentage of neurons in this population from firing.
	 *
	 * @param killrate the percentage of neurons to stop firing
	 * @param saveRelays if true, do nothing if there is only one node in this population
	 */
	public void killNeurons(float killrate, boolean saveRelays)
	{
		Random rand = new Random();

		Node[] neurons = getNodes();

		if(saveRelays && (neurons.length == 1)) {
            return;
        }

		for (Node neuron : neurons) {
			if(rand.nextFloat() < killrate)
			{
				SpikingNeuron n = (SpikingNeuron)neuron;
				n.setBias(0.0f);
				n.setScale(0.0f);
			}
		}
	}

//	/**
//	 * Blocks the input from a given percentage of dendrites in the population.
//	 *
//	 * @param killrate the percentage of dendrates to block
//	 */
//	public void killDendrites(float killrate)
//	{
//		Random rand = new Random();
//
//		Node[] neurons = getNodes();
//
//		for(int i = 0; i < neurons.length; i++)
//		{
//			System.out.println("checking neuron");
//			SpikingNeuron n = (SpikingNeuron)neurons[i];
//			ExpandableSynapticIntegrator integrator = (ExpandableSynapticIntegrator)n.getIntegrator();
//			Termination[] inputs = integrator.getTerminations();
//			for(int j = 0; j < inputs.length; j++)
//			{
//				System.out.println("checking dendrite");
//				if(rand.nextFloat() < killrate)
//				{
//					System.out.println("killing dendrite");
//					try
//					{
//						integrator.removeTermination(inputs[j].getName());
//					}
//					catch(StructuralException se)
//					{
//						System.err.println("Error in killDendrites, trying to remove a termination that doesn't exist.");
//					}
//
//				}
//			}
//		}
//	}


}
