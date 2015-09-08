package nars.nar.experimental;

import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Param;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.MapCacheBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.budget.Itemized;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.concept.DefaultConcept;
import nars.link.TLink;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.Deriver;
import nars.nal.PremiseProcessor;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.op.app.Commander;
import nars.process.ConceptProcess;
import nars.process.ConceptTaskLinkProcess;
import nars.process.ConceptTaskTermLinkProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.task.filter.LimitDerivationPriority;
import nars.term.Atom;
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by me on 9/5/15.
 */
public abstract class AbstractAlann extends AbstractNARSeed<MapCacheBag<Term,Concept>, Param> {

    final static Procedure2<Budget,Budget> budgetMerge = Budget.plus;

    @Deprecated
    public final Default param = new NewDefault() {

        @Override
        protected DerivationFilter[] getDerivationFilters() {
            return new DerivationFilter[]{
                    new FilterBelowConfidence(0.01),
                    new FilterDuplicateExistingBelief(),
                    new LimitDerivationPriority()
                    //param.getDefaultDerivationFilters().add(new BeRational());
            };
        }

    }; // shadow defaults, will replace once refactored
    protected final List<Task> sorted = Global.newArrayList();
    protected final Map<Term, Concept> conceptsMap;
    final Random rng = new XorShift1024StarRandom(1);
    final ItemAccumulator<Task> newTasks = new ItemAccumulator(Budget.plus);
    final int maxNewTasksPerCycle = 10;
    final int maxNewTaskHistory = 100;
    Commander commander;

    public AbstractAlann(MapCacheBag<Term, Concept> concepts) {
        super(concepts);
        this.conceptsMap = concepts.data;
    }

    protected abstract void processConcepts();

    public void init(NAR nar) {
        param.init(nar);

        //param.taskProcessThreshold.set(0); //process everything, even if budget is zero

        commander = new Commander(nar);
    }

    protected void processNewTasks() {
        final int size = newTasks.size();
        if (size!=0) {

            int toDiscard = Math.max(0, size - maxNewTaskHistory);
            int remaining = newTasks.update(maxNewTaskHistory, sorted);

            if (size > 0) {

                int toRun = Math.min( maxNewTasksPerCycle, remaining);

                TaskProcess.run(memory, sorted, toRun, toDiscard);

                //System.out.print("newTasks size=" + size + " run=" + toRun + "=(" + x.length + "), discarded=" + toDiscard + "  ");
            }
        }
    }


    @Override
    public void cycle() {
        processNewTasks();
        processConcepts();
    }


    @Override final public boolean accept(final Task t) {
        return newTasks.add(t);
    }

    @Deprecated @Override public Concept nextConcept() {
        throw new RuntimeException("should not be called, this method will be deprecated");
    }




    @Override
    public Concept conceptualize(final Term term, final Budget budget, final boolean createIfMissing) {
        return conceptsMap.compute(term, (k,existing) -> {
            if (existing!=null) {
                budgetMerge.value(existing.getBudget(), budget);
                return existing;
            }
            else {
                return newConcept(term, budget, memory);
            }
        });
    }

    @Override
    public boolean reprioritize(Term term, float newPriority) {
        throw new RuntimeException("N/A");
    }

    @Override
    public Concept remove(Concept c) {
        Itemized removed = concepts.remove(c.getTerm());
        if ((removed==null) || (removed!=c))
            throw new RuntimeException("concept unknown");

        return c;
    }

    @Override
    public Param getParam() {
        param.the(Deriver.class, NewDefault.der);
        param.setTermLinkBagSize(32);
        return param;
    }

    @Override
    public Random getRandom() {
        return rng;
    }

    final Random newRandom() {
        return new XorShift1024StarRandom(1);
    }

    @Override
    final public CacheBag<Term, Concept> getConceptIndex() {
        return concepts;
    }

    public Memory newMemory() {

        final Param p = getParam();

        return new Memory(
                getRandom(),
                getMaximumNALLevel(),
                p,
                getConceptBuilder(),
                getPremiseProcessor(p),
                concepts
        );
    }

    @Override
    public PremiseProcessor getPremiseProcessor(final Param p) {
        return param.getPremiseProcessor(p);
    }

