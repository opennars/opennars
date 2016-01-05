package nars.nar.experimental;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.Termed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Single-thread implementation of ALANN
 */
public class DefaultAlann extends AbstractAlann  {

    //private Iterator<Concept> indexIterator = null;

    private final int defaultTTL;
    List<Derivelet> derivers = Global.newArrayList();
    private final DeriveletContext context;

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

        //log.info(derivers.size() + " derivelets ready");

        defaultTTL = m.duration() * 2; //longer ttl means potentially deeper search before reboots
        this.context = new MyDeriveletContext(this);

        memory.eventCycleEnd.on(x -> processConcepts());

    }


    @Override
    public Concept conceptualize(Termed termed, Budget activation, float scale) {
        return memory.concept(termed); //TODO handle activation
    }

    @Override
    public float conceptPriority(Termed termed, float priIfNonExistent) {
        return Float.NaN; //TODO
    }

    @Override
    protected void processConcepts() {

        /*if (concepts().size() > 0)*/

        final long now = memory.time();

        final List<Derivelet> derivers = this.derivers;
        derivers.forEach(d -> {
            if (!d.cycle(now)) {
                restart(d); //recycle this derivelet
            }
        });


    }


    final Supplier<Concept> fromInput = () -> {
        if (commander.isEmpty()) return null;
        Task t = commander.commandIterator.next();
        return concept(t.term());
    };

    final void restart(final Derivelet d) {

        Concept next = fromInput.get();

        if (next != null) {
            d.start(next, defaultTTL, context);
        }
    }


    @Override
    public NAR forEachConcept(Consumer<Concept> each) {
        commander.concepts.forEach(each);
        return this;
    }



    private final class MyDeriveletContext extends DeriveletContext {
        public MyDeriveletContext(NAR nar) {
            super(nar, nar.memory.random, DefaultAlann.this.commander);
        }

        @Override
        public Concept concept(Termed term) {
            return nar.concept(term);
        }
    }
}
