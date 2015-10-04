package nars.meta;

import nars.meta.pre.PairMatchingProduct;
import nars.narsese.NarseseParser;
import nars.term.Term;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 8/29/15.
 */
public class PairMatchingProductTest {

    final NarseseParser parse = NarseseParser.the();

    @Test
    public void testNoNormalization() throws Exception {

        String a = "<x --> #1>";
        String b = "<y --> #1>";
        PairMatchingProduct p = new PairMatchingProduct(
            parse.term(a),
            parse.term(b)
        );
        String expect = "(<x --> #1>, <y --> #2>)";
        assertEquals(expect, p.toString());
        Term pn = p.normalized();
        assertEquals(expect, pn.toString());

        p.set((Term)parse.term(a), parse.term(b));
        assertEquals(expect, p.toString());
    }
}