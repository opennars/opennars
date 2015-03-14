package ca.nengo.util.impl;

//import ca.nengo.model.InstantaneousOutput;

import ca.nengo.model.Network;
import ca.nengo.model.Node;
import ca.nengo.model.Projection;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.NetworkArrayImpl;
import ca.nengo.util.TaskSpawner;
import ca.nengo.util.ThreadTask;

import java.util.*;

/**
 * A pool of threads for running nodes in. All interaction with the threads
 * is done through this class.
 *
 * @author Eric Crawford
 */
//TODO replace with Reactor dispatcher
@Deprecated public class NodeThreadPool {
	protected static final int maxNumJavaThreads = 100;
	protected static final int defaultNumJavaThreads = 0;

	// numThreads can change throughout a simulation run. Therefore, it should not be used during a run,
	// only at the beginning of a run to create the threads.
	protected static int myNumJavaThreads = defaultNumJavaThreads;
	protected int myCurrentNumJavaThreads;
	protected int myNumThreads;
	protected NodeThread[] myThreads;
	protected final Object myLock = new Object();

	@Deprecated protected Node[] myNodes;
    @Deprecated protected Projection[] myProjections;
    @Deprecated protected ThreadTask[] myTasks;

	protected volatile int numThreadsComplete;
	protected volatile int numThreadsWaiting;

	protected volatile boolean threadsRunning;
	protected volatile boolean runFinished;
	protected float myStartTime;
	protected float myEndTime;
	
	protected static boolean myCollectTimings;
	protected long myRunStartTime;
	protected double myAverageTimePerStep;
	protected int myNumSteps;
	
	public static int getNumJavaThreads(){
		return myNumJavaThreads;
	}

	public static void setNumJavaThreads(int value){
		myNumJavaThreads = value;
	}
	
	public static int getMaxNumJavaThreads(){
		return maxNumJavaThreads;
	}
	

	public static boolean isMultithreading(){
		return myNumJavaThreads != 0;
	}

	// to turn it back on, call setNumThreads with a positive value
	public static void turnOffMultithreading(){
		myNumJavaThreads = 0;
	}

	public static boolean isCollectingTimings() {
		return myCollectTimings;
	}

	public static void setCollectTimings(boolean collectTimings) {
		myCollectTimings = collectTimings;
	}
	
	public float getStartTime(){
		return myStartTime;
	}

	public float getEndTime(){
		return myEndTime;
	}

	public boolean getRunFinished(){
		return runFinished;
	}

	// Dummy default constructor.
	protected NodeThreadPool(){
	}
	
	public NodeThreadPool(Network network, List<ThreadTask> threadTasks, boolean interactive){
		initialize(network, threadTasks, interactive);
	}
	
