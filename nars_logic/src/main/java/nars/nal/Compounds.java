package nars.nal;

import com.gs.collections.api.set.MutableSet;
import nars.$;
import nars.Global;
import nars.Op;
import nars.Symbols;
import nars.budget.Budget;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.*;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.truth.Truth;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import static com.google.common.collect.ObjectArrays.concat;
import static java.util.Arrays.copyOfRange;
import static nars.Op.*;
import static nars.Symbols.*;
import static nars.term.Statement.pred;
import static nars.term.Statement.subj;
import static nars.util.data.Util.hashCombine;

/**
 * static compound builder support
 */
public interface Compounds {

    /**
     * universal zero-length product
     */
    Compound Empty = new GenericCompound(Op.PRODUCT, -1, Terms.Empty) {
        @Override
        public Term clone(Term[] replaced) {
            if (replaced.length == 0) return this;
            return super.clone(replaced);
        }
    };

    /**
     * implications, equivalences, and interval
     */
    int InvalidEquivalenceTerm =
            or(IMPLICATION, IMPLICATION_WHEN, IMPLICATION_AFTER, IMPLICATION_BEFORE,
                    EQUIV, EQUIV_AFTER, EQUIV_WHEN,
                    INTERVAL);
    /**
     * equivalences and intervals (not implications, they are allowed
     */
    int InvalidImplicationPredicate =
            or(EQUIV, EQUIV_AFTER, EQUIV_WHEN, INTERVAL);

    static void ensureFeasibleVolume(int vol, TermContainer c) {
        if (vol > Global.COMPOUND_VOLUME_MAX) {
            //$.logger.error("Term volume overflow");
            /*c.forEach(x -> {
                Terms.printRecursive(x, (String line) ->
                    $.logger.error(line)
                );
            });*/
            throw new RuntimeException("Term volume overflow: " + c);
        }
    }

    static void appendSeparator(Appendable p, boolean pretty) throws IOException {
        p.append(ARGUMENT_SEPARATOR);
        if (pretty) p.append(' ');
    }

    static void writeCompound1(Op op, Term singleTerm, Appendable writer, boolean pretty) throws IOException {
        writer.append(COMPOUND_TERM_OPENER);
        writer.append(op.str);
        writer.append(ARGUMENT_SEPARATOR);
        singleTerm.append(writer, pretty);
        writer.append(COMPOUND_TERM_CLOSER);
    }

    static byte[] newCompound1Key(Op op, Term singleTerm) {

        byte opByte = (byte) op.ordinal();

        byte[] termBytes = singleTerm.bytes();

        return ByteBuf.create(1 + termBytes.length)
                .add(opByte)
                .add(termBytes)
                .toBytes();
    }

    static void appendCompound(Compound c, Appendable p, boolean pretty) throws IOException {

        boolean opener = c.appendTermOpener();
        if (opener)
            p.append(COMPOUND_TERM_OPENER);


        boolean appendedOperator = c.appendOperator(p);

        if (c.size() == 1)
            p.append(ARGUMENT_SEPARATOR);

        c.appendArgs(p, pretty, appendedOperator);


        appendCloser(p);

    }

    static void appendCloser(Appendable p) throws IOException {
        p.append(COMPOUND_TERM_CLOSER);
    }

    /** universal compound hash function */
    static <T extends Term> int hash(TermVector subterms, Op op, int hashSalt) {
        int h = hashCombine( subterms.hashCode(), op.ordinal() );
        if (hashSalt!=0)
            h = hashCombine(h, hashSalt);
        return h;
    }

    default Term negation(Term t) {
        if (t.op() == Op.NEGATE) {
            // (--,(--,P)) = P
            return ((Compound) t).term(0);
        }
        return term(Op.NEGATE, -1, false, t);
    }

