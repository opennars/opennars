package nars.nal;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 12/12/15.
 */
public class TrieDeriverTest {

    @Test public void testNAL3Rule() {
        NAR x = testRule(
            "((|,X,A..+) --> M), M, task(\".\") |- (X --> M), (Truth:StructuralDeduction)",
            "<(|, puppy, kitten) --> smetign>."
        );
        x.log();
        x.frame(4);
    }


    public NAR testRule(String rule, String... inputs) {
        TrieDeriver d = new TrieDeriver(
            rule
        );

        assertEquals(1, d.roots.length);

        Default x = new Default() {
            @Override
            protected Deriver newDeriver() {
                return d;
            }
        };
        for (String i : inputs)
            x.input(i);
        return x;
    }

}