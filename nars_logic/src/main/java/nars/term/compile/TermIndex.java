package nars.term.compile;

import nars.Global;
import nars.Op;
import nars.budget.Budget;
import nars.index.MapIndex;
import nars.nal.PremiseAware;
import nars.nal.PremiseMatch;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.*;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.match.EllipsisMatch;
import nars.term.transform.Subst;
import nars.term.variable.Variable;
import nars.truth.Truth;
import nars.util.WeakValueHashMap;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static nars.Op.*;
import static nars.util.data.Util.hashCombine;

/**
 *
 */
public interface TermIndex extends TermBuilder {



    /** universal zero-length product */
    Compound Empty = new GenericCompound(Op.PRODUCT, -1, TermVector.Empty);
    TermVector EmptyList = new TermVector();
    TermSet EmptySet = TermSet.the();

    void clear();

    void forEach(Consumer<? super Termed> c);

    //Termed get(Object t);

    Termed getTermIfPresent(Termed t);

    /** # of contained terms */
    int size();

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

    /** universal compound hash function */
    static <T extends Term> int hash(TermVector subterms, Op op, int hashSalt) {
        int h = hashCombine( subterms.hashCode(), op.ordinal() );
        if (hashSalt!=0)
            h = hashCombine(h, hashSalt);
        return h;
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


    /** gets an existing item or applies the builder to produce something to return */
    default <K extends Term> Termed<K> apply(K key, Function<K,Termed> builder) {
        Termed existing = getTermIfPresent(key);
        if (existing == null) {
            putTerm(existing = builder.apply(key));
        }
        return existing;
    }

    TermContainer getIfAbsentIntern(TermContainer s);



    void putTerm(Termed termed);



    default Termed internAtomic(Term t) {
        return t; /* as-is */
    }

    default Termed intern(Term t) {
        return t instanceof Compound ?
                internCompound((Compound) t)
                : internAtomic(t);
    }

    /** returns the resolved term according to the substitution    */
    default Term transform(Compound src, Subst f, boolean fullMatch) {

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

        Term result = newTerm(src, new TermVector(sub));

        //apply any known immediate transform operators
        //TODO decide if this is evaluated incorrectly somehow in reverse
        if (Op.isOperation(result)) {
            ImmediateTermTransform tf = f.getTransform(Operator.operatorTerm((Compound)result));
            if (tf!=null) {
                return applyImmediateTransform(f, result, tf);
            }
        }

        return result;
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
            return transform((Compound)src, f, false);
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

    default TermContainer internSubterms(Term[] t) {
        return new TermVector<Term>(t, this::the);
    }

    default Termed internCompound(Compound t) {
        return internCompound(t.op(), t.relation(), t.subterms());
    }









//    class ImmediateTermIndex implements TermIndex {
//
//        @Override
//        public Termed get(Object key) {
//            return (Termed)key;
//        }
//
//        @Override
//        public TermContainer getIfAbsentIntern(TermContainer s) {
//            return s;
//        }
//
//        @Override
//        public Termed internAtomic(Term t) {
//            return t;
//        }
//
//        @Override
//        public void forEach(Consumer<? super Termed> c) {
//
//        }
//
//        @Override
//        public Termed getTermIfPresent(Termed t) {
//            return t;
//        }
//
//        @Override
//        public Termed intern(Term tt) {
//            return tt;
//        }
//
//        @Override
//        public int subtermsCount() {
//            return 0;
//        }
//
//
//        @Override
//        public void clear() {
//
//        }
//
//
//        @Override
//        public void putTerm(Termed termed) {
//
//        }
//
//        @Override
//        public int size() {
//            return 0;
//        }
//
//
//    }

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

//    static GuavaIndex memoryGuava(Clock c, int expirationCycles) {
//        return new GuavaIndex(c, expirationCycles);
////        return new MapIndex(
////
////                new WeakValueHashMap(capacity),
////                new WeakValueHashMap(capacity*2)
////        );
//    }

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
