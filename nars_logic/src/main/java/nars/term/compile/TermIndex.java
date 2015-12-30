package nars.term.compile;

import nars.Global;
import nars.Op;
import nars.bag.impl.CacheBag;
import nars.index.GuavaIndex;
import nars.index.MapIndex;
import nars.nal.Compounds;
import nars.nal.PremiseAware;
import nars.nal.PremiseMatch;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.TermVector;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;
import nars.term.match.Ellipsis;
import nars.term.match.EllipsisMatch;
import nars.term.transform.Subst;
import nars.term.variable.Variable;
import nars.time.Clock;
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

    Termed get(Object t);
    Termed getIfPresent(Termed t);

    /** gets an existing item or applies the builder to produce something to return */
    default <K extends Term> Termed<K> apply(K key, Function<K,Termed> builder) {
        Termed existing = getIfPresent(key);
        if (existing == null) {
            put(key, existing = builder.apply(key));
        }
        return existing;
    }

    TermContainer internSubterms(TermContainer s);

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


    class ImmediateTermIndex implements TermIndex {

        @Override
        public Termed get(Object key) {
            return (Termed)key;
        }

        @Override
        public TermContainer internSubterms(TermContainer s) {
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
        public Termed getIfPresent(Termed t) {
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
        public int size() {
            return 0;
        }


        @Override
        public Termed internCompound(Op op, int relation, TermContainer subterms) {
            return new GenericCompound((TermVector)subterms, op, relation);
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
