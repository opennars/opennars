package nars.nal.filter;

import nars.nal.DerivationFilter;
import nars.nal.NAL;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.task.TaskSeed;
import nars.nal.term.Term;
import nars.nal.term.Variable;

/**
* Created by me on 2/9/15.
*/
public class FilterOperationWithSubjOrPredVariable implements DerivationFilter {
    @Override public String reject(NAL nal, TaskSeed task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        Term t = task.getTerm();
        if (t instanceof Operation) {
            Operation op = (Operation)t;
            if (/*op.getSubject() instanceof Variable || */op.getPredicate() instanceof Variable) {
                return "Operation with variable as predicate";
            }
        }
        return null;
    }
}
