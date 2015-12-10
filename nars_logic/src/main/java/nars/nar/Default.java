package nars.nar;

import com.gs.collections.impl.bag.mutable.HashBag;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.concept.util.ConceptActivator;
import nars.nal.Deriver;
import nars.nal.RuleMatch;
import nars.task.Task;
import nars.task.flow.SortedTaskPerception;
import nars.term.Term;
import nars.term.compile.TermIndex;
import nars.time.FrameClock;
import nars.util.data.list.FasterList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

/**
 * Various extensions enabled
 */
public class Default extends AbstractNAR {

    /**
     * max # of tasks to accumulate in sorted buffer
     */

    @Deprecated public Default() {
        this(1024, 1,1,3);
    }

    public Default(int numConcepts,
                   int conceptsFirePerCycle,
                   int tasklinkFirePerConcept,
                   int termlinkFirePerConcept) {
        this(new Memory(new FrameClock(),
                TermIndex.memory(numConcepts*8)
        ), numConcepts, conceptsFirePerCycle, tasklinkFirePerConcept, termlinkFirePerConcept);
    }

    public Default(Memory mem, int i, int i1, int i2, int i3) {
        super(mem, i, i1, i2, i3);

        //new QueryVariableExhaustiveResults(this.memory());

        /*
        the("memory_sharpen", new BagForgettingEnhancer(memory, core.active));
        */

    }

    @Override
    protected DefaultCycle2 initCore(int activeConcepts, Deriver deriver, Bag<Term, Concept> conceptBag, ConceptActivator activator) {

        return new DefaultCycle2(this, deriver,
                conceptBag, activator
        );
    }

    /**
     * groups each derivation's tasks as a group before inputting into
     * the main perception buffer, allowing post-processing such as budget normalization.
     *
     * ex: this can ensure that a premise which produces many derived tasks
     * will not consume budget unfairly relative to another premise
     * with less tasks but equal budget.
     */
    public static class DefaultCycle2 extends DefaultCycle {

        /** re-used, not to be used outside of this */
        private final RuleMatch matcher;

        /**
         * holds the resulting tasks of one derivation so they can
         * be normalized or some other filter or aggregation
         * applied collectively.
         */
        final Collection<Task> derivedTasksBuffer;


        public DefaultCycle2(NAR nar, Deriver deriver, Bag<Term, Concept> concepts, ConceptActivator ca) {
            super(nar, deriver, concepts, ca);

            matcher = new RuleMatch(nar.memory.random);
            /* if detecting duplicates, use a list. otherwise use a set to deduplicate anyway */
            derivedTasksBuffer =
                    Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS ?
                        new FasterList() : Global.newHashSet(1);

        }



        @Override
        protected void fireConcept(Concept c) {

            Collection<Task> buffer = derivedTasksBuffer;
            Consumer<Task> narInput = nar::input;
            Deriver deriver = this.deriver;

            fireConcept(c, p -> {

                deriver.run(p, matcher, buffer::add);

                if (Global.DEBUG_DETECT_DUPLICATE_DERIVATIONS) {
                    HashBag<Task> b = detectDuplicates(buffer);
                    buffer.clear();
                    b.addAll(buffer);
                }


                if (!buffer.isEmpty()) {

                    Task.normalize(
                            buffer,
                            p.getTask().getPriority()
                            //p.getMeanPriority()
                    );

                    buffer.forEach(narInput);

                    buffer.clear();
                }

            });

        }

        static HashBag<Task> detectDuplicates(Collection<Task> buffer) {
            HashBag<Task> taskCount = new HashBag<>();
            taskCount.addAll(buffer);
            taskCount.forEachWithOccurrences((t,i) -> {
                if (i == 1) return;

                System.err.println("DUPLICATE TASK(" + i +"): " + t);
                List<Task> equiv = buffer.stream().filter(u -> u.equals(t)).collect(toList());
                HashBag<String> rules = new HashBag();
                equiv.forEach(u -> {
                    String rule = u.getLogLast().toString();
                    rules.add(rule);

//                    System.err.println("\t" + u );
//                    System.err.println("\t\t" + rule );
//                    System.err.println();
                });
                rules.forEachWithOccurrences( (String r, int c) -> System.err.println("\t" + c + '\t' + r));
                System.err.println("--");

            });
            return taskCount;
        }
    }


    @Override
    public SortedTaskPerception initInput() {

        int perceptionCapacity = 64;
        int inputsPerCycle = -1 /* everything */;

        SortedTaskPerception input = new SortedTaskPerception(
                this,
                task -> true /* allow everything */,
                this::process,
                perceptionCapacity,
                inputsPerCycle
        ) {
            @Override
            protected void onOverflow(Task t) {
                memory.eventError.emit("Overflow: " + t + " " + getStatistics());
            }
        };
        //input.inputsMaxPerCycle.set(conceptsFirePerCycle);;
        return input;
    }

    public SortedTaskPerception getInput() {
        return (SortedTaskPerception) input;
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
