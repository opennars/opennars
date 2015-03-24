package nars.nal.rule.filter;

import nars.nal.NAL;
import nars.nal.entity.Sentence;
import nars.nal.entity.Task;
import nars.nal.entity.Term;
import nars.nal.entity.Variable;
import nars.nal.nal8.Operation;

/**
* Created by me on 2/9/15.
*/
public class FilterOperationWithSubjOrPredVariable implements NAL.DerivationFilter {
    @Override public String reject(NAL nal, Task task, boolean solution, boolean revised, boolean single, Sentence currentBelief, Task currentTask) {
        Term t = task.sentence.term;
        if (t instanceof Operation) {
            Operation op = (Operation)t;
            if (op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                return "Operation with variable as subject or predicate";
            }
        }
        return null;
    }
}
