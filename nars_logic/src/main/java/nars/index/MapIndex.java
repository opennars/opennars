package nars.index;

import nars.Global;
import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Termed;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by me on 12/7/15.
 */
public class MapIndex extends AbstractMapIndex {

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


    @Override @NotNull
    public Termed getIfAbsentIntern(Term x) {
        Termed y = getTermIfPresent(x);
        if (y == null) {
            y = intern(x);
            putTerm(y);
        }
        return y;
    }

    @Override
    public final Termed getTermIfPresent(Termed t) {
        return data.get(t);
    }

    @Override
    public void clear() {
        data.clear();
        subterms.clear();
    }

    @Override
    public int subtermsCount() {
        return subterms.size();
    }

//    @Override
//    public Object remove(Term key) {
//        return data.remove(key);
//    }

    @Override
    public void putTerm(Termed termed) {
        data.put(termed.term(), termed);
    }

    @Override
    public int size() {
        return data.size();
    }


    @Override public TermContainer getIfAbsentIntern(TermContainer s) {
        TermContainer existing = getSubtermsIfPresent(s);
        if (existing == null) {
            s = internSubterms(s.terms());
            putSubterms(s);
            return s;
        }
        return existing;
    }

    protected void putSubterms(TermContainer subterms) {
        this.subterms.put(subterms,subterms);
    }

    protected TermContainer getSubtermsIfPresent(TermContainer subterms) {
        return this.subterms.get(subterms);
    }


    @Override
    public final void forEach(Consumer<? super Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
