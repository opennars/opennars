package nars.term.compile;

import javassist.scopedpool.SoftValueHashMap;
import nars.Global;
import nars.MapIndex;
import nars.Memory;
import nars.Op;
import nars.bag.impl.CacheBag;
import nars.nal.Compounds;
import nars.nal.PremiseAware;
import nars.nal.RuleMatch;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.term.Term;
import nars.term.TermVector;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.EllipsisMatch;
import nars.term.transform.Subst;
import nars.term.variable.Variable;
import nars.util.WeakValueHashMap;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public interface TermIndex extends Compounds, CacheBag<Term, Termed> {


    void forEach(Consumer<? super Termed> c);

    Termed _get(Termed t);

    /** gets an existing item or applies the builder to produce something to return */
    default <K extends Term> Termed<K>  get(K key, Function<K, Termed> builder) {
        Termed existing = _get(key);
        return existing == null ?
                builder.apply(key) : existing;
    }


    default Term term(Object t) {
        Termed tt = get(t);
        if (tt == null) return null;
        return tt.term();
    }

    /** returns the resolved term according to the substitution    */
    default Term term(Compound src, Subst f, boolean fullMatch) {

        Term y = f.getXY(this);
        if (y!=null)
            return y;

        int len = src.size();
        List<Term> sub = Global.newArrayList(len /* estimate */);

        for (int i = 0; i < len; i++) {
            Term t = src.term(i);
            if (!get(t, f, sub)) {
                if (fullMatch)
                    return null;
            }
        }

        Term result = term(src, new TermVector(sub));

        //apply any known immediate transform operators
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

        return ((tf instanceof PremiseAware) && (f instanceof RuleMatch)) ?
                ((PremiseAware) tf).function(args, (RuleMatch) f) :
                tf.function(args, this);
    }


    default Term get(Subst f, Term src) {
        if (src instanceof Compound) {
            Term y = f.getXY(src);

            //attempt 1: apply known substitution
            //containsTerm prevents infinite recursion
            if ((y == null || y.containsTerm(src)))
                return null;

            return y;
        } else if (src instanceof Variable) {
            Term x = f.getXY(this);
            if (x != null)
                return x;
        }

        return src;

    }


    /** resolve the this term according to subst by appending to sub.
     * return false if this term fails the substitution */
    default boolean get(Term src, Subst f, Collection<Term> sub) {
        Term u = get(f, src);
        if (u == null) {
            u = src;
        }
        /*else
            changed |= (u!=this);*/

        if (u instanceof EllipsisMatch) {
            EllipsisMatch m = (EllipsisMatch)u;
            m.forEach(sub::add);
        } else {
            sub.add(u);
        }

        return true;
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
        public Termed _get(Termed t) {
            return t;
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
