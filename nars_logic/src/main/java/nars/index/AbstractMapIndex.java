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
        return TermMetadata.hasMetadata(t) ?
                $.the(op, relation, t) :
                newInternCompound(op, t, relation);
    }

    protected static Termed newInternCompound(Op op, TermContainer subterms, int relation) {
        return new GenericCompound(
                op, relation, (TermVector) subterms
        );
    }

    @Override
    public Termed the(Term x) {

        if (!isInternable(x)) {
            return x;
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
    public abstract int size();

    @Override
    public Termed internCompound(Op op, int relation, TermContainer t) {
        return intern(op, relation, getIfAbsentIntern(t));
    }

    @Override public Termed internAtomic(Term t) {
        return t;
    }

    @Override
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
