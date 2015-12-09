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
import nars.nal.Deriver;
import nars.nal.nal7.Tense;
import nars.task.Task;
import nars.term.Terms;

import java.util.function.Consumer;

/** Firing a concept (reasoning event). Derives new Tasks via reasoning rules
 *
 *  Concept
 *     Task
 *     TermLinks
 *
 * */
public abstract class ConceptProcess extends AbstractPremise {



    protected final TaskLink taskLink;
    protected final Concept concept;

    private Task currentBelief = null;
    private transient boolean cyclic;

    @Override
    public final Task getTask() {
        return getTaskLink().getTask();
    }

    public final TaskLink getTaskLink() {
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

    public abstract void derive(Deriver p, Consumer<Task> t);

    @Override
    public String toString() {
        return new StringBuilder().append(getClass().getSimpleName())
                .append('[').append(concept.toString()).append(':').append(taskLink).append(']')
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

    @Override public final void updateBelief(Task nextBelief) {
        if (nextBelief!=currentBelief) {
            this.currentBelief = nextBelief;
            this.cyclic = (nextBelief != null) && Tense.overlapping(getTask(), nextBelief);
        }
    }


    @Override
    public final Task getBelief() {
        return currentBelief;
    }


    //TODO cache this value
    @Override
    public final boolean isCyclic() {
        return cyclic;
    }


    /** iteratively supplies a matrix of premises from the next N tasklinks and M termlinks */
    public static void firePremiseSquare(NAR nar, Consumer<ConceptProcess> proc, final Concept concept, TaskLink[] tasks, TermLink[] terms, float taskLinkForgetDurations) {

        Memory m = nar.memory;
        int dur = m.duration();

        final long now = nar.time();

        int tasksCount = concept.nextTaskLinks(dur, now,
                taskLinkForgetDurations * dur,
                tasks);

        if (tasksCount == 0) return;

        int termsCount = concept.nextTermLinks(dur, now,
                m.termLinkForgetDurations.floatValue(),
                terms);

        if (termsCount == 0) return;


        firePremises(nar, proc, concept, tasks, terms);

    }

    public static void firePremises(NAR nar, Consumer<ConceptProcess> proc, Concept concept, TaskLink[] tasks, TermLink[] terms) {

        for (final TaskLink taskLink : tasks) {

            if (taskLink == null) break;

            for (TermLink termLink : terms) {
                if (termLink == null) break;

                if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
                    continue;

                proc.accept(new ConceptTaskTermLinkProcess(
                        nar, concept, taskLink, termLink));
            }
        }
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

//    public static ConceptProcess premise(NAR nar, Concept concept, TaskLink taskLink, TermLink termLink) {
////        if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
////            return null;
//
////        if (taskLink.isDeleted())
////            throw new RuntimeException("tasklink null"); //bag should not have returned this
//
//    }



//    public abstract Stream<Task> derive(final Deriver p);

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

//    /** override-able filter for derivations which can be applied
//     * once the term and the truth value are known */
//    public boolean validJudgment(Term derivedTerm, Truth truth) {
//        return true;
//    }
//
//    /** override-able filter for derivations which can be applied
//     * once the term and the truth value are known */
//    public boolean validGoal(Term derivedTerm, Truth truth) {
//        return true;
//    }

}
