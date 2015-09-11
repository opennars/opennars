package nars.util;

import nars.NAR;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
import nars.term.Statement;
import nars.util.graph.StatementGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 5/11/15.
 */
public class StatementGraphTest {

    @Test
    public void test() {

        NAR n = new Default();

        StatementGraph m = new StatementGraph(n) {
            @Override public boolean containsStatement(Statement term) {
                return term instanceof Inheritance;
            }
        };

        n.input("<a --> b>. :|:");
        n.input("<b --> c>. :|:");
        n.frame();
        n.input("<x <-> y>. :|:");
        n.input("<d --> b>. :|:");
        n.frame();
        n.frame();
        n.frame();

        String g = m.graph.toString();
        assertEquals("([a, b, c, x, y, d], [<a --> b>=(a,b), <b --> c>=(b,c), <x <-> y>=(x,y), <d --> b>=(d,b)])", g);

    }
}
