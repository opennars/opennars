package nars.term.transform;

import nars.$;
import nars.Op;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


@Deprecated public class FindSubstTest {

    @Test
    public void testFindSubst1() {
        testFindSubst($.$("<a-->b>"), $.$("<?C-->b>"), true);
        testFindSubst($.$("(--,a)"), $.$("<?C-->b>"), false);
    }


    public FindSubst testFindSubst(Term a, Term b, boolean returnsTrue) {

        FindSubst f = new FindSubst(Op.VAR_QUERY, new XORShiftRandom());

        boolean r = f.next(b, a, 1024);

        assertEquals(returnsTrue, r);
        if (r) {

            //identifier: punctuation, mapA, mapB
            assertEquals("?:{?1=a},{}", f.toString());

            //output
            assertEquals(
                    "<a --> b> <?1 --> b> -?> true",
                    a + " " + b + " -?> " + r /*+ " remaining power"*/);
        }

        return f;
    }
}