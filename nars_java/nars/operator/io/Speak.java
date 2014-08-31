package nars.operator.io;

import nars.entity.AbstractTask;

/**
 * TODO wrap as operator
 * @author me
 */
public class Speak extends AbstractTask {
    public final String text;
    public final Class channel;

    public Speak(Class channel, String text) {
        super();
        this.channel = channel;
        this.text = text;
    }

    
    @Override
    public CharSequence getKey() {
        return "Echo";
    }
    
}
