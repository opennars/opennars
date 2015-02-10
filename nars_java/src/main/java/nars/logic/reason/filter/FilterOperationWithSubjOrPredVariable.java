package nars.logic.reason.filter;

import nars.logic.NAL;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.Variable;
import nars.logic.nal8.Operation;

/**
* Created by me on 2/9/15.
*/
public class FilterOperationWithSubjOrPredVariable implements NAL.DerivationFilter {
    @Override public String reject(NAL nal, Task task, boolean revised, boolean single, Task parent, Sentence otherBelief, Sentence derivedCurrentBelief, Task derivedCurrentTask) {
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
