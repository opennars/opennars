/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package nars.process;

import nars.Global;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Terms;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
abstract public class ConceptProcess extends NAL implements Serializable {



    protected final TaskLink taskLink;
    protected final Concept concept;

    private Task currentBelief;
    private transient boolean cyclic;

    @Override public Task getTask() {
        return getTaskLink().getTask();
    }

    public TaskLink getTaskLink() {
        return taskLink;
    }

    @Override public Concept getConcept() {
        return concept;
    }


    public ConceptProcess(NAR nar, Concept concept, TaskLink taskLink) {
        super(nar);

        this.taskLink = taskLink;
        this.concept = concept;

    }

    @Override
    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
                .append("[").append(concept.toString()).append(':').append(taskLink).append(']')
                .toString();
    }



//    protected void beforeFinish(final long now) {
//
//        Memory m = nar.memory();
//        m.logic.TASKLINK_FIRE.hit();
//        m.emotion.busy(getTask(), this);
//
//    }

//    @Override
//    final protected Collection<Task> afterDerive(Collection<Task> c) {
//
//        final long now = nar.time();
//
//        beforeFinish(now);
//
//        return c;
//    }

    @Override public void updateBelief(Task nextBelief) {
        this.currentBelief = nextBelief;

        if (nextBelief == null)
        cyclic = false;
        else {
            Task t = getTask();
            cyclic = Stamp.overlapping(t, nextBelief);
        }
    }


    @Override
    public Task getBelief() {
        return currentBelief;
    }


    //TODO cache this value
    @Override
    public boolean isCyclic() {
        return cyclic;
    }


    /** iteratively supplies a matrix of premises from the next N tasklinks and M termlinks */
    public static Stream<Task> nextPremiseSquare(NAR nar, final Concept concept, float taskLinkForgetDurations, Function<ConceptProcess,Stream<Task>> proc, int maxTaskLinks, int maxTermLinks) {

        Set<TaskLink> tasks = Global.newHashSet(maxTaskLinks);
        //TODO replace with one batch selector call
        for (int i = 0; i < maxTaskLinks; i++) {
            TaskLink tl = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, nar.memory());
            if (tl!=null) tasks.add(tl);
        }
        if (tasks.isEmpty()) return Stream.empty();

        Set<TermLink> terms = Global.newHashSet(maxTermLinks);
        float termLinkForgetDurations = concept.getMemory().termLinkForgetDurations.floatValue();

        //TODO replace with one batch selector call
        for (int i = 0; i < maxTermLinks; i++) {
            TermLink tl = concept.getTermLinks().forgetNext(termLinkForgetDurations, nar.memory());
            if (tl!=null) terms.add(tl);
        }
        if (terms.isEmpty()) return Stream.empty();


        Stream.Builder<Stream<Task>> streams = Stream.builder();

        for (final TaskLink a : tasks) {
            for (final TermLink b : terms) {
                fireConcept(nar, concept, proc, streams, a, b);
            }
        }

        return streams.build().flatMap(s -> s);
    }

    public boolean validateDerivedBudget(Budget budget) {
        return !budget.summaryLessThan(memory().derivationThreshold.floatValue());
    }

    private static ConceptProcess fireConcept(NAR nar, Concept concept, Function<ConceptProcess, Stream<Task>> proc, Stream.Builder<Stream<Task>> streams, TaskLink a, TermLink b) {
        ConceptProcess p = premise(nar, concept, a, b);

        if (p!=null) {
            final Stream<Task> substream = proc.apply(p);
            if (substream!=null)
                streams.accept(substream);
        }
        return p;
    }

//    /** supplies at most 1 premise containing the pair of next tasklink and termlink into a premise */
//    public static Stream<Task> nextPremise(NAR nar, final Concept concept, float taskLinkForgetDurations, Function<ConceptProcess,Stream<Task>> proc) {
//
//        TaskLink taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, nar.memory());
//        if (taskLink == null) return Stream.empty();
//
//        TermLink termLink = concept.getTermLinks().forgetNext(nar.memory().termLinkForgetDurations, nar.memory());
//        if (termLink == null) return Stream.empty();
//
//
//        return proc.apply(premise(nar, concept, taskLink, termLink));
//
//    }

    public static ConceptProcess premise(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
        if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
            return null;

        if (taskLink.isTaskDeleted())
            return null;

        return new ConceptTaskTermLinkProcess(nar, concept, taskLink, termLink);
    }

    /** gets the average summary of one or both task/belief task's */
    public float getMeanPriority() {
        float total = 0;
        int n = 0;
        Task pt = getTask();
        if (pt!=null) {
            float p = pt.getPriority();
            //if (Float.isNaN(p)) p = 0; //NaN should not happen
            total += p;
            n++;
        }
        Task pb = getBelief();
        if (pb!=null) {
            float p = pt.getPriority();
            //if (Float.isNaN(p)) p = 0; //NaN should not happen
            total += p;
            n++;
        }


        if (Global.DEBUG) {
            Budget.requireNotDeleted(total);
            if (n == 0)
                throw new RuntimeException("missing both parent task and parent belief");
        }

        return total/n;
    }

    public abstract Stream<Task> derive(final Function<ConceptProcess,Stream<Task>> p);

//    public static void forEachPremise(NAR nar, @Nullable final Concept concept, @Nullable TaskLink taskLink, int termLinks, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
//        if (concept == null) return;
//
//        concept.updateLinks();
//
//        if (taskLink == null) {
//            taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, concept.getMemory());
//            if (taskLink == null)
//                return;
//        }
//
//
//
//
//        proc.accept( new ConceptTaskLinkProcess(nar, concept, taskLink) );
//
//        if ((termLinks > 0) && (taskLink.type!=TermLink.TRANSFORM))
//            ConceptProcess.forEachPremise(nar, concept, taskLink,
//                    termLinks,
//                    proc
//            );
//    }

//    /** generates a set of termlink processes by sampling
//     * from a concept's TermLink bag
//     * @return how many processes generated
//     * */
//    public static int forEachPremise(NAR nar, Concept concept, TaskLink t, final int termlinksToReason, Consumer<ConceptProcess> proc) {
//
//        int numTermLinks = concept.getTermLinks().size();
//        if (numTermLinks == 0)
//            return 0;
//
//        TermLink[] termlinks = new TermLink[termlinksToReason];
//
//        //int remainingProcesses = Math.min(termlinksToReason, numTermLinks);
//
//        //while (remainingProcesses > 0) {
//
//            Arrays.fill(termlinks, null);
//
//            concept.getPremiseGenerator().nextTermLinks(concept, t, termlinks);
//
//            int created = 0;
//            for (TermLink tl : termlinks) {
//                if (tl == null) break;
//
//                proc.accept(
//                    new ConceptTaskTermLinkProcess(nar, concept, t, tl)
//                );
//                created++;
//            }
//
//
//          //  remainingProcesses--;
//
//
//        //}
//
//        /*if (remainingProcesses == 0) {
//            System.err.println(now + ": " + currentConcept + ": " + remainingProcesses + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
//                    //+ currentConcept.getTermLinks().values()
//            );
//            //currentConcept.taskLinks.printAll(System.out);
//        }*/
//
//        return created;
//
//    }

}
