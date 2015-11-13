package nars.concept;

import nars.NAR;
import nars.nar.Default;
import org.junit.Test;

/**
 * Created by me on 9/9/15.
 */
public class ActivationTest {

    @Test
    public void testDerivedBudgets() {

        NAR n= new Default();

        //TODO System.err.println("TextOutput.out impl in progress");
        //n.stdout();


        n.input("$0.1$ <a --> b>.");
        n.input("$0.1$ <b --> a>.");
        n.frame(15);


        n.forEachConcept(System.out::println);
    }
}
