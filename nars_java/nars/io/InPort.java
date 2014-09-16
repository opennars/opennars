package nars.io;

import java.io.IOException;
import java.util.Iterator;
import nars.io.buffer.Buffer;


/**
 * An attached Input, Buffer, and Attention Allocation State
 * @author me
 */
abstract public class InPort<X,Y> implements Iterator<Y> {
    public final Input<X> input;
    public final Buffer<Y> buffer;
    private float attention;
    
    
//    /** initializes with default FIFO and attention=1.0 */
//    public InPort(Input<X> input, float initialAttention) {        
//        this(input, new FIFO(), 1.0);
//    }
    
    public InPort(Input<X> input, Buffer<Y> buffer, float initialAttention) {
        super();
        this.input = input;
        this.buffer = buffer;
        this.attention = initialAttention;
    }
 
    /** add a task to the end of the buffer */
    protected boolean queue(Y task) {
        if (task!=null)
            return buffer.add(task);
        return false;
    }
     
    @Override
    public boolean hasNext() {
        if (buffer == null) {
            return !input.finished(false);
        }
        
        return buffer.size() > 0;
    }
    
//    protected X nextXDirect() {
//        try {
//            if (input.finished(false))
//                return null;
//            
//            X x = input.next();
//            if (x == null)
//                return null;
//            return x;
//        }
//        catch (IOException e) {
//            return null;
//        }
//            
//    }
    
    abstract public Iterator<Y> process(X x);
    
    public void update() throws IOException {
        if (buffer == null) return;
        
        while (!input.finished(false) && (buffer.available() > 0) ) {            
            X x = input.next();
            if (x == null)
                continue;
            
            Iterator<Y> yy = process(x);
            if (yy!=null) {
                while (yy.hasNext())
                    queue(yy.next());
            }            
        }
    }

    public float getAttention() {
        return attention;
    }        
    
    public boolean finish() {
        return input.finished(true);
    }
    
    public boolean finished() {
        if (buffer!=null)
            if (buffer.size() > 0)
                return false;
        
        return input.finished(false);
    }
    
    public Y next() {
        /*if (buffer == null)
            return nextXDirect();*/
        
        Y n = buffer.poll();
        
        //TODO update statistics
        
        return n;
    }
    
    public int getItemsBuffered() {
        return buffer.size();
    }
    
    //public float getMass(X input) // allows variable weighting of input items; default=1.0
    
    //public double getInputMassRate(double windowSeconds); // calculates throughput rate in mass/sec within a given past window size, using an internal histogram of finite resolution

    
    
    
}
