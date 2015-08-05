/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import nars.Events;
import nars.Memory;
import nars.Param;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.task.Task;

import javax.annotation.Nullable;
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

        concept.setUsed(now);

        taskLink.setUsed(now);
        taskLink.setFired(now);

        emit(Events.ConceptProcessed.class, this);
        memory.logic.TASKLINK_FIRE.hit();
        memory.emotion.busy(getTask(), this);

    }

    @Override
    protected void afterDerive() {

        final long now = memory.time();

        beforeFinish(now);

        if (derived!=null) {
            memory.add(derived);
        }
    }



    @Override
    public Task getBelief() {
        return currentBelief;
    }

    @Deprecated public void setBelief(Task nextBelief) {
        this.currentBelief = nextBelief;
    }


    public static void run(final Memory memory, Supplier<Concept> conceptSource, int concepts, Consumer<ConceptProcess> proc) {

        final Param p = memory.param;
        final float tasklinkForgetDurations = p.taskLinkForgetDurations.floatValue();
        final int termLinkSelections = p.termLinkMaxReasoned.intValue();
        final int termLinkAttempts = p.termLinkMaxMatched.intValue();

        for (int i = 0; i < concepts; i++) {
            Concept c = conceptSource.get();
            if (c==null) continue;

            ConceptProcess.run(c,
                    termLinkSelections, termLinkAttempts,
                    tasklinkForgetDurations,
                    proc
            );
        }
    }

    public static void run(@Nullable final Concept concept, int termLinks, int termLinkAttempts, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
        run(concept, null, termLinks, termLinkAttempts, taskLinkForgetDurations, proc);
    }

    public static void run(@Nullable final Concept concept, @Nullable TaskLink taskLink, int termLinks, int termLinkAttempts, float taskLinkForgetDurations, Consumer<ConceptProcess> proc) {
        if (concept == null) return;

        concept.updateLinks();

        if (taskLink == null) {
            taskLink = concept.getTaskLinks().forgetNext(taskLinkForgetDurations, concept.getMemory());
            if (taskLink == null)
                return;
        }

        proc.accept( new ConceptProcessTaskLink(concept, taskLink) );

        if ((termLinkAttempts > 0) && (taskLink.type!=TermLink.TRANSFORM))
            ConceptProcess.getTermLinks(concept, taskLink,
                    termLinks, termLinkAttempts,
                    proc
            );
    }

    /** generates a set of termlink processes by sampling
     * from a concept's TermLink bag
     * @return how many processes generated
     * */
    public static int getTermLinks(Concept concept, TaskLink t, final int termlinksToReason, final int maxSelections, Consumer<ConceptProcess> proc) {


        int numTermLinks = concept.getTermLinks().size();
        if (numTermLinks == 0)
            return 0;

        final Memory memory = concept.getMemory();




        /** use the time since last cycle as a sort of estimate for how to divide this cycle into subcycles;
         * this isnt necessary for default mode but realtime mode and others may have
         * irregular or unpredictable clocks.
         */
        //float cyclesSincePrevious = memory.timeSinceLastCycle();

        int remainingAttempts = maxSelections;

        int remainingProcesses = termlinksToReason;

        while ((remainingAttempts-- > 0) && (remainingProcesses > 0)) {

           final TermLink bLink = concept.nextTermLink(t);

            if (bLink!=null) {

                proc.accept(
                        new ConceptProcessTaskTermLink(concept, t, bLink)
                );

                remainingProcesses--;
            }

        }

        /*if (remainingProcesses == 0) {
            System.err.println(now + ": " + currentConcept + ": " + remainingProcesses + "/" + termLinksToFire + " firings over " + numTermLinks + " termlinks" + " " + currentTaskLink.getRecords() + " for TermLinks "
                    //+ currentConcept.getTermLinks().values()
            );
            //currentConcept.taskLinks.printAll(System.out);
        }*/

        return termlinksToReason - remainingProcesses;

    }






}
