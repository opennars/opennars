package nars.testing.condition;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.io.out.Output;

/**
 * Counts # of outputs of different types
 */
public class OutputCount extends NARReaction {

    int inputs = 0;
    int outputs = 0;
    int others = 0;

    public OutputCount(NAR n) {
        super(n, Output.DefaultOutputEvents);
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.IN.class) inputs++;
        else if (event == Events.OUT.class) outputs++;
        else others++;
    }

    public int getInputs() {
        return inputs;
    }

    public int getOutputs() {
        return outputs;
    }

    public int getOthers() {
        return others;
    }

    public void clear() { inputs = outputs = others = 0; }
}
