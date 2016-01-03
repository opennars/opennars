package nars.nal;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 12/12/15.
 */
public class TrieDeriverTest {

    final String r0 = "(S --> P), (S <-> P), task(\"?\") |- (S --> P), (Truth:StructuralIntersection, Punctuation:Judgment)";

    final String r1 = "((|,X,A..+) --> M), M, task(\".\") |- (X --> M), (Truth:StructuralDeduction)";
    final String r1Case = "<(|, puppy, kitten) --> animal>.";

    final String rN = "(C --> {A..+}), (C --> {B..+}) |- (C --> {A..+,B..+}), (Truth:Union), (C --> intersect({A..+},{B..+})), (Truth:Intersection)";



    @Test public void testNAL3Rule() {

        NAR x = testRule(r1, r1Case);

        assertEquals(1, ((TrieDeriver)(((Default)x).core.der)).roots.length);

        x.log().frame(4);
    }

    @Test public void testTriePreconditions0() {
        TrieDeriver d = testRule(r0);
        TrieDeriver e = testRule(r1);
        TrieDeriver f = testRule(rN);

//        assertEquals(1, d.roots.length);
//        assertEquals(2, d.rules.size());

//
//        out.println(d.trie);
//
//        d.trie.printSummary();
//        d.derivationLinks.entries().forEach( System.out::println/*("\t" + k + "\t" + v)*/);
//
//        for (Term p : d.roots)
//            out.println(p);
    }

    public Default testRule(String rule, String... inputs) {
        return (Default) new Default() {
            @Override protected Deriver newDeriver() {
                return new TrieDeriver(rule);
            }
        }.input(inputs);
    }
    public TrieDeriver testRule(String rule) {
        TrieDeriver d = new TrieDeriver(rule);
        new Default() {
            @Override protected Deriver newDeriver() {
                return d;
            }
        };
        return d;
    }

    @Test public void testBackwardsRules() {
        String r = "(A --> B), (B --> C), neq(A,C) |- (A --> C), (Truth:Deduction, Desire:Strong, Derive:AllowBackward)";
        TrieDeriver d = testRule(r);
        //d.trie.printSummary();

        Default n = testRule(r, "b:a.", "a:c.", "a:b?" );
        //n.log();
        n.frame(15);

        //TODO write test
    }
}