    static void setAppend(Compound set, Appendable p, boolean pretty) throws IOException {

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


    static void imageAppend(GenericCompound image, Appendable p, boolean pretty) throws IOException {

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

    default Term image(Op o, Term[] res) {

        int index = 0, j = 0;
        for (Term x : res) {
            if (x.equals(Op.Imdex)) {
                index = j;
            }
            j++;
        }

        if (index == -1) {
            throw new RuntimeException("invalid image subterms: " + Arrays.toString(res));
        } else {
            int serN = res.length - 1;
            Term[] ser = new Term[serN];
            System.arraycopy(res, 0, ser, 0, index);
            System.arraycopy(res, index + 1, ser, index, (serN - index));
            res = ser;
        }

        return term(
                o,
                index, res);
    }

    static boolean hasImdex(Term[] r) {
        for (Term x : r) {
            //        if (t instanceof Compound) return false;
//        byte[] n = t.bytes();
//        if (n.length != 1) return false;
            if (x.equals(Op.Imdex)) return true;
        }
        return false;
    }

//    /**
//     * Try to make a Product from an ImageExt/ImageInt and a component. Called by the logic rules.
//     *
//     * @param image     The existing Image
//     * @param component The component to be added into the component list
//     * @param index     The index of the place-holder in the new Image -- optional parameter
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term product(Compound<Term> image, Term component, int index) {
//        Term[] argument = image.termsCopy();
//        argument[index] = component;
//        return $.p(argument);
//    }

    static void productAppend(Compound product, Appendable p, boolean pretty) throws IOException {

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

//    /**
//     * recursively flatten a embedded conjunction subterms if they are of a specific order
//     */
//    static Term[] flatten(Term[] args, Order order) {
//        //determine how many there are with same order
//
//        int expandedSize;
//        while ((expandedSize = getFlattenedLength(args, order)) != args.length) {
//            args = _flatten(args, order, expandedSize);
//        }
//        return args;
//    }

    static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, Budget budget) {
        return spawn(parent, content, punctuation, truth, occ, budget.getPriority(), budget.getDurability(), budget.getQuality());
    }

    static Task spawn(Task parent, Compound content, char punctuation, Truth truth, long occ, float p, float d, float q) {
        return new MutableTask(content, punctuation)
                .truth(truth)
                .budget(p, d, q)
                .parent(parent)
                .occurr(occ);
    }

    //    private static Term junction(Op o, Collection<Term> t) {
//        return junction(o, Terms.toArray(t));
//    }


    default Term junction(Op op, Term[] t) {
        if (t.length == 0) return null;


        boolean done = true;

        //TODO use a more efficient flattening that doesnt involve recursion and multiple array creations
        TreeSet<Term> s = new TreeSet();
        for (Term x : t) {
            if (x.op(op)) {
                for (Term y : ((Compound)x).terms()) {
                    s.add(y);
                    if (y.op(op))
                        done = false;
                }
            } else {
                s.add(x);
            }
        }

        Term[] sa = s.toArray(new Term[s.size()]);

        if (!done) {
            return junction(op, sa);
        }

        if (sa.length == 1) return sa[0];

        return term(op, -1, false, sa   /* already sorted via TreeSet */);

    }

    default Term statement(Op op, Term[] t) {

        switch (t.length) {
            case 1:
                return t[0];
            case 2:

                Term subject = t[0];
                Term predicate = t[1];

                if (subject == null || predicate == null)
                    return null;

                //special statement filters
                switch (op) {
                    case EQUIV:
                    case EQUIV_AFTER:
                    case EQUIV_WHEN:
                        if (!validEquivalenceTerm(subject)) return null;
                        if (!validEquivalenceTerm(predicate)) return null;
                        break;

                    case IMPLICATION:
                    case IMPLICATION_AFTER:
                    case IMPLICATION_BEFORE:
                    case IMPLICATION_WHEN:
                        if (subject.isAny(InvalidEquivalenceTerm)) return null;
                        if (predicate.isAny(InvalidImplicationPredicate)) return null;

                        if (predicate.isAny(Op.ImplicationsBits)) {
                            Term oldCondition = subj(predicate);
                            if ((oldCondition.isAny(Op.ConjunctivesBits)) && oldCondition.containsTerm(subject))
                                return null;

                            return impl2Conj(op, subject, predicate, oldCondition);
                        }
                        break;
                    default:
                        break;
                }

                if (op.isCommutative())
                    t = Terms.toSortedSetArray(t);

                if (t.length == 1) return t[0]; //reduced to one

                if (!Statement.invalidStatement(t[0], t[1]))
                    return term(op, -1, false, t); //already sorted

                return null;
            default:
                throw new RuntimeException("invalid statement arguments");
        }
    }

    default Term subtractSet(Op setType, Compound A, Compound B) {
        if (A.equals(B))
            return null; //empty set
        return term(setType, TermContainer.difference(A, B));
    }

    static boolean validEquivalenceTerm(Term t) {
        return !t.isAny(InvalidEquivalenceTerm);
//        if ( instanceof Implication) || (subject instanceof Equivalence)
//                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
//                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
//            return null;
//        }
    }

    default Term impl2Conj(Op op, Term subject, Term predicate, Term oldCondition) {
        Op conjOp;
        switch (op) {
            case IMPLICATION:
                conjOp = CONJUNCTION;
                break;
            case IMPLICATION_AFTER:
                conjOp = SEQUENCE;
                break;
            case IMPLICATION_WHEN:
                conjOp = PARALLEL;
                break;
            case IMPLICATION_BEFORE:
                conjOp = SEQUENCE;
                //swap order
                Term x = oldCondition;
                oldCondition = subject;
                subject = x;
                break;
            default:
                throw new RuntimeException("invalid case");
        }

        return term(op,
                term(conjOp, subject, oldCondition),
                pred(predicate));
    }

    default Term term(Op op, int relation, boolean sort, Term... t) {
        if (t == null)
            return null;

        if (sort && op.isCommutative())
            t = Terms.toSortedSetArray(t);

        int arity = t.length;
        if (op.minSize > 1 && arity == 1) {
            return t[0]; //reduction
        }

        if (!op.validSize(arity)) {
            //throw new RuntimeException(Arrays.toString(t) + " invalid size for " + op);
            return null;
        }

        return make(op, relation, t).term();
    }

    Termed make(Op op, int relation, Term... t);


    default Term newIntersectINT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_INT,
                Op.SET_INT,
                Op.SET_EXT);
    }

    default Term newIntersectEXT(Term[] t) {
        return newIntersection(t,
                Op.INTERSECT_EXT,
                Op.SET_EXT,
                Op.SET_INT);
    }

    default Term newIntersection(Term[] t, Op intersection, Op setUnion, Op setIntersection) {
        switch (t.length) {
            case 1:
                return t[0];
            case 2:
                return newIntersection2(t[0], t[1], intersection, setUnion, setIntersection);
            default:
                //HACK use more efficient way
                return newIntersection2(
                    newIntersection2(t[0], t[1], intersection, setUnion, setIntersection),
                    newIntersection(copyOfRange(t, 2, t.length), intersection, setUnion, setIntersection),
                    intersection, setUnion, setIntersection
                );
        }

        //return newCompound(intersection, t, -1, true);
    }

    @Deprecated default Term newIntersection2(Term term1, Term term2, Op intersection, Op setUnion, Op setIntersection) {

        Op o1 = term1.op();
        Op o2 = term2.op();

        if ((o1 == setUnion) && (o2 == setUnion)) {
            //the set type that is united
            return term(setUnion, TermContainer.union((Compound) term1, (Compound) term2));
        }


        if ((o1 == setIntersection) && (o2 == setIntersection)) {
            //the set type which is intersected
            return intersect(setIntersection, (Compound) term1, (Compound) term2);
        }

        if (o2 == intersection && o1!=intersection) {
            //put them in the right order so everything fits in the switch:
            Term x = term1;
            term1 = term2;
            term2 = x;
            o2 = o1;
            o1 = intersection;
        }

        //reduction between one or both of the intersection type

        if (o1 == intersection) {
            Term[] suffix;

            if (o2 == intersection) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                suffix = ((Compound) term2).terms();

            } else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                suffix = new Term[]{term2};
            }

            return term(intersection, -1, true, concat(
                    ((Compound) term1).terms(), suffix, Term.class
            ));

        }

