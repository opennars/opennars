package nars;

import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.term.compound.GenericCompound;

import java.io.PrintStream;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by me on 12/7/15.
 */
public class MapIndex implements TermIndex {

    public final Map<Term, Termed> data;
    public final Map<TermContainer, TermContainer> subterms;


    public MapIndex() {
        this(Global.newHashMap(), Global.newHashMap());
    }

    public MapIndex(Map<Term, Termed> data, Map<TermContainer, TermContainer> subterms) {
        super();
        this.data = data;
        this.subterms = subterms;
    }


    @Override
    public final Termed _get(Termed t) {
        return data.get(t);
    }

    @Override
    public void clear() {
        data.clear();
        subterms.clear();
    }


    @Override
    public Object remove(Term key) {
        return data.remove(key);
    }

    @Override
    public Termed put(Term term, Termed termed) {
        return data.put(term, termed);
    }

    @Override
    public int size() {
        return data.size();
    }



    @Override public Termed intern(Term tt) {
        Term t = tt.term();

        if (t instanceof TermMetadata) {

            //the term instance will remain unique
            // as determined by TermData's index method
            // however we can potentially index its subterms
            return tt;
        }

        Termed interned = t instanceof Compound ?
                internCompound((Compound) t)
                : internAtomic(t);

        Termed existing = data.putIfAbsent(interned.term(), interned);
        if (existing!=null)
            throw new RuntimeException("displaced: " + existing + " with " + interned);

        return interned;
    }



    public Termed internCompound(Op op, int relation, TermContainer t) {
        if ((op == Op.SEQUENCE) || (op == Op.PARALLEL)) {
            //intermval metadata, handle special
            return $.the(op, relation, t);
        } else {
            //TODO find existing instance and don't construct a duplciate which will get unified on re-entry
            return internCompound(op, internSubterms(t), relation);
        }
    }

    public Termed internAtomic(Term t) {
        return t;
    }
    protected TermContainer internSubterms(TermContainer s) {
        Map<TermContainer, TermContainer> st = subterms;
        TermContainer existing = st.get(s);
        if (existing == null) {
            s = internSubterms(s.terms());
            st.put(s, s);
            return s;
        }
        return existing;
    }





    protected Termed internCompound(Op op, TermContainer subterms, int relation) {
        return new GenericCompound(
            (TermVector) subterms, op, relation
        );
    }


    @Override
    public void print(PrintStream out) {
        BiConsumer itemPrinter = (k, v) -> System.out.println(v.getClass().getSimpleName() + ": " + v);
        data.forEach(itemPrinter);
        System.out.println("--");
        subterms.forEach(itemPrinter);
    }

//    protected <T extends Term> Compound<T> compileCompound(Compound<T> c) {
//        return compileCompound(c, get(c.subterms()));
//    }



//    protected <T extends Term> Compound<T> compileCompound(Compound<T> c, TermContainer subs) {
////        if ((c instanceof GenericCompound) && (!(c instanceof TermMetadata))) {
////            //special case, fast clone
////            return new GenericCompound(c.op(), (TermVector)subs, ((GenericCompound) c).relation) {
////                @Override
////                public Term clone(Term[] replaced) {
////                    //TODO use a table with the following lookup
////                    //before needing to construct a term:
////                    // (op,relation), (subterm) --> compound
////                    //this way an existing op+relation+subterm
////                    //can be retrieved without constructing
////                    //the term that will eventually match with it
////
////                    return MapIndex.this.getTerm(
////                            compileCompound(this,
////                                get(new TermVector(replaced)))
////                           );
////                }
////            };
////        }
//        return (Compound<T>) c.clone(subs);
//    }



    @Override
    public final void forEach(Consumer<? super Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
