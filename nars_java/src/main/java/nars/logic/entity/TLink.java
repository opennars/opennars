package nars.logic.entity;

import nars.logic.Terms.Termable;

public interface TLink<T extends Termable> {

    public short getIndex(final int i);
    
    public T getTarget();
    
    public float getPriority();
    
}
