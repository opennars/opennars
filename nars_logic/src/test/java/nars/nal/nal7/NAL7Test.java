package nars.nal.nal7;

import nars.Global;
import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
    public static Iterable configurations() {
        return AbstractNALTest.nars(7, false);
    }


    @Test
    public void temporal_deduction_explification() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<($x, room) --> enter> =\\> <($x, door) --> open>>", 0.9f, 0.9f);
        tester.believe("<<($y, door) --> open> =\\> <($y, key) --> hold>>", 0.8f, 0.9f);

        tester.mustBelieve(cycles, "<<(*,$1,room) --> enter> =\\> <(*,$1,key) --> hold>>", 0.72f, 0.58f);
        tester.mustBelieve(cycles, "<<(*,$1,key) --> hold> =/> <(*,$1,room) --> enter>>", 1.00f, 0.37f);
        tester.run();
    }

    @Test
    public void temporal_induction_comparison() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<(*, $x, door) --> open> =/> <(*, $x, room) --> enter>>", 0.9f, 0.9f);
        tester.believe("<<(*, $y, door) --> open> =\\> <(*, $y, key) --> hold>>", 0.8f, 0.9f);

        tester.mustBelieve(cycles, "<<(*,$1,key) --> hold> =/> <(*,$1,room) --> enter>>", 0.9f, 0.39f);
        tester.mustBelieve(cycles, "<<(*,$1,room) --> enter> =\\> <(*,$1,key) --> hold>>", 0.8f, 0.42f);
        tester.mustBelieve(cycles, "<<(*,$1,key) --> hold> </> <(*,$1,room) --> enter>>", 0.73f, 0.44f);
        tester.run();
    }

    @Test
    public void temporal_analogy() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<<(*, $x, door) --> open> =/> <(*, $x, room) --> enter>>",
                0.95f, 0.9f);
        tester.believe("<<(*, $x, room) --> enter> <|> <(*, $x, corridor_100) --> leave>>",
                1.0f, 0.9f);

        tester.mustBelieve(cycles, "<<(*, $x, door) --> open> =/> <(*, $x, corridor_100) --> leave>>", 0.95f, 0.81f);
        tester.run();
    }

    @Test
    public void inference_on_tense() throws InvalidInputException {
        TestNAR tester = test();

        tester.believe("<(&/,<($x, key) --> hold>,/5) =/> <($x, room) --> enter>>", 1.0f, 0.9f);
        tester.input("<(John, key) --> hold>. :|:");

        tester.mustBelieve(cycles, "<(*,John,room) --> enter>", 1.00f, 0.81f, Tense.Future); //":\:"
        tester.run();
    }

    @Test
    public void inference_on_tense_2() throws InvalidInputException {
        TestNAR tester = test();
         

        tester.believe("<(&/,<($x, key) --> hold>,/5) =/> <($x, room) --> enter>>", 1.0f, 0.9f);
        tester.input("<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(John, key) --> hold>", 1.00f, 0.45f, Tense.Past); //":\:"
        tester.run();
    }

    @Test
    public void inference_on_tense_3() throws InvalidInputException {
        TestNAR tester = test();
         

        tester.believe("<<(*,John,key) --> hold> =/> <(*,John,room) --> enter>>", 1.0f, 0.9f);
        tester.input("<(*,John,key) --> hold>. :|:");

        tester.mustBelieve(cycles, "<(*,John,room) --> enter>",
                1.00f, 0.81f,
                Tense.Future); //":/:"

        tester.run();
    }

    @Test
    public void inference_on_tense_4() throws InvalidInputException {
        TestNAR tester = test();
         

        tester.believe("<<(*,John,key) --> hold> =/> <(*,John,room) --> enter>>", 1.0f, 0.9f);
        tester.input("<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(*,John,key) --> hold>",
                1.00f, 0.45f,
                Tense.Past ); //:\\:
        tester.run();
    }

    @Test
    public void induction_on_events() throws InvalidInputException {
        TestNAR tester = test();
        


        tester.input("<(*,John,door) --> open>. :|:");
        tester.inputAt(10, "<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<<(*,John,room) --> enter> =\\> (&/,<(*,John,door) --> open>)>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void induction_on_events2() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<(*,John,door) --> open>. :|:");
        tester.inputAt(10, "<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,door) --> open>) =/> <(*,John,room) --> enter>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void induction_on_events3() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<(*,John,door) --> open>. :|:");
        tester.inputAt(10, "<(*,John,room) --> enter>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,door) --> open>) </> <(*,John,room) --> enter>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }


    @Test
    public void induction_on_events_with_variable_introduction() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<John --> (/,open,_,door)>. :|:");
        tester.inputAt(10, "<John --> (/,enter,_,room)>. :|:");

        tester.mustBelieve(cycles, "<(&/,<$1 --> (/,open,_,door)>) </> <$1 --> (/,enter,_,room)>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void induction_on_events_with_variable_introduction2() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<John --> (/,open,_,door)>. :|:");
        tester.inputAt(10, "<John --> (/,enter,_,room)>. :|:");

        tester.mustBelieve(cycles, "<(&/,<$1 --> (/,open,_,door)>) =/> <$1 --> (/,enter,_,room)>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void induction_on_events_with_variable_introduction3() throws InvalidInputException {
        TestNAR tester = test();
        

        tester.input("<John --> (/,open,_,door)>. :|:");
        tester.inputAt(10, "<John --> (/,enter,_,room)>. :|:");

        tester.mustBelieve(cycles, "<<$1 --> (/,enter,_,room)> =\\> (&/,<$1 --> (/,open,_,door)>)>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void induction_on_events_composition() throws InvalidInputException {
        TestNAR tester = test();
        

        tester.input("<(*,John,key) --> hold>. :|:");
        tester.inputAt(10, "<<(*,John,door) --> open> =/> <(*,John,room) --> enter>>. :|:");

        tester.mustBelieve(cycles, "<(&/,<(*,John,key) --> hold>,<(*,John,door) --> open>) =/> <(*,John,room) --> enter>>",
                1.00f, 0.45f,
                10);
        tester.run();
    }

    @Test
    public void updating_and_revision() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<(*,John,key) --> hold>. :|:");
        tester.inputAt(10, "<(*,John,key) --> hold>. :|: %0%");

        tester.mustBelieve(cycles, "<(*,John,key) --> hold>", //TODO: Check truth value
                0.4f, 0.91f,
                10);

        tester.run();
    }

    //NAL7 tests which were accidentally in NAL8 category:

    @Test
    public void variable_introduction_on_events() throws InvalidInputException {
        TestNAR tester = test();

        tester.input("<{t003} --> (/,at,SELF,_)>. :|:");
        tester.inputAt(10, "<{t003} --> (/,on,{t002},_)>. :|:");

        tester.mustBelieve(cycles, "(&&,<#1 --> (/,at,SELF,_)>,<#1 --> (/,on,{t002},_)>)",
                1.0f, 0.81f,
                10);
        tester.run();
    }

    //TODO: investigate
    @Test
    public void variable_elimination_on_temporal_statements() throws InvalidInputException {
        TestNAR tester = test();
         

        tester.input("(&|,<(*,{t002},#1) --> on>,<(*,SELF,#1) --> at>). :|:");
        tester.inputAt(10, "<(&|,<(*,$1,#2) --> on>,<(*,SELF,#2) --> at>) =|> <(*,SELF,$1) --> reachable>>.");

        tester.mustBelieve(cycles, "<(*,SELF,{t002}) --> reachable>.",
                1.0f, 0.81f,
                10); // :|:
        tester.run();
    }

}
