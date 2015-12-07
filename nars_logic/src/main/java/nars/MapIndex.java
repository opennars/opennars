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
public final class MapIndex extends MapCacheBag<Term, Termed, Map<Term, Termed>> implements TermIndex {

    public MapIndex() {
        super(new UnifriedMap(1024));
    }


    @Override
    public final boolean test(Term term) {
        return true;
    }

    @Override
    public final Term apply(Compound c, Term subterm, int depth) {
        return getTerm(subterm);
    }

    public MapIndex(Map<Term, Termed> data) {
        super(data);
    }
    //new ConcurrentHashMap(4096); //TODO try weakref identity hash map etc

    @Override
    public final Termed get(Term t) {

        Map<Term, Termed> d = this.data;
        Termed existing = d.get(t);
        if (existing ==null) {

            if (t instanceof TermMetadata) {
                //the term instance will remain unique
                // as determined by TermData's index method
                // however we can potentially index its subterms
                return compile(t);
            }

            d.put(t, existing = compile(t));
        }
        return existing;

//        return data.compute(t, (k, vExist) -> {
//            if (vExist == null) return k.index(this);
//            else
//                return vExist;
//        });

    }
    public Term getTerm(Term t) {
        Termed u = get(t);
        if (u == null)
            return null;
        return u.getTerm();
    }

    protected Termed compile(Term t) {
        return t;
    }


    @Override
    public final void forEachTerm(Consumer<Termed> c) {
        data.forEach((k, v) -> c.accept(v));
    }
}
