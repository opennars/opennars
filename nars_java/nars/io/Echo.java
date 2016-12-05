package nars.io;

import nars.entity.Item;


/**
 * TODO wrap as operator
 * @author me
 */
public class Echo extends Item<CharSequence> {
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