        if (term1.equals(term2))
            return term1;

        return term(intersection, -1, true, term1, term2);


    }

    default Term intersect(Op resultOp, Compound a, Compound b) {
        MutableSet<Term> i = TermContainer.intersect(a, b);
        if (i.isEmpty()) return null;
        return term(resultOp, i);
    }

    default Term difference(Compound a, Compound b) {

        if (a.size() == 1) {

            if (b.containsTerm(a.term(0))) {
                return null;
            } else {
                return a;
            }

        } else {
            //MutableSet dd = Sets.difference(a.toSet(), b.toSet());
            MutableSet dd = a.toSet();
            boolean changed = false;
            for (Term bb : b.terms()) {
                changed |= dd.remove(bb);
            }

            if (!changed) {
                return a;
            }
            else if (dd.isEmpty()) {
                return null;
            }
            else {
                Term[] i = Terms.toArray(dd);
                if (i == null) return null;
                return term(a.op(), i);
            }
        }

    }


//    static Term[] _flatten(Term[] args, Order order, int expandedSize) {
//        Term[] ret = new Term[expandedSize];
//        int k = 0;
//        for (Term a : args) {
//            if (a.op().isConjunctive(order)) {
//                //arraycopy?
//                for (Term t : ((Compound) a).terms()) {
//                    ret[k++] = t;
//                }
//            } else {
//                ret[k++] = a;
//            }
//        }
//
//        return ret;
//    }
//
//    static int getFlattenedLength(Term[] args, Order order) {
//        int sz = 0;
//        for (Term a : args) {
//            if (a.op().isConjunctive(order))
//                sz += a.size();
//            else
//                sz++;
//        }
//        return sz;
//    }

    /** main compound construction entry-point */
    default  Term term(Op op, Collection<Term> t) {
        return term(op, Terms.toArray(t));
    }

    /** main compound construction entry-point */
    default Term term(Op op, Term... t) {
        return term(op, -1, t);
    }

    default Term term(Op op, int relation, Term... t) {

        if (t == null)
            return null;

        /* special handling */
        switch (op) {
            case NEGATE:
                if (t.length!=1)
                    throw new RuntimeException("invalid negation subterms: " + Arrays.toString(t));
                return negation(t[0]);
            case SEQUENCE:
                return Sequence.makeSequence(t);
            case PARALLEL:
                return Parallel.makeParallel(t);
            case INSTANCE:
                return $.inst(t[0], t[1]);
            case PROPERTY:
                return $.property(t[0], t[1]);
            case INSTANCE_PROPERTY:
                return $.instprop(t[0], t[1]);
            case CONJUNCTION:
                return junction(CONJUNCTION, t);
            case DISJUNCTION:
                return junction(DISJUNCTION, t);
            case IMAGE_INT:
            case IMAGE_EXT:
                //if no relation was specified and it's an Image,
                //it must contain a _ placeholder
                if (hasImdex(t)) {
                    //TODO use result of hasImdex in image construction to avoid repeat iteration to find it
                    return image(op, t);
                }
                if ((relation == -1) || (relation > t.length))
                    return null;
                    //throw new RuntimeException("invalid index relation: " + relation + " for args " + Arrays.toString(t));

                break;
            case DIFF_EXT:
                Term et0 = t[0], et1 = t[1];
                if ((et0.op(SET_EXT) && et1.op(SET_EXT) )) {
                    return subtractSet(Op.SET_EXT, (Compound)et0, (Compound)et1);
                }
                if (et0.equals(et1))
                    return null;
                break;
            case DIFF_INT:
                Term it0 = t[0], it1 = t[1];
                if ((it0.op(SET_INT) && it1.op(SET_INT) )) {
                    return subtractSet(Op.SET_INT, (Compound)it0, (Compound)it1);
                }
                if (it0.equals(it1))
                    return null;
                break;
            case INTERSECT_EXT: return newIntersectEXT(t);
            case INTERSECT_INT: return newIntersectINT(t);
        }


        if (op.isStatement()) {
            //test for valid statement
            return statement(op, t);
        } else {
            return term(op, relation, op.isCommutative(), t);
        }

    }


