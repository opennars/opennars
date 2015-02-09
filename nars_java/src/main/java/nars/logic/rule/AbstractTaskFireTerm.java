package nars.logic.rule;

import nars.logic.FireConcept;
import nars.logic.entity.TaskLink;
import nars.logic.entity.TermLink;

/**
 * Created by me on 2/8/15.
 */
public interface AbstractTaskFireTerm {

    abstract public boolean apply(FireConcept f, TaskLink taskLink, TermLink termLink);

}
