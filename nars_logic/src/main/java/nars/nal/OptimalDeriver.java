//package nars.nal;
//
//import com.gs.collections.api.list.MutableList;
//import com.gs.collections.impl.multimap.list.FastListMultimap;
//import nars.Op;
//import nars.meta.PreCondition;
//import nars.meta.TaskRule;
//import nars.meta.pre.BeliefTermType;
//import nars.meta.pre.TaskTermMinVolume;
//import nars.meta.pre.TaskTermType;
//
///** incomplete yet
// *
// * http://moscova.inria.fr/~maranget/papers/ml05e-maranget.pdf
// * http://www.cs.tufts.edu/~nr/pubs/match-abstract.html
// *      http://www.cs.tufts.edu/~nr/pubs/match.pdf
// * http://lampwww.epfl.ch/~emir/written/MatchingObjectsWithPatterns-TR.pdf
// */
//abstract public class OptimalDeriver extends Deriver {
//
//    final FastListMultimap<PreCondition,TaskRule> dependencies = new FastListMultimap<>();
//    //Multimap<Term, TaskRule> ruleByPreconditions = HashMultimap.create();
//
//    public OptimalDeriver(DerivationRules rules) {
//        super(rules);
//
//        for (final TaskRule r : rules) {
//
//            final PreCondition[] p = r.preconditions;
//            final Op o1 = r.getTaskTermType();
//            final Op o2 = r.getBeliefTermType();
//
//            for (int i = 0; i < p.length; i++) {
//                dependencies.put(p[i], r);
//            }
//
//            if (o1 != Op.VAR_PATTERN) {
//                dependencies.put(TaskTermType.the(o1), r);
//                int o1v = r.getTaskTermVolumeMin();
//                dependencies.put(new TaskTermMinVolume(o1v), r);
//            }
//
//
//
//            if (o2 != Op.VAR_PATTERN) {
//                dependencies.put(BeliefTermType.the(o2), r);
//                int o2v = r.getBeliefTermVolumeMin();
//                dependencies.put(new TaskTermMinVolume(o2v), r);
//            }
//        }
//
//        MutableList<PreCondition> sortedDeps = dependencies.keysView().toSortedListBy(p -> {
//            return dependencies.get(p).size();
//        });
//    /*for (int i = 0; i < sortedDeps.size(); i++) {
//        PreCondition k = sortedDeps.get(i);
//        System.out.println(dependencies.get(k).size() + "\t" + k);
//    }*/
//
//
//
//        //printSummary();
//    }
//
//
//
//}
