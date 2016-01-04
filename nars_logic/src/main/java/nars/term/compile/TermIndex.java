package nars.term.compile;

import javassist.scopedpool.SoftValueHashMap;
import nars.Op;
import nars.budget.Budget;
import nars.index.MapIndex;
import nars.index.MapIndex2;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.*;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.truth.Truth;

import java.io.PrintStream;
import java.util.HashMap;
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


    /** gets an existing item or applies the builder to produce something to return */
    default <K extends Term> Termed<K> apply(K key, Function<K,Termed> builder) {
        Termed existing = getTermIfPresent(key);
        if (existing == null) {
            putTerm(existing = builder.apply(key));
        }
        return existing;
    }

    TermContainer internSub(TermContainer s);



    void putTerm(Termed termed);

    default TermContainer unifySubterms(TermContainer s) {
        TermVector t = (TermVector)s;
        Term[] x = t.terms();
        for (int i = 0; i < x.length; i++) {
            x[i] = theTerm(x[i]); //since they are equal this will not need re-hashed
        }
        return s;
    }


    default Termed makeAtomic(Term t) {
        return t; /* as-is */
    }

    default Termed makeTerm(Term t) {
        return t instanceof Compound ?
                makeCompound((Compound) t)
                : makeAtomic(t);
    }


    int subtermsCount();

//    default TermContainer internSubterms(Term[] t) {
//        return new TermVector<>(t, this::the);
//    }


    default Termed makeCompound(Compound t) {
        return make(t.op(), t.relation(), t.subterms());
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
//        return new MapIndex2(
//                new HashMap(capacity)
//                //new UnifriedMap()
//        );
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
