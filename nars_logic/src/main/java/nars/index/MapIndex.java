package nars.index;

import nars.term.TermContainer;
import nars.term.Termed;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by me on 12/7/15.
 */
public class MapIndex extends AbstractMapIndex {

    public final Map<Termed, Termed> data;
    public final Map<TermContainer, TermContainer> subterms;


//    public MapIndex() {
//        this(Global.newHashMap(), Global.newHashMap());
//    }

    public MapIndex(Map<Termed, Termed> data, Map<TermContainer, TermContainer> subterms) {
        super();
        this.data = data;
        this.subterms = subterms;
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
        data.put(termed, termed);
    }

    @Override
    public int size() {
        return data.size();
    }


    @Override
    protected void putSubterms(TermContainer s) {
        this.subterms.put(s,s);
    }

    @Override protected TermContainer getSubtermsIfPresent(TermContainer subterms) {
        return this.subterms.get(subterms);
    }


    @Override
    public final void forEach(Consumer<? super Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
