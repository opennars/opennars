package nars.io.ports;

import nars.io.commands.Reboot;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;


/**
 * An attached Input and Buffer
 * @author me
 */
abstract public class InPort<X,Y> implements Iterator<Y> {
    public final Input<X> input;
    public final ArrayDeque<Y> buffer;
    
    public InPort(Input<X> input, ArrayDeque<Y> buffer) {
        super();
        this.input = input;
        this.buffer = buffer;
    }
 
    /** add a task to the end of the buffer */
    protected boolean queue(Y task) {
        if (task instanceof Reboot) {
            buffer.clear();
            return false;
        }
            
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
    
    /** takes input object. any tasks that it generates are input into the InPort
     *  via queue() methods.
     */
    abstract public void perceive(X x);    
    
    public Iterator<Y> postprocess(final Iterator<Y> yy) {
        return yy;
    }
    
    public void update() throws IOException {
        if (buffer == null) return;
        
        int DEFAULT_CAPACITY = 1024;
        while (!input.finished(false) && (DEFAULT_CAPACITY - buffer.size()> 0) ) {            
            X x = input.next();
            if (x == null)
                continue;
            
            perceive(x);
        }
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
        Y n = buffer.poll();
        return n;
    }
    
    /** empties the buffer */
    public void reset() {        
        buffer.clear();
    }

    public int getItemsBuffered() {
        return buffer.size();
    }
}