//    /**
//     * Try to make a new ImageInt/ImageExt.
//     * @return the Term generated from the arguments
//     * @param argList The list of term
//     */
//    static Term makeImage(Term[] argList, ObjectIntToObjectFunction<Term[], Term> build) {
//        int l = argList.length;
//        if (l < 2) {
//            return argList[0];
//        }
//
//        //Term relation = argList[0];
//
//        Term[] argument = new Term[l];
//        int index = 0, n = 0;
//        for (int j = 0; j < l; j++) {
//            //        if (t instanceof Compound) return false;
////        byte[] n = t.bytes();
////        if (n.length != 1) return false;
//            if (argList[j].equals(Op.Index)) {
//                index = j;
//                if (n == l-1)
//                    break;
//            } else {
//                argument[n++] =  argList[j];
//            }
//        }
//        if (n == l - 1) {
//            argument = Arrays.copyOf(argument, n);
//        } else if (n == l) {
//            index = l;
//        }
//
//        return build.valueOf(argument, index);
//    }


//   /**
//     * default method to make the oldName of an image term from given fields
//     *
//     * @param op the term operate
//     * @param arg the list of term
//     * @param relationIndex the location of the place holder
//     * @return the oldName of the term
//     */
//    protected static CharSequence makeImageName(final Op op, final Term[] arg, final int relationIndex) {
//        throw new RuntimeException("should not be used, utf8 instead");
////        final int sizeEstimate = 24 * arg.length + 2;
////
////        StringBuilder name = new StringBuilder(sizeEstimate)
////            .append(COMPOUND_TERM_OPENER.ch)
////            .append(op)
////            .append(Symbols.ARGUMENT_SEPARATOR)
////            .append(arg[relationIndex].toString());
////
////        for (int i = 0; i < arg.length; i++) {
////            name.append(Symbols.ARGUMENT_SEPARATOR);
////            if (i == relationIndex) {
////                name.append(Symbols.IMAGE_PLACE_HOLDER);
////            } else {
////                name.append(arg[i].toString());
////            }
////        }
////        name.append(COMPOUND_TERM_CLOSER.ch);
////        return name.toString();
//    }


