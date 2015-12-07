package nars.term.compile;

import com.google.common.cache.CacheBuilder;
import nars.MapIndex;
import nars.bag.impl.CacheBag;
import nars.bag.impl.GuavaCacheBag;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.transform.CompoundTransform;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 *
 */
public interface TermIndex extends CacheBag<Term,Termed>, CompoundTransform<Compound, Term> {

    @Override
    Termed get(Term t);

    void forEachTerm(Consumer<Termed> c);


    class GuavaIndex extends GuavaCacheBag<Term,Termed> implements TermIndex {


        public GuavaIndex(CacheBuilder<Term, Termed> data) {
            super(data);
        }

        @Override
        public void forEachTerm(Consumer<Termed> c) {
            data.asMap().forEach((k,v) -> {
                c.accept(v);
            });
        }

        @Override
        public final boolean test(Term term) {
            return true;
        }

        @Override
        public final Term apply(Compound c, Term subterm, int depth) {
            Termed s = get(subterm);
            if (s == null)
                return null;
            return s.getTerm();
        }
    }

    /** default memory-based (Guava) cache */
    static TermIndex memory(int capacity) {
//        CacheBuilder builder = CacheBuilder.newBuilder()
//            .maximumSize(capacity);
        return new MapIndex(
            new HashMap(capacity)
            //new UnifriedMap()
        );
    }

    /** for long-running processes, this uses
     * a weak-value policy */
    static TermIndex memoryAdaptive(int capacity) {
        CacheBuilder builder = CacheBuilder.newBuilder()
            .maximumSize(capacity)
            .recordStats()
            .weakValues();
        return new GuavaIndex(builder);
    }
}
