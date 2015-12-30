package nars.nal;

import nars.nal.meta.PremiseBranch;
import nars.nal.meta.RuleTrie;
import nars.term.compile.TermIndex;

import java.util.Collections;

/**
 * separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations
 */
public class TrieDeriver extends RuleTrie {


    public TrieDeriver(String rule) {
        super(new PremiseRuleSet(Collections.singleton(rule)));
    }

    public TrieDeriver(PremiseRuleSet rules, TermIndex target) {
        super(rules);
        rules.patterns.setTarget(target);
    }

    @Override public final void run(PremiseMatch m) {

        //int now = m.now();

        for (PremiseBranch r : root) {
            r.accept(m);
        }

        //m.revert(now);

    }

    //final static Logger logger = LoggerFactory.getLogger(TrieDeriver.class);



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
