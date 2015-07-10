package nars.op.scheme;

import nars.NAR;
import nars.io.out.TextOutput;
import nars.nar.Default;
import nars.op.software.scheme.Environment;
import nars.op.software.scheme.Evaluator;
import nars.op.software.scheme.Reader;
import nars.op.software.scheme.expressions.Expression;
import nars.util.bind.NALObjects;
import org.junit.Test;

import java.util.List;

import static nars.op.software.scheme.expressions.NumberExpression.number;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by me on 7/10/15.
 */
public class TestNALScheme {

    //----

    @Test
    public void testDynamicSchemeProxy() throws Exception {

        NAR n = new NAR(new Default());

        //TextOutput.out(n);

        Environment env = new NALObjects(n).build("scm", Environment.class);

        String input = "(define factorial (lambda (n) (if (= n 1) 1 (* n (factorial (- n 1))))))";

        env.eval(input);

        n.frame(50);

        List<Expression> result = env.eval("(factorial 3)");

        assertThat(result.get(0), is(number(6)));

        n.frame(50);

        //n.frame(1660);


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

}
