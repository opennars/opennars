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
    public void accept(Task o) {
        nar().memory.eventSpeak.emit( Operation.args(o.getTerm()).terms() );
        //nar().emit(echo.class, Arrays.toString( o.args() ) );
    }

}
