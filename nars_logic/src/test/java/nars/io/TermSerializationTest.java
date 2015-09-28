package nars.io;

import nars.term.Term;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 9/7/15.
 */
@RunWith(Parameterized.class)
public class TermSerializationTest extends AbstractSerializationTest<String,Term> {

    public TermSerializationTest(String input) {
        super(input);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{

                //TODO add all term types here

                { "x" },
                { "[x]" },
                { "[x,y]" },
                { "{x}" },
                { "{x,y}" },
                { "#A" },
                { "$A" },
                { "%A" },
                { "?A" },
                { "(--, x)" },
                { "(x, y, z)" },
                { "<a --> b>" },
                { "(x ==> y)" },
                { "(&&, x, y)" },
                { "<a --> (b, c)>" },
                { "<(\\,neutralization,_,base) --> acid>." },
                { "<abc --> (/,x,_)>." },

                { "(&/, x, /3, y)" },

                //TODO images
                //Intervals
                //Immediate Operations (Command)
        });
    }


    @Override
    Term parse(String input) {
        return p.term(input);
    }

    @Override
    protected Term post(Term deserialized) {
        Term t = super.post(deserialized);
        t.rehash();
        return t;
    }

    @Override
    public void testEquality(Term a, Term b) {

        assertTrue(b.hashCode()!=0);
        Assert.assertEquals(a.volume(), b.volume());
        Assert.assertEquals(a, b);
        Assert.assertEquals(a.toString(), b.toString());
    }
}
