package nars.nal;

import nars.nal.meta.PreCondition;
import nars.nal.meta.RuleBranch;
import nars.nal.meta.RuleTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations
 */
public class TrieDeriver extends RuleTrie {


    public TrieDeriver(String rule) {
        super(new PremiseRuleSet(Collections.singleton(rule)));
    }

    public TrieDeriver(PremiseRuleSet rules) {
        super(rules);
    }

    @Override public final void run(RuleMatch m) {

        int now = m.now();

        for (RuleBranch r : root) {
            forEachRule(r, m);
        }

        m.revert(now);

    }

    final static Logger logger = LoggerFactory.getLogger(TrieDeriver.class);

    private static void forEachRule(RuleBranch r, RuleMatch match) {

        //logger.info("BRANCH {}",r);

        for (PreCondition x : r.precondition) {

            //logger.info("{}: {}",x, match.xy);


            if (!x.test(match)) {
                return;
            }
        }

        int now = match.now(); //RESTORE POINT ----

        RuleBranch[] children = r.children;

//        int branchingFactor = children.length;
//        //limit each branch to an equal fraction of the input power
//        Versioned<Integer> branchPower = match.branchPower;
//        branchPower.set( branchPower.get() / branchingFactor );
//        //System.out.println("    branch power: " + branchPower + " x " + branchingFactor);


        for (RuleBranch s : children) {
            forEachRule(s, match);
            match.revert(now);
        }

    }


//    final static void run(RuleMatch m, List<TaskRule> rules, int level, Consumer<Task> t) {
//
//        final int nr = rules.size();
//        for (int i = 0; i < nr; i++) {
//
//            TaskRule r = rules.get(i);
//            if (r.minNAL > level) continue;
//
//            PostCondition[] pc = m.run(r);
//            if (pc != null) {
//                for (PostCondition p : pc) {
//                    if (p.minNAL > level) continue;
//                    ArrayList<Task> Lx = m.apply(p);
//                    if(Lx!=null) {
//                        for (Task x : Lx) {
//                            if (x != null)
//                                t.accept(x);
//                        }
//                    }
//                    /*else
//                        System.out.println("Post exit: " + r + " on " + m.premise);*/
//                }
//            }
//        }
//    }


}
