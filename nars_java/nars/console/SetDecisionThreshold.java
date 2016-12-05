package nars.console;

import nars.core.control.AbstractTask;

/**
 * Sets the global volume / noise level, =(100% - "silence level")
 */
public class SetDecisionThreshold extends AbstractTask<CharSequence> {
    public final double volume;

    public SetDecisionThreshold(double volume) {
        super();
        this.volume=volume;
    }

    @Override
    public CharSequence name() {
        return "SetDecisionThreshold(" + volume + ')';
    }
    
}
