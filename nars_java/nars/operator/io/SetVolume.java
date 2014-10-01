package nars.operator.io;

import nars.entity.AbstractTask;

/**
 * Sets the global volume / noise level, =(100% - "silence level")
 */
public class SetVolume extends AbstractTask {
    public final int volume;

    public SetVolume(int volume) {
        super();
        this.volume = volume;
    }

    @Override
    public CharSequence name() {
        return "SetVolume(" + volume + ')';
    }
    
}
