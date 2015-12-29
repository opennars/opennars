package nars.nal.nal8;

import nars.Narsese;
import nars.task.Task;
import nars.term.Term;
import nars.term.transform.Subst;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.List;
import java.util.Random;
import java.util.function.Function;


public abstract class PatternOperation implements Function<Task, List<Task>> {

    final Random rng = new XorShift128PlusRandom(1);
    public final Term pattern;

    protected PatternOperation(String pattern) {
        this.pattern = Narsese.the().termRaw(pattern);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + pattern.toString() + ']';
    }

    @Override
    public List<Task> apply(Task operationTask) {
//
//        if (operationTask.isGoal()) {
//            FindSubst s = new FindSubst(Op.VAR_PATTERN, rng);
//            if (s.matchAll(pattern, operationTask.get(), Global.UNIFICATION_POWER)) {
//                return run(operationTask, s);
//            }
//        }

        return null;
    }

    public abstract List<Task> run(Task operationTask, Subst map1);
}
