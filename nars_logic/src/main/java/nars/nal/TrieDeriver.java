package nars.nal;

import nars.nal.meta.PreCondition;
import nars.nal.meta.RuleTrie;
import nars.util.data.DequePool;

import java.util.Random;

/**
 * separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations
 */
public class TrieDeriver extends RuleTrie {


    public TrieDeriver() {
        this(Deriver.standard);
    }

    public TrieDeriver(DerivationRules rules) {
        super(rules);
    }

    @Override
    public final void forEachRule(RuleMatch match) {

        globalRNG = match.subst.random;

        for (RuleBranch r : root) {
            forEachRule(r, match);
        }
    }

    private final void forEachRule(RuleBranch r, RuleMatch match) {

        for (PreCondition x : r.precondition) {
            if (!x.test(match))
                return;
        }


        RuleMatch subMatch = getSubMatch();

        for (RuleBranch s : r.children) {
            match.copyTo(subMatch);
            forEachRule(s, subMatch);
        }

        returnSubMatch(subMatch);

    }

    Random globalRNG; //HACK refsthe same RNG which should be used everywhere

    final DequePool<RuleMatch> submatches = new DequePool<RuleMatch>(8) {
        @Override
        public RuleMatch create() {
            return new RuleMatch(globalRNG);
        }
    };

    private final RuleMatch getSubMatch() {
        return submatches.get();
    }


    private final void returnSubMatch(RuleMatch subMatch) {
        submatches.put(subMatch);
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
