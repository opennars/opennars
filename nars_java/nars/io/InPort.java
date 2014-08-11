package nars.io;

import java.io.IOException;
import nars.core.Perception;
import nars.io.buffer.Buffer;


/**
 * An attached Input, Buffer, and Attention Allocation State
 * @author me
 */
public class InPort<X> {
    public final Input<X> input;
    public final Buffer<X> buffer;
    private float attention;
    private final Perception perception;
    
//    /** initializes with default FIFO and attention=1.0 */
//    public InPort(Input<X> input, float initialAttention) {        
//        this(input, new FIFO(), 1.0);
//    }
    
    public InPort(Perception p, Input<X> input, Buffer<X> buffer, float initialAttention) {
        super();
        this.perception = p;
        this.input = input;
        this.buffer = buffer;
        this.attention = initialAttention;
    }
 
    /** add a task to the end of the buffer */
    public boolean queue(X task) {
        return buffer.add(task);
    }
     
    public boolean hasNext() {
        if (buffer == null) {
            return !input.finished(false);
        }
        
        return buffer.size() > 0;
    }
    
    protected X nextXDirect() {
        try {
            if (input.finished(false))
                return null;
            
            X x = input.next();
            if (x == null)
                return null;
            return x;
        }
        catch (IOException e) {
            return null;
        }
            
    }
    
    public void update() throws IOException {
        if (buffer == null) return;
        
        while (!input.finished(false) && (buffer.available() > 0) ) {            
            X x = input.next();
            if (x != null) {
                queue(x);
            }
        }
    }

    public float getAttention() {
        return attention;
    }        
    
    public boolean finished() {
        if (buffer!=null)
            if (buffer.size() > 0)
                return false;
        
        return input.finished(false);
    }
    
    public X next() {
        if (buffer == null)
            return nextXDirect();
        
        X n = buffer.poll();
        
        //TODO update statistics
        
        return n;
    }
    
    //public float getMass(X input) // allows variable weighting of input items; default=1.0
    
    //public double getInputMassRate(double windowSeconds); // calculates throughput rate in mass/sec within a given past window size, using an internal histogram of finite resolution

    
    
    
}
