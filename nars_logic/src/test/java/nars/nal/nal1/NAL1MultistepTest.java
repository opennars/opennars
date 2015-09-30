package nars.nal.nal1;

import nars.NAR;
import nars.meter.TestNAR;
import nars.meter.experiment.DeductiveChainTest;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL1MultistepTest extends AbstractNALTest {


    public NAL1MultistepTest(Supplier<NAR> b) {
        super(b);
    }

    @Parameterized.Parameters(name = "{index}:{0}")
    public static Iterable<Supplier<NAR>> configurations() {
        return AbstractNALTest.core1;
    }

    @Test
    public void multistep2() {
        DeductiveChainTest dc = new DeductiveChainTest(nar(), 2, 600);
        //dc.nar.stdout();
        dc.run();
    }

    @Test
    public void multistep3() {
        new DeductiveChainTest(nar(), 3, 3000).run();
    }

    @Test
    public void multistep4() {
        new DeductiveChainTest(nar(), 4, 4000).run();
    }

    @Test
    public void multistep1() {
        new DeductiveChainTest(nar(), 1, 200).run();
    }


    @Test
    @Deprecated public void multistep() throws InvalidInputException {
        long time = 150;

        //TextOutput.out(n);

        TestNAR test = test();

        //we know also 73% is the theoretical maximum it can reach
        if (test.nar.nal() <= 2)
            test.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);
        else
            //originally checked for 0.25% exact confidence
            test.mustBelieve(time, "<a --> d>", 1f, 1f, 0.25f, 0.99f);

        test.believe("<a --> b>", 1.0f, 0.9f);
        test.believe("<b --> c>", 1.0f, 0.9f);
        test.believe("<c --> d>", 1.0f, 0.9f);


        test.run();
    }

}
