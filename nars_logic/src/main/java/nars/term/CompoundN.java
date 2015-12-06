package nars.term;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.term.compile.TermIndex;
import nars.term.transform.VariableNormalization;
import nars.term.visit.SubtermVisitor;
import nars.term.visit.TermPredicate;
import nars.util.utf8.ByteBuf;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import static nars.Symbols.ARGUMENT_SEPARATORbyte;
import static nars.Symbols.COMPOUND_TERM_CLOSERbyte;
import static nars.util.data.Util.hashCombine;


public abstract class CompoundN<T extends Term> implements Compound<T> {

    protected final TermVector<T> terms;

    /**
     * true iff definitely normalized, false to cause it to update on next normalization.
     * used to prevent repeated normalizations
     */
    protected transient boolean normalized = false;
    protected transient final int hash;


    protected CompoundN(Term... t) {
        this(new TermVector(t));
    }
    protected CompoundN(Term[] t, int hashSalt) {
        this(new TermVector(t), hashSalt);
    }

    protected CompoundN(TermVector subterms) {
        this(subterms, 0);
    }

    /** if hash salt is non-zero, it will be combined with the default hash value of the compound */
    protected CompoundN(TermVector subterms, int hashSalt) {
        this.terms = subterms;
        this.normalized = !hasVar();

        int h = hashCombine( subterms().hashCode(), op().ordinal() );
        if (hashSalt!=0)
            h = hashCombine(h, hashSalt);

        this.hash = h;
    }

    public CompoundN(T t) {
        this(new Term[] { t } );
    }

    @Override
    public String toString() {
        return toString(true); //TODO make this default to false
    }

    @Override
    public int compareTo(final Object o) {
        if (this == o) return 0;

        Term t = (Term) o;
        int diff = Integer.compare(op().ordinal(), t.op().ordinal());
        //int diff = op().compareTo(t.op());
        if (diff != 0) return diff;

        return subterms().compareTo( ((Compound)o).subterms() );
    }


    @Override
    public final void addAllTo(Collection<Term> set) {
        terms.addAllTo(set);
    }





    @Override
    public final TermVector<T> subterms() {
        return terms;
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that)
            return true;
        if (!(that instanceof Compound))
            return false;

        if (hash != that.hashCode()) return false;

        return equalsCompound((Compound) that);
/*

        TermContainer csubs = c.subterms();
        TermVector<T> osubs = this.terms;
        if (osubs.equals(csubs)) {
            //TODO dont share if contains Sequence/Parallel because these could not actual be 'equal'

            if (osubs.getClass() == csubs.getClass())
                this.terms = (TermVector<T>) csubs; //share instance iff equal class
            return (op() == c.op());
        }
        return false;
        */
    }

    public boolean equalsCompound(Compound that) {
        Compound c = that;

        return c.subterms().equals(subterms()) && (c.op() == op());
    }


    @Override
    public <T extends Term> T normalized() {
        return normalized(false);
    }

    @Override
    public Term normalized(TermIndex termIndex) {
        //return cloneTransforming(termIndex.getCompoundTransformer());
        transform(termIndex.getCompoundTransformer());
        return this;
    }

    //    @Override
//    @Deprecated public final int rehashCode() {
////        int ch = subterms().hashCode();
////        if (ch == 0) {
////            rehash();
////            return subterms().hashCode();
////        }
////        return ch;
//        return hashCode();
//    }


    /**
     * recursively set duration to interval subterms
     */
    @Override
    public void setDuration(int duration) {
        int n = size();
        for (int i = 0; i < n; i++)
            term(i).setDuration(duration);
    }


    @Override
    public final int hashCode() {
        return hash;
//        if (ch == 0) {
//            throw new RuntimeException("should have hashed");
////            rehash();
////            ch = this.contentHash;
//        }
//        return ch;
    }


    @Override
    public final int varDep() {
        return terms.varDep();
    }

    @Override
    public final int varIndep() {
        return terms.varIndep();
    }

    @Override
    public final int varQuery() {
        return terms.varQuery();
    }

    @Override
    public final int vars() {
        return terms.vars();
    }

    public final Term[] cloneTermsReplacing(int index, Term replaced) {
        return terms.cloneTermsReplacing(index, replaced);
    }


    public final boolean isEmpty() {
        return terms.isEmpty();
    }

    public final boolean contains(Object o) {
        return terms.contains(o);
    }

