package nars.term.transform;

import nars.Global;
import nars.Op;
import nars.term.Term;
import org.junit.Test;

import java.util.Map;

import static nars.$.$;
import static org.junit.Assert.assertNotNull;
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


        Map<Term,Term> m = Global.newHashMap();
        m.put($("$1"), $("test"));

        assertNull(
            new MapSubstitution(m).applyCompletely($("<($1, is, cat) --> test>"), Op.VAR_INDEPENDENT)
        );
        assertNotNull(
            new MapSubstitution(m).applyCompletely($("<($1, is, cat) --> notTest>"), Op.VAR_INDEPENDENT)
        );

    }
}