	/**
	 * 1. Checks whether the GPU is to be used for the simulation. If it is, creates
	 * a GPU Thread, passes this thread the nodes and projections which are to be run on the GPU,
	 * and calls the initialization function of the gpu thread's NEFGPUInterface. Starts the GPU thread.
	 * 
	 * 2. Creates the appropriate number of java threads and assigns to each a fair number of
	 * projections, nodes and tasks from those that remain after the GPU data has been dealt with.
	 * Starts the Java threads.
	 * 
	 * 3. Initializes synchronization primitives and variables for collecting timing data if applicable.
	 * 
	 * @author Eric Crawford
	 */
	protected void initialize(Network network, List<ThreadTask> threadTasks, boolean interactive){
		

		Iterable<Node> nodes = network.nodes();
		Projection[] projections = network.getProjections();
		
		List<Node> nodeList = collectNodes(nodes, false, null);
		List<Projection> projList = collectProjections(nodes, projections, null);
		List<ThreadTask> taskList = collectTasks(nodes, null);
		taskList.addAll(threadTasks);
		
		myNodes = nodeList.toArray(new Node[nodeList.size()]);
		myProjections = projList.toArray(new Projection[projList.size()]);
		myTasks = taskList.toArray(new ThreadTask[taskList.size()]);
		
		threadsRunning = false;
		runFinished = false;
		numThreadsWaiting = 0;
		numThreadsComplete = 0;
		
		boolean useGPU = NEFGPUInterface.getUseGPU();
		
		int numNonJavaThreads = 0;
		GPUThread gpuThread = null;
		if(useGPU){ 
			gpuThread = new GPUThread(this, interactive);
			
			int myNodeslength = myNodes.length;
			// The NEFGPUInterface removes from myNodes ensembles that are to be run on the GPU and returns the rest.
			myNodes = gpuThread.getNEFGPUInterface().takeGPUNodes(myNodes);
			
			if(myNodes.length == myNodeslength){
				//don't create a GPU thread if there are no nodes to run on the GPU.
				gpuThread = null;
				useGPU = false;
			}else{
				// The NEFGPUInterface removes from myProjections projections that are to be run on the GPU and returns the rest.
				myProjections = gpuThread.getNEFGPUInterface().takeGPUProjections(myProjections);

				gpuThread.getNEFGPUInterface().initialize();

				gpuThread.setCollectTimings(myCollectTimings);
				gpuThread.setName("GPUThread0");

				gpuThread.setPriority(Thread.MAX_PRIORITY);
				gpuThread.start();

				numNonJavaThreads += 1;
			}
		}

		myCurrentNumJavaThreads = Math.min(myNodes.length, myNumJavaThreads);
		myCurrentNumJavaThreads = Math.max(myCurrentNumJavaThreads, 1);

		myNumThreads = myCurrentNumJavaThreads + numNonJavaThreads;

		myThreads = new NodeThread[myNumThreads];

		if(useGPU){
			myThreads[myNumThreads-1] = gpuThread;
		}

		//In the remaining nodes (non-GPU nodes), DO break down the NetworkArrays, we don't want to call the 
		// "run" method of nodes which are members of classes which derive from the NetworkImpl class since 
		// NetworkImpls create their own LocalSimulators when run.
        //List<Node> var = collectNodes(myNodes, true, null);
        myNodes = nodeList.toArray(new Node[nodeList.size()]);

		int nodesPerJavaThread = (int) Math.ceil((float) myNodes.length / (float) myCurrentNumJavaThreads);
		int projectionsPerJavaThread = (int) Math.ceil((float) myProjections.length / (float) myCurrentNumJavaThreads);
        int tasksPerJavaThread = (int) Math.ceil((float) myTasks.length / (float) myCurrentNumJavaThreads);
        
		int nodeOffset = 0, projectionOffset = 0, taskOffset = 0;
		int nodeStartIndex, nodeEndIndex, projectionStartIndex, projectionEndIndex, taskStartIndex, taskEndIndex;
		
		// Evenly distribute projections, nodes and tasks to the java threads.
		for(int i = 0; i < myCurrentNumJavaThreads; i++){

			nodeStartIndex = nodeOffset;
			nodeEndIndex = myNodes.length - nodeOffset >= nodesPerJavaThread ?
					nodeOffset + nodesPerJavaThread : myNodes.length;

			nodeOffset += nodesPerJavaThread;

			projectionStartIndex = projectionOffset;
			projectionEndIndex = myProjections.length - projectionOffset >= projectionsPerJavaThread ?
					projectionOffset + projectionsPerJavaThread : myProjections.length;

			projectionOffset += projectionsPerJavaThread;

			taskStartIndex = taskOffset;
			taskEndIndex = myTasks.length - taskOffset >= tasksPerJavaThread ?
					taskOffset + tasksPerJavaThread : myTasks.length;

			taskOffset += tasksPerJavaThread;

			myThreads[i] = new NodeThread(this, myNodes, nodeStartIndex,
					nodeEndIndex, myProjections, projectionStartIndex,
					projectionEndIndex, myTasks, taskStartIndex, taskEndIndex);
			
			myThreads[i].setCollectTimings(myCollectTimings);
			myThreads[i].setName("JavaThread" + i);

			myThreads[i].setPriority(Thread.MAX_PRIORITY);
			myThreads[i].start();
		}
		
		myRunStartTime = myCollectTimings ? new Date().getTime() : 0;
		myAverageTimePerStep = 0;
		myNumSteps = 0;
	}

	/**
	 * Tell the threads in the current thread pool to take a step. The step consists of three
	 * phases: projections, nodes, tasks. All threads must complete a stage before any thread begins
	 * the next stage, so, for example, all threads must finish processing all of their projections 
	 * before any thread starts processing its nodes.
	 * 
	 * @author Eric Crawford
	 */
	public void step(float startTime, float endTime) throws SimulationException {
		myStartTime = startTime;
		myEndTime = endTime;
		
		
		long stepInterval = myCollectTimings ? new Date().getTime() : 0;
		
		try
		{
			int oldPriority = Thread.currentThread().getPriority();
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			// start the projection processing, wait for it to finish
			startThreads();

			// start the node processing, wait for it to finish
			startThreads();
			
			// start the task processing, wait for it to finish
			startThreads();

			Thread.currentThread().setPriority(oldPriority);
		}
		catch(Exception e) {
			throw new SimulationException(e);
		}
		
		if(myCollectTimings){
			stepInterval = new Date().getTime() - stepInterval;
			myAverageTimePerStep = (myAverageTimePerStep * myNumSteps + stepInterval) / (myNumSteps + 1);
            
            myNumSteps++;
		}
	}

	/**
	 * Tells the threads to run for one phase (projections, nodes or tasks). 
	 * The threads should be waiting on myLock at the time this is called.
	 * 
	 * @author Eric Crawford
	 */
	private void startThreads() throws InterruptedException {
		synchronized(myLock){
			if(runFinished)
				throw new InterruptedException();
			
			numThreadsComplete = 0;
			threadsRunning = true;
			myLock.notifyAll();  //release all the threads from the threadWait() loop
			
			while(threadsRunning || numThreadsWaiting < myThreads.length) 
				myLock.wait();  //we don't want the stepthread to be able to continue (and start threads) until all the threads are waiting to be started
		}
	}

