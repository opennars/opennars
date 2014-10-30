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
    public final AtomicInteger threads = new AtomicInteger(2);

    public Cycle() {
        
    }

    public void init(Memory m) {
        this.memory = m;
    }
    
    public int inputTasksPriority() {
        return 2;
    }

    public int newTasksPriority() {
        return memory.newTasks.size();
    }

    public int novelTasksPriority() {
        if (memory.getNewTaskCount() == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    public int conceptsPriority() {
        if (memory.getNewTaskCount() == 0) {
            return 2;
        } else {
            return 0;
        }
    }

    
}