//    /**
//     * Get the other term in the Image
//     *
//     * @return The term related
//     */
//    public Term getTheOtherComponent() {
//        if (term.length != 2) {
//            return null;
//        }
//        Term r = (relationIndex == 0) ? term[1] : term[0];
//        return r;
//    }


    //    @Override
//    public byte[] bytes() {
//
//        final int len = this.size();
//
//        //calculate total size
//        int bytes = 2 + 2 + 2;
//        for (int i = 0; i < len; i++) {
//            Term tt = this.term(i);
//            bytes += tt.bytes().length;
//            if (i != 0) bytes++; //comma
//        }
//
//        ByteBuf b = ByteBuf.create(bytes)
//                .add((byte) COMPOUND_TERM_OPENER)
//                .add(this.op().bytes)
//                .add((byte) ARGUMENT_SEPARATOR)
//                .add(this.relation().bytes());
//
//
//        final int relationIndex = this.relationIndex;
//        for (int i = 0; i < len; i++) {
//            Term tt = this.term(i);
//            b.add((byte) ARGUMENT_SEPARATOR);
//            if (i == relationIndex) {
//                b.add((byte) Symbols.IMAGE_PLACE_HOLDER);
//            } else {
//                b.add(tt.bytes());
//            }
//        }
//        b.add((byte) COMPOUND_TERM_CLOSER);
//
//        return b.toBytes();
//
//    }

