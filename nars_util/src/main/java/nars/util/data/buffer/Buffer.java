package nars.util.data.buffer;

import java.util.Queue;

/**
 *
 */
public interface Buffer<B> extends Queue<B> {
      
    public int available();
      
}
