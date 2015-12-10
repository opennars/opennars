package nars.term.compound;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import nars.$;
import nars.Op;
import nars.nal.nal3.SetExt;
import nars.nal.nal3.SetInt;
import nars.nal.nal3.SetTensional;
import nars.nal.nal4.Image;
import nars.nal.nal4.Product;
import nars.nal.nal7.Order;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.term.*;
import nars.term.visit.SubtermVisitor;
import nars.util.utf8.ByteBuf;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.collect.ObjectArrays.concat;
import static nars.Op.SET_EXT;
import static nars.Op.SET_INT;
import static nars.Symbols.COMPOUND_TERM_CLOSERbyte;
import static nars.nal.nal5.Conjunctive.flatten;


public class GenericCompound<T extends Term> implements Compound<T> {

    protected final TermVector<T> terms;

    protected final transient int hash;
    protected final Op op;
    public final int relation;
    private boolean normalized = false;


    public static Term COMPOUND(Op op, Term... subterms) {

        //if no relation was specified and it's an Image,
        if (op.isImage()) {
            //it must contain a _ placeholder
            if (Image.hasPlaceHolder(subterms)) {
                return Image.build(op, subterms);
            }
            return null;
        }

        return COMPOUND(op, subterms, -1);
    }

    public static Term COMPOUND(Op op, Term[] t, int relation) {

        /* special handling */
        switch (op) {
            case SEQUENCE:
                return Sequence.makeSequence(t);
            case PARALLEL:
                return Parallel.makeParallel(t);
            case INTERSECT_EXT:
                return newIntersectEXT(t);
        }


        //REDUCTIONS
        if (op.isStatement()) {
            return newStatement(op, t);
        } else {
            switch (op) {
                case IMAGE_INT:
                case IMAGE_EXT:
                    if ((relation == -1) || (relation > t.length))
                        throw new RuntimeException("invalid index relation: " + relation + " for args " + Arrays.toString(t));
                    break;
                case CONJUNCT:
                    t = flatten(t, Order.None);
                    break;
                case DIFF_EXT:
                    Term t0 = t[0], t1 = t[1];
                    if ((t0.op(SET_EXT) && t1.op(SET_EXT) )) {
                        return SetExt.subtractExt((Compound)t0, (Compound)t1);
                    }
                    break;
                case DIFF_INT:
                    Term it0 = t[0], it1 = t[1];
                    if ((it0.op(SET_INT) && it1.op(SET_INT) )) {
                        return SetInt.subtractInt((Compound)it0, (Compound)it1);
                    }
                    break;
                case INTERSECT_EXT: return newIntersectEXT(t);
                case INTERSECT_INT: return newIntersectINT(t);
                /*case DISJUNCT:
                    break;*/
            }
        }


        return newCompound(op, t, relation, op.isCommutative());
    }

    @Nullable
    public static Term newStatement(Op op, Term[] t) {
        if (op.isCommutative())
            t = Terms.toSortedSetArray(t);
        switch (t.length) {
            case 1: return t[0];
            case 2:
                if (!Statement.invalidStatement(t[0], t[1]))
                    return newCompound(op, t, -1, false); //already sorted
        }
        return null;
    }


    private static Term newCompound(Op op, Term[] t, int relation, boolean sort) {
        if (sort && op.isCommutative())
            t = Terms.toSortedSetArray(t);

        int numSubs = t.length;
        if (op.minSize == 2 && numSubs == 1) {
            return t[0]; //reduction
        }

        if (!op.validSize(numSubs)) {
            //throw new RuntimeException(Arrays.toString(t) + " invalid size for " + op);
            return null;
        }

        return new GenericCompound(op, t, relation);
    }

