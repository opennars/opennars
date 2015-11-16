package nars.term.transform;

import nars.term.Term;
import nars.term.TermContainer;
import nars.util.math.ShuffledPermutations;

import java.util.Random;

/**
 * proxy to a TermContainer providing access to its subterms via a shuffling order
 */
public final class ShuffleTermVector extends ShuffledPermutations implements TermContainer {

    private final TermContainer compound;

    public ShuffleTermVector(Random rng, TermContainer x) {
        super();
        restart(x.size(), rng);
        this.compound = x;
    }

    @Override
    public int structure() {
        return compound.structure();
    }

    @Override
    public int volume() {
        return compound.volume();
    }

    @Override
    public int complexity() {
        return compound.complexity();
    }

    @Override
    public final int size() {
        return compound.size();
    }

    @Override
    public final Term term(int i) {
        return compound.term(get(i));
    }

    @Override
    public boolean impossibleSubTermVolume(int otherTermVolume) {
        return compound.impossibleToMatch(otherTermVolume);
    }

    @Override
    public int compareTo(Object o) {
        return compound.compareTo(o);
    }

}
