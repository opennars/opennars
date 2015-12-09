package nars.util.data.buffer;

/**
 * Base class for FIFOs that filter some input;
 * according to priority or budget or some other feature
 */
public abstract class FilterFIFO<B> extends FIFO<B> {
    
    /** predicate acceptance function */
    public abstract boolean accept(B x);
    
    /** allows the FIFO to handle rejected items */
    public abstract boolean onRejected(B x);
}
