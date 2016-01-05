package nars.term.compound;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.Op;
import nars.Symbols;
import nars.nal.nal8.Operator;
import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.compile.TermPrinter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import static nars.Symbols.*;


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

    public static void productAppend(Compound product, Appendable p, boolean pretty) throws IOException {

        int s = product.size();
        p.append(COMPOUND_TERM_OPENER);
        for (int i = 0; i < s; i++) {
            product.term(i).append(p, pretty);
            if (i < s - 1) {
                p.append(pretty ? ", " : ",");
            }
        }
        p.append(COMPOUND_TERM_CLOSER);
    }

    public static void imageAppend(GenericCompound image, Appendable p, boolean pretty) throws IOException {

        int len = image.size();

        p.append(COMPOUND_TERM_OPENER);
        p.append(image.op().str);

        int relationIndex = image.relation();
        int i;
        for (i = 0; i < len; i++) {
            Term tt = image.term(i);

            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');

            if (i == relationIndex) {
                p.append(Symbols.IMAGE_PLACE_HOLDER);
                p.append(ARGUMENT_SEPARATOR);
                if (pretty) p.append(' ');
            }

            tt.append(p, pretty);
        }
        if (i == relationIndex) {
            p.append(ARGUMENT_SEPARATOR);
            if (pretty) p.append(' ');
            p.append(Symbols.IMAGE_PLACE_HOLDER);
        }

        p.append(COMPOUND_TERM_CLOSER);

    }

    public static void setAppend(Compound set, Appendable p, boolean pretty) throws IOException {

        int len = set.size();

        //duplicated from above, dont want to store this as a field in the class
        char opener, closer;
        if (set.op(Op.SET_EXT)) {
            opener = Op.SET_EXT_OPENER.ch;
            closer = Symbols.SET_EXT_CLOSER;
        } else {
            opener = Op.SET_INT_OPENER.ch;
            closer = Symbols.SET_INT_CLOSER;
        }

        p.append(opener);
        for (int i = 0; i < len; i++) {
            Term tt = set.term(i);
            if (i != 0) p.append(Symbols.ARGUMENT_SEPARATOR);
            tt.append(p, pretty);
        }
        p.append(closer);
    }

    public static void operationAppend(Compound argsProduct, Operator operator, Appendable p, boolean pretty) throws IOException {

        Term predTerm = operator.identifier(); //getOperatorTerm();

        if ((predTerm.volume() != 1) || (predTerm.hasVar())) {
            //if the predicate (operator) of this operation (inheritance) is not an atom, use Inheritance's append format
            TermPrinter.appendSeparator(p, pretty);
            return;
        }


        Term[] xt = argsProduct.terms();

        predTerm.append(p, pretty); //add the operator name without leading '^'
        p.append(COMPOUND_TERM_OPENER);


        int n = 0;
        for (Term t : xt) {
            if (n != 0) {
                p.append(ARGUMENT_SEPARATOR);
                if (pretty)
                    p.append(' ');
            }

            t.append(p, pretty);


            n++;
        }

        p.append(COMPOUND_TERM_CLOSER);

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
                setAppend(this, p, pretty);
                break;
            case PRODUCT:
                productAppend(this, p, pretty);
                break;
            case IMAGE_INT:
            case IMAGE_EXT:
                imageAppend(this, p, pretty);
                break;
            default:
                if (op.isStatement()) {
                    if (Op.isOperation(this)) {
                        operationAppend((Compound) term(0), (Operator) term(1), p, pretty); //TODO Appender
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

    @Override
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
