package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;

/**
 * Sets the global volume / noise level, =(100% - "silence level")
 */
public class SetVolume extends ImmediateOperator {
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
    public void accept(Operation terms) {
//        m.param.outputVolume.set(volume);
    }

}
