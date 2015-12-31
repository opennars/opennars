package nars.nal.meta.pre;

import nars.$;
import nars.Symbols;
import nars.nal.PremiseMatch;
import nars.nal.meta.AtomicBooleanCondition;
import nars.term.Term;

/**
 * Created by me on 8/27/15.
 */
public class TaskPunctuation extends AtomicBooleanCondition<PremiseMatch> {

    public final char punc;
    public final String id;


    public static final TaskPunctuation TaskJudgment = new TaskPunctuation('.');

    public static final TaskPunctuation TaskQuestion = new TaskPunctuation('?') {
        @Override protected boolean test(char taskPunc) {
            return taskPunc == Symbols.QUESTION || taskPunc == Symbols.QUEST;
        }
    };

//    public static final TaskPunctuation TaskNotQuestion = new TaskPunctuation(
//            ' ' /* this char wont be used */, "Punc{.|!}") {
//        @Override protected final boolean test(char taskPunc) {
//            return taskPunc != Symbols.QUESTION && taskPunc != Symbols.QUEST;
//        }
//
//    };

    public static final Term TaskQuestionTerm = $.oper("task", "\"?\"");

    public static final TaskPunctuation TaskGoal = new TaskPunctuation('!');

    TaskPunctuation(char p) {
        this(p, "Punc:\"" + p + '\"');
    }

    TaskPunctuation(char p, String id) {
        punc = p;
        this.id = id;
    }

    @Override
    public final String toString() {
        return id;
    }

    @Override
    public boolean booleanValueOf(PremiseMatch m) {
        char taskPunc = m.premise.getTask().getPunctuation();
        return test(taskPunc);
    }

    protected boolean test(char taskPunc) {
        return taskPunc == punc;
    }

    @Override
    public String toJavaConditionString() {
        return "'" + punc + "' == p.getTask().getPunctuation()";
    }
}
