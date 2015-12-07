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

        Map<Term, Termed> d = this.data;
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
        T compiled;
        if (t instanceof TermMetadata) {

            //the term instance will remain unique
            // as determined by TermData's index method
            // however we can potentially index its subterms
            compileSubterms((TermVector) ((Compound)t).subterms());
            return t;
        }
        else if (t instanceof Compound) {
            compiled = (T) compileCompound((Compound)t);
        } else {
            compiled = t; //nothing to change
        }

        data.put(t, compiled);
        return compiled;
    }

    public void print(PrintStream out) {
        BiConsumer itemPrinter = (k, v) -> {
            System.out.println(v.getClass().getSimpleName() + ": " + v);
        };
        data.forEach(itemPrinter);
        System.out.println("--");
        subterms.forEach(itemPrinter);
    }

    protected <T extends Term> Compound<T> compileCompound(Compound<T> c) {
        TermContainer subs = c.subterms();
        Map<TermContainer, TermContainer> st = this.subterms;
        TermContainer existing = st.get(subs);
        if (existing == null) {
            subs = compileSubterms((TermVector) subs);
            st.put(subs, subs);
        }

        if (existing == subs) {
            // already using the shared subterms
            return c;
        } else {
            // different instance, so clone new compound
            return (Compound<T>) c.clone(subs);
        }
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