    public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {

        if (t instanceof Atom) {
            return new AtomConcept(t, b,
                    termLinks, taskLinks,
                    null, m
            );
        }
        else {
            return new DefaultConcept(t, b,
                    taskLinks, termLinks,
                    null,
                    param.newConceptBeliefGoalRanking(),
                    m
            );
        }

    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return param.getConceptBuilder();
    }

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks =
                new CurveBag<>(rng, /*sentenceNodes,*/ param.getConceptTaskLinks());
        taskLinks.mergeAverage();

        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag<>(rng, /*termlinkKeyNodes,*/ param.getConceptTermLinks());
        termLinks.mergeAverage();

        return newConcept(t, b, taskLinks, termLinks, m);
    }

    /** particle that travels through the graph,
     * responsible for deciding what to derive */
    public static class Derivelet  {


        /** modulating the TTL (time-to-live) allows the system to control
         * the quality of attention it experiences.
         * a longer TTL will cause derivelets to restart
         * less frequently and continue exploring potentially "yarny"
         * paths of knowledge
          */
        int ttl;


        /** current location */
        public Concept concept;

        /** utility context */
        public DeriveletContext context;


        public ConceptProcess nextPremise(long now) {

            final Concept concept = this.concept;



            concept.getBudget().forget(now, context.getForgetCycles(), 0);



            TaskLink tl = concept.getTaskLinks().forgetNext();
            if ((tl == null) || (tl.getTask().isDeleted()))
                return null;

            /*if (runner.nextFloat() < 0.1) {
                return new ALANNConceptTaskLinkProcess(concept, tl);
            }
            else*/ {
                TermLink tm = concept.getTermLinks().forgetNext();
                if ((tm != null) && (tl.type != TermLink.TRANSFORM)) {
                    return new Derivelet.ALANNConceptTaskTermLinkProcess(concept, tl, tm);
                }
                else {
                    return new Derivelet.ALANNConceptTaskLinkProcess(concept, tl);
                }
            }

        }

        protected void inputDerivations(final Set<Task> derived) {
            if (derived!=null) {
                //transform this ConceptProcess's derivation to a TaskProcess and run it
                final Memory mem = concept.getMemory();

                derived.forEach(/*newTaskProcess*/ t -> {

                    if (t.init(mem)) {
                        //System.err.println("direct input: " + t);
                        TaskProcess.run(mem, t);
                    }

                });
            }
        }

        /** determines a next concept to move adjacent to
         *  the concept it is currently at
         */
        public Concept nextConcept() {

            final Concept concept = this.concept;

            if (concept == null) {
                return null;
            }


            final float x = context.nextFloat();

            //calculate probability it will stay at this concept
            final float stayProb = (concept.getPriority()) * 0.5f;
            if (x < stayProb ) {
                //stay here
                return concept;
            }
            else {
                final TLink tl;
                float rem = 1.0f - stayProb;
                if (x > stayProb + rem/2 ) {
                    tl = concept.getTermLinks().peekNext();
                } else {
                    tl = concept.getTaskLinks().peekNext();
                }
                if (tl != null) {
                    Concept c = context.concept(tl.getTerm());
                    if (c != null) return c;
                }
            }

            return null;
        }

        /** run next iteration; true if still alive by end, false if died and needs recycled */
        final public boolean cycle(final long now) {

            final Concept current = this.concept;

            if (this.ttl-- == 0) {
                //died
                return false;
            }

            if ( (this.concept = nextConcept()) == null) {
                //dead-end
                return false;
            }

            final ConceptProcess p = nextPremise(now);
            if (p!=null) {
                p.run();
            }
            else {
                //no premise
                return false;
            }

            return true;
        }


        public final void start(final Concept concept, int ttl, final DeriveletContext context) {
            this.context = context;
            this.concept = concept;
            this.ttl = ttl;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + concept;
        }

        private class ALANNConceptTaskTermLinkProcess extends ConceptTaskTermLinkProcess {

            public ALANNConceptTaskTermLinkProcess(Concept concept, TaskLink tl, TermLink tm) {
                super(concept, tl, tm);
            }

            @Override protected final void inputDerivations() {
                Derivelet.this.inputDerivations(derived);
            }

            /*@Override
            protected synchronized void derive() {
                super.derive();
            }*/
        }

        private class ALANNConceptTaskLinkProcess extends ConceptTaskLinkProcess {
            public ALANNConceptTaskLinkProcess(Concept concept, TaskLink tl) {
                super(concept, tl);
            }

            @Override protected final void inputDerivations() {
                Derivelet.this.inputDerivations(derived);
            }

            /*@Override
            protected synchronized void derive() {
                super.derive();
            }*/
        }
    }
}
