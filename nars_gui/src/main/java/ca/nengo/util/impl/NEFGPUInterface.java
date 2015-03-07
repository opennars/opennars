package ca.nengo.util.impl;

import ca.nengo.math.impl.MultiLevelKLNetworkPartitioner;
import ca.nengo.model.*;
import ca.nengo.model.impl.GroupTarget;
import ca.nengo.model.impl.NetworkArrayImpl;
import ca.nengo.model.impl.NetworkImpl;
import ca.nengo.model.impl.NetworkImpl.SourceWrapper;
import ca.nengo.model.impl.NetworkImpl.TargetWrapper;
import ca.nengo.model.impl.RealOutputImpl;
import ca.nengo.neural.nef.NEFGroup;
import ca.nengo.neural.nef.impl.DecodedSource;
import ca.nengo.neural.nef.impl.DecodedTarget;
import ca.nengo.neural.nef.impl.NEFGroupImpl;
import ca.nengo.neural.neuron.impl.LIFSpikeGenerator;
import ca.nengo.neural.neuron.impl.SpikingNeuron;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Allows running NEFEnsembles on the GPU. 
 * Passes the ensemble data to the GPU through a native function. 
 * Passes input to the GPU each step and stores the output from the GPU in the appropriate locations.
 *
 * @author Eric Crawford
 */
public class NEFGPUInterface {
	private static boolean myUseGPU = false;
	private static int myNumDevices = 0;
	private static int myNumDetectedDevices = 0;
	private static String myErrorMessage;
	
	private static boolean showTiming = false;
	private boolean myShowTiming;
	private long averageTimeSpentInGPU;
	private long averageTimeSpentInCPU;
	private long totalRunTime;
	private int numSteps;
	
	protected boolean myRequireAllOutputsOnCPU;
	
	protected NEFGroupImpl[] myGPUEnsembles;
	protected Projection[] myGPUProjections;
	protected Projection[] nonGPUProjections;
	protected Node[] myGPUNetworkArrays;
	
	protected Node[] myNodes;
	protected Projection[] myProjections;
	
	protected float myStartTime;
	protected float myEndTime;
	
	float[][][] representedInputValues;
	float[][][] representedOutputValues;
	float[][] spikeOutput;
	boolean[][] inputOnGPU;
	
	/**	Load the shared library that contains the native functions.
	 * This is called just once, when this class is initially loaded.
	 * 
	 */
	static{
		try {
			myErrorMessage = "";
			System.loadLibrary("NengoGPU");
			myNumDetectedDevices = nativeGetNumDevices();
			
			if(myNumDetectedDevices < 1)
			{
				myErrorMessage = "No CUDA-enabled GPU detected.";
				System.out.println(myErrorMessage);
			}
			
		} catch (java.lang.UnsatisfiedLinkError e) {
			myNumDetectedDevices = 0;
			myErrorMessage = "Couldn't load native library NengoGPU - Linker error:";
			// System.out.println(myErrorMessage);
			// System.out.println(e);
		} catch (Exception e) {
			myNumDetectedDevices = 0;
			myErrorMessage = "Couldn't load native library NengoGPU - General exception:";
			System.out.println(myErrorMessage);
			System.out.println(e.getMessage());
			System.out.println(Arrays.toString(e.getStackTrace()));
		}
	}

	static native int nativeGetNumDevices();

	static native void nativeSetupRun(float[][][][] terminationTransforms,
			int[][] isDecodedTermination, float[][] terminationTau,
			float[][][] encoders, float[][][][] decoders, float[][] neuronData,
			int[][] projections, int[][] networkArrayData, int[][] ensembleData, 
			int[] isSpikingEnsemble, int[] collectSpikes, int[][] outputRequiredOnCPU, float maxTimeStep, 
			int[] deviceForNetworkArrays, int numDevicesRequested);

	static native void nativeStep(float[][][] representedInput,
			float[][][] representedOutput, float[][] spikes, float startTime,
			float endTime);

