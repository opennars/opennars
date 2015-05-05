package nars.util.data.buffer;

/**
 * Base class for FIFOs that filter some input;
 * according to priority or budget or some other feature
 */
abstract public class FilterFIFO<B> extends FIFO<B> {
    
    /** predicate acceptance function */
    abstract public boolean accept(B x);
    
    /** allows the FIFO to handle rejected items */
    abstract public boolean onRejected(B x);
}
