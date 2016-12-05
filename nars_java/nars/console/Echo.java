package nars.console;

import nars.core.control.AbstractTask;

/**
 * TODO wrap as operator
 * @author me
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
