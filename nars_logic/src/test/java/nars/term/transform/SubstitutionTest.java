package nars.term.transform;

import nars.Global;
import nars.NAR;
import nars.nar.Terminal;
import nars.term.Term;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNull;

/**
 * Created by me on 11/12/15.
 */
public class SubstitutionTest {

    @Test
    public void testInvalidSubstitution() {
        //test what happens when substitution attempts to create an invalid term
        //Substitution{subs=
            // {$1=test,
            // <$1 --> $2>=<test --> (/, _, tim, is, cat)>,
            // $2=(/, _, tim, is, cat)}, numSubs=3, numDep=0, numIndep=2, numQuery=0}

        //   with: <($1, is, cat) --> test>

        NAR t = new Terminal();
        Map<Term,Term> m = Global.newHashMap();
        m.put(t.term("$1"), t.term("test"));
        m.put(t.term("<$1 --> $2>"), t.term("<test --> (/, _, tim, is, cat)>"));
        m.put(t.term("$2"), t.term("(/, _, tim, is, cat)"));
        assertNull(
            new Substitution(m).apply(t.term("<($1, is, cat) --> test>"))
        );
        assertNull(
            new Substitution(m).apply(t.term("<<($1, is, cat) --> test> --> super>"))
        );
    }
}