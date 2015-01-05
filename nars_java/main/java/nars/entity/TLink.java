package nars.entity;

public interface TLink<T> {

    public short getIndex(final int i);
    
    public T getTarget();
    
    public float getPriority();
    
}
