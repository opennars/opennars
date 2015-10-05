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
import nars.concept.Concept;
import nars.link.*;
import nars.meter.LogicMeter;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static nars.budget.BudgetFunctions.clonePriorityMultiplied;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskProcess extends NAL implements Serializable {

    private static final Procedure2<Budget, Budget> DEFAULT_TERMLINK_ACCUMULATION_MERGE = Budget.plus;

    public final Task task;

    /**
     * configuration
     */
    boolean activateTermLinkTemplates = true;
    boolean activateTermLinkTemplateTargetsFromTask = true;
    boolean immediateTermLinkPropagation = false; /* false = buffered until next concept fire */

    public TaskProcess(NAR nar, Task task) {
        super(nar);

        this.task = task;
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

        sb.append(getClass().getSimpleName()).append('[');

        getTask().toString(sb, nar.memory(), true, false, true, true);

        sb.append(']');

        return sb.toString();

    }


    /**
     * when a task is processed, a tasklink
     * can be created at the concept of its term
     */
    public boolean link(Concept c, Task t) {

        if (linkTask(c, t))
            return linkTerms(c, t.getBudget(), true);  // recursively insert TermLink

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
    public boolean linkTerms(final Concept c, final Budget b, boolean updateTLinks) {

        if ((b == null) || (b.isDeleted())) return false;

        final TermLinkBuilder termLinkBuilder = c.getTermLinkBuilder();

        final List<TermLinkTemplate> tl = termLinkBuilder.templates();
        if (tl == null) return false;

        int recipients = termLinkBuilder.templates().size();
        if (recipients == 0) return false;




        //subPriority = b.getPriority() / (float) Math.sqrt(recipients);
        final float factor = 1f / (recipients);
        Budget subBudget = BudgetFunctions.clonePriorityMultiplied(b, factor);

        if (subBudget.summaryLessThan(nar.memory().termLinkThreshold.floatValue()))
            return false;

        final NAR nar = this.nar;
        final int numTemplates = tl.size();
        final float termLinkThresh = nar.memory().termLinkThreshold.floatValue();

        boolean activity = false;

        for (int i = 0; i < numTemplates; i++) {

            final TermLinkTemplate t = tl.get(i);

                /*if (t.type == TermLink.TRANSFORM)
                    continue;*/

            //only apply this loop to non-transform termlink templates
            DEFAULT_TERMLINK_ACCUMULATION_MERGE.value(t, subBudget);

            if ((t.getTarget().equals(getTerm()))) {
                continue;
            }


            if (updateTLinks) {
                if (t.summaryGreaterOrEqual(termLinkThresh)) {
                    if (link(t, c))
                        activity = true;
                }
            }

        }

        return activity;

    }


    public boolean link(TermLinkTemplate t, Concept c) {

        TermLinkBuilder termLinkBuilder = c.getTermLinkBuilder();

        termLinkBuilder.set(t, false, c.getMemory());

        Concept otherConcept = getTermLinkTemplateTarget(t);


        //activate this termlink to peer
        c.activateTermLink(termLinkBuilder.setIncoming(false));  // this concept termLink to that concept

        //activate peer termlink to this
        otherConcept.activateTermLink(termLinkBuilder.setIncoming(true)); // that concept termLink to this concept

        final Budget termlinkBudget = termLinkBuilder.getBudget();

        //if (otherConcept.getTerm() instanceof Compound) {

        linkTerms(otherConcept, termlinkBudget, immediateTermLinkPropagation);
        //}
        /*} else {

        }*/


        //spent ?
        //setPriority(0);

        return true;

    }

    Concept getTermLinkTemplateTarget(TermLinkTemplate t) {
        if (activateTermLinkTemplates)
            return nar.conceptualize(t.getTerm(), t);
        else
            return nar.concept(t.getTerm());
    }

    Concept getTermLinkTemplateTarget(TermLinkTemplate t, Budget taskBudget) {
        if (activateTermLinkTemplateTargetsFromTask)
            return nar.conceptualize(t.getTerm(), taskBudget);
        else
            return nar.concept(t.getTerm());
    }

    /**
     * Link to a new task from all relevant concepts for continued processing in
     * the near future for unspecified time.
     * <p>
     * The only method that calls the TaskLink constructor.
     *
     * @param task The task to be linked
     */
    protected boolean linkTask(final Concept c, final Task task) {

        final TermLinkBuilder termLinkBuilder = c.getTermLinkBuilder();
        final TaskLinkBuilder taskLinkBuilder = c.getTaskLinkBuilder();

        final List<TermLinkTemplate> templates = termLinkBuilder.templates();
        final int numTemplates = termLinkBuilder.size();
        if (numTemplates == 0) {
            //distribute budget to incoming termlinks?
            return false;
        }


        taskLinkBuilder.setTask(task);

        final Budget subBudget = clonePriorityMultiplied(task.getBudget(), 1f / numTemplates);
        if (subBudget.summaryLessThan(nar.memory().taskLinkThreshold.floatValue()))
            return false;


        taskLinkBuilder.setBudget(subBudget);

        //give self transform task subBudget (previously it got the entire budget)
        activateTaskLink(c, taskLinkBuilder);


        for (int i = 0; i < numTemplates; i++) {
            TermLinkTemplate linkTemplate = templates.get(i);

            //if (!(task.isStructural() && (linkTemplate.getType() == TermLink.TRANSFORM))) { // avoid circular transform

//            final Term componentTerm = linkTemplate.getTarget();
//            if (componentTerm.equals(getTerm())) // avoid circular transform
//                continue;

            Concept componentConcept = getTermLinkTemplateTarget(linkTemplate, subBudget);
            if (componentConcept != null) {

                //share merge term instances
                linkTemplate.setTargetInstance(componentConcept.getTerm());


                /** activate the peer task tlink */
                activateTaskLink(componentConcept, taskLinkBuilder);

            } else {
                //taskBudgetBalance += subBudget.getPriority();
            }

        }

        return true;
    }

    /**
     * Insert a TaskLink into the TaskLink bag
     * <p>
     * called only from Memory.continuedProcess
     *
     * @param taskLink The termLink to be inserted
     * @return the tasklink which was selected or updated
     */
    protected static TaskLink activateTaskLink(Concept c, final TaskLinkBuilder taskLink) {
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
    protected boolean processConcept(final Concept c) {

        final Task task = getTask();

        //share the same Term instance for fast comparison and reduced memory usage (via GC)
        task.setTermShared((Compound) c.getTerm());

        final LogicMeter logicMeter = nar.memory().logic;

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



    public Stream<Task> process(Function<TaskProcess, Stream<Task>> processor) {


        final Task task = getTask();

//        /** deleted in the time between this was created, and run() */
//        if (task.isDeleted()) {
//            throw new RuntimeException(this + " deleted before creation");
//            //return;
//        }

        final Memory memory = this.nar.memory();
        memory.eventTaskProcess.emit(this);
        memory.logic.TASK_PROCESS.hit();

        final Concept c = nar.conceptualize(task, task.getBudget());

        if (c == null) {
            memory.remove(task, "Unable to conceptualize");
            return null;
        }

        memory.emotion.busy(task, this);

        if (processConcept(c)) {

            link(c, task);

        }

        return null;
    }

    public void run() {
        process(null);
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
