package nars.nal.nal8;

import nars.Global;
import nars.Op;
import nars.narsese.NarseseParser;
import nars.task.Task;
import nars.term.Term;
import nars.term.transform.FindSubst;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


abstract public class PatternFunction implements Function<Task<Operation>, List<Task>> {

    public final Term pattern;

    public PatternFunction(String pattern) {
        this.pattern = NarseseParser.the().termRaw(pattern);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + pattern.toString() + "]";
    }

    @Override
    public List<Task> apply(Task<Operation> operationTask) {
        FindSubst s = new FindSubst(Op.VAR_PATTERN, new XorShift1024StarRandom(1));
        if (s.next(pattern, operationTask.getTerm(), Global.UNIFICATION_POWER)) {
            return run(operationTask, s.xy);
        }
        return null;
    }

    abstract public List<Task> run(Task<Operation> operationTask, Map<Term, Term> map1);
}
