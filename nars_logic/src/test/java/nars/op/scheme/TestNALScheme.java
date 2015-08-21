package nars.op.scheme;

import jdk.nashorn.internal.ir.annotations.Ignore;
import nars.NAR;
import nars.clock.HardRealtimeClock;
import nars.nar.Default;
import nars.op.software.scheme.SchemeClosure;
import nars.op.software.scheme.expressions.Expression;
import nars.util.java.NALObjects;
import org.junit.Test;

import java.util.List;

import static nars.op.software.scheme.expressions.NumberExpression.number;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by me on 7/10/15.
 */
public class TestNALScheme {

//    //----
//    @Test @Ignore
//    public void testDynamicBrainfuckProxy() throws Exception {
//
//        NAR n = new NAR(new Default().clock(new HardRealtimeClock(false)) );
//
//        //TextOutput.out(n);
//
//        BrainfuckMachine bf= new NALObjects(n).build("scm", BrainfuckMachine.class);
//
//        bf.execute("++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.");
//
//        n.frame(6500);
//
//    }

/*
    @Test @Ignore
    public void testDynamicSchemeProxy() throws Exception {

        NAR n = new NAR(new Default().clock(new HardRealtimeClock(false)) );

        //TextOutput.out(n);

        SchemeClosure env = new NALObjects(n).build("scm", SchemeClosure.class);

        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";

        env.eval(input);

        List<Expression> result = env.eval("(factorial 3)");

        assertThat(result.get(0), is(number(6)));

        n.frame(50);

        n.frame(1660);
        }
*/

//
//
//        new Thread( () -> { Repl.repl(System.in, System.out, e); } ).start();
//
//        while (true) {
//            n.frame(10);
//            Thread.sleep(500);
//        }
//


//        Class derivedClass = new NALObject().add(new TestHandler()).connect(TestClass.class, n);
//
//        System.out.println(derivedClass);
//
//        Object x = derivedClass.newInstance();
//
//        System.out.println(x);
//
//        ((TestClass)x).callable();


}
