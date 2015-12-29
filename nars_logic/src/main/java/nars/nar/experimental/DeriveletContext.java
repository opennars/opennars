package nars.nar.experimental;

import nars.NAR;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.term.Termed;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Created by me on 9/5/15.
 */
public abstract class DeriveletContext implements Consumer<NAR> {

    /** random # generator local to this thread */
    public final Random rng;

    /** current concept, next concept */
    public final Supplier<Concept> conceptSupply;
    public final NAR nar;
    public static final Deriver deriver = Deriver.getDefaultDeriver();

    //private float forgetCycles;

    protected DeriveletContext(NAR nar, Random rng, Supplier<Concept> conceptSupply) {
        this.nar = nar;
        this.rng = rng;
        this.conceptSupply = conceptSupply;
        nar.memory.eventFrameStart.on(this);
    }

    @Override
    public void accept(NAR nar) {
        //each cycle
        //Memory memory = nar.memory;
        //forgetCycles = memory.duration() * 1; //memory.conceptForgetDurations.floatValue();
    }

    public float nextFloat() {
        return rng.nextFloat();
    }


    public abstract Concept concept(Termed term);

//    public float getForgetCycles() {
//        return forgetCycles;
//    }
}
