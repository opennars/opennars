package nars;

import nars.bag.impl.MapCacheBag;
import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by me on 12/7/15.
 */
public class MapIndex extends MapCacheBag<Term, Termed, Map<Term, Termed>> implements TermIndex {

    private final Map<TermContainer, TermContainer> subterms = new HashMap();


    public MapIndex(Map<Term, Termed> data) {
        super(data);
    }
    //new ConcurrentHashMap(4096); //TODO try weakref identity hash map etc



    @Override
    public final Termed get(Term t) {

        Map<Term, Termed> d = data;
        Termed existing = d.get(t);
        if (existing ==null) {
            return compile(t);
        }
        return existing;

//        return data.compute(t, (k, vExist) -> {
//            if (vExist == null) return k.index(this);
//            else
//                return vExist;
//        });

    }

    protected <T extends Term> T compile(T t) {

        if (t instanceof TermMetadata) {

            //the term instance will remain unique
            // as determined by TermData's index method
            // however we can potentially index its subterms
            return t;
        }

        T compiled = t instanceof Compound ? (T) compileCompound((Compound) t) : t;
        data.put(t, compiled);
        return compiled;
    }

    @Override
    public void print(PrintStream out) {
        BiConsumer itemPrinter = (k, v) -> System.out.println(v.getClass().getSimpleName() + ": " + v);
        data.forEach(itemPrinter);
        System.out.println("--");
        subterms.forEach(itemPrinter);
    }

    protected <T extends Term> Compound<T> compileCompound(Compound<T> c) {
        return compileCompound(c, get(c.subterms()));
    }

    private <T extends Term> TermContainer get(TermContainer<T> s) {
        Map<TermContainer, TermContainer> st = subterms;
        TermContainer existing = st.get(s);
        if (existing == null) {
            st.put(s, existing = compileSubterms((TermVector) s));
        }
        return existing;
    }

    protected <T extends Term> Compound<T> compileCompound(Compound<T> c, TermContainer subs) {
//        if ((c instanceof GenericCompound) && (!(c instanceof TermMetadata))) {
//            //special case, fast clone
//            return new GenericCompound(c.op(), (TermVector)subs, ((GenericCompound) c).relation) {
//                @Override
//                public Term clone(Term[] replaced) {
//                    //TODO use a table with the following lookup
//                    //before needing to construct a term:
//                    // (op,relation), (subterm) --> compound
//                    //this way an existing op+relation+subterm
//                    //can be retrieved without constructing
//                    //the term that will eventually match with it
//
//                    return MapIndex.this.getTerm(
//                            compileCompound(this,
//                                get(new TermVector(replaced)))
//                           );
//                }
//            };
//        }
        return (Compound<T>) c.clone(subs);
    }

    private TermContainer compileSubterms(TermVector subs) {
        Term[] ss = subs.term;
        int s = ss.length;
        //modifies in place, since the items will be equal
        for (int i = 0; i < s; i++) {
            ss[i] = getTerm(ss[i]);
        }
        return subs;
    }


    @Override
    public final void forEach(Consumer<? super Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
