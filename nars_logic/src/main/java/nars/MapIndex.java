package nars;

import nars.bag.impl.MapCacheBag;
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
public class MapIndex extends MapCacheBag<Term,Termed> implements TermIndex {

    public final Map<TermContainer, TermContainer> subterms;


    public MapIndex(Map<Term, Termed> data, Map<TermContainer, TermContainer> subterms) {
        super(data);
        this.subterms = subterms;
    }

    //new ConcurrentHashMap(4096); //TODO try weakref identity hash map etc


    @Override
    public Termed _get(Termed t) {
        return super.get(t);
    }

    @Override
    public Termed get(Object t) {
        if (t instanceof Termed) {
            Term tt = ((Termed)t).term();
            return get(tt, this::intern);
        } else {
            throw new RuntimeException("invalid key");
        }
    }



    private final class InternGenericCompound extends GenericCompound {

        public InternGenericCompound(Op op, TermVector subterms, int relation) {
            super(op, subterms, relation);
        }

        //equals ==
//
//        @Override
//        public Term clone(Term[] replaced) {
//            if (subterms().equals(replaced))
//                return this;
//            return term(op(), relation, replaced);
//        }
    }

    public Termed intern(Term tt) {
        Term t = tt.term();

        if (t instanceof TermMetadata) {

            //the term instance will remain unique
            // as determined by TermData's index method
            // however we can potentially index its subterms
            return tt;
        }

        Termed compiled = t instanceof Compound ?
                make((Compound) t)
                : t;

        Termed existing = data.put(compiled.term(), compiled);
        if (existing!=null)
            throw new RuntimeException("displaced: " + existing + " with " + compiled);

        return compiled;
    }


    @Override
    public Termed make(Op op, int relation, Term... t) {
        if ((op == Op.SEQUENCE) || (op == Op.PARALLEL)) {
            //intermval metadata, handle special
            return $.the(op, t, relation);
        } else {
            //TODO find existing instance and don't construct a duplciate which will get unified on re-entry
            return make(op, new TermVector(t), relation);
        }
    }

    protected Termed make(Compound t) {
        return make(t.op(), t.subterms(), t.relation());
    }

    protected Termed make(Op op, TermContainer subterms, int relation) {
        return new InternGenericCompound(
            op, (TermVector) get(subterms), relation
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

    private <T extends Term> TermContainer get(TermContainer<T> s) {
        Map<TermContainer, TermContainer> st = subterms;
        TermContainer existing = st.get(s);
        if (existing == null) {
            s = compileSubterms((TermVector) s);
            st.put(s, s);
            return s;
        }
        return existing;
    }

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


    protected TermContainer compileSubterms(TermVector subs) {
        return new TermVector(subs.terms(), this::term);
//        Term[] ss = subs.term;
//        int s = ss.length;
//        for (int i = 0; i < s; i++) {
//            ss[i] = getTerm(ss[i]);
//        }
//        return subs;
    }


    @Override
    public final void forEach(Consumer<? super Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
