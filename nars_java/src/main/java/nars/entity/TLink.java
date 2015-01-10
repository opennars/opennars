package nars.entity;

import nars.language.Terms.Termable;

public interface TLink<T extends Termable> {

    public short getIndex(final int i);
    
    public T getTarget();
    
    public float getPriority();
    
}
