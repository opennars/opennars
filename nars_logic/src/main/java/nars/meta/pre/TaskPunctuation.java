package nars.meta.pre;

import nars.meta.PreCondition;
import nars.meta.RuleMatch;

/**
 * Created by me on 8/27/15.
 */
public class TaskPunctuation extends PreCondition {

    public final char punc;
    transient private final String id;


    public static final TaskPunctuation TaskJudgment = new TaskPunctuation('.');
    public static final TaskPunctuation TaskQuestion = new TaskPunctuation('?');
    public static final TaskPunctuation TaskGoal = new TaskPunctuation('!');

    TaskPunctuation(char p) {
        super();
        this.punc = p;
        this.id = getClass().getSimpleName() + "[" + punc + "]";
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean test(final RuleMatch r) {
        return r.premise.getTask().getPunctuation() == punc;
    }

}
