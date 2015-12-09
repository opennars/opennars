package nars.task.flow;

import nars.NAR;
import nars.task.Task;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

/** an input that generates tasks in batches, which are stored in a buffer */
public class TaskQueue extends ArrayDeque<Task> implements Input , Consumer<Task> {

    //private On reg=null;

    public TaskQueue() {
        this(1);
    }

    public TaskQueue(int initialCapacity) {
        super(initialCapacity);
    }

    public TaskQueue(Collection<Task> x) {
        super(x);
    }
    public TaskQueue(Task[] x) {
        super(x.length);
        Collections.addAll(this, x);
    }

    /*protected int accept(Iterator<Task> tasks) {
        if (tasks == null) return 0;
        int count = 0;
        while (tasks.hasNext()) {
            Task t = tasks.next();
            if (t==null)
                continue;
            queue.add(t);
            count++;
        }
        return count;
    }*/

    @Override
    public void accept(Task task) {
        if (task==null) return;

        add(task);
    }

    @Override
    public Task get() {
        if (!isEmpty()) {
            return removeFirst();
        }
        return null;
    }

    public void addIfNotNull(Task task) {
        if (task!=null)
            add(task);
    }

    public void input(NAR nar) {
        nar.input((Input)this);
    }


//    @Override
//    public void stop() {
//        clear();
//    }



}
