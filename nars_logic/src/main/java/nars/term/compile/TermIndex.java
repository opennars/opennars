package nars.term.compile;

import com.gs.collections.api.set.MutableSet;
import nars.$;
import nars.Global;
import nars.Op;
import nars.Symbols;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.index.GuavaIndex;
import nars.index.MapIndex;
import nars.nal.PremiseAware;
import nars.nal.PremiseMatch;
import nars.nal.nal7.Parallel;
import nars.nal.nal7.Sequence;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.*;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.match.EllipsisMatch;
import nars.term.transform.CompoundTransform;
import nars.term.transform.Subst;
import nars.term.transform.VariableNormalization;
import nars.term.variable.Variable;
import nars.time.Clock;
import nars.truth.Truth;
import nars.util.WeakValueHashMap;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.ObjectArrays.concat;
import static java.util.Arrays.copyOfRange;
import static nars.Op.*;
import static nars.Symbols.*;
import static nars.term.Statement.pred;
import static nars.term.Statement.subj;
import static nars.util.data.Util.hashCombine;

/**
 *
 */
public interface TermIndex extends CacheBag<Term, Termed> {


    /**
     * universal zero-length product
     */
    Compound Empty = new GenericCompound(Op.PRODUCT, -1, Terms.Empty);
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

    static boolean hasImdex(Term[] r) {
        for (Term x : r) {
            //        if (t instanceof Compound) return false;
//        byte[] n = t.bytes();
//        if (n.length != 1) return false;
            if (x.equals(Op.Imdex)) return true;
        }
        return false;
    }

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

    static boolean validEquivalenceTerm(Term t) {
        return !t.isAny(InvalidEquivalenceTerm);
//        if ( instanceof Implication) || (subject instanceof Equivalence)
//                || (predicate instanceof Implication) || (predicate instanceof Equivalence) ||
//                (subject instanceof CyclesInterval) || (predicate instanceof CyclesInterval)) {
//            return null;
//        }
    }

    void forEach(Consumer<? super Termed> c);

    @Override
    Termed get(Object t);
    Termed getTermIfPresent(Termed t);

    /** gets an existing item or applies the builder to produce something to return */
    default <K extends Term> Termed<K> apply(K key, Function<K,Termed> builder) {
        Termed existing = getTermIfPresent(key);
        if (existing == null) {
            putTerm(existing = builder.apply(key));
        }
        return existing;
    }

    TermContainer getIfAbsentIntern(TermContainer s);

    default Term term(Term src, TermContainer subs) {
        if (src instanceof Compound) {
            Compound csrc = (Compound)src;
            if (csrc.subterms().equals(subs))
                return src;
            return term(csrc, subs.terms());
        } else {
            return src;
        }
    }

    @Deprecated @Override
    default Termed put(Term term, Termed termed) {
        throw new RuntimeException("n/a");
    }

    void putTerm(Termed termed);


    default Term term(Object t) {
        Termed tt = get(t);
        if (tt == null) return null;
        return tt.term();
    }

    default Termed internAtomic(Term t) {
        return t; /* as-is */
    }

    default Termed intern(Term t) {
        return t instanceof Compound ?
                internCompound((Compound) t)
                : internAtomic(t);
    }

    /** returns the resolved term according to the substitution    */
    default Term apply(Compound src, Subst f, boolean fullMatch) {

        Term y = f.getXY(this);
        if (y!=null)
            return y;

        int len = src.size();

        List<Term> sub = Global.newArrayList(len /* estimate */);

        for (int i = 0; i < len; i++) {
            Term t = src.term(i);
            if (!apply(t, f, sub)) {
                if (fullMatch)
                    return null;
            }
        }

        if (sub.size() > 0) {
            //check if last item is a shim, if so, remove it
            if (sub.get(sub.size()-1).equals(Ellipsis.Shim))
                sub = sub.subList(0, sub.size()-1);
        }

        Term result = term(src, new TermVector(sub));

        //apply any known immediate transform operators
        //TODO decide if this is evaluated incorrectly somehow in reverse
        if (Op.isOperation(result)) {
            ImmediateTermTransform tf = f.getTransform(Operator.operatorTerm((Compound)result));
            if (tf!=null) {
                return applyImmediateTransform(f, result, tf);
            }
        }

        if (result == null) return null;

        return get(result).term();
    }