	static native void nativeKill();
	
	public NEFGPUInterface(boolean interactive){
		myRequireAllOutputsOnCPU = interactive;
    }

	public static int getNumDetectedDevices(){
		return myNumDetectedDevices;
	}
	
	public static void setNumDevices(int value){
		myNumDevices = Math.min(Math.max(value, 0), myNumDetectedDevices);
	}
	
	public static int getNumDevices(){
		return myNumDevices;
	}
	
	// get whether or not to use the GPU. set whether or not to use the GPU by using setRequestedNumDevices
	public static boolean getUseGPU(){
		return myNumDevices > 0;
	}
	
	public static void showGPUTiming(){
		showTiming = true;
	}
	
	public static void hideGPUTiming(){
		showTiming = false;
	}
	
	public static String getErrorMessage(){
		return myErrorMessage;
	}
	
	public void setRequireAllOutputsOnCPU(boolean require){
		myRequireAllOutputsOnCPU = require;
	}
	
	/**
	 * Gets all the necessary data from the nodes and projections which are assigned to run on GPUss
	 * and puts it in a form appropriate for passing to the native setup function. The native setup function
	 * will create a thread for each GPU in use, process the data further until its in a form suitable
	 * for running on the GPU, and finally move all the data to the GPU. The GPU threads will be waiting
	 * for a call to nativeStep which will tell them to take a step.
	 * 
	 * @author Eric Crawford
	 */
	public void initialize(){
		int[] nodeAssignments = findOptimalNodeAssignments(myGPUNetworkArrays, myGPUProjections, myNumDevices);

		boolean requireAllOutputsOnCPU = myRequireAllOutputsOnCPU;
		
		myShowTiming = showTiming;
		if(myShowTiming){
			averageTimeSpentInGPU = 0;
			averageTimeSpentInCPU = 0;
			numSteps = 0;
			totalRunTime = 0;
		}
		
		ArrayList<Node> GPUNodeList = new ArrayList<Node>();
		
		for(Node currentNode : myGPUNetworkArrays){
			// all the nodes in myGPUNetworkArrays are going to run on the GPU. 
			if(currentNode instanceof NetworkArrayImpl){
				List<Node> nodeList = Arrays.asList(((NetworkImpl) currentNode).getNodes());
				GPUNodeList.addAll(nodeList);
			}
			else{
				GPUNodeList.add(currentNode);
			}
		}
		
		myGPUEnsembles = GPUNodeList.toArray(new NEFGroupImpl[GPUNodeList.size()]);

		if (myGPUEnsembles.length == 0)
			return;

		// Put the data in a format appropriate for passing to the GPU. 
		// Most of this function is devoted to this task.
		int i = 0, j = 0, k = 0, numEnsemblesCollectingSpikes = 0;
		NEFGroup workingNode;
		NTarget[] targets;
		DecodedSource[] origins;

		float[][][][] terminationTransforms = new float[myGPUEnsembles.length][][][];
		int[][] isDecodedTermination = new int[myGPUEnsembles.length][];
		float[][] terminationTau = new float[myGPUEnsembles.length][];
		float[][][] encoders = new float[myGPUEnsembles.length][][];
		float[][][][] decoders = new float[myGPUEnsembles.length][][][];
		float[][] neuronData = new float[myGPUEnsembles.length][];
		EnsembleData ensembleData = new EnsembleData();
		int[][] ensembleDataArray = new int[myGPUEnsembles.length][];
		int[] collectSpikes = new int[myGPUEnsembles.length];
		int[][] outputRequiredOnCPU = new int[myGPUNetworkArrays.length][];
		int[] isSpikingEnsemble = new int[myGPUEnsembles.length];
		float maxTimeStep = ((LIFSpikeGenerator) ((SpikingNeuron) myGPUEnsembles[0]
				.getNodes()[0]).getGenerator()).getMaxTimeStep();
		
		

		
		// We put the list of projections in terms of the GPU nodes
		// For each projection we record 4 numbers: the index of the origin
		// ensemble, the index of the origin in its ensemble, the index of
		// the termination ensemble and the index of the termination in its ensemble
		int[][] adjustedProjections = new int[myGPUProjections.length][6];
		
		
		inputOnGPU = new boolean[myGPUNetworkArrays.length][];
		
		Node workingArray;
		int networkArrayOffset = 0;

		NetworkArrayData networkArrayData = new NetworkArrayData();
		int[][] networkArrayDataArray = new int[myGPUNetworkArrays.length][];

		int totalInputSize = 0;
		
		// store networkArray data
		for(i = 0; i < myGPUNetworkArrays.length; i++){
			
			networkArrayData.reset();
			workingArray = myGPUNetworkArrays[i];
			
			networkArrayData.indexOfFirstNode = networkArrayOffset;
			
			if(workingArray instanceof NEFGroupImpl){
				networkArrayOffset++;
			}else{
				networkArrayOffset += ((NetworkImpl) workingArray).getNodes().length;
			}
			
			networkArrayData.endIndex = networkArrayOffset;
				
			NTarget[] networkArrayTargets = workingArray.getTargets();
			networkArrayData.numTerminations = networkArrayTargets.length;
			
			
			for(j = 0; j < networkArrayTargets.length; j++){
				networkArrayData.totalInputSize += networkArrayTargets[j].getDimensions();
			}
			
			totalInputSize += networkArrayData.totalInputSize;
			
			NSource[] networkArraySources;
			if(workingArray instanceof NEFGroupImpl)
			{
				networkArraySources = ((NEFGroupImpl) workingArray).getDecodedOrigins();
			}else{
				networkArraySources = workingArray.getSources();
			}
			networkArrayData.numOrigins = networkArraySources.length;
			
			for(j = 0; j < networkArraySources.length; j++){
				networkArrayData.totalOutputSize += networkArraySources[j].getDimensions();
			}
			
			if(workingArray instanceof NEFGroupImpl){
				networkArrayData.numNeurons = ((NEFGroupImpl) workingArray).getNeurons();
			}else{
				Node[] subNodes = ((NetworkImpl) workingArray).getNodes();
				for(j = 0; j < subNodes.length; j++){
					networkArrayData.numNeurons += ((NEFGroupImpl) subNodes[j]).getNeurons();
				}
			}

			networkArrayDataArray[i] = networkArrayData.getAsArray();
			
			inputOnGPU[i] = new boolean[networkArrayTargets.length];
			outputRequiredOnCPU[i] = new int[networkArraySources.length];
			
			for(j = 0; j < networkArrayTargets.length; j++){
				NTarget target = networkArrayTargets[j];
				boolean terminationWrapped = target instanceof TargetWrapper;
				if(terminationWrapped)
					target = ((TargetWrapper) target).getBaseTermination();
				
				k = 0;
				boolean projectionMatches = false;
				
				while(!projectionMatches && k < myGPUProjections.length){
					NTarget projectionTarget = myGPUProjections[k].getTarget();
					boolean projectionTerminationWrapped = projectionTarget instanceof TargetWrapper;
					if(projectionTerminationWrapped)
						projectionTarget = ((TargetWrapper) projectionTarget).getBaseTermination();
					
					projectionMatches = target == projectionTarget;
					
					if(projectionMatches)
						break;
					
					k++;
				}
	
				if (projectionMatches) {
					adjustedProjections[k][2] = i;
					adjustedProjections[k][3] = j;
					adjustedProjections[k][4] = target.getDimensions();
					adjustedProjections[k][5] = -1;
	
					inputOnGPU[i][j] = true;
				} else {
					inputOnGPU[i][j] = false;
				}
			}
			
			for (j = 0; j < networkArraySources.length; j++) {
				NSource source = networkArraySources[j];
				boolean originWrapped = source instanceof SourceWrapper;
				if(originWrapped)
					source = ((SourceWrapper) source).getWrappedOrigin();
				
				for (k = 0; k < myGPUProjections.length; k++) {
					NSource projectionSource = myGPUProjections[k].getSource();
					boolean projectionOriginWrapped = projectionSource instanceof SourceWrapper;
					
					if(projectionOriginWrapped)
						projectionSource = ((SourceWrapper) projectionSource).getWrappedOrigin();
					
					if (source == projectionSource) {
						adjustedProjections[k][0] = i;
						adjustedProjections[k][1] = j;
					}
				}
				
				outputRequiredOnCPU[i][j] = (source.getRequiredOnCPU() || requireAllOutputsOnCPU) ? 1 : 0;
				
				// even if its not explicitly required on the CPU, it might be implicitly
				// if it is attached to a projection whose termination is on the CPU
				if(outputRequiredOnCPU[i][j] == 0){
    				for (k = 0; k < nonGPUProjections.length; k++) {
    					NSource projectionSource = nonGPUProjections[k].getSource();
                        boolean projectionOriginWrapped = projectionSource instanceof SourceWrapper;
                        
                        if(projectionOriginWrapped)
                            projectionSource = ((SourceWrapper) projectionSource).getWrappedOrigin();
                    
                        if (source == projectionSource){
                            outputRequiredOnCPU[i][j] = 1;
                        }
    				}
				}
			}
		}
		
		nonGPUProjections = null;
		
		// store NEFEnsemble data
		for (i = 0; i < myGPUEnsembles.length; i++) {
			
			workingNode = myGPUEnsembles[i];
			
			ensembleData.reset();

			ensembleData.dimension = workingNode.getDimension();
			ensembleData.numNeurons = workingNode.getNodeCount();
			
			isSpikingEnsemble[i] = (workingNode.getMode() == SimulationMode.DEFAULT) ? 1 : 0;

			targets = workingNode.getTargets();

			int terminationDim = 0;
			ensembleData.maxTransformDimension = 0;

			terminationTransforms[i] = new float[targets.length][][];
			terminationTau[i] = new float[targets.length];
			isDecodedTermination[i] = new int[targets.length];
			
			for (j = 0; j < targets.length; j++) {

				if (targets[j] instanceof DecodedTarget) {
					terminationTransforms[i][j] = ((DecodedTarget) targets[j])
							.getTransform();
					terminationTau[i][j] = targets[j].getTau();

					terminationDim = targets[j].getDimensions();
					ensembleData.totalInputSize += terminationDim;

					if (terminationDim > ensembleData.maxTransformDimension) {
						ensembleData.maxTransformDimension = terminationDim;
					}

					isDecodedTermination[i][j] = 1;
					
					ensembleData.numDecodedTerminations++;
				} else if (targets[j] instanceof GroupTarget) {
					terminationTransforms[i][j] = new float[1][1];
					
					
					// when we do learning, this will have to be changed, as well as some code in the NengoGPU library.
					// currently it assumes all neurons in the ensemble have the same weight for each non-decoded termination
					// (mainly just to support gates which have uniform negative weights).
					// When we do learning, will have to extract the whole weight matrix.
					NTarget[] neuronTargets = ((GroupTarget) targets[j]).getNodeTerminations();
					terminationTransforms[i][j][0] = ((PlasticNodeTarget) neuronTargets[0]).getWeights();
					terminationTau[i][j] = targets[j].getTau();
					isDecodedTermination[i][j] = 0;

					terminationDim = targets[j].getDimensions();
					
					ensembleData.totalInputSize += terminationDim;
					ensembleData.nonDecodedTransformSize += terminationDim;
					ensembleData.numNonDecodedTerminations++;
				}
			}

			encoders[i] = workingNode.getEncoders();
			float[] radii = workingNode.getRadii();
			for (j = 0; j < encoders[i].length; j++) {
				for (k = 0; k < encoders[i][j].length; k++)
					encoders[i][j][k] = encoders[i][j][k] / radii[k];
			}

			origins = ((NEFGroupImpl) workingNode).getDecodedOrigins();

			ensembleData.numOrigins = origins.length;
			ensembleData.maxDecoderDimension = 0;

			decoders[i] = new float[origins.length][][];
			int originDim;
			for (j = 0; j < origins.length; j++) {
				decoders[i][j] = origins[j].getDecoders();
				originDim = origins[j].getDimensions();

				ensembleData.totalOutputSize += originDim;

				if (originDim > ensembleData.maxDecoderDimension) {
					ensembleData.maxDecoderDimension = originDim;
				}
			}

			neuronData[i] = ((NEFGroupImpl) workingNode).getStaticNeuronData();

			//collectSpikes[i] = (workingNode.isCollectingSpikes() || requireAllOutputsOnCPU) ? 1 : 0;
			collectSpikes[i] = workingNode.isCollectingSpikes() ? 1 : 0;
			numEnsemblesCollectingSpikes++;

			ensembleDataArray[i] = ensembleData.getAsArray();
		}
		
		nativeSetupRun(terminationTransforms, isDecodedTermination,
				terminationTau, encoders, decoders, neuronData,
				adjustedProjections, networkArrayDataArray, ensembleDataArray,
				isSpikingEnsemble, collectSpikes, outputRequiredOnCPU, maxTimeStep, nodeAssignments, myNumDevices);

		// Set up the data structures that we pass in and out of the native step call.
		// They do not change in size from step to step so we can re-use them.
		representedInputValues = new float[myGPUNetworkArrays.length][][];
		representedOutputValues = new float[myGPUNetworkArrays.length][][];
		spikeOutput = new float [myGPUEnsembles.length][];
		
		for (i = 0; i < myGPUNetworkArrays.length; i++) {
			targets = myGPUNetworkArrays[i].getTargets();
			representedInputValues[i] = new float[targets.length][];
		}

		for (i = 0; i < myGPUNetworkArrays.length; i++) {
			NSource[] networkArraySources;
			if(myGPUNetworkArrays[i] instanceof NEFGroupImpl)
			{
				networkArraySources = ((NEFGroupImpl) myGPUNetworkArrays[i]).getDecodedOrigins();
			}else{
				networkArraySources = myGPUNetworkArrays[i].getSources();
			}

			representedOutputValues[i] = new float[networkArraySources.length][];

			for (j = 0; j < networkArraySources.length; j++) {
				if(outputRequiredOnCPU[i][j] != 0){
					representedOutputValues[i][j] = new float[networkArraySources[j].getDimensions()];
				}else{
					representedOutputValues[i][j] = null;
				}
			}
		}
		
		for (i = 0; i < myGPUEnsembles.length; i++) {
			if(collectSpikes[i] != 0){
				spikeOutput[i] = new float[myGPUEnsembles[i].getNeurons()];
			}else{
				spikeOutput[i] = null;
			}
		}
	}
	
