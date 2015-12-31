package nars.index;

import nars.$;
import nars.Op;
import nars.term.*;
import nars.term.compile.TermIndex;
import nars.term.compound.GenericCompound;

import java.util.function.Consumer;

/**
 * Created by me on 12/31/15.
 */
public abstract class AbstractMapIndex implements TermIndex {
    public AbstractMapIndex() {
        super();
    }

    public static boolean isInternable(Term t) {
        return !TermMetadata.hasMetadata(t);
    }

    /** get the instance that will be internalized */
    public static Termed intern(Op op, int relation, TermContainer t) {
        if ((TermMetadata.hasMetadata(t))) {

            //intermval metadata, handle special
            return $.the(op, relation, t);
        } else {
            //TODO find existing instance and don't construct a duplciate which will get unified on re-entry
            return newInternCompound(op, t, relation);
        }
    }

    protected static Termed newInternCompound(Op op, TermContainer subterms, int relation) {
        return new GenericCompound(
                op, relation, (TermVector) subterms
        );
    }




    @Override
    public final Termed get(Object t) {
        if (!(t instanceof Termed)) {
            throw new RuntimeException("invalid key");
        }

        Termed xx = (Termed) t;
        Term x = xx.term();

        if (!isInternable(x)) {
            return xx;
        }

        return getIfAbsentIntern(x);

        //requires concurrent:
        //return data.computeIfAbsent(tt.term(), this::intern);

    }

    public abstract Termed getIfAbsentIntern(Term x);

    @Override
    public abstract Termed getTermIfPresent(Termed t);

    @Override
    public abstract void clear();

    @Override
    public abstract int subtermsCount();

    @Override
    public abstract Object remove(Term key);


    @Override
    public abstract int size();

    public Termed internCompound(Op op, int relation, TermContainer t) {
        return intern(op, relation, getIfAbsentIntern(t));
    }

    @Override public Termed internAtomic(Term t) {
        return t;
    }

    abstract public TermContainer getIfAbsentIntern(TermContainer s);


//    @Override
//    public void print(PrintStream out) {
//        BiConsumer itemPrinter = (k, v) -> System.out.println(v.getClass().getSimpleName() + ": " + v);
//        forEach(d -> itemPrinter);
//        System.out.println("--");
//        subterms.forEach(itemPrinter);
//    }

    @Override
    public abstract void forEach(Consumer<? super Termed> c);
}
