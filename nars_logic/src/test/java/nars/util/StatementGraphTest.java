package nars.util;

import nars.NAR;
import nars.clock.FrameClock;
import nars.nal.nal1.Inheritance;
import nars.nar.Default;
import nars.term.Statement;
import nars.util.graph.StatementGraph;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 5/11/15.
 */
public class StatementGraphTest {

    @Test
    public void test() {

        NAR n = new Default(100,1,1,1, new FrameClock());

        StatementGraph m = new StatementGraph(n) {
            @Override public boolean containsStatement(Statement term) {
                return term instanceof Inheritance;
            }
        };

        n.input("<a --> b>. :|:");
        n.frame();
        n.input("<b --> c>. :|:");
        n.frame();
        n.input("<x <-> y>. :|:");
        n.frame();
        n.input("<d --> b>. :|:");
        n.frame();

        n.frame(4);


        //String g = m.graph.toString();

        assertTrue( m.graph.vertexSet().size() > 4 );
        assertTrue( m.graph.edgeSet().size() > 4 );
    }
}
