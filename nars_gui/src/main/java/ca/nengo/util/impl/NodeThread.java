package ca.nengo.util.impl;

import ca.nengo.model.Node;
import ca.nengo.model.Projection;
import ca.nengo.model.SimulationException;
import ca.nengo.model.impl.SocketUDPNode;
import ca.nengo.util.ThreadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A thread for running projections, nodes and tasks in. Projections are all runs before nodes, nodes before tasks.
 *
 * @author Eric Crawford
 */
public class NodeThread extends Thread {

	private final NodeThreadPool myNodeThreadPool;

	private final Node[] myNodes;
	private List<Node> myDeferredSocketNodes;
	private final int myStartIndexInNodes;
	private final int myEndIndexInNodes;

	private final Projection[] myProjections;
	private final int myStartIndexInProjections;
	private final int myEndIndexInProjections;

	private final ThreadTask[] myTasks;
	private final int myStartIndexInTasks;
	private final int myEndIndexInTasks;

	private boolean myCollectTimings;

	private double myAverageTimeOnProjectionsPerStep;
	private double myAverageTimeOnNodesPerStep;
	private double myAverageTimeOnTasksPerStep;

	private int myNumSteps;

	public NodeThread(NodeThreadPool nodePool, Node[] nodes,
			int startIndexInNodes, int endIndexInNodes,
			Projection[] projections, int startIndexInProjections,
			int endIndexInProjections, ThreadTask[] tasks,
            int startIndexInTasks, int endIndexInTasks) {

		myNodeThreadPool = nodePool;

		myNodes = nodes;
		myDeferredSocketNodes = null; //new ArrayList<Node>(2);
		myProjections = projections;
        myTasks = tasks;

		myStartIndexInNodes = startIndexInNodes;
		myEndIndexInNodes = endIndexInNodes;

		myStartIndexInProjections = startIndexInProjections;
		myEndIndexInProjections = endIndexInProjections;

		myStartIndexInTasks = startIndexInTasks;
		myEndIndexInTasks = endIndexInTasks;
		
		myNumSteps = 0;
		myAverageTimeOnProjectionsPerStep = 0;
		myAverageTimeOnNodesPerStep = 0;
		myAverageTimeOnTasksPerStep = 0;
	}
	
	

	public void waitForPool() {
		try {
			myNodeThreadPool.threadWait();
		} catch (Exception e) {
		}
	}

	public void finished() {
		try {
			myNodeThreadPool.threadFinished();
		} catch (Exception e) {
		}
	}

	// might have to make these protected?
	protected void runProjections(float startTime, float endTime) throws SimulationException{
		
		for (int i = myStartIndexInProjections; i < myEndIndexInProjections; i++) {
			
			Object values = myProjections[i].getSource().get();
			myProjections[i].getTarget().apply(values);
		}
		
	}
	
	protected void runNodes(float startTime, float endTime) throws SimulationException{
		
		
		for (int i = myStartIndexInNodes; i < myEndIndexInNodes; i++) {
			if (myNodes[i] instanceof SocketUDPNode && ((SocketUDPNode)myNodes[i]).isReceiver()) {
                if (myDeferredSocketNodes==null) myDeferredSocketNodes = new ArrayList();
				myDeferredSocketNodes.add(myNodes[i]);
				continue;
			}
			myNodes[i].run(startTime, endTime);
		}

        if (myDeferredSocketNodes!=null) {
            Iterator<Node> it = myDeferredSocketNodes.iterator();
            while (it.hasNext()) {
                it.next().run(startTime, endTime);
            }
            myDeferredSocketNodes.clear();
        }
	}
	
	protected void runTasks(float startTime, float endTime) throws SimulationException {
		
		for (int i = myStartIndexInTasks; i < myEndIndexInTasks; i++) {
            myTasks[i].run(startTime, endTime);
        }
	}
	
	public void run() {
		try {
			float startTime, endTime;

			waitForPool();

			while (true) {
				startTime = myNodeThreadPool.getStartTime();
				endTime = myNodeThreadPool.getEndTime();
				
				long projectionInterval, nodeInterval, taskInterval;
				
				projectionInterval = myCollectTimings ? new Date().getTime() : 0;
				
				runProjections(startTime, endTime);
				
				projectionInterval = myCollectTimings ? new Date().getTime() - projectionInterval : 0;

				finished();
				
				nodeInterval = myCollectTimings ? new Date().getTime() : 0;

				runNodes(startTime, endTime);
				
				nodeInterval = myCollectTimings ? new Date().getTime() - nodeInterval : 0;

				finished();
				
				taskInterval = myCollectTimings ? new Date().getTime() : 0;

                runTasks(startTime, endTime);
                
                taskInterval = myCollectTimings ? new Date().getTime() - taskInterval : 0;

                finished();
                
                if(myCollectTimings){
	                myAverageTimeOnProjectionsPerStep = (myAverageTimeOnProjectionsPerStep * myNumSteps + projectionInterval) / (myNumSteps + 1);
	                myAverageTimeOnNodesPerStep = (myAverageTimeOnNodesPerStep * myNumSteps + nodeInterval) / (myNumSteps + 1);
	                myAverageTimeOnTasksPerStep = (myAverageTimeOnTasksPerStep * myNumSteps + taskInterval) / (myNumSteps + 1);
	                
	                myNumSteps++;
                }
                
				// This is the means of getting out of the loop. The pool will interrupt
				// this thread at the appropriate time.
				if (Thread.currentThread().isInterrupted() || myNodeThreadPool.getRunFinished()) {
					kill();
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			myNodeThreadPool.kill();
			finished();
		}
	}
	
	protected void kill(){
		if(myCollectTimings){
			StringBuilder timingOutput = new StringBuilder();
			timingOutput.append("Timings for thread: ").append(this.getName()).append('\n');
			timingOutput.append("Average time processing projections per step: ").append(myAverageTimeOnProjectionsPerStep).append(" ms\n");
			timingOutput.append("Average time processing nodes per step: ").append(myAverageTimeOnNodesPerStep).append(" ms\n");
			timingOutput.append("Average time processing tasks per step: ").append(myAverageTimeOnTasksPerStep).append(" ms\n");
			
			System.out.print(timingOutput.toString());
		}
	}
	
	public void setCollectTimings(boolean myCollectTimings) {
		this.myCollectTimings = myCollectTimings;
	}
	
	public double getMyAverageTimeOnProjectionsPerStep() {
		return myAverageTimeOnProjectionsPerStep;
	}

	public double getMyAverageTimeOnNodesPerStep() {
		return myAverageTimeOnNodesPerStep;
	}

	public double getMyAverageTimeOnTasksPerStep() {
		return myAverageTimeOnTasksPerStep;
	}
}
