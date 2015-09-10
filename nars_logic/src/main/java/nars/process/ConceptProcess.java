/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Memory;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
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

    @Override public Task getTask() {
        return getTaskLink().getTask();
    }

    @Override public TaskLink getTaskLink() {
        return taskLink;
    }

    @Override public final Concept getConcept() {
        return concept;
    }


    public ConceptProcess(Concept concept, TaskLink taskLink) {
        super(concept.getMemory());

        this.taskLink = taskLink;
        this.concept = concept;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
                .append("[").append(concept.toString()).append(':').append(taskLink).append(']')
                .toString();
    }


    protected void beforeFinish(final long now) {

        memory.eventConceptProcessed.emit(this);
        memory.logic.TASKLINK_FIRE.hit();
        memory.emotion.busy(getTask(), this);

    }

    @Override
    final protected void afterDerive() {

        final long now = memory.time();

        beforeFinish(now);

        inputDerivations();

    }



    @Override
    public Task getBelief() {
        return currentBelief;
    }

    @Deprecated public void setBelief(Task nextBelief) {
        this.currentBelief = nextBelief;
    }



    public static void forEachPremise(@Nullable final Concept concept, int termLinks, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
        forEachPremise(concept, null, termLinks, taskLinkForgetDurations, proc);
    }

    public static void forEachPremise(@Nullable final Concept concept, @Nullable TaskLink taskLink, int termLinks, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
        if (concept == null) return;

        concept.updateLinks();

        if (taskLink == null) {
            taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, concept.getMemory());
            if (taskLink == null)
                return;
        }

        proc.accept( new ConceptTaskLinkProcess(concept, taskLink) );

        if ((termLinks > 0) && (taskLink.type!=TermLink.TRANSFORM))
            ConceptProcess.forEachPremise(concept, taskLink,
                    termLinks,
                    proc
            );
    }

    /** generates a set of termlink processes by sampling
     * from a concept's TermLink bag
     * @return how many processes generated
     * */
    public static int forEachPremise(Concept concept, TaskLink t, final int termlinksToReason, Consumer<ConceptProcess> proc) {

        int numTermLinks = concept.getTermLinks().size();
        if (numTermLinks == 0)
            return 0;

        final Memory memory = concept.getMemory();

        TermLink[] termlinks = new TermLink[termlinksToReason];

        //int remainingProcesses = Math.min(termlinksToReason, numTermLinks);

        //while (remainingProcesses > 0) {

            Arrays.fill(termlinks, null);

            concept.getPremiseGenerator().nextTermLinks(concept, t, termlinks);

            int created = 0;
            for (TermLink tl : termlinks) {
                if (tl == null) break;

                proc.accept(
                    new ConceptTaskTermLinkProcess(concept, t, tl)
                );
                created++;
            }


          //  remainingProcesses--;


        //}

        /*if (remainingProcesses == 0) {
            System.err.println(now + ": " + currentConcept + ": " + remainingProcesses + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
                    //+ currentConcept.getTermLinks().values()
            );
            //currentConcept.taskLinks.printAll(System.out);
        }*/

        return created;

    }


    public Set<Task> getDerived() {
        if (derived == null)
            return Collections.emptySet();
        return derived;
    }
}
