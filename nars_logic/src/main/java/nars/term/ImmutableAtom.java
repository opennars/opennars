package nars.term;

import nars.nal.nal7.TemporalRules;
import nars.term.transform.TermVisitor;
import nars.util.data.id.LiteralUTF8Identifier;

/**
 *
 * An atomic term type which has an immutable, static Identifier at constructoin
 */
public abstract class ImmutableAtom extends LiteralUTF8Identifier implements Term {

    public ImmutableAtom(final String name) {
        super(name);
    }

    public ImmutableAtom(final byte[] b) {
        super(b);
    }

    @Override
    public int complexity() {
        return 1;
    }

    @Override
    public final int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

    @Override
    public void recurseTerms(final TermVisitor v, Term parent) {
        v.visit(this, parent);
    }

    @Override
    public void recurseSubtermsContainingVariables(TermVisitor v, Term parent) {   }

    @Override
    abstract public Term clone();

    @Override
    public final int containedTemporalRelations() {        return 0;     }

    @Override
    public final int length() {
        throw new RuntimeException("Atomic terms have no subterms and length() should be zero");
        //return 0;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override public int volume() { return 1; }

    final public boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    @Override
    final public byte[] bytes() {
        return data;
    }

    @Override public final boolean impossibleStructure(int possibleSubtermStructure) {
        /*
        for atomic terms, there will be only one
        bit set in this (for the operator). if it does not equal
        the parameter, then the structure can not match.
        */
        return structure()!=possibleSubtermStructure;
    }

    @Override
    public final boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public final boolean containsTermRecursivelyOrEquals(Term target) {
        return equals(target);
    }


    @Override
    public Term cloneDeep() {
        return this;
    }

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }


    @Override
    public final boolean isNormalized() {
        return true;
    }

    @Override
    public final Term normalized() {
        return this;
    }

}