	/**
	 * Called by the threads in this node pool. Called once they finish a phase (projections, nodes or tasks).
	 * Forces them to wait on myLock.
	 * 
	 * @author Eric Crawford
	 */
	public void threadWait() throws InterruptedException{
		synchronized(myLock){
			numThreadsWaiting++;
			if(numThreadsWaiting == myThreads.length)
				myLock.notifyAll(); //all the threads are done the step and in a waiting state, so free the stepthread
			
			while(!threadsRunning) {
                myLock.wait();
            }
		}
	}

	/**
	 * Called by the threads in this pool to signal that they are done a phase. 
	 * 
	 * @author Eric Crawford
	 */
	public void threadFinished() throws InterruptedException{
		
		synchronized(myLock){
			numThreadsComplete++;

			if(numThreadsComplete == myThreads.length){
				threadsRunning = false;
				numThreadsWaiting=0;
				myLock.notifyAll(); //this is to move the threads into the threadwait loop
			}
			else{
				while(threadsRunning) //threads wait here when they are finished, but others are still running (i.e. wait here until end of one step)
					myLock.wait();
			}
	
			threadWait(); //threads wait here when they're all finished (i.e. wait here between steps)
		}
	}

	/**
	 * Kill the threads in the pool by interrupting them. Each thread will handle
	 * the interrupt signal by ending its run method, which kills it.
	 * 
	 * @author Eric Crawford
	 */
	public void kill(){
		synchronized(myLock)
		{
			runFinished = true;

			for(int i = 0; i < myThreads.length; i++){
				myThreads[i].interrupt();
			}
			
			if(myCollectTimings){
				StringBuilder timingOutput = new StringBuilder();
				timingOutput.append("Timings for NodeThreadPool:\n");
				
				long approxRunTime = new Date().getTime() - myRunStartTime;
				timingOutput.append("Approximate total run time: ").append(approxRunTime).append(" ms\n");
				
				timingOutput.append("Average time per step: ").append(myAverageTimePerStep).append(" ms\n");
				
				System.out.print(timingOutput.toString());
			}

			myLock.notifyAll();
		}
		
		
	}
	
    /**
     * Return all the nodes in the network except subnetworks. Essentially returns a "flattened"
     * version of the network. The breakDownNetworkArrays param lets the caller choose whether to include
     * Network Arrays in the returned list (=false) or to return the NEFEnsembles in the network array (=true).
     * This facility is provided because sometimes Network Arrays should be treated like Networks, which is what
     * they are as far as java is concerned (they extend the NetworkImpl class), and sometimes it is better to 
     * treat them like NEFEnsembles, which they are designed to emulate (they're supposed to be an 
     * easier-to-build version of large NEFEnsembles).
     * 
     * @author Eric Crawford
     */
    public static List<Node> collectNodes(Iterable<Node> startingNodes, boolean breakDownNetworkArrays, List<Node> nodes){

        if (nodes == null)
            nodes = new ArrayList();
        else
            nodes.clear();

        boolean isNetwork = false;
        for (Node workingNode : startingNodes) {

            //Decide whether to break the node into its subnodes
            /*if((workingNode.getClass().getCanonicalName().contains("CCMModelNetwork"))){
            	isNetwork = false;
            }
            else*/
            if(workingNode instanceof NetworkArrayImpl)
            {
            	if(breakDownNetworkArrays){
            		isNetwork = true;
            	}else{
            		isNetwork = false;
            	}
            }
            else if(workingNode instanceof Network){
            	isNetwork = true;
            }
            else{
                isNetwork = false;
            }
            
            
            if(isNetwork){
            	collectNodes(((Network) workingNode).nodes(), breakDownNetworkArrays, nodes);
            }
            else{
            	nodes.add(workingNode);
            } 
        }

        return nodes;
    }
    

    /**
     * Returns all the projections that would be in a "flattened" version of the network.
     * 
     * @author Eric Crawford
     */
    public static List<Projection> collectProjections(Iterable<Node> startingNodes, Projection[] startingProjections, List<Projection> projections){

        if (projections == null)
            projections = new ArrayList();
        else {
            projections.clear();
        }

        Collections.addAll(projections, startingProjections);


        for (Node workingNode : startingNodes) {

            if(workingNode instanceof Network) {
                Network nwn = ((Network) workingNode);
                collectProjections(nwn.nodes(), nwn.getProjections(), projections);
            }
        }

        return projections;
    }

    /**
     * Returns all the tasks that would be in a "flattened" version of the network.
     * 
     * @author Eric Crawford
     */
    public static List<ThreadTask> collectTasks(Iterable<Node> startingNodes, List<ThreadTask> tasks){

        if (tasks == null)
            tasks = new ArrayList<ThreadTask>();
        else
            tasks.clear();

        for (Node workingNode : startingNodes) {

            if(workingNode instanceof Network) { // && !(workingNode.getClass().getCanonicalName().contains("CCMModelNetwork")))
                collectTasks( ((Network) workingNode).nodes(), tasks);
            }
            
            if(workingNode instanceof TaskSpawner)
            {
                tasks.addAll(Arrays.asList(((TaskSpawner) workingNode).getTasks()));
            }
        }

        return tasks;
    }
}
