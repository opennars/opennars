package nars.term;

import nars.term.transform.CompoundTransform;
import nars.term.transform.TermVisitor;
import nars.term.transform.VariableNormalization;
import nars.util.utf8.ByteBuf;

import java.util.Arrays;
import java.util.Collection;

import static nars.Symbols.ARGUMENT_SEPARATORbyte;
import static nars.Symbols.COMPOUND_TERM_CLOSERbyte;


public abstract class DefaultCompound2<T extends Term> implements Compound<T> {

    final TermVector<T> terms;

    /**
     * true iff definitely normalized, false to cause it to update on next normalization.
     * used to prevent repeated normalizations
     */
    protected transient boolean normalized = false;

    /**
     * subclasses should be sure to call init() in their constructors; it is not done here
     * to allow subclass constructors to set data before calling init()
     */
    protected DefaultCompound2() {
        super();
        terms = isCommutative() ?
                new TermSet() :
                new TermVector();
    }

    @Override
    public String toString() {
        return toString(true); //TODO make this default to false
    }

    @Override
    public int compareTo(final Object o) {
        if (this == o) return 0;

        Term t = (Term) o;
        int diff = op().compareTo(t.op());
        if (diff != 0) return diff;

        return subterms().compareTo(((Compound)o).subterms());
    }


    /**
     * call this after changing Term[] contents: recalculates variables and complexity
     */
    protected void init(final T... term) {

        this.terms.init(term, getHashSeed(), op());

        this.normalized = !hasVar();

    }


    @Override
    public final void addAllTo(Collection<Term> set) {
        terms.addAllTo(set);
    }

    @Override
    public final Term[] newSubtermArray() {
        return terms.newArray();
    }


    //    /**
//     * (shallow) Clone the component list
//     */
    public final Term[] cloneTerms(final Term... additional) {
        return Compound.cloneTermsAppend(terms.term, additional);
    }


    @Override
    public final TermContainer subterms() {
        return terms;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Compound)) return false;

        Compound c = (Compound)that;
        return (c.op() == op() && c.subterms().equals(subterms()));
    }


    @Override
    public <T extends Term> T normalized() {
        return normalized(false);
    }


    @Override
    public final int rehashCode() {
        int ch = subterms().hashCode();
        if (ch == 0) {
            rehash();
            return subterms().hashCode();
        }
        return ch;
    }


    /** any additional properties of this term which could be used to influence the contentHash as a means of short circuiting equality */
    protected int getHashSeed() {
        return 0;
    }

    /**
     * recursively set duration to interval subterms
     */
    @Override
    public final void setDuration(int duration) {
        int n = size();
        for (int i = 0; i < n; i++)
            term(i).setDuration(duration);
    }

    @Override
    public final void rehash() {
        init(this.terms.term);
    }

    @Override
    public final int hashCode() {
        int ch = subterms().hashCode();
        if (ch == 0) {
            throw new RuntimeException("should have hashed");
//            rehash();
//            ch = this.contentHash;
        }
        return ch;
    }




    /**
     * searches for a subterm
     * TODO parameter for max (int) level to scan down
     */
    @Override
    public boolean containsTermRecursively(final Term target) {
        if (impossibleSubterm(target)) return false;

        for (final Term x : terms.term) {
            if (impossibleSubTermOrEquality(target))
                continue;
            if (x.equals(target)) return true;
            if (x instanceof Compound) {
                if (x.containsTermRecursively(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Compound cloneDeep() {
        Term c = clone(terms.cloneTermsDeep());
        if (c == null) return null;

//        if (c.operator() != operator()) {
//            throw new RuntimeException("cloneDeep resulted in different class: " + c + '(' + c.getClass() + ") from " + this + " (" + getClass() + ')');
//        }


        return ((Compound) c);
    }

    @Override
    abstract public Term clone();

    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    protected final <T extends Term> T normalized(final boolean destructive) {

        if (normalized) {
            return (T) this;
        } else {
            final Compound result = VariableNormalization.normalizeFast(this, destructive).getResult();
            if (result == null)
                return null;

            //HACK
            ((DefaultCompound2) result).normalized = true;

            return (T) result;
        }

    }

    /**
     * transforms destructively, may need to use on a new clone
     *
     * @return if changed
     */
    @Override
    public boolean transform(CompoundTransform<Compound<T>, T> trans, int depth) {

        boolean changed = false;

        Compound<T> thiss = null;

        T[] term = this.terms.term;
        final int len = term.length;

        for (int i = 0; i < len; i++) {
            T t = term[i];

            if (trans.test(t)) {
                if (thiss == null) thiss = this;
                T s = trans.apply(thiss, t, depth + 1);
                if (!s.equals(t)) {
                    term[i] = s;
                    changed = true;
                }
            } else if (t instanceof Compound) {
                //recurse
                changed |= ((Compound) t).transform(trans, depth + 1);
            }

        }


        if (changed) {
            if (isCommutative()) {
                Arrays.sort(term);
            }

            rehash();
        }

        return changed;
    }


    @Override
    public int getByteLen() {
        int len = /* opener byte */1;

        final int n = size();
        for (int i = 0; i < n; i++) {
            len += term(i).getByteLen() + 1 /* separator or closer if end*/;
        }

        return len;
    }


    @Override
    public byte[] bytes() {

        final int numArgs = size();

        ByteBuf b = ByteBuf.create(getByteLen());

        b.add((byte) op().ordinal()); //header

        appendBytes(numArgs, b);

        b.add(COMPOUND_TERM_CLOSERbyte); //closer

        return b.toBytes();
    }

    protected void appendBytes(int numArgs, ByteBuf b) {

        for (int i = 0; i < numArgs; i++) {
            Term t = term(i);

            if (i != 0) {
                b.add(ARGUMENT_SEPARATORbyte);
            }

            b.add(t.bytes());
        }

    }


    @Override
    public final void recurseTerms(final TermVisitor v, final Term parent) {
        v.visit(this, parent);

        final int n = size();
        for (int i = 0; i < n; i++) {
            term(i).recurseTerms(v, this);
        }
    }

    @Override
    public Compound<T> normalizeDestructively() {
        return normalized(true);
    }

}
