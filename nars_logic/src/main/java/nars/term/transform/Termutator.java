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
public final class Termutator extends ShuffledPermutations implements TermContainer {

    private final TermContainer compound;

    public Termutator(Random rng, TermContainer x) {
        restart(x.size(), rng);
        compound = x;
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
    public int size() {
        return compound.size();
    }

    @Override
    public Term term(int i) {
        return compound.term(get(i));
    }

    @Override
    public String toString() {
        return TermContainer.toString(this);
    }

    @Override
    public Term termOr(int index, Term resultIfInvalidIndex) {
        throw new RuntimeException("unimpl yet");
    }

    @Override
    public boolean impossibleSubTermVolume(int otherTermVolume) {
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
    public boolean equals(Object obj) {
        return compound.equals(obj);
    }

    @Override
    public int compareTo(Object o) {
        return compound.compareTo(o);
    }

    @Override
    public Iterator iterator() {
        return compound.iterator();
    }

    @Override
    public Term[] termsCopy() {
        return TermContainer.copyByIndex(this);
    }

    @Override
    public void setNormalized(boolean b) {

    }

    @Override
    public boolean isNormalized() {
        return false;
    }

    @Override
    public Term[] terms() {
        throw new RuntimeException("only termsCopy available and its not efficient");
    }
}
