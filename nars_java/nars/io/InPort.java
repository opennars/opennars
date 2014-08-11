package nars.io;

import java.io.IOException;
import java.util.Iterator;
import nars.io.Output.ERR;
import nars.io.buffer.Buffer;
import nars.storage.Memory;


/**
 * An attached Input, Buffer, and Attention Allocation State
 * @author me
 */
public class InPort<X> implements Iterator<X> {
    private final Input<X> input;
    private final Buffer<X> buffer;
    private float attention;
    
//    /** initializes with default FIFO and attention=1.0 */
//    public InPort(Input<X> input, float initialAttention) {        
//        this(input, new FIFO(), 1.0);
//    }
    
    public InPort(Input<X> input, Buffer<X> buffer, float initialAttention) {
        super();
        this.input = input;
        this.buffer = buffer;
        this.attention = initialAttention;
    }
    
    public boolean hasNext() {
        return buffer.size() > 0;
    }
    
    public void update(Memory m) {
        while (!input.finished(false) && (buffer.available() > 0) ) {
            X x;
            try {
                x = input.next();
                if (x == null)
                    break;
                buffer.add(x);
            } catch (IOException ex) {
                m.output(ERR.class, ex);
                break;
            }            
        }
    }
    
    public boolean finished() {
        return input.finished(false) && buffer.size() == 0;
    }
    
    public X next() {        
        X n = buffer.poll();
        
        //TODO update statistics
        
        return n;
    }
    
    //public float getMass(X input) // allows variable weighting of input items; default=1.0
    
    //public double getInputMassRate(double windowSeconds); // calculates flow rate in mass/sec within a given past window size, using an internal histogram of finite resolution
    
    
}
