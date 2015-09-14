package nars.task;

import nars.Global;
import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by me on 8/31/15.
 */
public class UniqueInputSerialTest {

    @Test
    public void inputTwoUniqueTasksDef() {
        inputTwoUniqueTasks(new Default());
    }
    /*@Test public void inputTwoUniqueTasksSolid() {
        inputTwoUniqueTasks(new Solid(4, 1, 1, 1, 1, 1));
    }*/
    /*@Test public void inputTwoUniqueTasksEq() {
        inputTwoUniqueTasks(new Equalized(4, 1, 1));
    }
    @Test public void inputTwoUniqueTasksNewDef() {
        inputTwoUniqueTasks(new Default());
    }*/

    public void inputTwoUniqueTasks(NAR n) {

        Global.DEBUG = true;

        Task x = n.inputTask("<a --> b>.");
        assertArrayEquals(new long[]{1}, x.getEvidence());
        n.frame();

        Task y = n.inputTask("<b --> c>.");
        assertArrayEquals(new long[]{2}, y.getEvidence());
        n.frame();

        n.reset();

        List<Task> z = n.inputs("<e --> f>.  <g --> h>. "); //test when they are input on the same parse
        assertArrayEquals(new long[]{3}, z.get(0).getEvidence());
        assertArrayEquals(new long[]{4}, z.get(1).getEvidence());

        n.frame(10);

        Task q = n.inputTask("<c --> d>.");
        assertArrayEquals(new long[]{5}, q.getEvidence());

    }

    @Test
    public void testDoublePremiseMultiEvidence() {

        Default d = new Default();
        d.input("<a --> b>.", "<b --> c>.");
        d.memory.eventDerived.on(t -> {
            System.out.println(t);
        });
        d.run(128);
    }
}
