package nars.term.transform;

import nars.term.Term;
import nars.term.TermContainer;
import nars.util.math.ShuffledPermutations;

import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

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
    public Term termOr(int index, Term resultIfInvalidIndex) {
        throw new RuntimeException("unimpl yet");
    }

    @Override
    public final boolean impossibleSubTermVolume(int otherTermVolume) {
        return compound.impossibleSubTermVolume(otherTermVolume);
    }

    @Override
    public boolean containsTerm(Term term) {
        return compound.containsTerm(term);
    }

    @Override
    public void forEach(Consumer action, int start, int stop) {
        compound.forEach(action, start, stop);
    }

    @Override
    public int varDep() {
        return compound.varDep();
    }

    @Override
    public int varIndep() {
        return compound.varIndep();
    }

    @Override
    public int varQuery() {
        return compound.varQuery();
    }

    @Override
    public int vars() {
        return compound.vars();
    }

    @Override
    public int compareTo(Object o) {
        return compound.compareTo(o);
    }

    @Override
    public Iterator iterator() {
        return compound.iterator();
    }

    public Term[] termsCopy() {
        return TermContainer.copyByIndex(this);
    }

    @Override
    public Term[] terms() {
        throw new RuntimeException("only termsCopy available and its not efficient");
    }
}
