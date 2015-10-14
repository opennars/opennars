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
        TestNAR tester = test();
        tester.believe("<<($x, room_101) --> enter> =\\> <($x, door_101) --> open>>.", 0.9f, 0.9f);
        tester.believe("<<($y, door_101) --> open> =\\> <($y, key_101) --> hold>>.", 0.8f, 0.9f);

        tester.mustBelieve(cycles, "<<(*,$1,room_101) --> enter> =\\> <(*,$1,key_101) --> hold>>", 0.72f, 0.58f);
        tester.mustBelieve(cycles, "<<(*,$1,key_101) --> hold> =/> <(*,$1,room_101) --> enter>>", 1.00f, 0.37f);
        tester.run();
    }

    @Test
    public void temporal_induction_comparison() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<(*, $x, door_101) --> open> =/> <(*, $x, room_101) --> enter>>", 0.9f, 0.9f);
        tester.believe("<<(*, $y, door_101) --> open> =\\> <(*, $y, key_101) --> hold>>", 0.8f, 0.9f);

        tester.mustBelieve(cycles, "<<(*,$1,key_101) --> hold> =/> <(*,$1,room_101) --> enter>>", 0.9f, 0.39f);
        tester.mustBelieve(cycles, "<<(*,$1,room_101) --> enter> =\\> <(*,$1,key_101) --> hold>>", 0.8f, 0.42f);
        tester.mustBelieve(cycles, "<<(*,$1,key_101) --> hold> </> <(*,$1,room_101) --> enter>>", 0.73f, 0.44f);
        tester.run();
    }

    @Test
    public void temporal_analogy() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<(*, $x, door_101) --> open> =/> <(*, $x, room_101) --> enter>>", 0.95f, 0.9f);
        tester.believe("<<(*, $x, room_101) --> enter> <=> <(*, $x, corridor_100) --> leave>>", 1.0f, 0.9f);

        tester.mustBelieve(cycles, "<<door_101 --> (/,open,$1,_)> =/> <corridor_100 --> (/,leave,$1,_)>>", 0.95f, 0.81f);
        tester.run();
    }

    @Test
    public void inference_on_tense() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<(&/,<($x, key_101) --> hold>,/5) =/> <($x, room_101) --> enter>>", 1.0f, 0.9f);
        tester.believe("<(John, key_101) --> hold>. :\\:");

        tester.mustBelieve(cycles, "<(*,John,room_101) --> enter>", 1.00f, 0.81f); //":\:" TODO HOW TEST FOR OCCURENCE? (really OCCURENCE, not creation!!)
        tester.run();
    }

    @Test
    public void inference_on_tense_2() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<(&/,<($x, key_101) --> hold>,/5) =/> <($x, room_101) --> enter>>", 1.0f, 0.9f);
        tester.believe("<(*,John,room_101) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(John, key_101) --> hold>", 1.00f, 0.45f); //":\:" TODO HOW TEST FOR OCCURENCE? (really OCCURENCE, not creation!!)
        tester.run();
    }

    @Test
    public void inference_on_tense_3() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<<(*,John,key_101) --> hold> =/> <(*,John,room_101) --> enter>>", 1.0f, 0.9f);
        tester.believe("<(*,John,key_101) --> hold>. :|:");

        tester.mustBelieve(cycles, "<(*,John,room_101) --> enter>", 1.00f, 0.81f); //":/:" TODO HOW TEST FOR OCCURENCE? (really OCCURENCE, not creation!!)
        tester.run();
    }

    @Test
    public void inference_on_tense_4() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<<(*,John,key_101) --> hold> =/> <(*,John,room_101) --> enter>>", 1.0f, 0.9f);
        tester.believe("<(*,John,room_101) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(*,John,key_101) --> hold>", 1.00f, 0.45f); //:\\: TODO HOW TEST FOR OCCURENCE? (really OCCURENCE, not creation!!)
        tester.run();
    }

    @Test
    public void induction_on_events() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<(*,John,door_101) --> open>. :|:");
        tester.believe("<(*,John,room_101) --> enter>. :|:");

        tester.mustBelieve(cycles, "<<(*,John,room_101) --> enter> =\\> (&/,<(*,John,door_101) --> open>)>", 1.00f, 0.45f); // :|: TODO HOW TEST FOR OCCURENCE?
        tester.run();
    }

    @Test
    public void induction_on_events2() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<(*,John,door_101) --> open>. :|:");
        tester.believe("<(*,John,room_101) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,door_101) --> open>) =/> <(*,John,room_101) --> enter>>", 1.00f, 0.45f); // :|: TODO HOW TEST FOR OCCURENCE?
        tester.run();
    }

    @Test
    public void induction_on_events3() throws InvalidInputException {
        TestNAR tester = test();
        tester.nar.stdout();

        tester.believe("<(*,John,door_101) --> open>. :|:");
        tester.believe("<(*,John,room_101) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,door_101) --> open>) </> <(*,John,room_101) --> enter>>", 1.00f, 0.45f); // :|: TODO HOW TEST FOR OCCURENCE?
        tester.run();
    }


}