    default Term applyImmediateTransform(Subst f, Term result, ImmediateTermTransform tf) {

        //Compound args = (Compound) Operator.opArgs((Compound) result).apply(f);
        Compound args = Operator.opArgs((Compound) result);

        return ((tf instanceof PremiseAware) && (f instanceof PremiseMatch)) ?
                ((PremiseAware) tf).function(args, (PremiseMatch) f) :
                tf.function(args, this);
    }


    default Term apply(Subst f, Term src) {
        if (src instanceof Compound) {
            return apply((Compound)src, f, false);
        } else if (src instanceof Variable) {
            Term x = f.getXY(src);
            if (x != null)
                return x;
        }

        return src;

    }


    /** resolve the this term according to subst by appending to sub.
     * return false if this term fails the substitution */
    default boolean apply(Term src, Subst f, Collection<Term> sub) {
        Term u = apply(f, src);
        if (u == null) {
            u = src;
        }
        /*else
            changed |= (u!=this);*/

        if (u instanceof EllipsisMatch) {
            EllipsisMatch m = (EllipsisMatch)u;
            m.apply(sub);
        } else {
            sub.add(u);
        }

        return true;
    }

    int subtermsCount();

    default Term negation(Term t) {
        if (t.op() == Op.NEGATE) {
            // (--,(--,P)) = P
            return ((Compound) t).term(0);
        }
        return term(Op.NEGATE, -1, false, t);
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

        return internCompound(op, relation, internSubterms(t)).term();
    }

    default TermContainer internSubterms(Term[] t) {
        return new TermVector(t, this::term);
    }

    Termed internCompound(Op op, int relation, TermContainer subterms);

    default Termed internCompound(Compound t) {
        return internCompound(t.op(), t.relation(), t.subterms());
    }

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

        if (term1==null || term2 == null) {
            throw new NullPointerException();
        }
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

            suffix = o2 == intersection ? ((Compound) term2).terms() : new Term[]{term2};

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

