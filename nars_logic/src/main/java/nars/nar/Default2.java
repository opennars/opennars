package nars.nar;

import nars.Memory;
import nars.process.concept.QueryVariableExhaustiveResults;

/**
 * Various extensions enabled
 */
public class Default2 extends Default {



    public Default2(Memory mem, int i, int i1, int i2, int i3) {
        super(mem, i, i1, i2, i3);

        new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }
}
