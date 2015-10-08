package nars.nal.nal7;

import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Created by me on 8/19/15.
 */
@RunWith(Parameterized.class)
public class NAL7Test extends AbstractNALTest {


    final int cycles = 200;

    public NAL7Test(Supplier<NAR> b) {
        super(b);
        Global.DEBUG = true;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core8;
    }


    @Test
    public void temporal_deduction_explification() throws InvalidInputException {
/*
'********** temporal_deduction_explification

<<(*, $x, room_101) --> enter> =\> <(*, $x, door_101) --> open>>. %0.9%

<<(*, $y, door_101) --> open> =\> <(*, $y, key_101) --> hold>>. %0.8%

100

''outputMustContain('<<(*,$1,room_101) --> enter> =\> <(*,$1,key_101) --> hold>>. %0.72;0.58%')
''outputMustContain('<<(*,$1,key_101) --> hold> =/> <(*,$1,room_101) --> enter>>. %1.00;0.37%')
 */
        TestNAR tester = test();
        tester.believe("<<($x, room_101) --> enter> =\\> <($x, door_101) --> open>>.", 0.9f, 0.9f);
        tester.believe("<<($y, door_101) --> open> =\\> <($y, key_101) --> hold>>.", 0.8f, 0.9f);

        tester.mustBelieve(cycles, "<<(*,$1,room_101) --> enter> =\\> <(*,$1,key_101) --> hold>>", 0.72f, 0.58f);
        tester.mustBelieve(cycles, "<<(*,$1,key_101) --> hold> =/> <(*,$1,room_101) --> enter>>", 1.00f, 0.37f);
        tester.run();
    }

}
