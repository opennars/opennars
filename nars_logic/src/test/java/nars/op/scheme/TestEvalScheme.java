package nars.op.scheme;


import nars.NAR;
import nars.meter.condition.OutputContainsCondition;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

public class TestEvalScheme extends JavaNALTest {

    public TestEvalScheme(NAR build) {
        super(build);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default() }});
    }


    @Test
    public void testCAR() {

        tester.nar.input("scheme((car, (quote, (*, 2, 3))), #x)!");

        tester.requires.add(new OutputContainsCondition(tester.nar, "<2 --> (/, scheme, (car, (quote, (2, 3))), _, SELF)>. :|: %1.00;0.99%", 1));

        tester.run(4);

    }
}
