package nars.nal;

import nars.nal.meta.PreCondition;
import nars.nal.meta.RuleTrie;

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

    @Override public final void run(RuleMatch match) {

        int now = match.now();

        for (RuleBranch r : root) {
            forEachRule(r, match);
        }

        match.revert(now);

    }

    private final void forEachRule(RuleBranch r, RuleMatch match) {

        //System.out.println(">> " + r);

        for (PreCondition x : r.precondition) {

            //System.out.println(x + " " + match.subst.y + " " + match.subst.parent);

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
