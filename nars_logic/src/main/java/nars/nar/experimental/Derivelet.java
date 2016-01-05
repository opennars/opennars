package nars.nar.experimental;

import nars.Global;
import nars.NAR;
import nars.bag.BLink;
import nars.concept.Concept;
import nars.nal.PremiseMatch;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Termed;
import nars.term.Terms;

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
    public BLink<Concept> concept;

    /**
     * utility context
     */
    public DeriveletContext context;

    PremiseMatch matcher;

    /**
     * temporary re-usable array for batch firing
     */
    private final Set<BLink<Termed>> terms = Global.newHashSet(1);
    /**
     * temporary re-usable array for batch firing
     */
    private final Set<BLink<Task>> tasks = Global.newHashSet(1);

    private BLink[] termsArray = new BLink[0];
    private BLink[] tasksArray = new BLink[0];

    public static int firePremises(BLink<Concept> conceptLink, BLink<Task>[] tasks, BLink<Termed>[] terms, Consumer<ConceptProcess> proc, NAR nar) {

        int total = 0;

        for (BLink<Task> taskLink : tasks) {
            if (taskLink == null) break;

            for (BLink<Termed> termLink : terms) {
                if (termLink == null) break;

                if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.get().term(), termLink.get().term()))
                    continue;

                total+= ConceptProcess.fireAll(
                    nar, conceptLink, taskLink, termLink, proc);
            }
        }

        return total;
    }


    /**
     * iteratively supplies a matrix of premises from the next N tasklinks and M termlinks
     * (recycles buffers, non-thread safe, one thread use this at a time)
     */
    public int firePremiseSquare(
            NAR nar,
            Consumer<ConceptProcess> proc,
            BLink<Concept> conceptLink,
            int tasklinks, int termlinks, Predicate<BLink> each) {

        Concept concept = conceptLink.get();

        concept.getTaskLinks().sample(tasklinks, each, tasks).commit();
        if (tasks.isEmpty()) return 0;

        concept.getTermLinks().sample(termlinks, each, terms).commit();
        if (terms.isEmpty()) return 0;

        //convert to array for fast for-within-for iterations
        tasksArray = this.tasks.toArray(tasksArray);
        this.tasks.clear();

        termsArray = this.terms.toArray(termsArray);
        this.terms.clear();

        return firePremises(conceptLink,
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

        final BLink<Concept> concept = this.concept;

        if (concept == null) {
            return null;
        }

        final float x = context.nextFloat();
        Concept c = concept.get();

        //calculate probability it will stay at this concept
        final float stayProb = 0.5f;//(concept.getPriority()) * 0.5f;
        if (x < stayProb) {
            //stay here
            return c;
        } else {
            float rem = 1.0f - stayProb;


            final BLink tl = ((x > (stayProb + (rem / 2))) ?
                    c.getTermLinks() :
                    c.getTaskLinks())
                    .sample();

            if (tl != null) {
                c = context.concept(((Termed) tl.get()));
                if (c != null) return c;
            }
        }

        return null;
    }

    /**
     * run next iteration; true if still alive by end, false if died and needs recycled
     */
    public final boolean cycle(final long now) {

        if (this.ttl-- == 0) {
            //died
            return false;
        }

        //TODO dont instantiate BagBudget
        if ((this.concept = new BLink(nextConcept(), 0, 0, 0)) == null) {
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
        this.concept = new BLink(concept, 0, 0, 0); //TODO
        this.ttl = ttl;
        this.matcher = new PremiseMatch(context.rng);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '@' + concept;
    }


}
