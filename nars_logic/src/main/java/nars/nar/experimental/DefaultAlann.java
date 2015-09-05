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
            derivers.add(new Derivelet());
        }
    }


    @Override
    public void init(NAR nar) {
        super.init(nar);
        this.context = new DeriveletContext(
                nar.memory, newRandom(), this
        ) {
            @Override
            public Concept concept(Term term) {
                return get(term);
            }
        };
    }

    @Override
    public Concept get() {
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

                    //recycle this derivelet
                    Concept next = indexIterator.next();
                    if (next != null) {
                        d.start(next, context);
                    } else {
                        continue;
                    }
                }


            }
        }

        //System.out.println("cycle " + memory.time());

    }



}
