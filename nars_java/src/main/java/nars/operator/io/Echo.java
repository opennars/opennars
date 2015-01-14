package nars.operator.io;

import nars.logic.entity.AbstractTask;

/**
 * explicitly repeated input (repetition of the content of input ECHO commands)
 */
public class Echo extends AbstractTask<CharSequence> {
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
    
}
