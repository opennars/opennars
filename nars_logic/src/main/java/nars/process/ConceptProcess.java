/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Memory;
import nars.Param;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

        taskLink.setFired(now);

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


    public static void forEachPremise(final Memory memory, Supplier<Concept> conceptSource, int concepts, Consumer<ConceptProcess> proc) {

        final Param p = memory.param;
        final float tasklinkForgetDurations = p.taskLinkForgetDurations.floatValue();
        final int termLinkSelections = p.conceptTaskTermProcessPerCycle.intValue();

        for (int i = 0; i < concepts; i++) {
            Concept c = conceptSource.get();
            if (c==null) continue;

            ConceptProcess.forEachPremise(c,
                    termLinkSelections,
                    tasklinkForgetDurations,
                    proc
            );
        }
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

//      Idea:
//        /** use the time since last cycle as a sort of estimate for how to divide this cycle into subcycles;
//         * this isnt necessary for default mode but realtime mode and others may have
//         * irregular or unpredictable clocks.
//         */
//        //float cyclesSincePrevious = memory.timeSinceLastCycle();


        int remainingProcesses = Math.min(termlinksToReason, numTermLinks);

        while (remainingProcesses > 0) {

           final TermLink bLink = concept.nextTermLink(t);

            if (bLink!=null) {

                proc.accept(
                        new ConceptTaskTermLinkProcess(concept, t, bLink)
                );

            }

            remainingProcesses--;

        }

        /*if (remainingProcesses == 0) {
            System.err.println(now + ": " + currentConcept + ": " + remainingProcesses + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
                    //+ currentConcept.getTermLinks().values()
            );
            //currentConcept.taskLinks.printAll(System.out);
        }*/

        return termlinksToReason - remainingProcesses;

    }


    public Set<Task> getDerived() {
        if (derived == null)
            return Collections.emptySet();
        return derived;
    }
}
