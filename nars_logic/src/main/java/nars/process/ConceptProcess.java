/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package nars.process;

import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Terms;

import java.util.Collection;
import java.util.function.Consumer;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
abstract public class ConceptProcess extends NAL  {



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

    @Override public final Concept getConcept() {
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


    @Override
    protected void beforeDerive() {
        nar.memory.eventConceptProcess.emit(this);
    }

    protected void beforeFinish(final long now) {

        Memory m = nar.memory();
        m.logic.TASKLINK_FIRE.hit();
        m.emotion.busy(getTask(), this);

    }

    @Override
    final protected Collection<Task> afterDerive(Collection<Task> c) {

        final long now = nar.time();

        beforeFinish(now);

        return c;
    }



    @Override
    public Task getBelief() {
        return currentBelief;
    }

    @Deprecated public void setBelief(Task nextBelief) {

        this.currentBelief = nextBelief;

        if (nextBelief == null)
            cyclic = false;
        else {
            Task t = getTask();
            cyclic = Stamp.overlapping(t, nextBelief);
        }
    }

    //TODO cache this value
    @Override
    public boolean isCyclic() {
        return cyclic;
    }



    public static void forEachPremise(NAR nar, final Concept concept, int termLinks, float taskLinkForgetDurations, Consumer<ConceptProcess> proc, long now) {

        TaskLink taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, nar.memory());
        if (taskLink == null) return;

        TermLink termLink = concept.getTermLinks().forgetNext(nar.memory().termLinkForgetDurations, nar.memory());
        if (termLink == null) return;


        if (!Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm())) {

            ConceptProcess cp = new ConceptTaskTermLinkProcess(nar, concept, taskLink, termLink);

            final Concept beliefConcept = nar.concept(termLink.target);
            if (beliefConcept != null) {
                //belief can be null:
                Task belief = beliefConcept.getBeliefs().top(taskLink.getTask(), now);
                cp.setBelief(belief);
            }

            proc.accept(cp);

        }

    }

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
