package nars.nal.meta;

import com.google.common.base.Joiner;
import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.Global;
import nars.nal.DerivationRules;
import nars.nal.Deriver;
import nars.nal.RuleMatch;
import nars.nal.TaskRule;
import org.magnos.trie.Trie;
import org.magnos.trie.TrieNode;
import org.magnos.trie.TrieSequencer;

import java.util.List;
import java.util.function.Consumer;

import static com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOM.indent;


public class RuleTrie extends Deriver {

    private final Trie<List<PreCondition>, TaskRule> trie;

    @Override
    protected void forEachRule(RuleMatch match) {
        throw new RuntimeException("impl in subclass");
    }

    public void printSummary() {
        printSummary(trie.root);
    }

    public static final class RuleBranch {

        public final PreCondition[] precondition; //precondition sequence

        public final RuleBranch[] children;

        public RuleBranch(PreCondition[] precondition, RuleBranch[] children) {
            this.precondition = precondition;
            this.children = children.length > 0 ? children : null;
        }
    }

    public final RuleBranch[] root;

    public RuleTrie(DerivationRules R) {
        super(R);

        //SimpleDeriver d = new SimpleDeriver(SimpleDeriver.standard);

        ObjectIntHashMap<PreCondition> conds = new ObjectIntHashMap<>();

        trie = new Trie(new TrieSequencer<List<PreCondition>>() {

            @Override
            public int matches(List<PreCondition> sequenceA, int indexA, List<PreCondition> sequenceB, int indexB, int count) {
                int i = 0;
                for (; i < count; i++) {
                    PreCondition a = sequenceA.get(i + indexA);
                    PreCondition b = sequenceB.get(i + indexB);
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
                return conds.getIfAbsentPutWithKey(pp, (p) -> 1 + conds.size());
            }
        });

        R.forEach((Consumer<? super TaskRule>) s -> {
            //List<PreCondition> ll = s.getConditions();
            //System.out.println(ll);
            for (PostCondition p : s.postconditions) {

                TaskRule existing = trie.put(s.getConditions(p), s);
                if (existing != null) {

                    if (!existing.equals(s)) {
                        System.err.println(s + " replaced: " + existing);
                    }
                }
            }
            //System.out.println(trie.size());
        });


        //System.out.println("unique conditions: " + conds.size());

    /*trie.root.forEach((p,c) -> {
        System.out.println(p + " " + c);
    });*/
        //System.out.println("root size: " + trie.root.getChildCount());

        this.root = compile(trie.root);

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

    public static void printSummary(TrieNode<List<PreCondition>,TaskRule> node) {

        node.forEach(n -> {
            List<PreCondition> seq = n.getSequence();

            int from = n.getStart();
            int to = n.getEnd();


            System.out.print(n.getChildCount() + "|" + n.getSize() + "  ");

            indent(from * 2);

            System.out.println(Joiner.on(", ").join( seq.subList(from, to)));

            printSummary(n);
        });

    }


    private static RuleBranch[] compile(TrieNode<List<PreCondition>, TaskRule> node) {

        List<RuleBranch> bb = Global.newArrayList(node.getChildCount());

        node.forEach(n -> {
            List<PreCondition> seq = n.getSequence();

            int from = n.getStart();
            int to = n.getEnd();

            List<PreCondition> sub = seq.subList(from, to);

            PreCondition[] subseq = sub.toArray(new PreCondition[sub.size()]);
            bb.add(new RuleBranch(subseq, compile(n)));
        });

        return bb.toArray(new RuleBranch[bb.size()]);
    }
}
