package nars.task;

import nars.Global;
import nars.NAR;
import nars.nar.AbstractNAR;
import nars.nar.Default;
import nars.task.flow.TaskQueue;
import org.junit.Test;

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

        TaskQueue z = n.inputs("<e --> f>.  <g --> h>. "); //test when they are input on the same parse

        n.frame(10);

        Task q = n.inputTask("<c --> d>.");
        assertArrayEquals(new long[]{5}, q.getEvidence());

    }


    @Test
    public void testDoublePremiseMultiEvidence() {

        AbstractNAR d = new Default(100,1,1,3).nal(2);
        d.input("<a --> b>.", "<b --> c>.");

        long[] ev = {1, 2};
        d.memory.eventDerived.on(t -> {
            assertArrayEquals("all derived terms should be double premise: " + t,
                    ev, t.getEvidence());

            //System.out.println(t);
        });

        d.frame(64);


    }
}
