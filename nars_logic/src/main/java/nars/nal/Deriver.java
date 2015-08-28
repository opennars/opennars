package nars.nal;

import com.gs.collections.api.list.MutableList;
import com.gs.collections.impl.multimap.list.FastListMultimap;
import nars.Op;
import nars.link.TermLink;
import nars.meta.PreCondition;
import nars.meta.RuleMatch;
import nars.meta.TaskRule;
import nars.meta.pre.BeliefTermType;
import nars.meta.pre.TaskTermMinVolume;
import nars.meta.pre.TaskTermType;
import nars.process.ConceptProcess;
import nars.process.concept.ConceptFireTaskTerm;
import nars.task.Task;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.function.Supplier;

/**
 * Created by patrick.hammer on 30.07.2015.
 */
abstract public class Deriver extends ConceptFireTaskTerm {

    public final DerivationRules rules;

    public static final Deriver defaults;


    static {

        Deriver r;

        try {
            r = new SimpleDeriver(DerivationRules.standard);
        } catch (Exception e) {
            r = null;
            e.printStackTrace();
            System.exit(1);
        }

        defaults = r;
    }


    public Deriver(DerivationRules rules) {
        this.rules = rules;
    }



    abstract public void forEachRule(final RuleMatch match);


    public static final ThreadLocal<RuleMatch> newThreadLocalRuleMatches() {
        return ThreadLocal.withInitial(new Supplier<RuleMatch>() {
            @Override
            public RuleMatch get() {
                //TODO use the memory's RNG for complete deterministic reproducibility
                return new RuleMatch(new XorShift1024StarRandom(1));
            }
        });
    }

    static final ThreadLocal<RuleMatch> matchers = newThreadLocalRuleMatches();


    @Override
    public final boolean apply(final ConceptProcess f, final TermLink bLink) {

        ///final Task task, final Sentence belief, Term beliefterm,
        //tLink.getTask(), belief, bLink.getTerm(),

        RuleMatch m = matchers.get();
        m.start(f);

        final Task task = f.getTask();

        if (task.isJudgment() || task.isGoal()) {

            forEachRule(m);

            //TODO also allow backward inference by traversing
        }

        return true;
    }



}
