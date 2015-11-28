package nars.nal;

import nars.nal.meta.PreCondition;
import nars.nal.meta.RuleTrie;

/** separates rules according to task/belief term type but otherwise involves significant redundancy we'll eliminate in other Deriver implementations */
public class TrieDeriver extends RuleTrie {



    public TrieDeriver() {
        this(Deriver.standard);
    }

    public TrieDeriver(DerivationRules rules) {
        super(rules);
    }

    @Override public final void forEachRule(RuleMatch match) {
        System.out.println("\nstart: " + match);
        //match.start();
        for (RuleBranch r : root) {
            forEachRule(r, match);
        }
    }

    private final static void forEachRule(RuleBranch r, RuleMatch match) {

        for (PreCondition x : r.precondition) {

            if (!x.test(match)) {
                System.out.println(x + " -\n");
                return;
            }
            System.out.println(x + " +");
            if (match.subst.xy().size() > 0) {
                System.out.println("  " + match.subst.xy());
            }
        }

        RuleBranch[] children = r.children;

        if (children == null) {
            System.out.println("  APPLY " + match.subst);
            match.apply();
        }
        else {
                System.out.println("--> \\FORK: " + match.subst);
                for (RuleBranch s : children) {
                    RuleMatch subMatch = match.clone();
                    System.out.println("---->: " + subMatch.subst);
                    forEachRule(s, subMatch);
                }
                System.out.println("<-- /FORK");

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
