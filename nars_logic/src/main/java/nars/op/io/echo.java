package nars.op.io;

import nars.io.out.TextOutput;
import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

import java.util.Arrays;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class echo extends ImmediateOperator {

    /** singleton */
    public static final echo the = new echo();

    protected echo() {
        super();
    }

    public static Task echo(Object signal) {
        return the.newTask(signal.toString());
    }

//    public static Task make(Class channel, Object signal) {
//        return the.newTask(channel.toString(), signal.toString());
//    }

    @Override
    public void accept(Operation o) {
        o.getMemory().emit(echo.class, Arrays.toString( o.args() ) );
    }

}