	/**
	 * 1. Load data from terminations into "representedInputValues". 
	 * 2. Call nativeStep which will run the GPU's for one step and return the results in "representedOutputValues".
	 * 3. Put the data from "representedOutputValues" into the appropriate origins.
	 * 
	 * @author Eric Crawford
	 */
	public void step(float startTime, float endTime){
		
		myStartTime = startTime;
		myEndTime = endTime;

		
		if(myGPUEnsembles.length > 0){
		
			try {
				
				int count, i, j;
				float[] inputRow = new float[0];
				NTarget[] targets;
				
				// get the input data from the terminations
				for (i = 0; i < myGPUNetworkArrays.length; i++) {
					targets = myGPUNetworkArrays[i].getTargets();
					count = targets.length;
					
					for (j = 0; j < count; j++) {
						// we only get input for non-GPU terminations
						if (!inputOnGPU[i][j]) {
							inputRow = ((RealSource) targets[j].get()).getValues();
								
							representedInputValues[i][j] = inputRow;
						}
					}
				}
				
	
				nativeStep(representedInputValues, representedOutputValues, spikeOutput, startTime, endTime);
	
				
				// Put data computed by GPU in the origins
				NSource[] sources;
				for (i = 0; i < myGPUNetworkArrays.length; i++) {
	
					if(myGPUNetworkArrays[i] instanceof NEFGroupImpl)
					{
						sources = ((NEFGroupImpl) myGPUNetworkArrays[i]).getDecodedOrigins();
					}else{
						sources = myGPUNetworkArrays[i].getSources();
					}
					
					count = sources.length;
	
					for (j = 0; j < count; j++) {
						float[] currentRepOutput = representedOutputValues[i][j];
						if(currentRepOutput != null){
							sources[j].accept(new RealOutputImpl(
                                    currentRepOutput.clone(),
                                    Units.UNK, endTime));
						}
					}
				}
				
				
				for(i = 0; i < myGPUEnsembles.length; i++){
				    NEFGroupImpl currentEnsemble = myGPUEnsembles[i];
				    
				    currentEnsemble.setTime(endTime);
				    
				    float[] currentSpikeOutput = spikeOutput[i];
				    if (currentSpikeOutput != null) {
				        currentEnsemble.setSpikePattern(currentSpikeOutput, endTime);
				    }
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}
	

	public void kill()
	{
		if (myGPUEnsembles.length == 0)
			return;
		
		nativeKill();
	}

	/**
	 * Used when there are multiple GPU's running a simulation. Finds a distribution of nodes to GPU's that minimizes 
	 * communication between GPU's while also ensuring the number of neurons running on each GPU is relatively balanced.
	 * Note that this problem (a variant of the min bisection problem) is NP-Complete, so a heuristic is employed.
	 * 
	 * @return an array of integers where the value in the i'th entry denotes the partition number of the i'th node ...
	 * in the "nodes" input array
	 * 
	 * @author Eric Crawford
	 */
	public static int[] findOptimalNodeAssignments(Node[] nodes, Projection[] projections, int numPartitions){
		
		if(numPartitions < 1)
		{
			return new int[0];
		}else if(numPartitions == 1){
			int[] nodeAssignments = new int[nodes.length];
			Arrays.fill(nodeAssignments, 0);
			return nodeAssignments;
		}
		
		MultiLevelKLNetworkPartitioner networkPartitioner = new MultiLevelKLNetworkPartitioner();
		networkPartitioner.initialize(nodes, projections, numPartitions);
		
		return networkPartitioner.getPartitionsAsIntArray();
	}
	
	/**
	 * Finds all nodes in the given array which are supposed to execute on the GPU. Stores
	 * those nodes in myGPUNetworkArrays and returns the rest.
	 * 
	 * @author Eric Crawford
	 */
	public Node[] takeGPUNodes(Node[] nodes){
		ArrayList<Node> gpuNodeList = new ArrayList<Node>();
		ArrayList<Node> nodeList = new ArrayList<Node>();
		
		for(int i = 0; i < nodes.length; i++){
			Node workingNode = nodes[i];
			boolean NEFEnsembleUseGPU = 
				workingNode instanceof NEFGroupImpl && ((NEFGroupImpl) workingNode).getUseGPU();
			
			boolean NetworkArrayUseGPU = 
				workingNode instanceof NetworkArrayImpl &&
				((NetworkImpl) workingNode).getUseGPU();
		
			if(NEFEnsembleUseGPU || NetworkArrayUseGPU){
				gpuNodeList.add(workingNode);
			}
			else{
				nodeList.add(workingNode);
			}
		}
		
		myGPUNetworkArrays = gpuNodeList.toArray(new Node[gpuNodeList.size()]);
		return nodeList.toArray(new Node[nodeList.size()]);
	}
	
	/**
	 * Finds all projections in the given array which are supposed to execute on the GPU. Stores
	 * those projections in myGPUProjections and returns the rest. takeGPUNodes should be called before
	 * this is called, since the nodes which run on the GPU determine which projections run on the GPU.
	 * (ie a projection runs on the GPU only if both its target and source run on the GPU).
	 * 
	 * @author Eric Crawford
	 */
	public Projection[] takeGPUProjections(Projection[] projections){
		// Sort out the GPU projections from the CPU projections
		ArrayList<Projection> gpuProjectionsList = new ArrayList<Projection>();
		ArrayList<Projection> projectionList = new ArrayList<Projection>();
		
		List<Node> GPUNetworkArrayList = Arrays.asList(myGPUNetworkArrays);
		
		for(int i = 0; i < projections.length; i++)
		{
			Node originNode = projections[i].getSource().getNode();
			Node terminationNode = projections[i].getTarget().getNode();

			boolean originNodeOnGPU = GPUNetworkArrayList.contains(originNode);
			boolean terminationNodeOnGPU = GPUNetworkArrayList.contains(terminationNode);
			
			if(originNodeOnGPU && terminationNodeOnGPU)
			{
				gpuProjectionsList.add(projections[i]);
			}
			else
			{
				projectionList.add(projections[i]);
			}
		}
		
		myGPUProjections = gpuProjectionsList.toArray(new Projection[gpuProjectionsList.size()]);
		nonGPUProjections = projectionList.toArray(new Projection[projectionList.size()]);
		return nonGPUProjections;
		
	}

	/**
	 * Used to hold data about each network array to pass to native code. Allows
	 * the fields to be set by name and returned as an array which is the form 
	 * the native code expects the data to be in.
	 * 
	 * @author Eric Crawford
	 */
	private static class NetworkArrayData {
		final int numEntries = 7;
		
		public int indexOfFirstNode;
		public int endIndex;
		public int numTerminations;
		public int totalInputSize;
		public int numOrigins;
		public int totalOutputSize;
		public int numNeurons;
		
		public void reset(){
			indexOfFirstNode = 0;
			endIndex = 0;
			numTerminations = 0;
			totalInputSize = 0;
			numOrigins = 0;
			totalOutputSize = 0;
			numNeurons = 0;
		}
		
		public int[] getAsArray() {
			int[] array = new int[numEntries];

			int i = 0;
			array[i++] = indexOfFirstNode;
			array[i++] = endIndex;
			array[i++] = numTerminations;
			array[i++] = totalInputSize;
			array[i++] = numOrigins;
			array[i++] = totalOutputSize;
			array[i++] = numNeurons;
			
			return array;
		}
			
	}

	/**
	 * Used to hold data about each ensemble to pass to native code.. Allows
	 * the fields to be set by name, but returned as an array which is the form 
	 * the native code expects the data to be in.
	 * 
	 * @author Eric Crawford
	 */
	private static class EnsembleData {
		final int numEntries = 10;

		public int dimension;
		public int numNeurons;
		public int numOrigins;

		public int totalInputSize;
		public int totalOutputSize;

		public int maxTransformDimension;
		public int maxDecoderDimension;

		public int numDecodedTerminations;
		public int numNonDecodedTerminations;
		
		public int nonDecodedTransformSize;
		

		public void reset() {
			dimension = 0;
			numNeurons = 0;
			numOrigins = 0;

			totalInputSize = 0;
			totalOutputSize = 0;

			maxTransformDimension = 0;
			maxDecoderDimension = 0;
			
			numDecodedTerminations = 0;
			numNonDecodedTerminations = 0;
			nonDecodedTransformSize = 0;
			
		}

		public int[] getAsArray() {
			int[] array = new int[numEntries];

			int i = 0;
			array[i++] = dimension;
			array[i++] = numNeurons;
			array[i++] = numOrigins;

			array[i++] = totalInputSize;
			array[i++] = totalOutputSize;

			array[i++] = maxTransformDimension;
			array[i++] = maxDecoderDimension;
			
			array[i++] = numDecodedTerminations;
			array[i++] = numNonDecodedTerminations;
			
			array[i++] = nonDecodedTransformSize;
			
			return array;
		}
	}
}