//    @Override
//    public final boolean equalsAll(Term[] cls) {
//        return terms.equalsAll(cls);
//    }

    @Override
    public final void forEach(Consumer<? super T> action, int start, int stop) {
        terms.forEach(action, start, stop);
    }

    @Override
    public final void forEach(Consumer<? super T> c) {
        terms.forEach(c);
    }

    @Override public T[] terms() {
        return terms.terms();
    }

    @Override
    public Term[] terms(IntObjectPredicate<T> filter) {
        return terms.terms(filter);
    }

    @Override
    public final Iterator<T> iterator() {
        return terms.iterator();
    }

    @Override
    public final  T[] termsCopy() {
        return terms.termsCopy();
    }

    @Override
    public final int structure() {
        return terms.structure() | (1 << op().ordinal());
    }

    @Override
    public final T term(int i) {
        return terms.term(i);
    }

    @Override
    public final Term termOr(int index, Term resultIfInvalidIndex) {
        return terms.termOr(index, resultIfInvalidIndex);
    }

    @Override
    public final boolean containsTerm(Term target) {
        return terms.containsTerm(target);
    }

    @Override
    public final int size() {
        return terms.size();
    }

    @Override
    public final int complexity() {
        return terms.complexity();
    }

    @Override
    public final int volume() {
        return terms.volume();
    }

    @Override
    public final boolean impossibleSubTermVolume(int otherTermVolume) {
        return terms.impossibleSubTermVolume(otherTermVolume);
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


    abstract public Term clone();


    /**
     * Normalizes if contain variables which need to be finalized for use in a Sentence
     * May return null if the resulting compound term is invalid
     */
    protected final <T extends Term> T normalized(final boolean destructive) {

        if (normalized) {
            return (T) this;
        } else {
            if (destructive) {
                throw new RuntimeException("destructive normalization disabled");
            }

            final Compound result = VariableNormalization.normalizeFast(this, destructive).get();
            if (result == null)
                return null;


            ((CompoundN) result).normalized = true;

            return (T) result;
        }

    }

//    /**
//     * produces a new clone if changed
//     * @return if changed
//     */
//    @Override
//    public boolean transform(CompoundTransform<Compound<T>, T> trans, int depth) {
//
//        boolean changed = false;
//
//        Term[] term = new Term[size()];
//        int mods = cloneTermsTransforming(trans,
//        final int len = term.length;
//
//        for (int i = 0; i < len; i++) {
//            T t = term[i];
//
//            if (trans.test(t)) {
//                T s = term[i] = trans.apply(this, t, depth + 1);
//                if (!s.equals(t)) {
//                    changed = true;
//                }
//            } else if (t instanceof Compound) {
//                //recurse
//                changed |= ((Compound) t).transform(trans, depth + 1);
//            }
//
//        }
//
//
//        if (changed) {
//            if (isCommutative()) {
//                Arrays.sort(term);
//            }
//        }
//
//        return changed;
//    }


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

            try {
                byte[] bb = t.bytes();
                if (bb.length!=t.getByteLen())
                    System.err.println("wtf");
                b.add(bb);
            }
            catch (ArrayIndexOutOfBoundsException a) {
                System.err.println("Wtf");
            }
        }

    }

    @Override
    public boolean and(TermPredicate v) {
        return terms.and(v);
    }
    @Override
    public boolean or(TermPredicate v) {
        return v.test(this) || terms.or(v);
    }


    @Override
    public final void recurseTerms(final SubtermVisitor v, final Term parent) {
        v.visit(this, parent);
        terms.visit(v, this);
    }

    @Override
    public Compound<T> normalizeDestructively() {
        return normalized(false);
    }

}
