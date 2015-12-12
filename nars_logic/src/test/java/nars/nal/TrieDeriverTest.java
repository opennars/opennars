package nars.nal;

import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 12/12/15.
 */
public class TrieDeriverTest {

    @Test
    public void testNAL3Rule() {
        TrieDeriver d = new TrieDeriver(
            "((|,X,A..+) --> M), M |- (X --> M), (Truth:StructuralDeduction)"
        );

        /*d.rules.forEach((Consumer<? super PremiseRule>) x -> {
            System.out.println(x);
            System.out.println("\t" + x.source);
        });
        System.out.println(d);*/

        assertEquals(2, d.rules.size());

        Default x = new Default() {
            @Override
            protected Deriver getDeriver() {
                return d;
            }
        };
        x.log();
        x.believe("<(|, boy, girl) --> youth>");
        x.frame(100);

    }
}