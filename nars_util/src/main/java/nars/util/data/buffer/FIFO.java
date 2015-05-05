package nars.util.data.buffer;

import java.util.ArrayDeque;

/**
 * FIFO Item buffer that processes items directly and immediately
 */
public class FIFO<B> extends ArrayDeque<B> implements Buffer<B> {

    public final static int DEFAULT_CAPACITY = 1024;
    
    public FIFO() {
        super();
    }

    
    public int capacity() {
        return DEFAULT_CAPACITY;
    }

    @Override
    public int available() {
        return capacity() - size();
    }
    
    
    
    

    
    
}
