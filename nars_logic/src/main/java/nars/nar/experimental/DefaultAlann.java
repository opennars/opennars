package nars.nar.experimental;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.bag.impl.MapCacheBag;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Term;
import org.infinispan.commons.equivalence.AnyEquivalence;
import org.infinispan.util.concurrent.BoundedConcurrentHashMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Single-thread implementation of ALANN
 */
public class DefaultAlann extends AbstractAlann implements Supplier<Concept> {

    private Iterator<Concept> indexIterator = null;
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
        }

    }


    @Override
    public void init(NAR nar) {
        super.init(nar);
        defaultTTL = nar.memory.duration() * 3;
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

//    public abstract class RunProb implements Runnable {
//        private final float prob;
//
//        public RunProb(float initialProb) {
//            this.prob = initialProb;
//        }
//
//        public float getProb() { return prob; }
//    }

    public static <D> D runProbability(Random rng, float[] probs, D[] choices) {
        float tProb = 0;
        for (int i = 0; i < probs.length; i++) {
            tProb += probs[i];
        }
        float s = rng.nextFloat() * tProb;
        int c = 0;
        for (int i = 0; i < probs.length; i++) {
            s -= probs[i];
            if (s <= 0) { c = i; break; }
        }
        return choices[c];
    }

    final Supplier<Concept> fromInput = () -> {
        Task t = commander.commandIterator.next();
        return concept(t.getTerm());
    };
    final Supplier<Concept> fromNext = () -> {
        return indexIterator.next();
    };

    final float[] fromNextp1 = new float[] { 0.2f, 0.8f };
    final Supplier<Concept>[] fromNextpC = new Supplier[] { fromInput, fromNext };

    final void restart(final Derivelet d) {

        Supplier<Concept> source = runProbability(rng,
                fromNextp1,
                fromNextpC
        );
        Concept next = source.get();

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
