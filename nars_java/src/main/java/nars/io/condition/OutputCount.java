package nars.io.condition;

import com.nurkiewicz.typeof.TypeOf;
import nars.core.NAR;
import nars.event.AbstractReaction;
import nars.io.Output;

import static com.nurkiewicz.typeof.TypeOf.whenTypeOf;

/**
 * Counts # of outputs
 */
public class OutputCount extends Output {

    int inputs = 0;
    int outputs = 0;
    int others = 0;

    public OutputCount(NAR n) {
        super(n);
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == IN.class) inputs++;
        else if (event == OUT.class) outputs++;
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
}
