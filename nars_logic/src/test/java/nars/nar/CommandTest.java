package nars.nar;

import nars.NAR;
import nars.Symbols;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.task.Task;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by me on 8/19/15.
 */
public class CommandTest {

    @Test
    public void testEcho() {
        NAR n = new Default();
        final AtomicBoolean invoked = new AtomicBoolean();
        n.on(new NullOperator("c") {

            @Override
            public List<Task> apply(Operation o) {
                invoked.set(true);
                assertEquals(1, o.args().length);
                assertEquals("x", o.arg(0).toString());
                return null;
            }
        });
        Task t = n.task("c(x);");
        assertNotNull(t);
        assertEquals(Symbols.COMMAND, t.getPunctuation());
        assertTrue(t.isCommand());
        assertEquals("c(x);", t.toString());

        n.input(t);

        n.frame(1);

        assertTrue(invoked.get());

        //no concepts created because this command bypassed inference
        assertEquals(0, n.numConcepts(true,true));

    }
}
