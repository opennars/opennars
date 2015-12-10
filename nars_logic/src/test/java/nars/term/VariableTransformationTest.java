package nars.term;

import nars.NAR;
import nars.nar.Terminal;
import nars.task.Task;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by me on 5/13/15.
 */
public class VariableTransformationTest {

//    @Test public void testTransformVariables() {
//        NAR nar = new Default();
//        Compound c = nar.term("<$a --> x>");
//        Compound d = Compound.transformIndependentToDependentVariables(c).normalized();
//        assertTrue(c!=d);
//        assertEquals(d, nar.term("<#1 --> x>"));
//    }

    @Test
    public void testDestructiveNormalization() {
        String t = "<$x --> y>";
        String n = "<$1-->y>";
        NAR nar = new Terminal();
        Term x = nar.term(t);
        assertEquals(n, x.toString());
        //assertTrue("immediate construction of a term from a string should automatically be normalized", x.isNormalized());

    }




//    public void combine(String a, String b, String expect) {
//        NAR n = new Default();
//        Term ta = n.term(a);
//        Term tb = n.term(b);
//        Term c = Conjunction.make(ta, tb).normalized();
//
//        Term e = n.term(expect).normalized();
//        Term d = e.normalized();
//        assertNotNull(e);
//        assertEquals(d, c);
//        assertEquals(e, c);
//    }

    @Test public void varNormTestIndVar() {
        //<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>. gets changed to this: <<($1, $4) --> bigger> ==> <($2, $1) --> smaller>>. after input

        NAR n = new Terminal();

        String t = "<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>";

        Term term = n.term(t);
        Task task = n.task(t + '.');
        //n.input("<<($1, $2) --> bigger> ==> <($2, $1) --> smaller>>.");

        System.out.println(t);
        System.out.println(term);
        System.out.println(task);


        Task t2 = n.inputTask(t + '.');
        System.out.println(t2);

        //TextOutput.out(n);
        n.frame(10);

    }
}

