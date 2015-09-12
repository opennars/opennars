package nars.nal;

import nars.meta.RuleMatch;
import nars.premise.Premise;
import nars.task.Task;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.function.Consumer;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
abstract public class Deriver implements Consumer<Premise> {

    public final DerivationRules rules;


    public Deriver(DerivationRules rules) {
        this.rules = rules;
    }


    abstract public void forEachRule(final RuleMatch match);


    public static final ThreadLocal<RuleMatch> newThreadLocalRuleMatches() {
        return ThreadLocal.withInitial(() -> {
            //TODO use the memory's RNG for complete deterministic reproducibility
            return new RuleMatch(new XorShift1024StarRandom(1));
        });
    }

    static final ThreadLocal<RuleMatch> matchers = newThreadLocalRuleMatches();

    @Override
    public void accept(Premise f) {

        ///final Task task, final Sentence belief, Term beliefterm,
        //tLink.getTask(), belief, bLink.getTerm(),

        RuleMatch m = matchers.get();
        m.start(f);

        final Task task = f.getTask();

        if (task.isJudgment() || task.isGoal()) {

            forEachRule(m);

            //TODO also allow backward inference by traversing
        }

    }



//    public final List<DerivationFilter> derivationFilters = Global.newArrayList();
//
//    public List<DerivationFilter> getDerivationFilters() {
//        return derivationFilters;
//    }

//    public boolean test(Task task) {
//
//        String rejectionReason = getDerivationRejection(this, task, solution, revised, singleOrDouble, getBelief(), getTask());
//        if (rejectionReason != null) {
//            memory.removed(task, rejectionReason);
//            return false;
//        }
//        return true;
//    }

//    /** tests validity of a derived task; if valid returns null, else returns a String rule explaining why it is invalid */
//    public String getDerivationRejection(final Premise nal, final Task task, final boolean solution, final boolean revised, final boolean single, final Sentence currentBelief, final Task currentTask) {
//
//        List<DerivationFilter> derivationFilters = getDerivationFilters();
//        final int dfs = derivationFilters.size();
//
//        for (int i = 0; i < dfs; i++) {
//            DerivationFilter d = derivationFilters.get(i);
//            String rejectionReason = d.reject(nal, task, solution, revised);
//            if (rejectionReason != null) {
//                return rejectionReason;
//            }
//        }
//        return null;
//    }

}
