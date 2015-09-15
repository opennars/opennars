package nars.op.scheme;


import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import static org.jgroups.util.Util.assertTrue;

@RunWith(Parameterized.class)
public class TestEvalScheme extends JavaNALTest {

    public TestEvalScheme(Supplier<NAR> build) {
        super(build);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Supplier[][]{
                {() -> new Default() }});
    }


    @Ignore
    @Test
    public void testCAR() {

        TestNAR tester = test();
        tester.nar.input("scheme((car, (quote, (*, 2, 3))), #x)!");

        assertTrue("test impl unfinished", false);
        //tester.requires.add(new OutputContainsCondition(tester.nar, "<2 --> (/, scheme, (car, (quote, (2, 3))), _, SELF)>. :|: %1.00;0.99%", 1));

        tester.run(4);

    }
}
