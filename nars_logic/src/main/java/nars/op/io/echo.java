package nars.op.io;

import nars.nal.nal8.Execution;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.ImmediateOperator;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class echo extends ImmediateOperator {







//    public static Task make(Class channel, Object signal) {
//        return the.newTask(channel.toString(), signal.toString());
//    }


    @Override
    public void execute(Execution e) {
        e.nar.memory.eventSpeak.emit( Operator.opArgs(e.term()).terms() );
    }
}
