package nars.tuprolog;

import nars.nal.AbstractSubGoalTree;

abstract public interface SubGoalElement extends AbstractSubGoalTree {

    
    public PTerm getValue();
    
    default public boolean isLeaf() { return true; }
    default public boolean isRoot() { return false; }


}