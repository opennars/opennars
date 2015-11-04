package nars.term;

import nars.Narsese;
import nars.nal.nal4.Product;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 8/28/15.
 */
public class VariableTest {

    static final Narsese p = Narsese.the();

    @Test
    public void testPatternVarVolume() {

        assertEquals(0, p.term("$x").complexity());
        assertEquals(1, p.term("$x").volume());

        assertEquals(0, p.term("%x").complexity());
        assertEquals(1, p.term("%x").volume());

        assertEquals(p.term("<x --> y>").volume(),
                p.term("<%x --> %y>").volume());

    }

    @Test public void testNumVars() {
        assertEquals(1, p.term("$x").vars());
        assertEquals(1, p.term("#x").vars());
        assertEquals(1, p.term("?x").vars());
        assertEquals(0, p.term("%x").vars());

        //the pattern variable is not counted toward # vars
        assertEquals(1, p.term("<$x <-> %y>").vars());
    }

    @Test
    public void testIndpVarNorm() {
        assertEquals(2, p.term("<$x <-> $y>").vars());

        testIndpVarNorm("$x", "$y", "($1, $2)");
        testIndpVarNorm("$x", "$x", "($1, $1)");
        testIndpVarNorm("$x", "#x", "($1, #2)");
        testIndpVarNorm("#x", "#x", "(#1, #1)");
    }

    @Test
    public void testIndpVarNormCompound() {
        //testIndpVarNorm("<$x <-> $y>", "<$x <-> $y>", "(<$1 <-> $2>, <$3 <-> $4>)");

        testIndpVarNorm("$x", "$x", "($1, $1)");
        testIndpVarNorm("#x", "#x", "(#1, #1)");
        testIndpVarNorm("<#x <-> #y>", "<#x <-> #y>", "(<#1 <-> #2>, <#1 <-> #2>)");
        testIndpVarNorm("<$x <-> $y>", "<$x <-> $y>", "(<$1 <-> $2>, <$1 <-> $2>)");
    }
    public void testIndpVarNorm(String vara, String varb, String expect) {


        Term a = p.term(vara);
        Term b = p.term(varb);
        //System.out.println(a + " " + b + " "  + Product.make(a, b).normalized().toString());
      assertEquals(
            expect,
            Product.make(a, b).normalized().toString()
        );
    }
}
