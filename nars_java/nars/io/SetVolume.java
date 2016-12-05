package nars.io;

import nars.entity.Item;

/**
 * Sets the global volume / noise level, =(100% - "silence level")
 */
public class SetVolume extends Item<CharSequence> {
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
