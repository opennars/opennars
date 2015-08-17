package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

import java.util.Arrays;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class Echo extends ImmediateOperator {

    /** singleton */
    public static final Echo the = new Echo();

    protected Echo() {
        super();
    }

    public static Task make(Object signal) {
        return the.newTask(signal.toString());
    }

    public static Task make(Class channel, Object signal) {
        return the.newTask(channel.toString(), signal.toString());
    }

    @Override
    public void accept(Operation o) {
        o.getMemory().emit(Echo.class, Arrays.toString( o.args() ) );
    }

}
