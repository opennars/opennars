package nars.term.compound;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Op;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal3.SetTensional;
import nars.nal.nal4.Image;
import nars.nal.nal4.Product;
import nars.nal.nal7.Order;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.term.*;
import nars.term.visit.SubtermVisitor;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static nars.Symbols.COMPOUND_TERM_CLOSERbyte;
import static nars.nal.nal5.Conjunctive.flattenAndSort;


public class GenericCompound<T extends Term> implements Compound<T> {

    protected final TermVector<T> terms;

    protected final transient int hash;
    protected final Op op;
    public final int relation;
    private boolean normalized = false;

    public static Term COMPOUND(Op op, Term a) {
        return COMPOUND(op, new Term[] { a });
    }

    public static Term COMPOUND(Op op, Term a, Term b) {
        return COMPOUND(op, new Term[] { a, b });
    }
    public static Term COMPOUND(Op op, Term a, Term b, Term c) {
        return COMPOUND(op, new Term[] { a, b, c });
    }

    public static Term COMPOUND(Op op, Term... subterms) {

        //if no relation is specified and it's an Image:
        if (op.isImage() && Image.hasPlaceHolder(subterms)) {
            return Image.build(op, subterms);
        }

        return COMPOUND(op, subterms, -1);
    }

    public static Term COMPOUND(Op op, Term[] t, int relation) {

        if (op.isCommutative()) {
            t = Terms.toSortedSetArray(t);
        }

        //REDUCTIONS
        if (op.isStatement()) {
            if ((t.length==2) && (Statement.invalidStatement(t[0], t[1]))) {
                return null;
            }
        } else {
            switch (op) {
                case IMAGE_INT:
                case IMAGE_EXT:
                    if ((relation == -1) || (relation > t.length))
                        throw new RuntimeException("invalid index relation: " + relation + " for args " + Arrays.toString(t));
                    break;
                case CONJUNCT:
                    t = flattenAndSort(t, Order.None);
                    break;
                case DIFF_EXT:
                    Term t0 = t[0], t1 = t[1];
                    if ((t0.op(Op.SET_EXT) && t1.op(Op.SET_EXT) )) {
                        return SetExt.subtractExt((Compound)t0, (Compound)t1);
                    }
                    break;
                case DIFF_INT:
                    Term it0 = t[0], it1 = t[1];
                    if ((it0.op(Op.SET_INT) && it1.op(Op.SET_INT) )) {
                        return SetInt.subtractInt((Compound)it0, (Compound)it1);
                    }
                    break;
                /*case DISJUNCT:
                    break;*/
            }
        }



        int numSubs = t.length;
        if (!op.validSize(numSubs)) {
            if (op.minSize == 2 && numSubs == 1) {
                return t[0]; //reduction
            }
            //throw new RuntimeException(Arrays.toString(t) + " invalid size for " + op);
            return null;
        }

        return new GenericCompound(op, t, relation);
    }

    protected GenericCompound(Op op, T... subterms) {
        this(op, subterms, 0);
    }

    protected GenericCompound(Op op, T[] subterms, int relation) {

        this.op = op;

        TermVector<T> terms = this.terms = op.isCommutative() ?
                TermSet.newTermSetPresorted(subterms) :
                new TermVector(subterms);
        hash = Compound.hash(terms, op, relation);
        this.relation = relation;
    }


    @Override
    public final Op op() {
        return op;
    }

    @Override
    public final boolean isCommutative() {
        return op.isCommutative();
    }

    public void append(Appendable p, boolean pretty) throws IOException {

        switch (op) {
            case SET_INT_OPENER:
            case SET_EXT_OPENER: SetTensional.Appender.accept(this, p);
                break;
            case PRODUCT:
                p.append(Product.toString(this)); //TODO Appender
                break;
            default:
                if (op.isStatement()) {
                    if (Operation.isOperation(this)) {
                        Operation.appendOperation((Compound) term(0), (Operator) term(1), p, pretty); //TODO Appender
                    }
                    else {
                        Statement.Appender.accept(this, p);
                    }
                } else {
                    Compound.appendCompound(this, p, pretty);
                }
                break;
        }



    }


    @Override
    public String toStringCompact() {
        return toString(false);
    }



    @Override
    public final int compareTo(Object o) {
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
    public Term clone(Term[] replaced) {
        return new GenericCompound(op(), replaced, relation);
    }

    @Override
    public final TermVector<T> subterms() {
        return terms;
    }

    @Override
    public final boolean equals(Object that) {
        if (this == that)
            return true;

        if (!(that instanceof GenericCompound))
            return false;

        GenericCompound c = (GenericCompound)that;
        if (hash != c.hash||
            (op != c.op) || (relation!=c.relation)
            )
            return false;

        return subterms().equals(c.subterms());
    }


    /**
     * recursively set duration to interval subterms
     */
    @Override
    public void setDuration(int duration) {
        if (TermMetadata.hasTemporals(this)) {
            int n = size();
            for (int i = 0; i < n; i++)
                term(i).setDuration(duration);
        }
    }


    @Override
    public final int hashCode() {
        return hash;
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
    public final Term[] terms(IntObjectPredicate<T> filter) {
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
        return terms.structure() | (1 << op.ordinal());
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
    public final boolean containsTermRecursively(Term target) {

        if (impossibleSubterm(target)) return false;

        return terms.containsTermRecursively(target);
    }



    @Override public final boolean isNormalized() {
        return normalized;
    }


    @Override
    public final void setNormalized(boolean b) {
        normalized = b;
    }

    @Override
    public int bytesLength() {
        int len = /* opener byte */1 + 1;

        int n = size();
        for (int i = 0; i < n; i++) {
            len += term(i).bytesLength() + 1 /* separator or closer if end*/;
        }

        return len;
    }

    @Override
    public String toString() {
        return toString(true); //TODO make this default to false
    }


    @Override
    public final byte[] bytes() {

        ByteBuf b = ByteBuf.create(bytesLength());

        b.add((byte) op().ordinal()); //header
        b.add((byte) relation); //header

        appendSubtermBytes(b);

        b.add(COMPOUND_TERM_CLOSERbyte); //closer

        return b.toBytes();
    }


    @Override
    public void appendSubtermBytes(ByteBuf b) {
        terms.appendSubtermBytes(b);
    }

    @Override
    public final boolean and(Predicate<Term> v) {
        return v.test(this) && terms.and(v);
    }
    @Override
    public final boolean or(Predicate<Term> v) {
        return v.test(this) || terms.or(v);
    }


    @Override
    public final void recurseTerms(SubtermVisitor v, Term parent) {
        v.accept(this, parent);
        terms.visit(v, this);
    }


}
