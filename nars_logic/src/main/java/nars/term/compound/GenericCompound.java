package nars.term.compound;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Op;
import nars.nal.nal8.Operator;
import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.compile.TermPrinter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;


public class GenericCompound<T extends Term> implements Compound<T> {

    public final TermVector<T> terms;
    public final Op op;
    public final int relation;

    protected final transient int hash;
    private transient boolean normalized = false;


//    protected GenericCompound(Op op, T... subterms) {
//        this(op, -1, subterms);
//    }
//
//    public GenericCompound(Op op, int relation, T... subterms) {
//
//        TermVector<T> terms = this.terms = op.isCommutative() ?
//                TermSet.newTermSetPresorted(subterms) :
//                new TermVector(subterms);
//        this.op = op;
//        this.relation = relation;
//        this.hash = TermIndex.hash(terms, op, relation+1);
//    }

    public GenericCompound(Op op, TermVector subterms) {
        this(op, -1, subterms);
    }

    public GenericCompound(Op op, int relation, TermVector subterms) {
        this.terms = subterms;
        this.normalized = (subterms.vars() == 0);
        this.op = op;
        this.relation = relation;
        this.hash = TermIndex.hash(terms, op, relation+1);
    }

    @Override
    public final Op op() {
        return op;
    }

    @Override
    public final boolean isCommutative() {
        return op.isCommutative();
    }

    @Override
    public void append(Appendable p, boolean pretty) throws IOException {
        switch (op) {
            case SET_INT_OPENER:
            case SET_EXT_OPENER:
                TermPrinter.setAppend(this, p, pretty);
                break;
            case PRODUCT:
                TermPrinter.productAppend(this, p, pretty);
                break;
            case IMAGE_INT:
            case IMAGE_EXT:
                TermPrinter.imageAppend(this, p, pretty);
                break;
            default:
                if (op.isStatement()) {
                    if (Op.isOperation(this)) {
                        Operator.operationAppend((Compound) term(0), (Operator) term(1), p, pretty); //TODO Appender
                    }
                    else {
                        Statement.append(this, p, pretty);
                    }
                } else {
                    TermPrinter.appendCompound(this, p, pretty);
                }
                break;
        }



    }


    @Override
    public final int compareTo(Object o) {
        int r=0;
        if (this != o) {
            Termed t = (Termed) o;
            //int diff = op().compareTo(t.op());
            int diff = Integer.compare(op().ordinal(), t.op().ordinal());
            if (diff != 0) r = diff;
            else {

                Compound c = (Compound) (t.term());
                int diff2 = Integer.compare(relation(), c.relation());
                if (diff2 != 0) return diff2;

                r=subterms().compareTo(c.subterms());
            }
        }

        return r;
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
    public final boolean equals(Object that) {
        return this == that || hash == that.hashCode() && equalsFurther((Termed) that);
    }

    private boolean equalsFurther(Termed thatTerm) {

        boolean r=false;
        Term t = thatTerm.term();
        if ((op == t.op()) /*&& (((t instanceof Compound))*/) {
            Compound c = (Compound) t;
            r=terms.equals(c.subterms()) && (relation == c.relation());
        }
        return r;
    }


    /**
     * recursively set duration to interval subterms
     */
    @Override
    public void setDuration(int duration) {
        if (TermMetadata.hasMetadata(this)) {
            Term[] y = terms();
            for (Term x : y)
                x.setDuration(duration);
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

//    public final Term[] cloneTermsReplacing(int index, Term replaced) {
//        return terms.cloneTermsReplacing(index, replaced);
//    }

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
        return terms.term;
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
    public int structure() {
        return terms.structure() | op.bit();
    }

    @Override
    public final T term(int i) {
        return terms.term(i);
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
        return !impossibleSubterm(target) && terms.containsTermRecursively(target);
    }

    public final boolean isNormalized() {
        return normalized;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    @Override
    public int relation() {
        return relation;
    }

    /** do not call this manually, it will be set by VariableNormalization only */
    public final void setNormalized() {
        this.normalized = true;
    }


//    @Override
//    public int bytesLength() {
//        int len = /* opener byte */1 + (op.isImage() ? 1 : 0);
//
//        int n = size();
//        for (int i = 0; i < n; i++) {
//            len += term(i).bytesLength() + 1 /* separator or closer if end*/;
//        }
//
//        return len;
//    }

//    @Override
//    public final byte[] bytes() {
//
//        ByteBuf b = ByteBuf.create(bytesLength());
//
//        b.add((byte) op().ordinal()); //header
//
//        if (op().isImage()) {
//            b.add((byte) relation); //header
//        }
//
//        appendSubtermBytes(b);
//
//        if (op().maxSize != 1) {
//            b.add(COMPOUND_TERM_CLOSERbyte); //closer
//        }
//
//        return b.toBytes();
//    }
//
//
//    @Override
//    public void appendSubtermBytes(ByteBuf b) {
//        terms.appendSubtermBytes(b);
//    }
//
//    @Override
//    public final boolean and(Predicate<? super Term> v) {
//        return v.test(this) && terms.and(v);
//    }
//    @Override
//    public final boolean or(Predicate<? super Term> v) {
//        return v.test(this) || terms.or(v);
//    }




}
