package nars.nar;

import nars.NAR;
import nars.Symbols;
import nars.concept.Concept;
import nars.nal.Compounds;
import nars.nal.nal8.operator.NullOperator;
import nars.task.Task;
import nars.term.Term;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by me on 8/19/15.
 */
public class CommandTest {

    @Test
    public void testEcho() {
        NAR n = new Default();
        AtomicBoolean invoked = new AtomicBoolean();
        n.onExec(new NullOperator("c") {

            @Override
            public List<Task> apply(Task o) {
                invoked.set(true);
                Term[] a = Compounds.opArgsArray(o.term());
                assertEquals(1, a.length);
                assertEquals("x", a[0].toString());
                return null;
            }
        });
        Task t = n.task("c(x);");
        assertNotNull(t);
        assertEquals(Symbols.COMMAND, t.getPunctuation());
        assertTrue(t.isCommand());
        assertEquals("c(x); :0:", t.toString());

        n.input(t);

        n.frame(1);

        assertTrue(invoked.get());

        //no concepts created because this command bypassed inference
        n.index().forEach(c -> assertFalse(c instanceof Concept));

    }
}
