package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class echo extends ImmediateOperator {







//    public static Task make(Class channel, Object signal) {
//        return the.newTask(channel.toString(), signal.toString());
//    }

    @Override
    public void accept(Task<Operation> o) {
        nar().memory.eventSpeak.emit( o.getTerm().args() );
        //nar().emit(echo.class, Arrays.toString( o.args() ) );
    }

}
