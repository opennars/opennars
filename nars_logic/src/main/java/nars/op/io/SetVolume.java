package nars.op.io;

import nars.Memory;
import nars.nal.nal8.ImmediateOperation;

/**
 * Sets the global volume / noise level, =(100% - "silence level")
 */
public class SetVolume extends ImmediateOperation {
    public final int volume;

    public SetVolume(int volume) {
        super();
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "SetVolume(" + volume + ')';
    }

    @Override
    public void execute(Memory m) {
        m.param.outputVolume.set(volume);
    }
}
