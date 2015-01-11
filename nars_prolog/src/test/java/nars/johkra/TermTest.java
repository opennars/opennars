package nars.johkra;

import org.junit.Test;

import java.text.ParseException;
import nars.johkra.Term;

/**
 * User: Johannes Krampf <johkra@gmail.com>
 * Date: 14.02.11
 */
public class TermTest {
    @Test
    public void testSuccess() throws Exception {
        Term term = new Term("pred(args)", null);

        assert(term.getPred().equals("pred"));
        assert(term.getArgs().get(0).getPred().equals("args"));
    }

    @Test(expected=ParseException.class)
    public void testFail() throws Exception {
        new Term("", null);

    }
}
