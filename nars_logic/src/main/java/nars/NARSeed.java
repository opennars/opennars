package nars;

import nars.bag.impl.CacheBag;
import nars.clock.Clock;
import nars.concept.Concept;
import nars.io.Perception;
import nars.nal.LogicPolicy;
import nars.process.CycleProcess;
import nars.process.DerivationReaction;
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Random;

/**
 * NAR design parameters which define a NAR at initialization.
 * All the information necessary to create a new NAR (builder / parameter object)
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
abstract public class NARSeed extends Param {

    //public final Random rng = new RandomAdaptor(new MersenneTwister(1));
    public final Random rng = new XorShift1024StarRandom(1);

    protected int maxNALLevel;

    abstract public CycleProcess newControlCycle();

    public NARSeed() {
    }

    /** avoid calling this directly; use Default.simulationTime() which also sets the forgetting mode */
    public NARSeed setClock(Clock clock) {
        this.clock = clock;
        return this;
    }


    protected Memory newMemory(Param narParam, LogicPolicy policy) {
        return new Memory(rng, getMaximumNALLevel(), narParam, policy, newIndex());
    }

    protected abstract CacheBag<Term,Concept> newIndex();

    protected abstract int getMaximumNALLevel();

    /** called after NAR created, for initializing it */
    public void init(NAR nar) {
    }



    @Override
    public String toString() {
        return getClass().getSimpleName();
    }


    abstract public LogicPolicy getLogicPolicy();

    public NARSeed level(int maxNALlevel) {
        this.maxNALLevel = maxNALlevel;
        return this;
    }


    abstract public Perception newPerception();

}
