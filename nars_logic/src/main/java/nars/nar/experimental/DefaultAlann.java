package nars.nar.experimental;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.bag.impl.MapCacheBag;
import nars.concept.Concept;
import nars.term.Term;
import org.infinispan.commons.equivalence.AnyEquivalence;
import org.infinispan.util.concurrent.BoundedConcurrentHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * Single-thread implementation of ALANN
 */
public class DefaultAlann extends AbstractAlann implements Supplier<Concept> {

    private final Iterator<Concept> indexIterator;
    private int defaultTTL;
    List<Derivelet> derivers = new ArrayList();
    private DeriveletContext context;

    final static int conceptLimit = 1024*1024;

    public DefaultAlann(int derivelets) {
        this(new MapCacheBag<>(

                new BoundedConcurrentHashMap(
                        /* capacity */ conceptLimit,
                        /* concurrency */ 1,
                        /* key equivalence */
                        AnyEquivalence.getInstance(Term.class),
                        AnyEquivalence.getInstance(Concept.class))


                //new LinkedHashMap<>(1024)
                //new ConcurrentHashMap(1024)
                //new UnifiedMap(1024)
        ), derivelets);
    }

    public DefaultAlann(MapCacheBag<Term, Concept> concepts, int numDerivelets) {
        super(concepts);

        indexIterator = Iterators.cycle(concepts);

        for (int i = 0; i < numDerivelets; i++) {
            Derivelet d = new Derivelet();
            derivers.add( d );
            //restart(d); //dont restart yet because there will be no concepts anyway
        }

    }


    @Override
    public void init(NAR nar) {
        super.init(nar);
        defaultTTL = nar.memory.duration() * 8;
        this.context = new MyDeriveletContext(nar);
    }

    @Override
    public final Concept get() {
        return indexIterator.next();
    }

    @Override
    protected void processConcepts() {

        if (concepts.size() > 0) {

            final long now = memory.time();

            final List<Derivelet> derivers = this.derivers;

            int numDerivers = derivers.size();
            for (int i = 0; i < numDerivers; i++) {

                final Derivelet d = derivers.get(i);

                //System.out.println(d + " cycle");

                if (!d.cycle(now)) {
                    restart(d); //recycle this derivelet
                }
            }
        }

        //System.out.println("cycle " + memory.time());

    }

    final void restart(final Derivelet d) {
        Concept next = indexIterator.next();
        if (next != null) {
            d.start(next, defaultTTL, context);
        }
    }


    private class MyDeriveletContext extends DeriveletContext {
        public MyDeriveletContext(NAR nar) {
            super(nar.memory, DefaultAlann.this.newRandom(), DefaultAlann.this);
        }

        @Override
        public final Concept concept(Term term) {
            return get(term);
        }
    }
}
