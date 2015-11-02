/*
 * Here comes the text of your license
 * Each line should be prefixed with  *
 */
package nars.process;

import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.nal.Deriver;
import nars.task.Task;
import nars.task.stamp.Stamp;
import nars.term.Terms;

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

    private Task currentBelief = null;
    private transient boolean cyclic;

    @Override final public Task getTask() {
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
        this.currentBelief = nextBelief;

        this.cyclic = (nextBelief!=null) ? Stamp.overlapping(getTask(), nextBelief) :
                            false;
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
    public static void firePremiseSquare(NAR nar, Deriver deriver, Consumer<Task> proc, final Concept concept, TaskLink[] tasks, TermLink[] terms, float taskLinkForgetDurations) {

        Memory m = nar.memory();
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


        firePremises(nar, deriver, proc, concept, tasks, terms);

    }

    public static void firePremises(NAR nar, Deriver deriver, Consumer<Task> proc, Concept concept, TaskLink[] tasks, TermLink[] terms) {
        for (final TaskLink taskLink : tasks) {
            if (taskLink == null) break;
            for (final TermLink termLink : terms) {
                if (termLink == null) break;

                if (Terms.equalSubTermsInRespectToImageAndProduct(taskLink.getTerm(), termLink.getTerm()))
                    continue;

                deriver.run(
                    new ConceptTaskTermLinkProcess(nar, concept, taskLink, termLink),
                    proc);
            }
        }
    }

    public boolean validateDerivedBudget(Budget budget) {
        if (budget.isDeleted()) {
            throw new RuntimeException("why is " + budget + " deleted");

        }
        return !budget.summaryLessThan(memory().derivationThreshold.floatValue());
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

    /** gets the average summary of one or both task/belief task's */
    public float getMeanPriority() {
        float total = 0;
        int n = 0;
        final Task pt = getTask();
        if (pt!=null) {
            if (!pt.isDeleted())
                total += pt.getPriority();
            n++;
        }
        final Task pb = getBelief();
        if (pb!=null) {
            if (!pb.isDeleted())
                total += pb.getPriority();
            n++;
        }

        return total/n;
    }

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
