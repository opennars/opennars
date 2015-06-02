package nars.nal.nal6;

import nars.NAR;
import nars.model.impl.Default;
import nars.nal.nal5.Conjunction;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.TermVisitor;
import nars.nal.term.Variable;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 5/13/15.
 */
public class VariableTransformationTest {

    @Test public void testTransformVariables() {
        NAR nar = new NAR(new Default());
        Compound c = nar.term("<$a --> x>");
        Compound d = Compound.transformIndependentToDependentVariables(c).normalized();
        assertTrue(c!=d);
        assertEquals(d, nar.term("<#1 --> x>"));
    }

    @Test
    public void testCombinations() {


        String same = "(&&,<$1 --> x>,<$1 --> y>)";
        String different = "(&&,<$1 --> x>,<$2 --> y>)";
        combine("<$1 --> x>", true, "<$1 --> y>", true, same);
        combine("<$1 --> x>", false, "<$1 --> y>", false, different);
        combine("<$1 --> x>", false, "<$1 --> y>", true, different);
        combine("<$1 --> x>", false, "<$1 --> y>", false, different);

    }

    static Term scope(Term x, boolean s) {

        x.recurseTerms(new TermVisitor() {

            boolean changed = false;

            @Override
            public void visit(Term t, Term superterm) {
                if (t instanceof Compound && t.hasVar()) {

                    Compound ct = ((Compound)t);
                    for (int i = 0; i < t.length(); i++) {
                        Term x = ct.term[i];
                        if (x instanceof Variable) {
                            Variable nv = ((Variable) x).clone(s);
                            if (nv!=ct.term[i]) changed = true;
                            ct.term[i] = nv;

                        }
                    }

                    if (changed)
                        ct.invalidate();
                }
            }
        });

        return x;
    }

    public void combine(String a, boolean scopedA, String b, boolean scopedB, String expect) {
        NAR n = new NAR(new Default());
        Term ta = scope(n.term(a), scopedA);
        Term tb = scope(n.term(b), scopedB);
        Term c = Conjunction.make(ta, tb).normalized();

        Term e = n.term(expect);
        assertNotNull(e);
        assertEquals(a + " (" + scopedB + ")  +    "   + b + " (" + scopedB + ")", e, c);
    }
}