    private static Term newIntersectINT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_INT,
                Op.SET_INT,
                Op.SET_EXT);
    }
    private static Term newIntersectEXT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_EXT,
                Op.SET_EXT,
                Op.SET_INT);
    }

    private static Term newIntersection(Term[] t, Op intersection, Op setUnion, Op setIntersection) {
        if (t.length == 2) {

            Term term1 = t[0], term2 = t[1];

            if (term2.op(intersection) && !term1.op(intersection)) {
                //put them in the right order so everything fits in the switch:
                Term x = term1;
                term1 = term2;
                term2 = x;
            }

            Op o1 = term1.op();

            if (o1 == setUnion) {
                //set union
                if (term2.op(setUnion)) {
                    Term[] ss = concat(
                            ((Compound) term1).terms(),
                            ((Compound) term2).terms(), Term.class);

                    return setUnion == SET_EXT ? $.set(ss) : $.setInt(ss);
                }

            } else {
                // set intersection
                if (o1 == setIntersection) {
                    if (term2.op(setIntersection)) {
                        Set<Term> ss = ((Compound) term1).toSet();
                        ss.retainAll(((Compound) term2).toSet());
                        if (ss.isEmpty()) return null;
                        return setIntersection == SET_EXT ? $.set(ss) : $.setInt(ss);
                    }


                } else {

                    if (o1 == intersection) {
                        Term[] suffix;

                        if (term2.op(intersection)) {
                            // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                            suffix = ((Compound) term2).terms();

                        } else {
                            // (&,(&,P,Q),R) = (&,P,Q,R)

                            if (term2.op(intersection)) {
                                // (&,R,(&,P,Q)) = (&,P,Q,R)
                                throw new RuntimeException("should have been swapped into another condition");
                            }

                            suffix = new Term[]{term2};
                        }

                        t = concat(
                                ((Compound) term1).terms(), suffix, Term.class
                        );

                    }
                }
            }


        }

        return newCompound(intersection, t, -1, true);
    }


    protected GenericCompound(Op op, T... subterms) {
        this(op, subterms, 0);
    }

    protected GenericCompound(Op op, T[] subterms, int relation) {

        this.op = op;

        TermVector<T> terms = this.terms = op.isCommutative() ?
                TermSet.newTermSetPresorted(subterms) :
                new TermVector(subterms);
        hash = Compound.hash(terms, op, relation+1);
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
            case SET_EXT_OPENER:
                SetTensional.append(this, p, pretty);
                break;
            case PRODUCT:
                Product.append(this, p, pretty);
                break;
            case IMAGE_INT:
            case IMAGE_EXT:
                Image.appendImage(this, p, pretty);
                break;
            default:
                if (op.isStatement()) {
                    if (Operation.isOperation(this)) {
                        Operation.appendOperation((Compound) term(0), (Operator) term(1), p, pretty); //TODO Appender
                    }
                    else {
                        Statement.append(this, p, pretty);
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

        Compound c = (Compound)t;
        diff = Integer.compare(relation(), c.relation());
        //int diff = op().compareTo(t.op());
        if (diff != 0) return diff;

        return subterms().compareTo( c.subterms() );
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
        return
            (hash == c.hash) &&
            (op == c.op) &&
            (relation == c.relation) &&
            (terms.equals(c.subterms()));
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
        int len = /* opener byte */1 + (op.isImage() ? 1 : 0);

        int n = size();
        for (int i = 0; i < n; i++) {
            len += term(i).bytesLength() + 1 /* separator or closer if end*/;
        }

        return len;
    }

    @Override
    public String toString() {
        return toString(false); //TODO make this default to false
    }

    @Override
    public int relation() {
        return relation;
    }

    @Override
    public final byte[] bytes() {

        ByteBuf b = ByteBuf.create(bytesLength());

        b.add((byte) op().ordinal()); //header

        if (op().isImage()) {
            b.add((byte) relation); //header
        }

        appendSubtermBytes(b);

        if (op().maxSize != 1) {
            b.add(COMPOUND_TERM_CLOSERbyte); //closer
        }

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
