package nars.core.task;

import nars.entity.AbstractTask;

/**
 *
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
