package nars.nal;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.meta.pre.PairMatchingProduct;
import nars.premise.Premise;
import nars.task.Task;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
abstract public class Deriver implements Function<Premise,Stream<Task>> {

    public final DerivationRules rules;

    public final ListMultimap<PairMatchingProduct, TaskRule> ruleIndex;


    public Deriver(DerivationRules rules) {

        this.rules = rules;

        this.ruleIndex = MultimapBuilder.treeKeys().arrayListValues().build();

        rules.forEach(r -> ruleIndex.put(r.pattern, r));
    }


    abstract public Stream<Task> forEachRule(final RuleMatch match);


    /** thread-specific pool of RuleMatchers
        this pool is local to this deriver */
    final ThreadLocal<RuleMatch> matchers = ThreadLocal.withInitial(() -> {
        //TODO use the memory's RNG for complete deterministic reproducibility
        return new RuleMatch(new XorShift1024StarRandom(1));
    });

    @Override
    public final Stream<Task> apply(final Premise f) {
        RuleMatch m = matchers.get();
        m.start(f);
        return forEachRule(m);
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
