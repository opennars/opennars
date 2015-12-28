package nars.nar.experimental;

import nars.Global;
import nars.NAR;
import nars.bag.BagBudget;
import nars.concept.Concept;
import nars.nal.RuleMatch;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Termed;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * particle that travels through the graph,
 * responsible for deciding what to derive
 */
public class Derivelet {


    /**
     * modulating the TTL (time-to-live) allows the system to control
     * the quality of attention it experiences.
     * a longer TTL will cause derivelets to restart
     * less frequently and continue exploring potentially "yarny"
     * paths of knowledge
     */
    int ttl;


    /**
     * current location
     */
    public Concept concept;

    /**
     * utility context
     */
    public DeriveletContext context;

    RuleMatch matcher;

    /**
     * temporary re-usable array for batch firing
     */
    private final Set<BagBudget<Termed>> terms = Global.newHashSet(1);
    /**
     * temporary re-usable array for batch firing
     */
    private final Set<BagBudget<Task>> tasks = Global.newHashSet(1);

    private BagBudget[] termsArray = new BagBudget[0];
    private BagBudget[] tasksArray = new BagBudget[0];


    /**
     * iteratively supplies a matrix of premises from the next N tasklinks and M termlinks
     * (recycles buffers, non-thread safe, one thread use this at a time)
     */
    public int firePremiseSquare(
            NAR nar,
            Consumer<ConceptProcess> proc,
            Concept concept,
            int tasklinks, int termlinks, Predicate<BagBudget> each) {

        //Memory m = nar.memory;
        //int dur = m.duration();

        //long now = nar.time();

        /* dur, now,
            taskLinkForgetDurations * dur,
            tasks); */
        int tasksCount = concept.getTaskLinks().next(tasklinks, each, tasks);
        if (tasksCount == 0) return 0;
        concept.getTaskLinks().commit();



        /*int termsCount = concept.nextTermLinks(dur, now,
            m.termLinkForgetDurations.floatValue(),
            terms);*/
        int termsCount = concept.getTermLinks().next(termlinks, each, terms);
        if (termsCount == 0) return 0;
        concept.getTermLinks().commit();


        /*System.out.println(tasks.size() + "," + terms.size() + ": "
                + tasks + " " + terms);*/

        //convert to array for fast for-within-for iterations
        tasksArray = this.tasks.toArray(tasksArray);
        this.tasks.clear();

        termsArray = this.terms.toArray(termsArray);
        this.terms.clear();

        return ConceptProcess.firePremises(concept,
                tasksArray, termsArray,
                proc, nar);

    }


    private NAR nar() {
        return context.nar;
    }

    /**
     * determines a next concept to move adjacent to
     * the concept it is currently at
     */
    public Concept nextConcept() {

        final Concept concept = this.concept;

        if (concept == null) {
            return null;
        }

        final float x = context.nextFloat();

        //calculate probability it will stay at this concept
        final float stayProb = 0.5f;//(concept.getPriority()) * 0.5f;
        if (x < stayProb) {
            //stay here
            return concept;
        } else {
            final BagBudget tl;
            float rem = 1.0f - stayProb;

            tl = ((x > (stayProb + (rem / 2))) ?
                    concept.getTermLinks() :
                    concept.getTaskLinks())
                        .peekNext();

            if (tl != null) {
                Concept c = context.concept(((Termed) tl.get()));
                if (c != null) return c;
            }
        }

        return null;
    }

    /**
     * run next iteration; true if still alive by end, false if died and needs recycled
     */
    final public boolean cycle(final long now) {

        if (this.ttl-- == 0) {
            //died
            return false;
        }

        if ((this.concept = nextConcept()) == null) {
            //dead-end
            return false;
        }


        int tasklinks = 1;
        int termlinks = 2;

        int fired = firePremiseSquare(context.nar,
                perPremise, this.concept,
                tasklinks, termlinks,
                Default.simpleForgetDecay
        );

        return fired > 0;
    }

    final Consumer<Task> perDerivation = (derived) -> {
        final NAR n = nar();

        derived = n.validInput(derived);
        if (derived != null)
            n.process(derived);
    };

    final Consumer<ConceptProcess> perPremise = p ->
            DeriveletContext.deriver.run(p, matcher, perDerivation);


    public final void start(final Concept concept, int ttl, final DeriveletContext context) {
        this.context = context;
        this.concept = concept;
        this.ttl = ttl;
        this.matcher = new RuleMatch(context.rng);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + concept;
    }


}
