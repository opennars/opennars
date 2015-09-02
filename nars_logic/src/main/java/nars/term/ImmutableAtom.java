package nars.term;

import nars.nal.nal7.TemporalRules;
import nars.term.transform.TermVisitor;
import nars.util.data.id.LiteralUTF8Identifier;

/**
 *
 * An atomic term type which has an immutable, static Identifier at constructoin
 */
public abstract class ImmutableAtom extends LiteralUTF8Identifier implements Term {

    public ImmutableAtom(String name) {
        super(name);
    }

    public ImmutableAtom(byte[] b) {
        super(b);
    }

    @Override
    public int complexity() {
        return 1;
    }

    @Override
    public int getTemporalOrder() {
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
    public int containedTemporalRelations() {        return 0;     }

    @Override
    public int length() {        return 0;    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override public int volume() { return 1; }

    public boolean impossibleSubTermVolume(final int otherTermVolume) {
        return true;
    }

    @Override
    public byte[] bytes() {
        return data;
    }

    @Override public boolean impossibleStructure(int possibleSubtermStructure) {
        /*
        for atomic terms, there will be only one
        bit set in this (for the operator). if it does not equal
        the parameter, then the structure can not match.
        */
        return structure()!=possibleSubtermStructure;
    }

    @Override
    public boolean containsTerm(Term target) {
        return false;
    }

    @Override
    public boolean containsTermRecursivelyOrEquals(Term target) {
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
    public boolean isNormalized() {
        return true;
    }

    @Override
    public Term normalized() {
        return this;
    }

}