//    /**
//     * constructor with partial values, called by make
//     * @param arg The component list of the term
//     * @param index The index of relation in the component list
//     */
//    public ImageInt(Term[] arg, int index) {
//        super(arg, index);
//    }
//
//
//    /**
//     * Clone an object
//     * @return A new object, to be casted into an ImageInt
//     */
//    @Override
//    public ImageInt clone() {
//        return new ImageInt(terms.term, relationIndex);
//    }
//
//    @Override
//    public Term clone(Term[] replaced) {
//        if ((replaced.length != size())
//                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
//            return Image.makeInt(replaced);
//
////        if (replaced.length != size())
////            //return null;
////            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));
//
//
//        return new ImageInt(replaced, relationIndex);
//    }
//
//    /**
//     * Try to make an Image from a Product and a relation. Called by the logic rules.
//     * @param product The product
//     * @param relation The relation
//     * @param index The index of the place-holder
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term make(Product product, Term relation, short index) {
//        if (relation instanceof Product) {
//            Product p2 = (Product) relation;
//            if ((product.size() == 2) && (p2.size() == 2)) {
//                if ((index == 0) && product.term(1).equals(p2.term(1))) {// (\,_,(*,a,b),b) is reduced to a
//                    return p2.term(0);
//                }
//                if ((index == 1) && product.term(0).equals(p2.term(0))) {// (\,(*,a,b),a,_) is reduced to b
//                    return p2.term(1);
//                }
//            }
//        }
//
//        Term[] argument = product.termsCopy(); //shallow clone necessary because the index argument is replaced
//        argument[index] = relation;
//        return make(argument, index);
//    }
//
//    /**
//     * Try to make an Image from an existing Image and a component. Called by the logic rules.
//     * @param oldImage The existing Image
//     * @param component The component to be added into the component list
//     * @param index The index of the place-holder in the new Image
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term make(ImageInt oldImage, Term component, short index) {
//        Term[] argList = oldImage.termsCopy();
//        int oldIndex = oldImage.relationIndex;
//        Term relation = argList[oldIndex];
//        argList[oldIndex] = component;
//        argList[index] = relation;
//        return make(argList, index);
//    }
//
//    /**
//     * Try to make a new compound from a set of term. Called by the public make methods.
//     * @param argument The argument list
//     * @param index The index of the place-holder in the new Image
//     * @return the Term generated from the arguments
//     */
//    public static ImageInt make(Term[] argument, int index) {
//        return new ImageInt(argument, index);
//    }
//
//
//    /**
//     * Get the operate of the term.
//     * @return the operate of the term
//     */
//    @Override
//    public Op op() {
//        return Op.IMAGE_INT;
//    }
//    /**
//     * Clone an object
//     * @return A new object, to be casted into an ImageExt
//     */
//    @Override
//    public ImageExt clone() {
//        return new ImageExt(terms.term, relationIndex);
//    }
//    @Override
//    public Term clone(Term[] replaced) {
//        if ((replaced.length != size())
//                || Image.hasPlaceHolder(replaced)) //TODO indexOfPlaceHolder
//            return Image.makeExt(replaced);
//
////        if (replaced.length != size())
////            //return null;
////            throw new RuntimeException("Replaced terms not the same amount as existing terms (" + terms().length + "): " + Arrays.toString(replaced));
//
//        return new ImageExt(replaced, relationIndex);
//    }
//
//
//
//    /**
//     * Try to make an Image from an existing Image and a component. Called by the logic rules.
//     * @param oldImage The existing Image
//     * @param component The component to be added into the component list
//     * @param index The index of the place-holder in the new Image
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term make(ImageExt oldImage, Term component, short index) {
//        Term[] argList = oldImage.termsCopy();
//        int oldIndex = oldImage.relationIndex;
//        Term relation = argList[oldIndex];
//        argList[oldIndex] = component;
//        argList[index] = relation;
//        return new ImageExt(argList, index);
//    }
//
//
//
//    /**
//     * get the operate of the term.
//     * @return the operate of the term
//     */
//    @Override
//    public final Op op() {
//        return Op.IMAGE_EXT;
//    }

}
