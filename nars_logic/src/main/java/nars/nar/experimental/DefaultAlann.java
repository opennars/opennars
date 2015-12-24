package nars.nar.experimental;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Single-thread implementation of ALANN
 */
public class DefaultAlann extends AbstractAlann  {

    //private Iterator<Concept> indexIterator = null;

    private int defaultTTL;
    List<Derivelet> derivers = Global.newArrayList();
    private DeriveletContext context;

    private final Logger log = LoggerFactory.getLogger(DefaultAlann.class);

//    public DefaultAlann(int derivelets) {
//        this(new MapCacheBag<>(
//
//                new BoundedConcurrentHashMap<>(
//                        /* capacity */ conceptLimit,
//                        /* concurrency */ 1,
//                        /* key equivalence */
//                        AnyEquivalence.getInstance(Term.class),
//                        AnyEquivalence.getInstance(Concept.class))
//
//
//                //new LinkedHashMap<>(1024)
//                //new ConcurrentHashMap(1024)
//                //new UnifiedMap(1024)
//        ), derivelets);
//    }
//
//    public DefaultAlann(MapCacheBag<Term, Concept,?> concepts, int numDerivelets) {
//        this(new LocalMemory(new FrameClock(), concepts), numDerivelets);
//    }

    public DefaultAlann(Memory m, int commanderCapacity, int numDerivelets) {
        super(m, commanderCapacity);

        //indexIterator = Iterators.cycle(m.getConcepts());

        for (int i = 0; i < numDerivelets; i++) {
            Derivelet d = new Derivelet();
            derivers.add( d );
        }

        log.info(derivers.size() + " derivelets ready");

        defaultTTL = m.duration() * 3;
        this.context = new MyDeriveletContext(this);

        memory.eventCycleEnd.on(x -> processConcepts());

    }



    @Override
    protected void processConcepts() {

        /*if (concepts().size() > 0)*/ {

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
        if (commander.isEmpty()) return null;
        Task t = commander.commandIterator.next();
        return concept(t.term());
    };
    final Supplier<Concept> fromNext = () -> {
        return null;
    };

    final float[] fromNextp1 = new float[] { 1f, 0f };
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


    @Override
    public Concept apply(Term t) {
        return newDefaultConcept(t, 32, 32, memory);
    }

    @Override
    protected Concept doConceptualize(Termed c, Budget b, float scale) {
        if (!(c instanceof Concept)) return null;
        return (Concept)c;
    }

    @Override
    public NAR forEachConcept(Consumer<Concept> each) {
        commander.concepts.forEach(each);
        return this;
    }


    private class MyDeriveletContext extends DeriveletContext {
        public MyDeriveletContext(NAR nar) {
            super(nar, nar.memory.random, DefaultAlann.this.commander);
        }

        @Override
        public final Concept concept(Termed term) {
            return nar.concept(term);
        }
    }
}
