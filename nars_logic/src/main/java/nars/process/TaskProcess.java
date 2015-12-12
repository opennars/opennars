/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.process;

import com.gs.collections.api.block.procedure.Procedure2;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.UnitBudget;
import nars.concept.Concept;
import nars.link.*;
import nars.task.Task;
import nars.term.Term;
import nars.term.TermMetadata;
import nars.term.Termed;
import nars.util.meter.LogicMeter;

import java.util.List;

import static nars.budget.BudgetFunctions.clonePriorityMultiplied;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskProcess extends AbstractPremise  {

    private static final Procedure2<Budget, Budget> PENDING_TERMLINK_BUDGET_MERGE = UnitBudget.plus;

    public final Task task;

    /**
     * configuration
     */
    static final boolean activateTermLinkTemplates = true;
    static final boolean activateTermLinkTemplateTargetsFromTask = true;
    static final boolean immediateTermLinkPropagation = false; /* false = buffered until next concept fire */
    private final TaskLinkBuilder taskLinkBuilder;

    public TaskProcess(NAR nar, Task task) {
        super(nar);

        this.task = task;
        taskLinkBuilder = new TaskLinkBuilder(nar.memory);
    }

    @Override
    public Task getBelief() {
        return null;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public final Term getTerm() {
        return getTask().getTerm();
    }

    @Override
    public TermLink getTermLink() {
        return null;
    }

    @Override
    public Concept getConcept() {
        return nar.concept(getTerm());
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        //sb.append(getClass().getSimpleName()).append('[');

        getTask().appendTo(sb, nar.memory, true, false, true, true);

        //sb.append(']');

        return sb.toString();

    }


    /**
     * when a task is processed, a tasklink
     * can be created at the concept of its term
     */
    public final boolean link(Concept c, Task t) {

        if (linkTask(c, t))
            return linkTemplates(c, t.getBudget(), true);  // recursively insert TermLink

        return false;
    }


    /**
     * Recursively build TermLinks between a compound and its components
     * <p>
     * called only from Memory.continuedProcess
     * activates termlinked concepts with fractions of the taskbudget

     *
     * @param b            The BudgetValue of the task
     * @param updateTLinks true: causes update of actual termlink bag, false: just queues the activation for future application.  should be true if this concept calls it for itself, not for another concept
     * @return whether any activity happened as a result of this invocation
     */
    public boolean linkTemplates(Concept c, Budget b, boolean updateTLinks) {

        if ((b == null) || (b.isDeleted())) return false;

        List<TermLinkTemplate> tl = c.getTermLinkTemplates();
        if (tl == null || tl.isEmpty())
            return false;

        //subPriority = b.getPriority() / (float) Math.sqrt(recipients);
        float factor = 1.0f / (tl.size());
        UnitBudget subBudget = BudgetFunctions.clonePriorityMultiplied(b, factor);

        if (subBudget.summaryLessThan(nar.memory.termLinkThreshold.floatValue()))
            return false;

        NAR nar = this.nar;

        float termLinkThresh = nar.memory.termLinkThreshold.floatValue();

        boolean activity = false;

        for (TermLinkTemplate t : tl) {

            /*if ((t.getTarget().equals(getTerm()))) {
                //self
                continue;
            }*/


            //only apply this loop to non-transform termlink templates
            PENDING_TERMLINK_BUDGET_MERGE.value(t, subBudget);

            if (updateTLinks) {
                if (t.summaryGreaterOrEqual(termLinkThresh)) {

                    if (link(t, c))
                        activity = true;
                }
            }

        }

        return activity;

    }


    public boolean link(TermLinkTemplate t, Concept concept) {

        TermLinkBuilder termLinkBuilder = concept.getTermLinkBuilder();
        Concept otherConcept = getTermLinkTemplateTarget(t);


        termLinkBuilder.set(t, false, concept.getMemory());

        //activate this termlink to peer
        concept.activateTermLink(termLinkBuilder.setIncoming(false));  // this concept termLink to that concept

        if (otherConcept == concept)
            return false;

        //activate peer termlink to this
        otherConcept.activateTermLink(termLinkBuilder.setIncoming(true)); // that concept termLink to this concept

        //if (otherConcept.getTermLinkTemplates()) {
            UnitBudget termlinkBudget = termLinkBuilder.getBudget();
            linkTemplates(otherConcept, termlinkBudget, immediateTermLinkPropagation);
        //}

        /*} else {

        }*/


        //spent ?
        //setPriority(0);

        return true;

    }

    final Concept getTermLinkTemplateTarget(TermLinkTemplate t) {
        Term target = t.getTarget();
        NAR nar = this.nar;
        return activateTermLinkTemplates ? nar.conceptualize(target, t) : nar.concept(target);
    }

    final Concept getTermLinkTemplateTarget(Termed t, Budget taskBudget) {
        Term tt = t.getTerm();
        return activateTermLinkTemplateTargetsFromTask ? nar.conceptualize(tt, taskBudget) : nar.concept(tt);
    }

    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     */
    protected final boolean linkTask(Concept c, Task task) {

        TermLinkBuilder termLinkBuilder = c.getTermLinkBuilder();
        TaskLinkBuilder taskLinkBuilder = getTaskLinkBuilder();




        List<TermLinkTemplate> templates = termLinkBuilder.templates();
        int numTemplates = templates.size();
        if (numTemplates == 0)
            return false;

        taskLinkBuilder.setTask(task);


        //ACTIVATE TASK LINKS
        //   options: entire budget, or the sub-budget that downtsream receives
        taskLinkBuilder.setBudget( task.getBudget() );
        activateTaskLink(c, taskLinkBuilder);



        UnitBudget subBudget = clonePriorityMultiplied(task.getBudget(), 1.0f / numTemplates);
        if (subBudget.summaryLessThan(nar.memory.taskLinkThreshold.floatValue()))
            return false;


        taskLinkBuilder.setBudget(subBudget);


        for (TermLinkTemplate linkTemplate : templates) {
            //if (!(task.isStructural() && (linkTemplate.getType() == TermLink.TRANSFORM))) { // avoid circular transform

//            final Term componentTerm = linkTemplate.getTarget();
//            if (componentTerm.equals(getTerm())) // avoid circular transform
//                continue;

            Concept componentConcept = getTermLinkTemplateTarget(linkTemplate, subBudget);
            if (componentConcept != null) {

                //possibly share term instances
                Term cterm = componentConcept.getTerm();
                if (!(cterm instanceof TermMetadata))
                    linkTemplate.setTargetInstance(cterm);

                /** activate the peer task tlink */
                activateTaskLink(componentConcept, taskLinkBuilder);

            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
    }

    private final TaskLinkBuilder getTaskLinkBuilder() {
        return taskLinkBuilder;
    }

    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     * @return the tasklink which was selected or updated
     */
    protected static TaskLink activateTaskLink(Concept c, TaskLinkBuilder taskLink) {
        return c.getTaskLinks().update(taskLink);
    }

    /**
     * Directly process a new task. Called exactly once on each task. Using
     * local information and finishing in a constant time. Provide feedback in
     * the taskBudget value of the task.
     * <p>
     * called in Memory.immediateProcess only
     *
     * @return whether it was processed
     */
    protected final boolean processConcept(Concept c) {

        Task task = getTask();

        task.onConcept(c);

        LogicMeter logicMeter = nar.memory.logic;

        switch (task.getPunctuation()) {

            case Symbols.JUDGMENT:

                if (!c.processBelief(this))
                    return false;

                logicMeter.JUDGMENT_PROCESS.hit();
                break;

            case Symbols.GOAL:

                if (!c.processGoal(this))
                    return false;

                logicMeter.GOAL_PROCESS.hit();
                break;

            case Symbols.QUESTION:

                if (!c.processQuestion(this))
                    return false;

                logicMeter.QUESTION_PROCESS.hit();
                break;

            case Symbols.QUEST:

                if (!c.processQuest(this))
                    return false;

                logicMeter.QUESTION_PROCESS.hit();
                break;

            default:
                throw new RuntimeException("Invalid sentence type: " + task);
        }

        return !task.isDeleted();
    }




    public Concept run() {


        Task task = getTask();

//        /** deleted in the time between this was created, and run() */
//        if (task.isDeleted()) {
//            throw new RuntimeException(this + " deleted before creation");
//            //return;
//        }

        Memory memory = nar.memory;

        Concept c = nar.conceptualize(task, task.getBudget());

        if (c == null) {
            memory.remove(task, "Inconceivable");
            return null;
        }

        memory.emotion.busy(task, this);

        if (processConcept(c)) {

            memory.eventTaskProcess.emit(this);

            link(c, task);

            return c;
        }
        memory.remove(task, null /* "Unprocessable" */);

        return null;

    }



//    /**
//     * queues a batch process run of TaskProcess's to their
//     * relevant concepts after conceptualizing them with
//     * the budget
//     *
//     * the input reverse sorted task list is divided into 3 sections:
//     *
//     *      0      ..    discarded   == to remove from the system
//     *           --ignore--          == (they are still in the newTaskBuffer this sort was generated from)
//     *      (last-remaining)..last   == to input (highest pri first)
//     *
//     * */
//    public static void run(final Memory memory, final List<Task> reverseSorted, final int toRun, final int toDiscard) {
//
//        final int size = reverseSorted.size();
//        if (size == 0) return;
//
//        if (toRun + toDiscard > size)
//            throw new RuntimeException("invalid buffer positions; size=" + size + ", toRun=" + toRun + ", toDiscard=" + toDiscard);
//
//        for (int i = 0; i < toDiscard; i++) {
//            memory.removed( reverseSorted.get(i) );
//        }
//
//        final int bottomPoint = Math.max(size-toRun, toDiscard);
//        //final TaskProcess[] r = new TaskProcess[size-bottomPoint];
//
//        //int j = 0;
//        for (int i = size-1; i >= bottomPoint; i--) {
//
//            final TaskProcess tp = new TaskProcess(memory, reverseSorted.get(i));
//            tp.run();
//
//            //target.accept();
//
//            //r[j++] =
//        }
//
//    }


}
