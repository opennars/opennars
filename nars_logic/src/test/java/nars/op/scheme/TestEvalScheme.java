package nars.op.scheme;


import nars.model.impl.Default;
import nars.NARSeed;
import nars.testing.condition.OutputContainsCondition;
import nars.nal.JavaNALTest;
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

        //nar.addPlugin(new Scheme());

        nar.input("scheme((*, car, (*, quote, (*, 2, 3))), #x)!");

        //OUT: <(^scheme,(*,car,(*,quote,(*,2,3))),$1) =/> <$1 <-> 2>>. :|: %1.00;0.99% {0|0 : 1 : }

        nar.requires.add(new OutputContainsCondition(nar, "<2 --> (/,scheme,(*,car,(*,quote,(*,2,3))),_,SELF)>. :|: %1.00;0.99%", 1));

        nar.run(4);

    }
}
