package nars.op.scheme;


import nars.NARSeed;
import nars.nal.JavaNALTest;
import nars.nar.Default;
import nars.meter.condition.OutputContainsCondition;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

public class TestEvalScheme extends JavaNALTest {

    public TestEvalScheme(NARSeed build) {
        super(build);
    }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return Arrays.asList(new Object[][]{
                {new Default().setInternalExperience(null)}});
    }


    @Test
    public void testCAR() {

        n.input("scheme((*, car, (*, quote, (*, 2, 3))), #x)!");

        n.requires.add(new OutputContainsCondition(n, "<2 --> (/, scheme, (car, (quote, (2, 3))), _, SELF)>. :|: %1.00;0.99%", 1));

        n.run(4);

    }
}
