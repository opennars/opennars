package nars;

import nars.bag.impl.MapCacheBag;
import nars.term.Term;
import nars.term.TermMetadata;
import nars.term.Termed;
import nars.term.compile.TermIndex;
import nars.term.compound.Compound;
import nars.util.data.map.UnifriedMap;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by me on 12/7/15.
 */
public final class MapTermIndex extends MapCacheBag<Term, Termed, Map<Term, Termed>> implements TermIndex {

    public MapTermIndex() {
        super(new UnifriedMap(1024));
    }


    @Override
    public final boolean test(Term term) {
        return true;
    }

    @Override
    public final Term apply(Compound c, Term subterm, int depth) {
        return get(subterm).getTerm();
    }

    public MapTermIndex(Map<Term, Termed> data) {
        super(data);
    }
    //new ConcurrentHashMap(4096); //TODO try weakref identity hash map etc

    @Override
    public final Termed get(Term t) {

        if (t instanceof TermMetadata) {
            return t.normalized(this); //term instance will remain unique because it has attached metadata
        }

        return data.compute(t, (k, vExist) -> {
            if (vExist == null) return k.normalized(this);
            else
                return vExist;
        });
        //return terms.computeIfAbsent(t, n -> n.normalized(this));
    }



    @Override
    public final void forEachTerm(Consumer<Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
