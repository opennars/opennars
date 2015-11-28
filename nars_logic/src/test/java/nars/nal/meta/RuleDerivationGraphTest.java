package nars.nal.meta;

import com.google.common.collect.Sets;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Global;
import nars.nal.SimpleDeriver;
import nars.nal.TaskRule;
import org.apache.commons.math3.stat.Frequency;
import org.junit.Test;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieNode;
import org.magnos.trie.TrieSequencer;

import java.util.*;

import static com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM.indent;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 8/15/15.
 */
public class RuleDerivationGraphTest {

    @Test
    public void testRuleStatistics() {
        SimpleDeriver d = new SimpleDeriver(SimpleDeriver.standard);

        List<TaskRule> R = d.rules;
        int registeredRules = R.size();


        Frequency f = new Frequency();
        for (TaskRule t : R)
            f.addValue(t);
        Iterator<Map.Entry<Comparable<?>, Long>> ii = f.entrySetIterator();
        while (ii.hasNext()) {
            Map.Entry<Comparable<?>, Long> e = ii.next();
            if (e.getValue() > 1) {
                System.err.println("duplicate: " + e);
            }
        }
        System.out.println("total: " + f.getSumFreq() + ", unique=" + f.getUniqueCount());

        HashSet<TaskRule> setRules = Sets.newHashSet(R);

        assertEquals("no duplicates", registeredRules, setRules.size());

        Set<PreCondition> preconds = new HashSet();
        int totalPrecond = 0;
        for (TaskRule t : R) {
            for (PreCondition p : t.postPreconditions) {
                totalPrecond++;
                preconds.add(p);
            }
        }
        System.out.println("total precondtions = " + totalPrecond + ", unique=" + preconds.size());

        //preconds.forEach(p -> System.out.println(p));


        //Set<TaskBeliefPair> ks = d.ruleIndex.keySet();
//        System.out.println("Patterns: keys=" + ks.size() + ", values=" + d.ruleIndex.size());
//        for (TaskBeliefPair pp : ks) {
//            System.out.println(pp + " x " + d.ruleIndex.get(pp).size());
//
//        }


    }

    public static class RuleBranch {

        public final PreCondition[] precondition; //precondition sequence

        public final RuleBranch[] children;

        public RuleBranch(PreCondition[] precondition, RuleBranch[] children) {
            this.precondition = precondition;
            this.children = children;
        }
    }

    public static class RuleTrie {

        RuleBranch[] root;

        public RuleTrie() {
            //SimpleDeriver d = new SimpleDeriver(SimpleDeriver.standard);

            ObjectIntHashMap<PreCondition> conds = new ObjectIntHashMap<>();

            Trie<List<PreCondition>, TaskRule> trie = new Trie( new TrieSequencer<List<PreCondition>>() {

                @Override
                public int matches(List<PreCondition> sequenceA, int indexA, List<PreCondition> sequenceB, int indexB, int count) {
                    int i = 0;
                    for (; i < count; i++) {
                        PreCondition a = sequenceA.get(i+indexA);
                        PreCondition b = sequenceB.get(i+indexB);
                        if (!a.equals(b))
                            return i;
                    /*
                    int c = a.compareTo(b);
                    if (c!=0)
                        return c;
                        */
                    }

                    return count;
                }

                @Override
                public int lengthOf(List<PreCondition> sequence) {
                    return sequence.size();
                }

                @Override
                public int hashOf(List<PreCondition> sequence, int index) {
                    //return sequence.get(index).hashCode();

                    PreCondition pp = sequence.get(index);
                    return conds.getIfAbsentPutWithKey(pp, (p) -> 1+conds.size());
                }
            });


            List<TaskRule> R = SimpleDeriver.standard;
            for (TaskRule s : R) {
                //List<PreCondition> ll = s.getConditions();
                //System.out.println(ll);
                TaskRule existing = trie.put( s.getConditions(), s );
                if (existing!=null) {

                    if (!existing.getConditions().toString().equals(s.getConditions().toString())) {
                        System.err.println(s + " replaced: " + existing);
                    }
                }
                //System.out.println(trie.size());
            }


            System.out.println("unique conditions: " + conds.size());

        /*trie.root.forEach((p,c) -> {
            System.out.println(p + " " + c);
        });*/
            System.out.println("root size: " + trie.root.getChildCount());

            root = compile(trie.root);

//        //System.out.println(trie);
//        trie.nodes.forEach(n -> {
//            int from = n.getStart();
//            int to = n.getEnd();
//            List<PreCondition> sub = n.getSequence().subList(from, to);
//            System.out.println(
//                    sub
//            );
//            //System.out.println(n);
//        });

            //System.out.println(trie.nodes);


        }

        private RuleBranch[] compile(TrieNode<List<PreCondition>, TaskRule> node) {

            List<RuleBranch> bb = Global.newArrayList(node.getChildCount());

            node.forEach(n -> {
                List<PreCondition> seq = n.getSequence();

                int from = n.getStart();
                int to = n.getEnd();

                List<PreCondition> sub = seq.subList(from, to);

                System.out.print(n.getChildCount() + "|" + n.getSize() + "  ");
                indent(from*2);
                System.out.println(sub);

                PreCondition[] subseq = sub.toArray(new PreCondition[sub.size()]);
                bb.add(new RuleBranch(subseq, compile(n)));
            });

            return bb.toArray(new RuleBranch[bb.size()]);
        }
    }

    @Test public void testRuleTrie() {
        new RuleTrie();
    }

    @Test public void testPostconditionSingletons() {
//        System.out.println(PostCondition.postconditions.size() + " unique postconditions " + PostCondition.totalPostconditionsRequested);
//        for (PostCondition p : PostCondition.postconditions.values()) {
//            System.out.println(p);
//        }

    }

//    @Test
//    public void testDerivationComparator() {
//
//        NARComparator c = new NARComparator(
//                new Default(),
//                new Default()
//        ) {
//
//
//        };
//        c.input("<x --> y>.\n<y --> z>.\n");
//
//
//
//        int cycles = 64;
//        for (int i = 0; i < cycles; i++) {
//            if (!c.areEqual()) {
//
//                /*System.out.println("\ncycle: " + c.time());
//                c.printTasks("Original:", c.a);
//                c.printTasks("Rules:", c.b);*/
//
////                System.out.println(c.getAMinusB());
////                System.out.println(c.getBMinusA());
//            }
//            c.frame(1);
//        }
//
//        System.out.println("\nDifference: " + c.time());
//        System.out.println("Original - Rules:\n" + c.getAMinusB());
//        System.out.println("Rules - Original:\n" + c.getBMinusA());
//
//    }
}
