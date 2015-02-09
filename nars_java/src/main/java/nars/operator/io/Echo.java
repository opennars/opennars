package nars.operator.io;

import nars.core.Memory;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal8.ImmediateOperation;
import nars.logic.nal8.Operator;
import reactor.core.processor.Operation;

import java.util.List;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class Echo extends ImmediateOperation {

    public final Object signal;
    public final Class channel;

    public Echo(Class channel, Object signal) {
        super();
        this.channel = channel;
        this.signal = signal;
    }

    
    @Override
    public CharSequence name() {
        return channel.getSimpleName() + ": " + signal.toString();
    }


    @Override
    public void execute(Memory m) {

    }

}
