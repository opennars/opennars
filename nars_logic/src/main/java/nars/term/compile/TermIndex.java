package nars.term.compile;

import javassist.scopedpool.SoftValueHashMap;
import nars.MapIndex;
import nars.Memory;
import nars.Op;
import nars.bag.impl.CacheBag;
import nars.concept.Concept;
import nars.nal.Compounds;
import nars.nal.nal7.CyclesInterval;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.transform.CompoundTransform;
import nars.term.variable.Variable;
import nars.util.WeakValueHashMap;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 *
 */
public interface TermIndex extends Compounds, CacheBag<Term, Termed> {


    void forEach(Consumer<? super Termed> c);



    default Term term(Object t) {
        Termed tt = get(t);
        if (tt == null) return null;
        return tt.term();
    }

    default Termed term(Compound c, CompoundTransform transform) {
        throw new RuntimeException("unimpl");
    }

    default Concept concept(Compound c, CompoundTransform transform) {
        return concept(term(c, transform));
    }

    static boolean validConceptTerm(Term term) {
        return !((term instanceof Variable) || (term instanceof CyclesInterval));
    }

    default Concept concept(Termed t) {
        if (t instanceof Concept) return ((Concept)t);

        if (!validConceptTerm(t.term())) return null;

        Termed u = get(t);
        if (u instanceof Concept) return ((Concept)u);
        return null;
    }


    class ImmediateTermIndex implements TermIndex {

        /** build a new instance on the heap */
        @Override public Termed make(Op op, int relation, Term... t) {
            return new UncachedGenericCompound(op, t, relation);
        }


        @Override
        public Termed get(Object key) {
            return (Termed)key;
        }


        @Override
        public void forEach(Consumer<? super Termed> c) {

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
        public int size() {
            return 0;
        }

        @Override
        public void start(Memory n) {
            throw new RuntimeException("should not be used by Memory");
        }


        private class UncachedGenericCompound extends GenericCompound {

            public UncachedGenericCompound(Op op, Term[] t, int relation) {
                super(op, relation, t);
            }

//            @Override
//            public Term clone(Term[] replaced) {
//                if (subterms().equals(replaced))
//                    return this;
//                return term(op(), relation, replaced);
//            }
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
    static TermIndex memorySoft(int capacity) {
        return new MapIndex(
                new SoftValueHashMap(capacity),
                new SoftValueHashMap(capacity*2)
        );
    }
    static TermIndex memoryWeak(int capacity) {
        return new MapIndex(
                new WeakValueHashMap(capacity),
                new WeakValueHashMap(capacity*2)
        );
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
