package nars.io;

import java.io.IOException;
import nars.core.Perception;
import nars.entity.Task;
import nars.io.buffer.Buffer;


/**
 * An attached Input, Buffer, and Attention Allocation State
 * @author me
 */
public class InPort<X> {
    public final Input<X> input;
    public final Buffer<Task> buffer;
    private float attention;
    private final Perception perception;
    
//    /** initializes with default FIFO and attention=1.0 */
//    public InPort(Input<X> input, float initialAttention) {        
//        this(input, new FIFO(), 1.0);
//    }
    
    public InPort(Perception p, Input<X> input, Buffer<Task> buffer, float initialAttention) {
        super();
        this.perception = p;
        this.input = input;
        this.buffer = buffer;
        this.attention = initialAttention;
    }
 
    /** add a task to the end of the buffer */
    public boolean queue(Task task) {
        return buffer.add(task);
    }
     
    public boolean hasNext() {
        return buffer.size() > 0;
    }
    
    public void update() throws IOException {
        while (!input.finished(false) && (buffer.available() > 0) ) {            
            X x = input.next();
            if (x == null)
                continue;

            Task t = perception.perceive(x);
            if (t != null) {
                queue(t);
            }
        }
    }

    public float getAttention() {
        return attention;
    }        
    
    public boolean finished() {
        return input.finished(false) && buffer.size() == 0;
    }
    
    public Task next() {
        Task n = buffer.poll();
        
        //TODO update statistics
        
        return n;
    }
    
    //public float getMass(X input) // allows variable weighting of input items; default=1.0
    
    //public double getInputMassRate(double windowSeconds); // calculates throughput rate in mass/sec within a given past window size, using an internal histogram of finite resolution

    
    
    
}
