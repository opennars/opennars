package nars;

import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.nal.PremiseProcessor;
import nars.process.CycleProcess;
import nars.term.Term;

import java.util.Random;
import java.util.function.Function;

/**
 * NAR design parameters which define a NAR at initialization.
 * All the information necessary to create a new NAR (builder / parameter object)
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
abstract public interface NARSeed<P extends Param> extends ConceptBuilder {


    CycleProcess newCycleProcess();

    default public Memory newMemory() {
        return new Memory(
                getRandom(),
                getMaximumNALLevel(),
                newParam(),
                getConceptBuilder(),
                getPremiseProcessor(),
                newIndex()
        );
    }

    Param newParam();

    /** common random number generator */
    Random getRandom();

    CacheBag<Term,Concept> newIndex();

    int getMaximumNALLevel();

    /** called after NAR created, for initializing it */
    void init(NAR nar);


    PremiseProcessor getPremiseProcessor();

    public ConceptBuilder getConceptBuilder();

    default public NARStream stream() {
        return new NARStream(new NAR(this));
    }

}
