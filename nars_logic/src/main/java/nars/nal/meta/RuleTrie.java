package nars.nal.meta;

import com.google.common.base.Joiner;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Global;
import nars.nal.PremiseMatch;
import nars.nal.PremiseRule;
import nars.nal.PremiseRuleSet;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieNode;
import org.magnos.trie.TrieSequencer;

import java.util.List;
import java.util.function.Consumer;

import static com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM.indent;


public class RuleTrie  {

    public final Trie<List<BooleanCondition<PremiseMatch>>, PremiseRule> trie;
    public final ProcTerm<PremiseMatch>[] roots;


    public void printSummary() {
        printSummary(trie.root);
    }


    public RuleTrie(PremiseRuleSet R) {
        super();

        ObjectIntHashMap<BooleanCondition> conds = new ObjectIntHashMap<>();

        trie = new Trie(new TrieSequencer<List<BooleanCondition>>() {

            @Override
            public int matches(List<BooleanCondition> sequenceA, int indexA, List<BooleanCondition> sequenceB, int indexB, int count) {
                for (int i = 0; i < count; i++) {
                    BooleanCondition a = sequenceA.get(i + indexA);
                    BooleanCondition b = sequenceB.get(i + indexB);
                    if (!a.equals(b))
                        return i;
                }

                return count;
            }

            @Override
            public int lengthOf(List<BooleanCondition> sequence) {
                return sequence.size();
            }

            @Override
            public int hashOf(List<BooleanCondition> sequence, int index) {
                //return sequence.get(index).hashCode();

                BooleanCondition pp = sequence.get(index);
                return conds.getIfAbsentPutWithKey(pp, (p) -> 1 + conds.size());
            }
        });

        R.forEach((Consumer<? super PremiseRule>) s -> {

            if (s == null || s.postconditions==null)
                return;

            for (PostCondition p : s.postconditions) {

                PremiseRule existing = trie.put(s.getConditions(p), s);
                if (existing != null) {

                    if (s!=existing && existing.equals(s)) {
                        System.err.println("DUPL: " + existing);
                        System.err.println("      " + existing.getSource());
                        System.err.println("EXST: " + s.getSource());
                        System.err.println();
                    }
                }
            }
        });

        roots = getBranches(trie.root);

    }

    public static void printSummary(TrieNode<List<BooleanCondition<PremiseMatch>>,PremiseRule> node) {

        node.forEach(n -> {
            List<BooleanCondition<PremiseMatch>> seq = n.getSequence();

            int from = n.getStart();
            int to = n.getEnd();


            System.out.print(n.getChildCount() + "|" + n.getSize() + "  ");

            indent(from * 2);

            System.out.println(Joiner.on(", ").join( seq.subList(from, to)));

            printSummary(n);
        });

    }


    private static ProcTerm<PremiseMatch>[] getBranches(TrieNode<List<BooleanCondition<PremiseMatch>>, PremiseRule> node) {

        List<ProcTerm<PremiseMatch>> bb = Global.newArrayList(node.getChildCount());

        node.forEach(n -> {
            List<BooleanCondition<PremiseMatch>> seq = n.getSequence();

            int from = n.getStart();
            int to = n.getEnd();

            List<BooleanCondition<PremiseMatch>> sub = seq.subList(from, to);

            BooleanCondition[] subseq = sub.toArray(new BooleanCondition[sub.size()]);
            bb.add(new PremiseBranch(subseq, getBranches(n)));
        });

        return bb.toArray(new ProcTerm[bb.size()]);
    }
}
