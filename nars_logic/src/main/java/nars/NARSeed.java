package nars;

import nars.model.ControlCycle;
import nars.nal.LogicPolicy;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

import java.util.Random;

/**
 * NAR design parameters which define a NAR at initialization.
 * All the information necessary to create a new NAR (builder / parameter object)
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
abstract public class NARSeed extends Param {

    public final Random rng = new RandomAdaptor(new MersenneTwister(1));

    protected int maxNALLevel;

    abstract public ControlCycle newControlCycle();

    public NARSeed() {
    }
            
    
//    /** initial runtime parameters */
//    public Param getParam() {
//        return param;
//    }
//    
//    public Bag<Task<Term>,Sentence<Term>> getNovelTaskBag() {
//        return novelTaskBag;
//    }
//    
//    public Attention getAttention() {
//        return attention;
//    }
//    
//    public ConceptBuilder getConceptBuilder() {
//        return conceptBuilder;
//    }

    protected Memory newMemory(Param narParam, LogicPolicy policy) {

        return new Memory(rng, getMaximumNALLevel(), narParam, policy, newControlCycle());
    }

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

}