            return b.containsTerm(a.term(0)) ? null : a;

        } else {
            //MutableSet dd = Sets.difference(a.toSet(), b.toSet());
            MutableSet dd = a.toSet();
            boolean changed = false;
            for (Term bb : b.terms()) {
                changed |= dd.remove(bb);
            }

            Term r;
            if (!changed) {
                r = a;
            }
            else if (dd.isEmpty()) {
                r = null;
            }
            else {
                Term[] i = Terms.toArray(dd);
                r = (i == null) ? null : term(a.op(), i);
            }
            return r;
        }

    }

    /** main compound construction entry-point */
    default  Term term(Op op, Collection<Term> t) {
        return term(op, Terms.toArray(t));
    }

    /** main compound construction entry-point */
    default Term term(Op op, Term... t) {
        return term(op, -1, t);
    }

    /** "clone"  */
    default Term term(Compound src, Term... replacingSubterms) {
        return term(src.op(), src.relation(), replacingSubterms);
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
                if (TermIndex.hasImdex(t)) {
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


        return op.isStatement() ? statement(op, t) : term(op, relation, op.isCommutative(), t);

    }

    /** returns how many subterms were modified, or -1 if failure (ex: results in invalid term) */
    default <T extends Term> int term(Compound src, CompoundTransform<Compound<T>, T> trans, Term[] target, int level) {
        int n = src.size();

        int modifications = 0;

        for (int i = 0; i < n; i++) {
            Term x = src.term(i);
            if (x == null)
                throw new RuntimeException("null subterm");

            if (trans.test(x)) {

                Term y = trans.apply( (Compound<T>)src, (T) x, level);
                if (y == null)
                    return -1;

                if (x!=y) {
                    modifications++;
                    x = y;
                }

            } else if (x instanceof Compound) {
                //recurse
                Compound cx = (Compound) x;
                if (trans.testSuperTerm(cx)) {

                    Term[] yy = new Term[cx.size()];
                    int submods = term(cx, trans, yy, level + 1);

                    if (submods == -1) return -1;
                    if (submods > 0) {
                        x = term(cx, yy);
                        if (x == null)
                            return -1;
                        modifications+= (cx!=x) ? 1 : 0;
                    }
                }
            }
            target[i] = x;
        }

        return modifications;
    }

    default <X extends Compound> X term(Compound src, CompoundTransform t) {
        return term(src, t, true);
    }

    default <X extends Compound> X term(Compound src, CompoundTransform t, boolean requireEqualityForNewInstance) {
        if (t.testSuperTerm(src)) {

            Term[] cls = new Term[src.size()];

            int mods = term(src, t, cls, 0);

            if (mods == -1) {
                return null;
            }
            else if (!requireEqualityForNewInstance || (mods > 0)) {
                return (X) term(src, cls);
            }
            //else if mods==0, fall through:
        }
        return (X) src; //nothing changed
    }

    default Termed normalized(Term t) {
        if (!(t instanceof Compound) || !t.hasVar()) {
            return t;
        }
        return term((Compound)t, VariableNormalization.normalizeFast((Compound)t));
    }


    class ImmediateTermIndex implements TermIndex {

        @Override
        public Termed get(Object key) {
            return (Termed)key;
        }

        @Override
        public TermContainer getIfAbsentIntern(TermContainer s) {
            return s;
        }

        @Override
        public Termed internAtomic(Term t) {
            return t;
        }

        @Override
        public void forEach(Consumer<? super Termed> c) {

        }

        @Override
        public Termed getTermIfPresent(Termed t) {
            return t;
        }

        @Override
        public Termed intern(Term tt) {
            return tt;
        }

        @Override
        public int subtermsCount() {
            return 0;
        }


        @Override
        public void clear() {

        }

        @Override
        public Object remove(Term key) {
            throw new RuntimeException("n/a");
        }

        @Override
        public Termed put(Term termed, Termed termed2) {
            throw new RuntimeException("n/a");
        }

        @Override
        public void putTerm(Termed termed) {

        }

        @Override
        public int size() {
            return 0;
        }


        @Override
        public Termed internCompound(Op op, int relation, TermContainer subterms) {
            return new GenericCompound(op, relation, (TermVector)subterms);
        }

    }

//    class GuavaIndex extends GuavaCacheBag<Term,Termed> implements TermIndex {
//
//
//        public GuavaIndex(CacheBuilder<Term, Termed> data) {
//            super(data);
//        }
//
//        @Override
//        public void forEachTerm(Consumer<Termed> c) {
//            data.asMap().forEach((k,v) -> {
//                c.accept(v);
//            });
//        }
//
//
//
//    }

    /** default memory-based (Guava) cache */
    static TermIndex memory(int capacity) {
//        CacheBuilder builder = CacheBuilder.newBuilder()
//            .maximumSize(capacity);
        return new MapIndex(
            new HashMap(capacity),new HashMap(capacity*2)
            //new UnifriedMap()
        );
    }
//    static TermIndex memorySoft(int capacity) {
//        return new MapIndex(
//                new SoftValueHashMap(capacity),
//                new SoftValueHashMap(capacity*2)
//        );
//    }
    static TermIndex memoryWeak(int capacity) {
        return new MapIndex(
            new WeakValueHashMap(capacity),
            new WeakValueHashMap(capacity*2)
        );
    }

    static TermIndex memoryGuava(Clock c, int expirationCycles) {
        return new GuavaIndex(c, expirationCycles);
//        return new MapIndex(
//
//                new WeakValueHashMap(capacity),
//                new WeakValueHashMap(capacity*2)
//        );
    }
    default void print(PrintStream out) {
        forEach(out::println);
    }

//    /** for long-running processes, this uses
//     * a weak-value policy */
//    static TermIndex memoryAdaptive(int capacity) {
//        CacheBuilder builder = CacheBuilder.newBuilder()
//            .maximumSize(capacity)
//            .recordStats()
//            .weakValues();
//        return new GuavaIndex(builder);
//    }
}
