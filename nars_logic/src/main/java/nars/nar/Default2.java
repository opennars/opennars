package nars.nar;

import nars.LocalMemory;
import nars.Memory;
import nars.io.SortedTaskPerception;
import nars.io.TaskPerception;

/**
 * Various extensions enabled
 */
public class Default2 extends Default {


    public Default2(int i, int i1, int i2, int i3) {
        this(new LocalMemory(), i, i1, i2, i3);
    }

    public Default2(Memory mem, int i, int i1, int i2, int i3) {
        super(mem, i, i1, i2, i3);

        //new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }

    @Override
    public TaskPerception initInput() {
        TaskPerception input = new SortedTaskPerception(
                this,
                task -> true /* allow everything */,
                task -> exec(task),
                64,
                1
        );
        //input.inputsMaxPerCycle.set(conceptsFirePerCycle);;
        return input;
    }
}
