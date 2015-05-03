package nars.rl.horde.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;
import java.util.Stack;


public class ThreadVectorPool implements VectorPool {
    static class AllocatedBuffer {
        final ArrayRealVector[] buffers;
        final int lastAllocation;
        final RealVector prototype;

        AllocatedBuffer(RealVector prototype, ArrayRealVector[] buffers, int lastAllocation) {
            this.prototype = prototype;
            this.buffers = buffers;
            this.lastAllocation = lastAllocation;
        }
    }

    int nbAllocation;
    private final Thread thread;
    private final Stack<ArrayRealVector[]> stackedVectors = new Stack<>();
    private final Stack<AllocatedBuffer> stackedBuffers = new Stack<>();
    private ArrayRealVector[] buffers;
    private int lastAllocation;
    private final RealVector prototype;
    private final int dimension;

    public ThreadVectorPool(RealVector prototype, int dimension) {
        this.dimension = dimension;
        this.thread = Thread.currentThread();
        this.prototype = prototype;
    }

    public void allocate() {
        if (buffers != null) {
            stackedBuffers.push(new AllocatedBuffer(prototype, buffers, lastAllocation));
            buffers = null;
            lastAllocation = -2;
        }
        buffers = stackedVectors.isEmpty() ? new ArrayRealVector[1] : stackedVectors.pop();
        lastAllocation = -1;
    }

    @Override
    public ArrayRealVector newVector() {
        return vectorCached();
    }

    private ArrayRealVector vectorCached() {
        if (Thread.currentThread() != thread)
            throw new RuntimeException("Called from a wrong thread");
        lastAllocation++;
        if (lastAllocation == buffers.length)
            buffers = Arrays.copyOf(buffers, buffers.length * 2);
        ArrayRealVector cached = buffers[lastAllocation];
        if (cached == null) {
            nbAllocation++;
            cached = new ArrayRealVector(prototype.getDimension());
            buffers[lastAllocation] = cached;
        }
        return cached;
    }


    public ArrayRealVector newVector(RealVector v) {
        assert dimension == v.getDimension();
        ArrayRealVector w = vectorCached();
        w.setSubVector(0, v);
        return w;
    }

    @Override
    public void releaseAll() {
        stackedVectors.push(buffers);
        if (stackedBuffers.isEmpty()) {
            buffers = null;
            lastAllocation = -2;
        } else {
            AllocatedBuffer allocated = stackedBuffers.pop();
            buffers = allocated.buffers;
            lastAllocation = allocated.lastAllocation;
        }
    }
}
