package nars.nar.experimental;

import nars.NARSeed;
import nars.Param;
import nars.bag.impl.CacheBag;
import nars.bag.impl.GuavaCacheBag;
import nars.concept.Concept;
import nars.cycle.AbstractCycle;
import nars.process.CycleProcess;
import nars.term.Term;

/**
 * Created by me on 9/1/15.
 */
abstract public class AbstractNARSeed<B extends CacheBag<Term,Concept>, P extends Param> extends AbstractCycle<B> implements NARSeed<P> {

    public AbstractNARSeed() {
        this((B)new GuavaCacheBag<Term,Concept>());
    }

    public AbstractNARSeed(B concepts) {
        super(concepts);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + getMaximumNALLevel() + ']';
    }

    @Override
    public CycleProcess getCycleProcess() {
        return this;
    }
}
