/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author me
 */
public class Cycle {
    private Memory memory;
    public final AtomicInteger threads = new AtomicInteger();
    private final int numThreads;

    public Cycle() {
        this(Parameters.THREADS);
    }
    
    public Cycle(int threads) {
        this.numThreads = threads;
        this.threads.set(threads);
        
    }

    int t(int threads) {
        if (threads == 1) return 1;
        else {
            return threads;
        }
    }
    
    public void init(Memory m) {
        this.memory = m;
    }
    
    public int inputTasksPriority() {
        return t(numThreads);
    }

    public int newTasksPriority() {
        return memory.newTasks.size();
    }

    public int novelTasksPriority() {
        if (memory.getNewTaskCount() == 0) {
            return t(numThreads);
        } else {
            return 0;
        }
    }

    public int conceptsPriority() {
        if (memory.getNewTaskCount() == 0) {
            return t(numThreads);
        } else {
            return 0;
        }
    }

    
}
