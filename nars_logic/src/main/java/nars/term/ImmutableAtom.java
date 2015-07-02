package nars.term;

import nars.nal.nal7.TemporalRules;
import nars.term.transform.TermVisitor;
import nars.util.data.id.Identifier;
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
    public int getComplexity() {
        return 1;
    }

    public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

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

    @Override
    public boolean isNormalized() {
        return true;
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
    public int compareTo(Object o) {
        if (this == o ) return 0;
        Class oc = o.getClass();
        Class c = getClass();
        if (o.getClass() == getClass()) {
            return compare(((ImmutableAtom) o).name());
        }
        return Integer.compare(oc.hashCode(), c.hashCode());
    }

    @Override
    public void identifierEquals(Identifier other) {
        //ignore
    }

    @Override
    public Identifier name() {
        return this;
    }
}
