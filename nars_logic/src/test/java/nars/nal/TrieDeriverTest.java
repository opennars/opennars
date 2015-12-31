package nars.nal;

import nars.NAR;
import nars.nal.meta.ProcTerm;
import nars.nar.Default;
import org.junit.Test;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;

/**
 * Created by me on 12/12/15.
 */
public class TrieDeriverTest {

    final String r1 = "((|,X,A..+) --> M), M, task(\".\") |- (X --> M), (Truth:StructuralDeduction)";
    final String r1Case = "<(|, puppy, kitten) --> animal>.";

    final String rN = "(C --> {A..+}), (C --> {B..+}) |- (C --> {A..+,B..+}), (Truth:Union), (C --> intersect({A..+},{B..+})), (Truth:Intersection)";



    @Test public void testNAL3Rule() {

        NAR x = testRule(r1, r1Case);

        assertEquals(1, ((TrieDeriver)(((Default)x).core.der)).roots.length);

        x.log().frame(4);
    }

    @Test public void testTriePreconditions() {
        TrieDeriver d = testRule(rN);

        out.println(d.trie);

        d.trie.printSummary();

        for (ProcTerm p : d.roots)
            out.println(p);
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

}