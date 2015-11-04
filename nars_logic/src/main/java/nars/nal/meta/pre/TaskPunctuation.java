package nars.nal.meta.pre;

import nars.$;
import nars.Symbols;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;

/**
 * Created by me on 8/27/15.
 */
public class TaskPunctuation extends PreCondition {

    public final char punc;
    public final String id;


    public static final TaskPunctuation TaskJudgment = new TaskPunctuation('.');

    public static final TaskPunctuation TaskQuestion = new TaskPunctuation('?');
    public static final Term TaskQuestionTerm = $.op("task", "\"?\"");

    public static final TaskPunctuation TaskGoal = new TaskPunctuation('!');

    TaskPunctuation(char p) {
        super();
        this.punc = p;
        this.id = getClass().getSimpleName() + '[' + punc + ']';
    }

    @Override
    public final String toString() {
        return id;
    }

    @Override
    public final boolean test(final RuleMatch r) {
        if(punc == Symbols.QUESTION)
            r.rule.allowQuestionTask = true;

        final char taskPunc = r.premise.getTask().getPunctuation();

        //Quests and questions handled similarly
        switch (taskPunc) {
            case Symbols.QUEST:
                //in rule file it is specified as task("?") but it's for both
                return taskPunc == Symbols.QUESTION;
            default:
                return taskPunc == punc;
        }

    }

}
