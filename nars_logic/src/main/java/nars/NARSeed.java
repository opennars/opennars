package nars;

import nars.concept.ConceptBuilder;

/**
 * NAR design parameters which define a NAR at initialization.
 * All the information necessary to create a new NAR (builder / parameter object)
 * These do not change after initialization.
 * For runtime parameters, @see Param
 */
@Deprecated abstract public interface NARSeed<P extends Param> extends ConceptBuilder {
    Memory newMemory();

//
//    default int getMaximumNALLevel() {
//        return 8;
//    }
//
//    /** called after NAR created, for initializing it */
//    public void init(NAR nar);
//


}
