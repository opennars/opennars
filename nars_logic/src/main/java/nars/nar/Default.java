package nars.nar;

import com.gs.collections.impl.bag.mutable.HashBag;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.BagBudget;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.nal.RuleMatch;
import nars.nar.experimental.Derivelet;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.task.flow.FIFOTaskPerception;
import nars.task.flow.SetTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.compile.TermIndex;
import nars.time.FrameClock;
import nars.util.data.MutableInteger;
import nars.util.data.list.FasterList;
import nars.util.event.Active;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * Various extensions enabled
 */
public class Default extends AbstractNAR {

    public final DefaultCycle core;
    public final TaskPerception input;


    @Deprecated
    public Default() {
        this(1024, 1, 1, 3);
    }

    public Default(int numConcepts,
                   int conceptsFirePerCycle,
                   int tasklinkFirePerConcept,
                   int termlinkFirePerConcept) {
        this(new Memory(new FrameClock(),
                TermIndex.memory(numConcepts * 2)
                //TermIndex.memoryWeak(numConcepts * 8)
        ), numConcepts, conceptsFirePerCycle, termlinkFirePerConcept, tasklinkFirePerConcept);
    }

    public Default(Memory mem, int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        super(mem);

        the("input", input = initInput());

        the("core", core = initCore(
                activeConcepts,
                conceptsFirePerCycle,
                termLinksPerCycle, taskLinksPerCycle
        ));

        if (core!=null) {
            beforeNextFrame(this::initHigherNAL);
        }


        //new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }

    public TaskPerception _initInput() {
        return new FIFOTaskPerception(this, null, this::process);
    }

    public TaskPerception initInput() {

        return new SetTaskPerception(
                memory, this::process, Budget.plus);

        /* {
            @Override
            protected void onOverflow(Task t) {
                memory.eventError.emit("Overflow: " + t + " " + getStatistics());
            }
        };*/
        //input.inputsMaxPerCycle.set(conceptsFirePerCycle);;
    }

    protected DefaultCycle initCore(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {

        DefaultCycle c = new DefaultCycle2(this, getDeriver(), newConceptBag(activeConcepts));

        //TODO move these to a PremiseGenerator which supplies
        // batches of Premises
        c.termlinksSelectedPerFiredConcept.set(termLinksPerCycle);
        c.tasklinksSelectedPerFiredConcept.set(taskLinksPerCycle);

        //tmpConceptsFiredPerCycle[0] = c.conceptsFiredPerCycle;
        c.conceptsFiredPerCycle.set(conceptsFirePerCycle);

        c.capacity.set(activeConcepts);

        return c;
    }


    public Bag<Concept> newConceptBag(int initialCapacity) {
        return new CurveBag<Concept>(initialCapacity, rng) {

            @Override public BagBudget<Concept> put(Object v) {
                BagBudget<Concept> b = get(v);
                Concept c = (Concept) v;
                if (b==null)
                    b = new BagBudget(c, 0,1,1);

                float p =
                        //Math.max(
                        //c.getTaskLinks().getPriorityMax()
                        c.getTaskLinks().getSummaryMean()
                        // c.getTermLinks().getPriorityMax()
                        //)
                        ;

                b.set(p, 1f, 1f);

                return put(c, b);
            }

//            @Override
//            protected BagBudget<Concept> getDefaultBudget(Concept o) {
//                return new BagBudget<>(o,
//                        o.getTaskLinks().getSummaryMean(), 0.5f, 0.5f);
//            }

        }.mergeNull();
    }

    @Override
    protected final void activate(Concept c) {
        core.activate(c);
    }

    @Override
    public NAR forEachConcept(Consumer<Concept> recip) {
        core.active.forEach(recip);
        return this;
    }


    public static final Predicate<BagBudget> simpleForgetDecay = (b) -> {
        float p = b.getPriority() * 0.95f;
        if (p > b.getQuality()*0.1f)
            b.setPriority(p);
        return true;
    };

    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public abstract static class DefaultCycle implements Serializable {

        final Active handlers = new Active();

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        public final MutableInteger conceptsFiredPerCycle;


        public final Deriver  deriver;


        public final MutableInteger tasklinksSelectedPerFiredConcept = new MutableInteger(1);
        public final MutableInteger termlinksSelectedPerFiredConcept = new MutableInteger(1);

        //public final MutableFloat activationFactor = new MutableFloat(1.0f);

//        final Function<Task, Task> derivationPostProcess = d -> {
//            return LimitDerivationPriority.limitDerivation(d);
//        };



        /**
         * concepts active in this cycle
         */
        public final Bag<Concept> active;


        @Deprecated
        public final transient NAR nar;

        public final MutableInteger capacity = new MutableInteger();

        /** activated concepts pending (re-)insert to bag */
        public final LinkedHashSet<Concept> activated = new LinkedHashSet();


//        @Deprecated
//        int tasklinks = 2; //TODO use MutableInteger for this
//        @Deprecated
//        int termlinks = 3; //TODO use MutableInteger for this

        /* ---------- Short-term workspace for a single cycle ------- */

        public DefaultCycle(NAR nar, Deriver deriver, Bag<Concept> concepts) {

            this.nar = nar;

            this.deriver = deriver;


            conceptsFiredPerCycle = new MutableInteger(1);
            active = concepts;

            handlers.add(
                    nar.memory.eventCycleEnd.on((m) -> {
                        fireConcepts(conceptsFiredPerCycle.intValue(), c->process(c));
                        activateConcepts();
                    }),
                    nar.memory.eventReset.on((m) -> reset())
            );

            alannForget = (budget) -> {
                // priority * e^(-lambda*t)
                //     lambda is (1 - durabilty) / forgetPeriod
                //     dt is the delta
                final long currentTime = nar.time(); //TODO cache

                long dt = budget.setLastForgetTime(currentTime);
                if (dt == 0) return true; //too soon to update

                float currentPriority = budget.getPriorityIfNaNThenZero();

                Memory m = nar.memory;
                final float forgetPeriod =    m.termLinkForgetDurations.floatValue() * m.duration(); //TODO cache

                float relativeThreshold = 0.1f; //BAG THRESHOLD

                float expDecayed = currentPriority * (float) Math.exp(
                        -((1.0f - budget.getDurability()) / forgetPeriod) * dt
                );
                float threshold = budget.getQuality() * relativeThreshold;

                float nextPriority = expDecayed;
                if (nextPriority < threshold) nextPriority = threshold;

                budget.setPriority(nextPriority);

                return true;
            };
        }

        private void activateConcepts() {
            if (!activated.isEmpty()) {
                activated.forEach(this::updateConcept);
                activated.clear();
            }

            active.commit();
        }

        protected void updateConcept(Concept c) {
            active.put(c);
        }

        abstract protected void process(ConceptProcess cp);

        /**
         * samples an active concept
         */
        public Concept next() {
            return active.peekNext().get();
        }


        public void reset() {


        }



        public final Predicate<BagBudget> alannForget;

        protected void fireConcepts(int conceptsToFire, Consumer<ConceptProcess> processor) {

            Bag<Concept> b = this.active;

            b.setCapacity(capacity.intValue()); //TODO share the MutableInteger so that this doesnt need to be called ever

            if (conceptsToFire == 0 || b.isEmpty()) return;

            b.next(conceptsToFire, cb -> {
                Concept c = cb.get();

                //c.getTermLinks().up(simpleForgetDecay);
                //c.getTaskLinks().update(simpleForgetDecay);

                activated.add(c); //update at end of cycle

                //if above firing threshold
                //fireConcept(c);
                der.firePremiseSquare(nar, processor, c,
                        tasklinksSelectedPerFiredConcept.intValue(),
                        termlinksSelectedPerFiredConcept.intValue(),
                        //simpleForgetDecay
                        alannForget
                );

                return true;
            });

        }

        /*{
            fireConcept(c, p -> {
                //direct: just input to nar
                deriver.run(p, nar::input);
            });
        }*/

        final Derivelet der = new Derivelet();



//        protected final void fireConceptSquare(Concept c, Consumer<ConceptProcess> withResult) {
//
//
//            {
//                int num = termlinksSelectedPerFiredConcept.intValue();
//                if (firingTermLinks == null ||
//                        firingTermLinks.length != num)
//                    firingTermLinks = new BagBudget[num];
//            }
//            {
//                int num = tasklinksSelectedPerFiredConcept.intValue();
//                if (firingTaskLinks == null ||
//                        firingTaskLinks.length != num)
//                    firingTaskLinks = new BagBudget[num];
//            }
//
//            firePremiseSquare(
//                nar,
//                withResult,
//                c,
//                firingTaskLinks,
//                firingTermLinks,
//                nar.memory.taskLinkForgetDurations.intValue()
//            );
//        }


//        public final Concept activate(Term term, Budget b) {
//            Bag<Term, Concept> active = this.active;
//            active.setCapacity(capacity.intValue());
//
////            ConceptActivator ca = conceptActivator;
////            ca.setActivationFactor( activationFactor.floatValue() );
////            return ca.update(term, b, nar.time(), 1.0f, active);
//        }

        public final Bag<Concept> concepts() {
            return active;
        }

        public void activate(Concept c) {
            activated.add(c);
            //core.active.put(c);
        }


        //try to implement some other way, this is here because of serializability

    }

//    @Deprecated
//    public static class CommandLineNARBuilder extends Default {
//
//        List<String> filesToLoad = new ArrayList();
//
//        public CommandLineNARBuilder(String[] args) {
//            super();
//
//            for (int i = 0; i < args.length; i++) {
//                String arg = args[i];
//                if ("--silence".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(100 - sl);
//                } else if ("--noise".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(sl);
//                } else {
//                    filesToLoad.add(arg);
//                }
//            }
//
//            for (String x : filesToLoad) {
//                taskNext(() -> {
//                    try {
//                        input(new File(x));
//                    } catch (FileNotFoundException fex) {
//                        System.err.println(getClass() + ": " + fex.toString());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//
//                //n.run(1);
//            }
//        }
//
//        /**
//         * Decode the silence level
//         *
//         * @param param Given argument
//         * @return Whether the argument is not the silence level
//         */
//        public static boolean isReallyFile(String param) {
//            return !"--silence".equals(param);
//        }
//    }



    /**
     * groups each derivation's tasks as a group before inputting into
     * the main perception buffer, allowing post-processing such as budget normalization.
     * <p>
     * ex: this can ensure that a premise which produces many derived tasks
     * will not consume budget unfairly relative to another premise
     * with less tasks but equal budget.
     */
    public static class DefaultCycle2 extends DefaultCycle {

        /**
         * re-used, not to be used outside of this
         */
        private final RuleMatch matcher;

        /**
         * holds the resulting tasks of one derivation so they can
         * be normalized or some other filter or aggregation
         * applied collectively.
         */
        final Collection<Task> derivedTasksBuffer;


        public DefaultCycle2(NAR nar, Deriver deriver, Bag<Concept> concepts) {
            super(nar, deriver, concepts);

            matcher = new RuleMatch(nar.memory.random);
            /* if detecting duplicates, use a list. otherwise use a set to deduplicate anyway */
            derivedTasksBuffer =
                    Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ?
                            new FasterList() : Global.newHashSet(1);

        }


//        @Override
//        protected void fireConcept(Concept c) {
//
//            Collection<Task> buffer = derivedTasksBuffer;
//            Consumer<Task> narInput = nar::input;
//            Deriver deriver = this.deriver;
//
//            BagBudget<Termed> term = c.getTermLinks().peekNext();
//            if (term!=null) {
//                BagBudget<Task> task = c.getTaskLinks().peekNext();
//                if (task!=null) {
//                    deriver.run(
//                            new ConceptTaskTermLinkProcess(nar, c , task, term),
//                            matcher,
//                            nar::input
//                    );
//                }
//            }
//


//        fireConceptSquare(c, p -> {
//
//
//            });


        @Override
        public void process(ConceptProcess p) {
            Collection<Task> buffer = derivedTasksBuffer;
            Consumer<Task> narInput = nar::input;

            Deriver deriver = this.deriver;
            deriver.run(p, matcher, buffer::add);

            if (Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS) {
                HashBag<Task> b = detectDuplicates(buffer);
                buffer.clear();
                b.addAll(buffer);
            }

            if (!buffer.isEmpty()) {

                Task.normalize(
                        buffer,
                        //p.getMeanPriority()
                        //p.getTask().getPriority()
                        p.getTask().getPriority()/buffer.size()
                );

                buffer.forEach(narInput);

                buffer.clear();
            }

        }

        static HashBag<Task> detectDuplicates(Collection<Task> buffer) {
            HashBag<Task> taskCount = new HashBag<>();
            taskCount.addAll(buffer);
            taskCount.forEachWithOccurrences((t, i) -> {
                if (i == 1) return;

                System.err.println("DUPLICATE TASK(" + i + "): " + t);
                List<Task> equiv = buffer.stream().filter(u -> u.equals(t)).collect(toList());
                HashBag<String> rules = new HashBag();
                equiv.forEach(u -> {
                    String rule = u.getLogLast().toString();
                    rules.add(rule);

//                    System.err.println("\t" + u );
//                    System.err.println("\t\t" + rule );
//                    System.err.println();
                });
                rules.forEachWithOccurrences((String r, int c) -> System.err.println("\t" + c + '\t' + r));
                System.err.println("--");

            });
            return taskCount;
        }

    }



    @Override
    protected void initNAL9() {
        super.initNAL9();

//        new EpoxParser(true).nodes.forEach((k,v)->{
//            on(Atom.the(k), (Term[] t) -> {
//                Node n = v.clone(); //TODO dont use Epox's prototype pattern if possible
//                for (int i = 0; i < t.length; i++) {
//                    Term p = t[i];
//                    n.setChild(i, new Literal(Float.parseFloat(p.toString())));
//                }
//                return Atom.the(n.evaluate());
//            });
//        });
    }
}
