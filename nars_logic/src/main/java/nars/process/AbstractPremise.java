package nars.process;

import nars.Memory;
import nars.premise.Premise;
import nars.task.Task;

/**
 * Base class for premises
 */
abstract public class AbstractPremise implements Premise {

    public final Memory memory;

    public AbstractPremise(Memory m) {
        this.memory = m;
    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    public void accept(Task derivedTask) {
        getMemory().add(derivedTask);
    }
}
