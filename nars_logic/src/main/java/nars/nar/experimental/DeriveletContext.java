package nars.nar.experimental;

import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.term.Term;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by me on 9/5/15.
 */
abstract public class DeriveletContext implements Consumer<NAR> {

    /** random # generator local to this thread */
    public final Random rng;

    /** current concept, next concept */
    public final Supplier<Concept> conceptSupply;
    public final NAR nar;
    private float forgetCycles;

    public DeriveletContext(NAR nar, Random rng, Supplier<Concept> conceptSupply) {
        this.nar = nar;
        this.rng = rng;
        this.conceptSupply = conceptSupply;
        nar.memory.eventFrameStart.on(this);
    }

    @Override
    public void accept(final NAR nar) {
        //each cycle
        Memory memory = nar.memory;
        forgetCycles = memory.duration() * memory.conceptForgetDurations.floatValue();
    }

    public float nextFloat() {
        return rng.nextFloat();
    }


    public abstract Concept concept(Term term);

    public float getForgetCycles() {
        return forgetCycles;
    }
}
