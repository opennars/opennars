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
import nars.task.Task;
import nars.term.Term;
import nars.term.TermMetadata;
import nars.term.Termed;
import nars.util.meter.LogicMeter;

import java.io.Serializable;
import java.util.List;

import static nars.budget.BudgetFunctions.clonePriorityMultiplied;

/**
 * "Direct" processing of a new task, in constant time Local processing,
 * involving one concept only
 */
public class TaskBeliefProcess extends AbstractPremise implements Serializable {

    private static final Procedure2<Budget, Budget> PENDING_TERMLINK_BUDGET_MERGE = Budget.plus;

    public final Task task;
    public final Task task2;

    /**
     * configuration
     */
    final static boolean activateTermLinkTemplates = true;
    final static boolean activateTermLinkTemplateTargetsFromTask = true;
    final static boolean immediateTermLinkPropagation = false; /* false = buffered until next concept fire */
    final private TaskLinkBuilder taskLinkBuilder;

    public TaskBeliefProcess(NAR nar, Task task, Task task2) {
        super(nar);

        this.task = task;
        this.task2 = task2;
        this.taskLinkBuilder = new TaskLinkBuilder(nar.memory);
    }

    @Override
    public Task getBelief() {
        return task2;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public final Term getTerm() {
        return task.getTerm();
    }

    @Override
    public TermLink getTermLink() {
        return new TermLink(task2.getTerm(),new Budget(0,0,0));
    }

    @Override
    public Concept getConcept() {
        return nar.concept(task.getTerm());
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        //sb.append(getClass().getSimpleName()).append('[');

        getTask().appendTo(sb, nar.memory, true, false, true, true);

        //sb.append(']');

        return sb.toString();

    }

}
