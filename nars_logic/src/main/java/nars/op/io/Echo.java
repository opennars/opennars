package nars.op.io;

import nars.Events;
import nars.Memory;
import nars.nal.nal8.ImmediateOperation;

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

    public Echo(Exception ex) {
        this(Events.ERR.class, ex);
    }


    @Override
    public String toString() {
        return channel.getSimpleName() + ": " + signal.toString();
    }


    @Override
    public void execute(Memory m) {

    }

}
