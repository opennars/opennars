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
        return (TermMetadata.hasMetadata(t) || op.isA(TermMetadata.metadataBits)) ?
                newMetadataCompound(op, relation, t) :
                newInternCompound(op, t, relation);
    }

    public static Term newMetadataCompound(Op op, int relation, TermContainer t) {
        //create unique
        return $.the(op, relation, t);
    }

    protected static Termed newInternCompound(Op op, TermContainer subterms, int relation) {
        return new GenericCompound(
            op, relation, (TermVector) subterms
        );
    }

    @Override
    public Termed the(Term x) {

        if (!isInternable(x)) {
            //TODO intern any subterms which can be
            return x;
        }

        Termed y = getTermIfPresent(x);
        if (y == null) {
            putTerm(y = intern(x));
        }
        return y;
    }

//    @Override
//    public abstract Termed getTermIfPresent(Termed t);

//    @Override
//    public abstract void clear();

//    @Override
//    public abstract int subtermsCount();

//    @Override
//    public abstract int size();

    @Override
    public Termed internCompound(Op op, int relation, TermContainer t) {
        return intern(op, relation, internSub(t));
    }

    @Override public Termed internAtomic(Term t) {
        return t;
    }

    @Override public TermContainer internSub(TermContainer s) {
        TermContainer existing = getSubtermsIfPresent(s);
        if (existing == null) {
            putSubterms(unifySubterms(s));
            return s;
        }
        return existing;
    }


    abstract protected void putSubterms(TermContainer subterms);
    abstract protected TermContainer getSubtermsIfPresent(TermContainer subterms);


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
