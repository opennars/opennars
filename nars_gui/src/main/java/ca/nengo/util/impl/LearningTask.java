package ca.nengo.util.impl;

import ca.nengo.model.SimulationException;
import ca.nengo.model.StructuralException;
import ca.nengo.neural.plasticity.impl.PlasticGroupImpl;
import ca.nengo.neural.plasticity.impl.PlasticGroupTarget;
import ca.nengo.util.ThreadTask;

/**
 * Implementation of a ThreadTask to multithread learning in a plastic ensemble.
 *
 * This task will seperate the learning calculations such as getDerivative into indepdent
 * threadable tasks.
 *
 * @author Jonathan Lai
 */

public class LearningTask implements ThreadTask {

    private PlasticGroupImpl myParent;
    private PlasticGroupTarget myTermination;

    private final int startIdx;
    private final int endIdx;
    private boolean finished;

    /**
     * @param parent Parent PlasticEnsemble of this task
     * @param termination PlasticEnsembleTermination that this task will learn on
     * @param start Starting index for the set of terminations to learn on
     * @param end Ending index for the set of terminations to learn on
     */
    public LearningTask(PlasticGroupImpl parent, PlasticGroupTarget termination, int start, int end) {
        myParent = parent;
        myTermination = termination;
        startIdx = start;
        endIdx = end;
        finished = true;
    }

    /**
     * @param copy LearningTask to copy the parent and termination values from
     * @param start Starting index for the set of terminations to learn on
     * @param end Ending index for the set of terminations to learn on
     */
    public LearningTask(LearningTask copy, int start, int end) {
        myParent = copy.myParent;
        myTermination = copy.myTermination;
        startIdx = start;
        endIdx = end;
        finished = copy.finished;
    }

	/**
	 * @see ca.nengo.model.Resettable#reset(boolean)
	 */
    public void reset(boolean randomize) {
        finished = false;
	}

    /**
     * @see ca.nengo.util.ThreadTask#getParent()
     */
    public PlasticGroupImpl getParent() {
        return myParent;
    }

    /**
     * @see ca.nengo.util.ThreadTask#isFinished()
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * @see ca.nengo.util.ThreadTask#run(float, float)
     */
    public void run(float startTime, float endTime) throws SimulationException {
        if (!finished) {
            try {
                myTermination.updateTransform(endTime, startIdx, endIdx);
            } catch (StructuralException e) {
                throw new SimulationException(e.getMessage());
            }
            finished = true;
        }
    }

    @Override
    public LearningTask clone() throws CloneNotSupportedException {
        return this.clone(myParent, myTermination);
    }
    
    public LearningTask clone(PlasticGroupImpl parent)
    throws CloneNotSupportedException {
    	try {
    		return this.clone(parent, (PlasticGroupTarget)parent.getTarget(myTermination.getName()));
    	} catch (StructuralException e) {
    		throw new CloneNotSupportedException("Error cloning LearningTask: " + e.getMessage());
    	}
    }
    
    public LearningTask clone(PlasticGroupImpl parent, PlasticGroupTarget term)
    throws CloneNotSupportedException {
    	LearningTask result = (LearningTask) super.clone();
    	result.myParent = parent;
    	result.myTermination = term;
    	return result;
    }

}
