package nars.meta.pre;

import nars.$;
import nars.Symbols;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.term.Term;

/**
 * Created by me on 8/27/15.
 */
public class TaskPunctuation extends PreCondition {

    public final char punc;
    public final String id;


    public static final TaskPunctuation TaskJudgment = new TaskPunctuation('.');

    public static final TaskPunctuation TaskQuestion = new TaskPunctuation('?');
    public static final Term TaskQuestionTerm = $.opr("task", "\"?\"");

    public static final TaskPunctuation TaskGoal = new TaskPunctuation('!');

    TaskPunctuation(char p) {
        super();
        this.punc = p;
        this.id = getClass().getSimpleName() + '[' + punc + ']';
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean test(final RuleMatch r) {
        if(punc == Symbols.QUESTION)
            r.rule.allowQuestionTask = true;
        if(r.premise.getTask().getPunctuation() == Symbols.QUEST && punc == Symbols.QUESTION) { //Quests and questions handled similarly
            return true;                                                                        //in rule file it is specified as task("?") but it's for both
        }
        return r.premise.getTask().getPunctuation() == punc;
    }

}
