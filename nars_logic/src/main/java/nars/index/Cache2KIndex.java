package nars.index;


import nars.term.Term;
import nars.term.TermContainer;
import nars.term.Termed;
import nars.term.compile.TermIndex;
import org.cache2k.Cache;

/** TermIndex implemented with Cache2K with
 * optional WeakRef policy.
 * suitable for running indefnitely and obeying AIKR
 * principles
 * TODO
 * */
public abstract class Cache2KIndex implements TermIndex {
    //http://cache2k.org/#Getting_started

    Cache<Term,Termed> data;
    Cache<TermContainer,TermContainer> subterms;

    public Cache2KIndex() {
//        CacheSource<Term,Termed> dataBuilder =
//                new CacheSource<Term,Termed>() {
//                    @Override
//                    public Termed get(Term o) throws Throwable {
//                        return o;
//                    }
//                };
//        Cache<Term,Termed> c =
//                CacheBuilder.newCache(Term.class, Termed.class)
//                        //.source()
//                        .eternal(true)
//                        .maxSize()
//                        .build();
    }
}
